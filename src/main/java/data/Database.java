package data;

import org.apache.derby.tools.ij;
import pathfinding.ConcreteNode;
import pathfinding.Node;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;

public class Database
{
	//Constants
	private static final String DB_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	private static final String DB_CREATE_SQL = "/db/DBCreate.sql";

	private String dbName;
	private boolean connected;
	private Statement statement;
	private Connection connection;

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
		connect();
		if (connected)
			initTables();
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
		String dataStr = node.getX() +
					", " + node.getY() +
					", " + node.getType() +
					", " + node.getFloor() +
					", '" + node.getBuilding() +
					"', '" + node.getName() + "')";

		try
		{
			//Check if the node exists first
			ResultSet results = statement.executeQuery("SELECT * FROM Nodes WHERE node_uuid='" + node.getID() + "'");
			if (!results.wasNull()) //TODO: why doesn't .next() work?
				statement.execute("DELETE FROM Nodes WHERE node_uuid='" + node.getID() + "'"); //Quash old value

			//Insert node base data
			statement.execute("INSERT INTO Nodes VALUES (" + "'" + node.getID() + "', " + dataStr);

			//Now insert neighbor relationships by UUID
			PreparedStatement insertNeighbor = connection.prepareStatement("INSERT INTO Edges VALUES ('" +
					node.getID() + "', '0000')");
			for (Node nbr : node.getNeighbors())
			{
				insertNeighbor.setString(1, nbr.getID());
				insertNeighbor.execute();
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
		try
		{
			ResultSet results = statement.executeQuery("SELECT * FROM Nodes WHERE node_uuid='" + uuid + "'");
			if (!results.next())
				return null;

			//Construct a new concrete node... don't do anything with the neighbors yet though.
			Node ret = new ConcreteNode(results.getString(1), results.getString(7), results.getString(6),
					results.getDouble(2), results.getDouble(3), results.getInt(4), results.getInt(5));
			return ret;
		} catch (SQLException e)
		{
			System.out.println("Error trying to get node by UUID!");
			e.printStackTrace();
		}
		return null;
	}

	public void deleteNodeByUUID(String uuid)
	{
		try
		{
			statement.execute("DELETE FROM Nodes WHERE node_uuid='" + uuid + "'");
		} catch (SQLException e)
		{
			System.out.println("Error trying to delete node from table!");
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
