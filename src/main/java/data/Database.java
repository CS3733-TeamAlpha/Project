package data;

import org.apache.derby.iapi.sql.execute.ResultSetStatistics;
import org.apache.derby.tools.ij;
import pathfinding.ConcreteNode;
import pathfinding.Node;

import java.io.CharArrayReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Class for database access using java derby.
 */
public class Database
{
	//Constants
	private static final String DB_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	private static final String DB_CREATE_SQL = "/db/DBCreate.sql";

	private String dbName;
	private boolean connected;
	private Statement statement;
	private Connection connection;
	private Hashtable<String, Node> nodeCache;

	//Saved prepared statements that may be frequently used. TODO: Optimize and make more things preparedStatements?
	private PreparedStatement checkExist;
	private PreparedStatement insertNode;
	private PreparedStatement insertEdge;
	private PreparedStatement deleteFrom;

	/**
	 * Construct a new database object that will connect to the named database and immediately initiate the connection
	 * @param name Path to database to connect to.
	 */
	public Database(String name)
	{
		dbName = name;
		connected = false;
		statement = null;
		connection = null;
		nodeCache = new Hashtable<String, Node>();

		checkExist = null;
		insertNode = null;
		insertEdge = null;
		deleteFrom = null;
		connect();
		if (connected)
		{
			initTables();
			reloadCache();
		}
	}

	/**
	 * Connects to the database name specified at construction. If the database cannot be found, it is created.
	 * @return Success of database connection.
	 * @// TODO: 2/9/17 Delete printlns 
	 */
	public boolean connect()
	{
		try
		{
			connection = DriverManager.getConnection("jdbc:derby:" + dbName + ";create=true");
			statement = connection.createStatement();
			connected = true;
		} catch (SQLException e)
		{
			System.out.println("Couldn't connect to database, aborting!");
			e.printStackTrace();
			return false;
		}
		System.out.println("Connected to database \"" + dbName + "\"!");
		return true;
	}

	/**
	 * Disconnects from the database.
	 */
	public void disconnect()
	{
		if (!connected)
			return; //Nothing to see here, move along...

		connected = false;
		nodeCache.clear();
		try
		{
			statement.close();
			connection.close();
			DriverManager.getConnection("jdbc:derby:" + dbName + ";shutdown=true").close();

		} catch (SQLException e)
		{
			if (!e.getSQLState().equals("XJ015") && !e.getSQLState().equals("08006")) //derby shutdowns always raise 08006 exceptions
			{
				System.out.println("Couldn't shutdown derby engine correctly!");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Initializes tables using /db/DBCreate.sql file.
	 */
	private void initTables()
	{
		try
		{
			//http://apache-database.10148.n7.nabble.com/run-script-from-java-w-ij-td100234.html
			ij.runScript(connection, getClass().getResource(DB_CREATE_SQL).openStream(), "UTF-8", new OutputStream()
			{
				@Override
				public void write(int i) throws IOException
				{
					//Needed so that we don't carpet bomb stdout with sql messages from ij. This is already kinda kludgy
					//to begin with though...
				}
			}, "UTF-8");
		} catch (IOException e)
		{
			System.out.println("Couldn't find database creation script... that's an error.");
			e.printStackTrace();
		}


	}

	/**
	 * Inserts a new node into the table. If a node by the same UUID is found, it is replaced with the new node. Note
	 * that all nodes MUST have a valid building UUID linked in the Buildings table. Otherwise, a constraint violation
	 * exception will be raised and the node will not be inserted. For testing purposes, a "default" building and
	 * default node building UUID of 00000000-0000-0000-0000-000000000000 are included.
	 * @param node Node object to insert.
	 */
	public void insertNode(Node node)
	{
		try
		{
			//Create prepared statements.
			//TODO: Factor out commonly used queries into reusable private fields.
			checkExist = connection.prepareStatement("SELECT node_uuid FROM Nodes WHERE NODE_UUID=?");
			insertNode = connection.prepareStatement("INSERT INTO Nodes VALUES(?, ?, ?, ?, ?, ?, ?)");
			insertEdge = connection.prepareStatement("INSERT INTO Edges VALUES(?, ?)");
			deleteFrom = connection.prepareStatement("DELETE FROM Nodes WHERE node_uuid=?");

			//Set up most preparedstatetments safely... no sql injection here
			checkExist.setString(1, node.getID());
			deleteFrom.setString(1, node.getID());
			insertNode.setString(1, node.getID());
			insertNode.setDouble(2, node.getX());
			insertNode.setDouble(3, node.getY());
			insertNode.setInt(4, node.getType());
			insertNode.setInt(5, node.getFloor());
			insertNode.setString(6, node.getBuilding());
			insertNode.setString(7, node.getName());
			insertEdge.setString(1, node.getID());

			//Check if the node exists first
			ResultSet results = checkExist.executeQuery();
			if (!results.wasNull()) //TODO: why doesn't .next() work?
				deleteFrom.execute();

			//Insert node base data
			insertNode.execute();

			//Now insert neighbor relationships by UUID
			for (Node nbr : node.getNeighbors())
			{
				//insertNeighbor.setString(2, nbr.getID());
				//insertNeighbor.execute();
				insertEdge.setString(2, nbr.getID());
				insertEdge.execute();
			}

		nodeCache.put(node.getID(), node);

		} catch (SQLException e)
		{
			System.out.println("Error inserting node into table 'Nodes'!");
			e.printStackTrace();
		}
	}

	/**
	 * Updates a node in the database. Use whenever modifications are made outside the database. Be warned, this is an
	 * expensive function to call.
	 * @param node Node to be updated
	 */
	public void updateNode(Node node)
	{
		//This function is expensive because it has to delete all edges where this node is the start, and all provider
		// & service records. Then it has to go back and re-insert them. There might be a more efficient way to do this,
		//but this method works for now. I suspect that going through to see what needs updating is less efficient anyways
		try
		{
			//Update edges
			PreparedStatement delOld = connection.prepareStatement("DELETE FROM Edges WHERE src=?");
			delOld.setString(1, node.getID());
			delOld.execute();

			PreparedStatement insNbr = connection.prepareStatement("INSERT INTO Edges VALUES(?, ?)");
			insNbr.setString(1, node.getID());
			for (Node nbr : node.getNeighbors())
			{
				insNbr.setString(2, nbr.getID());
				insNbr.execute();
			}

			//Update providers
			PreparedStatement delOffices = connection.prepareStatement("DELETE FROM DoctorOffices WHERE node_uuid=?");
			delOffices.setString(1, node.getID());
			delOffices.execute();

			PreparedStatement insPrv = connection.prepareStatement("INSERT INTO DoctorOffices VALUES(?, ?)");
			insPrv.setString(2, node.getID());
			for (String prv : node.getProviders())
			{
				insPrv.setString(1, getProviderUUID(prv));
				insPrv.execute();
			}

			//Update services
			PreparedStatement delServices = connection.prepareStatement("DELETE FROM Services WHERE node=?");
			delServices.setString(1, node.getID());
			delServices.execute();

			PreparedStatement insSrv = connection.prepareStatement("INSERT INTO Services VALUES(?, ?)");
			insSrv.setString(1, node.getID());
			for (String srv : node.getServices())
			{
				insSrv.setString(2, getProviderUUID(srv));
				insSrv.execute();
			}

		} catch (SQLException e)
		{
			System.out.println("Error trying to update a node's edges!");
			e.printStackTrace();
		}
	}

	/**
	 * Gets a node by its UUID. Returns null if that node didn't exist.
	 * @param uuid UUID of node to be returned.
	 * @return Node if node found, null otherwise.
	 */
	public Node getNodeByUUID(String uuid)
	{
		//Check the node cache first
		if (nodeCache.containsKey(uuid))
			return nodeCache.get(uuid);

		try
		{
			ResultSet results = statement.executeQuery("SELECT * FROM Nodes WHERE node_uuid='" + uuid + "'");
			if (!results.next())
				return null;

			//Construct a new concrete node...
			Node ret = new ConcreteNode(results.getString(1), results.getString(7), results.getString(6),
					results.getDouble(2), results.getDouble(3), results.getInt(4), results.getInt(5));

			//Grab the neighbors off the edges table and make the appropriate links
			results = statement.executeQuery("SELECT dst FROM Edges WHERE src='" + ret.getID() + "'");
			while (results.next())
			{
				if (nodeCache.containsKey(results.getString(1)))
					ret.addNeighbor(nodeCache.get(results.getString(1)));
			}

			//Now get links heading to this node
			results = statement.executeQuery("SELECT src FROM Edges WHERE dst='" + ret.getID() + "'");
			while (results.next())
			{
				if (nodeCache.containsKey(results.getString(1)))
					nodeCache.get(results.getString(1)).addNeighbor(ret);
			}

			//TODO: Investigate how much of this is actually going to be used, there might be some redundancy here.

			nodeCache.put(ret.getID(), ret);
			return ret;
		} catch (SQLException e)
		{
			System.out.println("Error trying to get node by UUID!");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Deletes the node of the given UUID. Also cascade deletes anything associated with this node as well.
	 * @param uuid UUID of node to delete.
	 */
	public void deleteNodeByUUID(String uuid)
	{
		try
		{
			statement.execute("DELETE FROM Nodes WHERE node_uuid='" + uuid + "'");

			//Needed because there can be no FOREIGN KEY constraint on the edges dst column. If there were, it would
			//not allow for delayed adding of nodes to the database (either that or a ton of constraint violation errors
			//would be generated).
			statement.execute("DELETE FROM Edges WHERE dst='" + uuid +"'");

			//That should've performed a cascade delete on the database, so now we just need to remove cache
			//references to this node, which should run in O(n) time, unfortunately.
			//TODO: Find a way of optimizing this algorithm.
			Node node = nodeCache.get(uuid);
			if (node == null)
			{
				System.out.println("Lord of the nullptr exceptions reporting in, sir, there's been a most unusual error... not sure how to proceed.");
				return;
			}

			for (String s : nodeCache.keySet())
				nodeCache.get(s).removeNeighbor(node);
			nodeCache.remove(uuid);
		} catch (SQLException e)
		{
			System.out.println("Error trying to delete node from table!");
			e.printStackTrace();
		}
	}

	/**
	 * Get all nodes on a given floor.
	 * @param floor Floor to get from.
	 * @return ArrayList of nodes found on the provided. ArrayList is empty if no nodes can be found.
	 */
	public ArrayList<Node> getNodesByFloor(int floor)
	{
		ArrayList<Node> retlist = new ArrayList<Node>();
		try
		{
			ResultSet results = statement.executeQuery("SELECT node_uuid FROM Nodes WHERE floor=" + floor);

			//Constructing new nodes will take longer than just grabbing them from the cache, plus we want these to be
			//graph-safe anyways. TODO: Maybe switch to getNodeByUUID instead?

			while (results.next())
				retlist.add(nodeCache.get(results.getString(1)));
		} catch (SQLException e)
		{
			System.out.println("Couldn't get nodes by floor " + floor + "!");
			e.printStackTrace();
		}

		return retlist;
	}

	/**
	 * Gets the UUID of a given building using its name
	 * @param name Name of the building to find
	 * @return 36-char UUID if found, otherwise an empty string.
	 */
	public String getBuildingUUID(String name)
	{
		String ret = "";

		try
		{
			PreparedStatement pstmt = connection.prepareStatement("SELECT building_uuid FROM Buildings WHERE name=?");
			pstmt.setString(1, name);
			ResultSet results = pstmt.executeQuery();
			if (results.next())
				ret = results.getString(1);

		} catch (SQLException e)
		{
			System.out.println("Error trying to get building's UUID!");
			e.printStackTrace();
		}

		return ret;
	}

	/**
	 * Adds a new building to the database.
	 * @param uuid UUID of building. Recommended to use java.util.UUID.randomUUID().toString()
	 * @param name Name of building.
	 */
	public void addBuilding(String uuid, String name)
	{
		try
		{
			PreparedStatement pstmt = connection.prepareStatement("INSERT INTO Buildings VALUES(?, ?)");
			pstmt.setString(1, uuid);
			pstmt.setString(2, name);
			pstmt.execute();
		} catch (SQLException e)
		{
			System.out.println("Error trying to add building!");
			e.printStackTrace();
		}
	}

	/**
	 * Gets all nodes on a given floor of a given building.
	 * @param buildingUUID UUID of building to grab from. If the UUID is not known, use getBuildingUUID.
	 * @param floor Floor number as an int.
	 * @return ArrayList of nodes found on this floor. If no nodes could be found, the array is empty. Never returns null.
	 */
	public ArrayList<Node> getNodesInBuildingFloor(String buildingUUID, int floor)
	{
		ArrayList<Node> ret = new ArrayList<Node>();
		try
		{
			PreparedStatement pstmt = connection.prepareStatement("SELECT node_uuid FROM Nodes WHERE building=? AND floor=?");
			pstmt.setString(1, buildingUUID);
			pstmt.setInt(2, floor);
			ResultSet results = pstmt.executeQuery();

			while (results.next())
				ret.add(nodeCache.get(results.getString(1))); //TODO: Maybe use getNodeByUUID?

		} catch (SQLException e)
		{
			System.out.println("Error trying to get nodes in building!");
			e.printStackTrace();
		}

		return ret;
	}

	/**
	 * Gets an ArrayList of building names
	 * @return ArrayList of building names. Who'd have thought?
	 */
	public ArrayList<String> getBuildings()
	{
		ArrayList<String> ret = new ArrayList<String>();
		try
		{
			ResultSet results = statement.executeQuery("SELECT name FROM Buildings");
			while (results.next())
			{
				if (results.getString(1) != "default")
					ret.add(results.getString(1));
			}

		} catch (SQLException e)
		{
			System.out.println("Error trying to get list of building names!");
			e.printStackTrace();
		}

		return ret;
	}

	/**
	 * Deletes an entire building using a UUID. WARNING: THIS WILL CASCADE DELETE ALL NODES IN THE BUILDING, THEIR FLOORS,
	 * PROVIDERS, ETC. USE WITH EXTREME CAUTION!
	 * @param uuid UUID of building to delete.
	 */
	public void deleteBuilding(String uuid)
	{
		try
		{
			PreparedStatement pstmt = connection.prepareStatement("DELETE FROM Buildings WHERE building_uuid=?");
			pstmt.setString(1, uuid);
			pstmt.execute();
		} catch (SQLException e)
		{
			System.out.println("Error trying to delete a building!");
			e.printStackTrace();
		}

	}

	/**
	 * Gets the UUID of a provider by name.
	 * @param name Name of a UUID. Should include title information.
	 * @return 36-char UUID.
	 */
	public String getProviderUUID(String name)
	{
		String ret = "";
		try
		{
			PreparedStatement pstmt = connection.prepareStatement("SELECT uuid FROM Providers WHERE name=?");
			pstmt.setString(1, name);
			ResultSet results = pstmt.executeQuery();
			if (results.next())
				ret = results.getString(1);

		} catch (SQLException e)
		{
			System.out.println("Error trying to get provider name by UUID!");
			e.printStackTrace();
		}

		return ret;
	}


	/**
	 * Adds a new provider to the database using a name and a uuid.
	 * @param uuid UUID of provider. Reccomended to use java.util.UUID.randomUUID().toString()
	 * @param name Name of provider. Include title information.
	 * TODO: Rename add functions to insert?
	 */
	public void addProvider(String uuid, String name)
	{

	}

	/**
	 * Gets a list of all provider names
	 * @return ArrayList of names
	 */
	public ArrayList<String> getProviderNames()
	{
		ArrayList<String> ret = new ArrayList<String>();
		return ret;
	}

	/**
	 * Adds a provider to a given node.
	 * @param providerUUID UUID of provider.
	 * @param nodeUUID UUID of node that provider should be linked to
	 */
	public void addProviderOffice(String providerUUID, String nodeUUID)
	{

	}

	/**
	 * Returns all of a provider's offices
	 * @param providerUUID UUID of provider
	 * @return ArrayList of nodes that the provider has an office at
	 */
	public ArrayList<Node> getProviderLocations(String providerUUID)
	{
		ArrayList<Node> ret = new ArrayList<Node>();
		return ret;
	}

	/**
	 * Removes a provider from a given node.
	 * @param providerUUID UUID of provider
	 * @param nodeUUID UUID of node to remove provider from.
	 */
	public void deleteProviderOffice(String providerUUID, String nodeUUID)
	{

	}

	/**
	 * Deletes a provider and any associated offices that provider may have.
	 * @param uuid UUID of provider to delete.
	 * TODO: Factor out these deleteX functions into a common deleteByUUID
	 */
	public void deleteProvider(String uuid)
	{

	}

	/**
	 * Loads all nodes in the database into the cache and links them together. Call this function anytime you create
	 * create multiple nodes with relationships - those relationships will be saved to the table and the cache but... they
	 * ...might...not...work.
	 * TODO: Make sure this function never actually needs calling.
	 * TODO: INVESTIGATE IF THIS FUNCTION IS ACTUALLY NEEDED!
	 */
	public void reloadCache()
	{
		//No need to clear the hash table, it's a hash table... it doesn't have duplicate entries...
		try
		{
			//First load all nodes, sans neighbor relation information
			ResultSet results = statement.executeQuery("SELECT * FROM Nodes");
			while (results.next())
			{
				Node node = new ConcreteNode(results.getString(1), results.getString(7), results.getString(6),
						results.getDouble(2), results.getDouble(3), results.getInt(4), results.getInt(5));
				nodeCache.put(node.getID(), node);
			}

			//Now link nodes together using the hashmap to speed things up:
			results = statement.executeQuery("SELECT * FROM Edges");
			while (results.next())
			{
				System.out.printf("Linking node '%s' to node '%s'!\n", results.getString(1), results.getString(2));
				nodeCache.get(results.getString(1)).addNeighbor(nodeCache.get(results.getString(2)));
			}

		} catch (SQLException e)
		{
			System.out.println("Error trying to load pathable nodes!");
			e.printStackTrace();
		}
	}


	/*Misc getters and setters*/

   /**
	 * Returns whether this database is connected or not.
	 * @return Connection status.
	 */
	public boolean isConnected()
	{
		return connected;
	}
}
