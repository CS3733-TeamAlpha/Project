package data;

import java.sql.*;

public class Database
{
	//Constants
	private static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";

	private String dbName;
	private boolean connected;

	private Statement statement;

	/**
	 * Construct a new database object that will connect to the named database and immediately initiate the connection
	 * @param name Path to database to connect to.
	 */
	public Database(String name)
	{
		dbName = name;
		connected = false;
		statement = null;
		connect();
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
			statement = DriverManager.getConnection("jdbc:derby:" + dbName + ";create=true").createStatement();
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
			DriverManager.getConnection("jdbc:derby:" + dbName + ";shutdown=true").close(); //Well that was quick!
		} catch (SQLException e)
		{
			if (!e.getSQLState().equals("XJ015"))
			{
				System.out.println("Couldn't shutdown derby engine correctly!");
				e.printStackTrace();
			}
		}
	}
}
