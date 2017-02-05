package data;

import java.sql.*;

import static java.sql.DriverManager.println;

public class DatabaseController {

	//private static String dbURL = "jdbc:derby://localhost:1527/myDB;create=true;user=me;password=mine";
	static final String DB_URL = "jdbc:derby:FHAlpha;create=true";
	private static String providerTable = "Provider";
	private static String locationTable = "Location";
	private static String officeTable = "Office";
	private static String neighborTable = "Neighbor";
	// jdbc Connection
	private static Connection connection = null;
	private static Statement stmt = null;



	public static void main(String[] args) {
		createConnection();
		//insertInfo(1, "Amil", "Shah");
		initializeProviderTable();
		initializeLocationTable();
		initializeOfficeTable();
		initializeNeighborTable();
		//printResults();
		shutdown();
	}

	//examples of commands to create tables and attributes, etc.
	//public static final String CREATE_ITEMS_DB = "CREATE TABLE items (item_id INTEGER NOT NULL, item_name VARCHAR(20) NOT NULL, item_price REAL NOT NULL, multiplicity_shop INTEGER NOT NULL, multiplicity_store INTEGER NOT NULL)";
	//public static final String INSERT_PRODUCT = "INSERT INTO items (item_id, item_name, item_price, multiplicity_shop, multiplicity_store) VALUES (?, ?, ?, ?, ?)";
	//public static final String CLEAR_ITEMS_DB = "DELETE FROM items";

	//used for creating connection to the DB
	protected static void createConnection() {

		try
		{
			Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
			//Get a connection
			connection = DriverManager.getConnection(DB_URL);
		}
		catch (Exception except)
		{
			except.printStackTrace();
			//remove this piece
			println("error here");
		}
	}

	//shuts down the statement
	private static void shutdown()
	{
		try
		{
			if (stmt != null)
			{
				stmt.close();
			}
			if (connection != null)
			{
				DriverManager.getConnection(DB_URL + ";shutdown=true");
				connection.close();
			}
		}
		catch (SQLException sqlExcept)
		{

		}

	}

	public static void initializeProviderTable(){
		try
		{
			stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE Provider(" +
					"ProviderID INT NOT NULL PRIMARY KEY, " +
					"FirstName VARCHAR(20), " +
					"LastName VARCHAR(20) " +
					")");
			stmt.close();
		}
		catch (SQLException e){
			e.printStackTrace();
		}
	}

	public static void initializeLocationTable(){
		try
		{
			//TODO: Location type is ok as a string? or change?
			//TODO: FLoorID reference foreign key
			stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE Location(" +
					"LocationID INT NOT NULL PRIMARY KEY, " +
					"LocationName VARCHAR(30), " +
					"LocationType VARCHAR(10), " +
					"XCoord INT, " +
					"YCoord INT, " +
					"FloorID INT" +
					")");
			stmt.close();
		}
		catch (SQLException e){
			e.printStackTrace();
		}
	}

	public static void initializeOfficeTable(){
		try
		{
			stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE Office(" +
					"ProviderID INT NOT NULL REFERENCES Provider(ProviderID), " +
					"LocationID INT REFERENCES Location(LocationID)" +
					")");
			stmt.close();
		}
		catch (SQLException e){
			e.printStackTrace();
		}
	}

	public static void initializeNeighborTable(){
		try
		{
			stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE Neighbor(" +
					"FromID INT NOT NULL REFERENCES Location(LocationID), " +
					"ToID INT REFERENCES Location(LocationID) " +
					")");
			stmt.close();
		}
		catch (SQLException e){
			e.printStackTrace();
		}
	}

	/*
	 * Get a single location by LocationID
	 * TODO: Fix return type instead of just printing
	 */
	public static void getLocationByID(int id){
		try
		{
			stmt = connection.createStatement();

			ResultSet results = stmt.executeQuery("SELECT * FROM Location" +
					"WHERE LocationID = " + id + "");
			//TODO: convert result into a location, or return relevant strings
			while(results.next())
			{
				int LocID = results.getInt(1);
				String LocName = results.getString(2);
				String LocType = results.getString(3);
				int XCoord = results.getInt(4);
				int YCoord = results.getInt(5);
				int Floor = results.getInt(6);
				System.out.println(LocID + "\t\t" + LocName + "\t\t" + LocType + "\t\t" + XCoord + YCoord + "\t\t" + Floor);
			}
			results.close();
			stmt.close();
		}
		catch (SQLException e){
			e.printStackTrace();
		}
	}

	/*
	 * Get a single provider by ProviderID
 	* TODO: Fix return type instead of just printing
 	*/
	public static void getProviderByID(int id){
		try
		{
			stmt = connection.createStatement();

			ResultSet results = stmt.executeQuery("SELECT * FROM Provider" +
					"WHERE ProviderID = " + id + "");
			//TODO: convert result into something, or return relevant strings
			System.out.println("ProviderID: " + id);
			while(results.next())
			{
				int LocID = results.getInt(2);
				System.out.println(LocID + ", ");
			}
			results.close();
			stmt.close();
		}
		catch (SQLException e){
			e.printStackTrace();
		}
	}

	/*
	 * Get locations a provider is associated with from the office table
	 * Use ProviderID
	 * TODO: Fix return type instead of just printing
	 * TODO: Rename office table?
	 */
	public static void getProviderLocations(int id){
		try
		{
			stmt = connection.createStatement();

			ResultSet results = stmt.executeQuery("SELECT * FROM Office" +
					"WHERE ProviderID = " + id + "");
			//TODO: convert result into a provider, or return relevant strings
			while(results.next())
			{
				int ProvID = results.getInt(1);
				String FName = results.getString(2);
				String LName = results.getString(3);
				System.out.println(ProvID + "\t\t" + FName + "\t\t" + LName);
			}
			results.close();
			stmt.close();
		}
		catch (SQLException e){
			e.printStackTrace();
		}
	}

	/*
	 * Get neightbors of a specific node
	 * TODO: Fix return type instead of just printing
	 */
	public static void getNeighbors(int id){
		try
		{
			stmt = connection.createStatement();

			ResultSet results = stmt.executeQuery("SELECT * FROM Neighbor" +
					"WHERE FromID = " + id + "");
			//TODO: convert result into something, or return relevant strings
			System.out.println("LocationID " + id + " connects to " );
			while(results.next())
			{
				int ToID = results.getInt(2);
				System.out.println(ToID + ", ");
			}
			results.close();
			stmt.close();
		}
		catch (SQLException e){
			e.printStackTrace();
		}
	}

	/*
	 * insert new provider
	 */
	public static void insertProvider (int provID, String fname, String lname) {
		try
		{
			stmt = connection.createStatement();
			stmt.execute("insert into " + providerTable + " values (" + provID + ", '" + fname + "', '" + lname + "')");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/*
	 * insert new location
	 */
	public static void insertLocation (int locID, String name, String type, int x, int y, int floor) {
		try
		{
			stmt = connection.createStatement();
			stmt.execute("insert into " + locationTable +
					" values (" + locID + ", '" + name + "', '" + type + "', " + x + ", " + y + ", " + floor + ")");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/*
	 * insert new location neighbor
	 */
	public static void insertNeightbor(int fromid, int toid){
		try
		{
			stmt = connection.createStatement();
			stmt.execute("insert into " + neighborTable +
					" values (" + fromid + ", " + toid + ")");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/*
	 * insert new provider office
	 */
	public static void insertOffice(int provID, int locID){
		try
		{
			stmt = connection.createStatement();
			stmt.execute("insert into " + officeTable +
					" values (" + provID + ", " + locID + ")");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

}