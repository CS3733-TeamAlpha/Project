package data;

import org.apache.derby.tools.ij;
import pathfinding.ConcreteNode;
import pathfinding.Node;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.Hashtable;

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
	 * Inserts a new node into the table. If a node by the same UUID is found, it is replaced with the new node.
	 * @param node Node object to insert.
	 */
	public void insertNode(Node node)
	{
		//Put this node into the cache
		nodeCache.put(node.getID(), node);

		try
		{
			String dataStr = node.getX() +
					", " + node.getY() +
					", " + node.getType() +
					", " + node.getFloor() +
					", '" + node.getBuilding() +
					"', '" + node.getName() + "')";

			//Check if the node exists first
			ResultSet results = statement.executeQuery("SELECT * FROM Nodes WHERE node_uuid='" + node.getID() + "'");
			if (!results.wasNull()) //TODO: why doesn't .next() work?
				statement.execute("DELETE FROM Nodes WHERE node_uuid='" + node.getID() + "'"); //Quash old value

			//Insert node base data
			statement.execute("INSERT INTO Nodes VALUES ('" + node.getID() + "', " + dataStr);

			//Now insert neighbor relationships by UUID, both into the database and into the cache
			PreparedStatement insertNeighbor = connection.prepareStatement("INSERT INTO Edges VALUES ('" +
					node.getID() + "', '0000')");
			for (Node nbr : node.getNeighbors())
			{
				//insertNeighbor.setString(2, nbr.getID());
				//insertNeighbor.execute();
				statement.execute("INSERT INTO Edges VALUES('" + node.getID() + "', '" + nbr.getID() + "')");
			}

		} catch (SQLException e)
		{
			System.out.println("Error inserting node into table 'Nodes'!");
			e.printStackTrace();
		}
	}

	/**
	 * Gets a node by its UUID. Returns null if that node didn't exist.
	 * @param uuid UUID of node to be returned.
	 * @return Node if node found, null otherwise. Note that this node is NOT safe for pathfinding!
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
			results = statement.executeQuery("SELECT * FROM Edges WHERE src='" + ret.getID() + "'");
			while (results.next())
			{
				if (nodeCache.containsKey(results.getString(2)))
					ret.addNeighbor(nodeCache.get(results.getString(2)));
			}

			//Now get links heading to this node
			results = statement.executeQuery("SELECT * FROM Edges WHERE dst='" + ret.getID() + "'");
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
				System.out.printf("Storing node '%s' in nodeCache hashtable...\n", node.getID());
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
