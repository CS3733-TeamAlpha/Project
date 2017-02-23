package data;

import org.apache.derby.tools.ij;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class for database access using java derby.
 */
public class Database implements Observer
{
	//Constants
	private static final String DB_CREATE_SQL = "/db/DBCreate.sql";
	private static final String DB_DROP_ALL = "/db/DBDropAll.sql";
	private static final String DB_INSERT_SQL = "/db/Inserts.sql";
	private static final String DB_INSERT_NODES = "/db/APP_NODES.sql";
	private static final String DB_INSERT_EDGES = "/db/APP_EDGES.sql";
	private static final String DB_INSERT_PROVIDERS = "/db/APP_PROVIDERS.sql";
	private static final String DB_INSERT_SERVICES = "/db/APP_SERVICES.sql";
	private static final String DB_INSERT_PROVIDEROFFICES = "/db/APP_PROVIDEROFFICES.sql";
	private static final int NODE_TYPE_KIOSK_NOT_SELECTED = 4;
	private static final int NODE_TYPE_KIOSK_SELECTED = 5;

	//Database things
	private String dbName;
	private boolean connected;
	private Statement statement;
	private Connection connection;

	private Hashtable<String, Node> nodeCache;
	private Hashtable<String, Provider> providerCache;

	//Saved prepared statements that may be frequently used. TODO: Optimize and make more things preparedStatements?
	private PreparedStatement checkExist;
	private PreparedStatement insertNode;
	private PreparedStatement insertEdge;
	private PreparedStatement deleteFrom;

	//Neato observer stuff

	@Override
	public void update(Observable observable, Object o)
	{
		//(cond [(Node? o) (...)])
		//whoa, that was a flashback I didn't want
		if (observable.getClass().equals(Node.class))
		{
			updateNode((Node)observable);
		}
		else if (observable.getClass().equals(Provider.class))
		{
			updateProvider((Provider)observable);
		}
	}

	/**
	 * Construct a new database object that will connect to the named database and immediately initiate the connection
	 *
	 * @param name Path to database to connect to.
	 */
	public Database(String name)
	{
		dbName = name;
		connected = false;
		statement = null;
		connection = null;
		nodeCache = new Hashtable<>();
		providerCache = new Hashtable<>();

		checkExist = null;
		insertNode = null;
		insertEdge = null;
		deleteFrom = null;
		connect();
	}

	/**
	 * Connects to the database name specified at construction. If the database cannot be found, it is created.
	 *
	 * @return Success of database connection.
	 * TODO: 2/9/17 Delete printlns
	 */
	public boolean connect()
	{
		try
		{
			DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
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
		initTables();
		reloadCache();
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
		runScript(DB_CREATE_SQL, false);
	}

	/**
	 * Inserts a new node into the table. If a node by the same UUID is found, it is replaced with the new node. Note
	 * that all nodes MUST have a valid building UUID linked in the Buildings table. Otherwise, a constraint violation
	 * exception will be raised and the node will not be inserted. For testing purposes, a "default" building and
	 * default node building UUID of 00000000-0000-0000-0000-000000000000 are included.
	 *
	 * @param node Node object to insert.
	 */
	public void insertNode(Node node)
	{
		node.addObserver(this);
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

			//Insert providers if they don't already exist. This algorithm sucks because java derby sucks. **** you, derby.
			for (Provider prv : node.getProviders())
			{
				try
				{
					PreparedStatement insPrv = connection.prepareStatement("INSERT INTO Providers VALUES(?, ?, ?, ?)");
					insPrv.setString(1, prv.getUUID());
					insPrv.setString(2, prv.getFirstName());
					insPrv.setString(3, prv.getLastName());
					insPrv.setString(4, prv.getTitle());
					insPrv.execute();
					if (!providerCache.containsKey(prv.getUUID()))
						providerCache.put(prv.getUUID(), prv);
				}
				catch (SQLException e)
				{
					if (e.getSQLState().equals("23505")) //unique constraint violation
					{
						PreparedStatement updPrv = connection.prepareStatement("UPDATE Providers SET FirstName=?,LastName=?,Title=? WHERE provider_uuid=?");
						updPrv.setString(1, prv.getFirstName());
						updPrv.setString(2, prv.getLastName());
						updPrv.setString(3, prv.getTitle());
						updPrv.setString(4, prv.getUUID());
						updPrv.execute();
					}
					else
					{
						System.out.println("Error trying to insert provider!");
						e.printStackTrace();
					}
				}
			}

			//Insert offices
			PreparedStatement insOff = connection.prepareStatement("INSERT INTO ProviderOffices VALUES(?,?)");
			for (Provider prv : node.getProviders())
			{
				insOff.setString(1, prv.getUUID());
				insOff.setString(2, node.getID());
				insOff.execute();
			}

			//Insert service info... this one should be much simpler
			PreparedStatement insSrv = connection.prepareStatement("INSERT INTO Services VALUES(?, ?)");
			insSrv.setString(1, node.getID());
			for (int i = 0; i < node.getServices().size(); i++)
			{
				String srv = node.getServices().get(i);
				try
				{
					insSrv.setString(2, srv);
					insSrv.execute();
				} catch (SQLException e2)
				{
					if (!e2.getSQLState().equals("23505"))
						e2.printStackTrace();
					else
					{
						System.out.println("Removing duplicate service " + srv + " from node");
						node.services.remove(srv);
					}
				}
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
	 * expensive function to call. Note that it does NOT delete orphan providers, you'll need to explicitly call
	 * deleteProvider() for that.
	 *
	 * @param node Node to be updated
	 */
	private void updateNode(Node node)
	{
		//This function is expensive because it has to delete all edges where this node is the start, and all provider
		// & service records. Then it has to go back and re-insert them. There might be a more efficient way to do this,
		//but this method works for now. I suspect that going through to see what needs updating is less efficient anyways

		//TODO: HOLY MOTHER OF CTHULHU, MAKE THIS FUNCTION FAST AGAIN! USE UPDATE!
		try
		{
			//Update the node
			PreparedStatement updateNode = connection.prepareStatement("UPDATE Nodes " +
					"SET posX = ?," +
					"	posY = ?," +
					"	type = ?," +
					"	floor = ?," +
					"	building = ?," +
					"	name = ?" +
					"WHERE node_uuid = ?");

			updateNode.setDouble(1, node.getX());
			updateNode.setDouble(2, node.getY());
			updateNode.setInt(3, node.getType());
			updateNode.setInt(4, node.getFloor());
			updateNode.setString(5, node.getBuilding());
			updateNode.setString(6, node.getName());
			updateNode.setString(7, node.getID());
			updateNode.execute();

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

			//Insert providers if they don't already exist. This algorithm sucks because java derby sucks. **** you, derby.
			node.getProviders().forEach((p) -> updateProvider(p));

			//Update services
			PreparedStatement resServices = connection.prepareStatement("UPDATE Services SET node=? WHERE name=?"); //Resolve services
			for (String srv : node.getServices())
			{
				resServices.setString(1, node.getID());
				resServices.setString(2, srv);
			}
			PreparedStatement delServices = connection.prepareStatement("DELETE FROM Services WHERE node=?");
			delServices.setString(1, node.getID());
			delServices.execute();

			PreparedStatement insSrv = connection.prepareStatement("INSERT INTO Services VALUES(?, ?)");
			insSrv.setString(1, node.getID());
			for (String srv : node.getServices())
			{
				insSrv.setString(2, srv);
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
	 *
	 * @param uuid UUID of node to be returned.
	 * @return Node if node found, null otherwise.
	 */
	public Node getNodeByUUID(String uuid)
	{
		//Check the node cache first
		if (nodeCache.containsKey(uuid))
			return nodeCache.get(uuid);

		return null;
	}

	/**
	 * Gets a node by its XY coordinates and floor.
	 * This is used for connecting elevators to each other. We assume elevators have
	 * the exact same XY coordinates.
	 *
	 * @param x X coordinate of node to search for
	 * @param y Y coordinate of node to search for
	 * @param floor Floor on which the search node should reside
	 * @return
	 */
	public Node getElevatorNodeByFloorCoordinates(double x, double y, int floor)
	{
		Node ret = null;
		try
		{
			PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM Nodes WHERE posX = ? AND posY = ? AND floor = ?");
			pstmt.setDouble(1, x);
			pstmt.setDouble(2, y);
			pstmt.setInt(3, floor);

			ResultSet results = pstmt.executeQuery();

			if (results.next())
				ret = nodeCache.get(results.getString(1));

		} catch (SQLException e)
		{
			System.out.println("Error trying to retrieve node from database.");
			e.printStackTrace();
		}

		return ret;
	}

	/**
	 * Get the node that is selected as the kiosk to be used for this app.
	 * The selected kiosk has a type of 5, while non-selected kiosks have a value of 4
	 * @return
	 */
	public Node getSelectedKiosk()
	{
		Node kiosk = null;
		try
		{

			//The node with type 5 is selected. Assume only one such node exists
			ResultSet results = statement.executeQuery("SELECT node_uuid FROM Nodes WHERE type=5");

			while (results.next())
				kiosk = nodeCache.get(results.getString(1));

		} catch (SQLException e)
		{
			System.out.println("Error retrieving selected kiosk!");
			e.printStackTrace();
		}
		return kiosk;
	}

	/**
	 * Compatibilty hack for DirectoryController
	 * TODO: EXTERMINATE
	 *
	 * @return ArrayList of all nodes in the database.
	 */
	public ArrayList<Node> getAllNodes()
	{
		ArrayList<Node> ret = new ArrayList<>();
		for (String s : nodeCache.keySet())
			ret.add(nodeCache.get(s));
		return ret;
	}

	/**
	 * Deletes the node of the given UUID. Also cascade deletes anything associated with this node as well.
	 *
	 * @param uuid UUID of node to delete.
	 */
	public void deleteNodeByUUID(String uuid)
	{
		try
		{
			//Remove service connections but leave the services, required since there is no cascade delete on services and services use a foreign key system
			PreparedStatement unlinkServices = connection.prepareStatement("UPDATE Services SET node=NULL WHERE node=?");
			unlinkServices.setString(1, uuid);
			unlinkServices.execute();

			statement.execute("DELETE FROM Nodes WHERE node_uuid='" + uuid + "'");

			//Needed because there can be no FOREIGN KEY constraint on the edges dst column. If there were, it would
			//not allow for delayed adding of nodes to the database (either that or a ton of constraint violation errors
			//would be generated).
			statement.execute("DELETE FROM Edges WHERE dst='" + uuid + "'");

			PreparedStatement stmt = connection.prepareStatement("DELETE FROM ProviderOffices WHERE NODE_UUID=?");
			stmt.setString(1, uuid);
			stmt.execute();

			//That should've performed a cascade delete on the database, so now we just need to remove cache
			//references to this node, which should run in O(n) time, unfortunately.
			//TODO: Find a way of optimizing this algorithm.
			Node node = nodeCache.get(uuid);

			//Notify the providers they are no longer associated with this location
			node.getProviders().forEach((prv) -> prv.locations.remove(uuid));

			for (String s : nodeCache.keySet())
				nodeCache.get(s).neighbors.remove(node);
			node.deleteObserver(this);
			nodeCache.remove(uuid);

		} catch (SQLException e)
		{
			System.out.println("Error trying to delete node from table!");
			e.printStackTrace();
		}
	}

	/**
	 * Get all nodes on a given floor.
	 *
	 * @param floor Floor to get from.
	 * @return ArrayList of nodes found on the provided. ArrayList is empty if no nodes can be found.
	 */
	public ArrayList<Node> getNodesByFloor(int floor)
	{
		ArrayList<Node> retlist = new ArrayList<>();
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
	 *
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
	 *
	 * @param uuid UUID of building. Recommended to use java.util.UUID.randomUUID().toString()
	 * @param name Name of building.
	 */
	public void insertBuilding(String uuid, String name)
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
	 *
	 * @param buildingUUID UUID of building to grab from. If the UUID is not known, use getBuildingUUID.
	 * @param floor        Floor number as an int.
	 * @return ArrayList of nodes found on this floor. If no nodes could be found, the array is empty. Never returns null.
	 */
	public ArrayList<Node> getNodesInBuildingFloor(String buildingUUID, int floor)
	{
		ArrayList<Node> ret = new ArrayList<>();
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
	 * Get the nearest node to a given node.
	 * First check for other nodes on the same floor/building, then determine nearest.
	 * Only hallway nodes are considered.
	 * @param n The node to find the nearest node to
	 * @return
	 */
	public Node getNearestHallwayNode(Node n)
	{
		double minDist = 0;
		Node nearest = null;
		for(Node nearN: getNodesInBuildingFloor(n.getBuilding(), n.getFloor()))
		{
			if(nearN.getType() == 0 && n != nearN &&
					(n.distance(nearN) < minDist || nearest == null))
			{
				minDist = n.distance(nearN);
				nearest = nearN;
			}
		}
		return nearest;
	}

	/**
	 * Remove entrance node neighbor relations, if they exist.
	 * This function should only occur when a previously linked entrace has its type changed.
	 * @param n
	 * @param type
	 */
	public void removeEntranceConnection(Node n, int type)
	{
		try
		{
			//works with the assumption that only one pair with the same type will exist
			ResultSet results = statement.executeQuery("SELECT node_uuid FROM Nodes WHERE type="+
					type);

			while (results.next())
			{
				Node linkNode = nodeCache.get(results.getString(1));
				linkNode.delNeighbor(n);
				updateNode(linkNode);
				n.delNeighbor(linkNode);
				updateNode(n);
			}

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Link entrance nodes together across different buildings.
	 * Type 6 up to type 19 are unique connections between buildings
	 * @param n Source node, checks if another node with the same type exists and connect
	 * @param type Type integer
	 * @apiNote wtf is this
	 */
	public void connectEntrances(Node n, int type)
	{
		try
		{
			//works with the assumption that only one pair with the same type will exist
			ResultSet results = statement.executeQuery("SELECT node_uuid FROM Nodes WHERE type="+
					type);

			if (results.next())
			{
				Node linkNode = nodeCache.get(results.getString(1));
				linkNode.addNeighbor(n);
				n.addNeighbor(linkNode);
			}

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Gets an ArrayList of building names
	 *
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
				if (!results.getString(1).equals("default"))
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
	 *
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

	public void addProvider(Provider p)
	{
		p.addObserver(this);
		try
		{
			PreparedStatement pstmt = connection.prepareStatement("INSERT INTO Providers VALUES(?, ?, ?, ?)");
			pstmt.setString(1, p.getUUID());
			pstmt.setString(2, p.getFirstName());
			pstmt.setString(3, p.getLastName());
			pstmt.setString(4, p.getTitle());
			pstmt.execute();

			updateProvider(p); //should put all necessary connections to the database.
			providerCache.put(p.getUUID(), p);
		} catch (SQLException e)
		{
			if (!e.getSQLState().equals("23505")) //unique constraint violation
			{
				System.out.println("Error trying to add provider to database!");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets a list of all providers
	 *
	 * @return ArrayList of all providers.
	 */
	public ArrayList<Provider> getProviders()
	{
		ArrayList<Provider> ret = new ArrayList<>();
		providerCache.forEach((id, provider) -> ret.add(provider));
		return ret;
	}

	public Provider getProviderByID(String id)
	{
		return providerCache.get(id);
	}

	/**
	 * Updates a provider in the database.
	 * @param p Provider to update.
	 */
	private void updateProvider(Provider p)
	{
		try
		{
			/*INSERT OR UPDATE PROVIDER*/
			PreparedStatement existsPrv = connection.prepareStatement("SELECT provider_uuid FROM Providers WHERE provider_uuid=?");
			existsPrv.setString(1, p.getUUID());
			ResultSet results = existsPrv.executeQuery();

			if (results.next()) //Update provider
			{
				PreparedStatement updPrv = connection.prepareStatement("UPDATE Providers SET FirstName=?,LastName=?,Title=? WHERE provider_uuid=?");
				updPrv.setString(1, p.getFirstName());
				updPrv.setString(2, p.getLastName());
				updPrv.setString(3, p.getTitle());
				updPrv.setString(4, p.getUUID());
				updPrv.execute();
			}
			else
			{
				//Add new provider
				p.addObserver(this);
				PreparedStatement insPrv = connection.prepareStatement("INSERT INTO Providers VALUES(?, ?, ?, ?)");
				insPrv.setString(1, p.getUUID());
				insPrv.setString(2, p.getFirstName());
				insPrv.setString(3, p.getLastName());
				insPrv.setString(4, p.getTitle());
				insPrv.execute();
				providerCache.put(p.getUUID(), p);
			}

			/*UPDATE OFFICES*/
			//Delete any existing offices in the database
			PreparedStatement stmt = connection.prepareStatement("DELETE FROM ProviderOffices WHERE provider_uuid=?");
			stmt.setString(1, p.getUUID());
			stmt.execute();

			//Add all the new ones
			PreparedStatement pstmt = connection.prepareStatement("INSERT INTO ProviderOffices VALUES(?, ?)");
			for(String nodeID : p.getLocationIds())
			{
				pstmt.setString(1, p.getUUID());
				pstmt.setString(2, nodeID);
				pstmt.execute();
			}

			//Changes to cached nodes shouldn't be necessary, their associated providers will have already done that.

		} catch (SQLException e)
		{
			System.out.println("Error trying to add provider location!");
			e.printStackTrace();
		}
	}

	public void deleteProvider(Provider provider)
	{
		provider.deleteObserver(this);
		providerCache.remove(provider.getUUID());

		//Notify the provider's associated nodes
		provider.getLocations().forEach((node) -> node.providers.remove(provider)); //HAIL LAMBDA! HAIL HYDR- wait, what?
		try
		{
			PreparedStatement pstmt = connection.prepareStatement("DELETE FROM Providers WHERE provider_uuid=?");
			pstmt.setString(1, provider.getUUID());
			pstmt.execute();

			pstmt = connection.prepareStatement("DELETE FROM PROVIDEROFFICES WHERE PROVIDER_UUID=?");
			pstmt.setString(1, provider.getUUID());
		} catch (SQLException e)
		{
			System.out.println("Error trying to delete provider!");
			e.printStackTrace();
		}

	}

	/**
	 * Get the location of a named service as a node
	 *
	 * @param name Name of the service to search for
	 * @return A node object if the service is found, null otherwise.
	 */
	public Node getServiceLocation(String name)
	{
		Node ret = null;
		try
		{
			PreparedStatement pstmt = connection.prepareStatement("SELECT node FROM Services WHERE name=?");
			pstmt.setString(1, name);
			ResultSet results = pstmt.executeQuery();
			if (results.next())
				ret = nodeCache.get(results.getString(1));

		} catch (SQLException e)
		{
			System.out.println("Error trying to get service location!");
			e.printStackTrace();
		}

		return ret;
	}

	/**
	 * Returns a list of all services in the database.
	 *
	 * @return ArrayList of strings of services.
	 */
	public ArrayList<String> getServices()
	{
		ArrayList<String> ret = new ArrayList<>();
		try
		{
			ResultSet results = statement.executeQuery("SELECT name FROM Services");
			while (results.next())
				ret.add(results.getString(1));

		} catch (SQLException e)
		{
			System.out.println("Error trying to get list of services!");
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * Delete a service by name.
	 * @param name Name of the service to delete.
	 */
	public void delService(String name)
	{
		try
		{
			PreparedStatement pstmt = connection.prepareStatement("DELETE FROM Services WHERE name=?");
			pstmt.setString(1, name);
			pstmt.execute();
		} catch (SQLException e)
		{
			System.out.println("Error trying to delete a service!");
			e.printStackTrace();
		}
	}

	/**
	 * Gets a user's password in hashed form
	 * @param username Username to get password for
	 * @return Hashed password, or null if the account does not exist
	 */
	public String getHashedPassword(String username)
	{
		String password = "";
		try
		{
			PreparedStatement pstmt = connection.prepareStatement("SELECT password FROM Logins WHERE username=?");
			pstmt.setString(1, username);
			ResultSet results = pstmt.executeQuery();
			if (results.next())
				password = results.getString(1);
			else
				return null;
		} catch (SQLException e)
		{
			System.out.printf("Error trying to get hashed password!");
			e.printStackTrace();
		}
		return password;
	}

	/**
	 * Insert a new username+password combo or update an existing username's password.
	 * @param username Username to insert or update associated password for
	 * @param hashed Password to insert or update
	 */
	public void storeHashedPassword(String username, String hashed)
	{
		try
		{
			PreparedStatement insertNew = connection.prepareStatement("INSERT INTO Logins VALUES(?, ?)");
			insertNew.setString(1, username);
			insertNew.setString(2, hashed);
			insertNew.execute();
		} catch (SQLException e)
		{
			if (e.getSQLState().equals("23505")) //Unique constraint violated, we need to update the record instead
			{
				//Nested try/catch? Disgusting...
				try
				{
					PreparedStatement updateOld = connection.prepareStatement("UPDATE Logins SET password=? WHERE username=?");
					updateOld.setString(1, hashed);
					updateOld.setString(2, username);
					updateOld.execute();
				} catch (SQLException e1)
				{
					System.out.println("Error trying to update username+hash!");
					e1.printStackTrace();
				}

			}
			else
			{
				System.out.println("Error trying to insert username+password into database!");
				e.printStackTrace();
			}
		}

	}

	/**
	 * Deletes the entire database record for a provided username.
	 * @param username Username to delete username+password combo for.
	 */
	public void deleteAccount(String username)
	{
		try
		{
			PreparedStatement pstmt = connection.prepareStatement("DELETE FROM Logins WHERE username=?");
			pstmt.setString(1, username);
			pstmt.execute();
		} catch (SQLException e)
		{
			System.out.println("Error trying to delete user " + username + "!");
			e.printStackTrace();
		}
	}

	public ArrayList<String> getAllAccounts()
	{
		try
		{
			ArrayList<String> userNames = new ArrayList<>();
			ResultSet results = statement.executeQuery("SELECT username FROM LOGINS");

			while(results.next())
			{
				userNames.add(results.getString("username"));
			}

			return userNames;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Loads all nodes in the database into the cache, links them together, and loads service + provider info. Note that
	 * this function will also invalidate any nodes you've already grabbed, so you'll want to make sure that you re-get
	 * locally stored nodes after calling this function...
	 */
	public void reloadCache()
	{
		//No need to clear the node cache or the provider cache, they're hashtables... they don't suffer from duplicate entries...
		try
		{
			//First load all nodes, sans neighbor relation information
			ResultSet results = statement.executeQuery("SELECT * FROM Nodes");
			while (results.next())
			{
				Node node = new Node(results.getString(1), results.getString(7), results.getString(6),
						results.getDouble(2), results.getDouble(3), results.getInt(4), results.getInt(5));
				node.addObserver(this);
				nodeCache.put(node.getID(), node);
			}

			//Now link nodes together using the hashmap to speed things up:
			results = statement.executeQuery("SELECT * FROM Edges");
			while (results.next())
			{
				Node src = nodeCache.get(results.getString(1));
				Node dst = nodeCache.get(results.getString(2));
				if (src == null || dst == null)
					System.out.println("DATABASE TRYING TO CONNECT NULL NODE(S)!");
				else
					src.neighbors.add(dst);
			}

			//Load all the providers
			results = statement.executeQuery("SELECT * From Providers");
			while (results.next())
			{
				Provider provider = new Provider(results.getString("firstName"), results.getString("lastName"),
						results.getString("provider_uuid"), results.getString("title"));
				provider.addObserver(this);
				providerCache.put(provider.getUUID(), provider);
			}

			//Link providers objects to node objects, using the database as a reference point for connections
			results = statement.executeQuery("SELECT * FROM ProviderOffices");
			while (results.next())
			{
				final String provider = results.getString("provider_uuid");
				final String node = results.getString("node_uuid");
				nodeCache.get(node).providers.add(providerCache.get(provider)); //these two lines being the same length is very satisfying
				providerCache.get(provider).locations.put(nodeCache.get(node).getID(), nodeCache.get(node));
			}

			//Load service info
			results = statement.executeQuery("SELECT * FROM Services");
			while (results.next())
				if (results.getString(1) != null)
					nodeCache.get(results.getString(1)).services.add(results.getString(2));
		} catch (SQLException e)
		{
			System.out.println("Error trying to load pathable nodes!");
			e.printStackTrace();
		}
	}

	/**
	 * Returns whether this database is connected or not.
	 *
	 * @return Connection status.
	 */
	public boolean isConnected()
	{
		return connected;
	}

	/**
	 * Resturns search results from the Nodes and Providers tables.
	 * @param searchText The text to search for
	 * @param top6 If true, only return the top 6 search results
	 * @return Any providers or nodes whos name contains the given search text (not case sensitive).
	 */
	public ArrayList<SearchResult> getResultsForSearch(String searchText, boolean top6)
	{
		try
		{
			searchText = searchText.toLowerCase();
			ArrayList<SearchResult> searchResults = new ArrayList<>();
			PreparedStatement pstmt = connection.prepareStatement("SELECT Name, node_uuid AS UUID, 'Location' AS SearchType FROM Nodes WHERE LOWER(NAME) LIKE ? UNION " +
					"SELECT (lastName || ', ' ||  firstName || '; ' || title) AS name, provider_uuid, 'Provider' AS SearchType FROM Providers WHERE LOWER(lastName || ', ' ||  firstName || '; ' || title) LIKE ?" + ((top6)? " FETCH FIRST 6 ROWS ONLY" : ""));
			pstmt.setString(1, "%" + searchText + "%");
			pstmt.setString(2, "%" + searchText + "%");
			ResultSet results = pstmt.executeQuery();
			while(results.next())
			{
				SearchResult res = new SearchResult();
				res.displayText = results.getString("name");
				res.id = results.getString(2);
				res.searchType = SearchType.valueOf(results.getString(3));
				searchResults.add(res);
			}

			return searchResults;

		} catch (SQLException e)
		{
			System.out.println("Error trying to get service location!");
			e.printStackTrace();
		}

		return null;
	}

	public void setSelectedKiosk(Node kiosk)
	{
		try
		{
			ResultSet results = statement.executeQuery("SELECT node_uuid FROM Nodes WHERE type="+
					NODE_TYPE_KIOSK_SELECTED);

			while (results.next())
			{
				Node oldKiosk = nodeCache.get(results.getString(1));
				oldKiosk.setType(NODE_TYPE_KIOSK_NOT_SELECTED);
				updateNode(oldKiosk);
			}

			nodeCache.get(kiosk.getID()).setType(NODE_TYPE_KIOSK_SELECTED);
			updateNode(nodeCache.get(kiosk.getID()));

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void resetDatabase()
	{
		nodeCache.clear();
		try
		{
			statement.close();
			connection.close();
			connection = DriverManager.getConnection("jdbc:derby:" + dbName + ";create=true");
		} catch (SQLException e)
		{
			//whatever
			e.printStackTrace();
		}

		runScript(DB_DROP_ALL, false);
		runScript(DB_CREATE_SQL, false);
		runScript(DB_INSERT_NODES, false);
		runScript(DB_INSERT_EDGES, false);
		runScript(DB_INSERT_SQL, false);
		runScript(DB_INSERT_PROVIDERS, false);
		runScript(DB_INSERT_SERVICES, false);
		runScript(DB_INSERT_PROVIDEROFFICES, false);

		try
		{
			statement = connection.createStatement();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		reloadCache();
	}

	private void runScript(String filepath, boolean showOutput)
	{
		try
		{
			//http://apache-database.10148.n7.nabble.com/run-script-from-java-w-ij-td100234.html
			ij.runScript(connection, getClass().getResource(filepath).openStream(), "UTF-8", new OutputStream()
			{
				@Override
				public void write(int i) throws IOException
				{
					if(showOutput)
						System.out.write(i);
				}
			}, "UTF-8");
		} catch (IOException e)
		{
			System.out.println("Couldn't find database creation script... that's an error.");
			e.printStackTrace();
		}
	}

	public List<Node> getAllServices()
	{
		return nodeCache.values().stream().filter(node -> node.getType() == 1).collect(Collectors.toList());
	}
}
