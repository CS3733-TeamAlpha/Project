package data;

import java.sql.*;
import java.util.ArrayList;

import pathfinding.ConcreteNode;

import static java.sql.DriverManager.println;

public class DatabaseController {

	//private static String dbURL = "jdbc:derby://localhost:1527/myDB;create=true;user=me;password=mine";
	static final String DB_URL = "jdbc:derby:FHAlpha;create=true";
	private static String providerTable = "Provider";
	private static String locationTable = "Location";
	private static String officeTable = "Office";
	private static String neighborTable = "Neighbor";
	private static String floorTable = "Floor";
	// jdbc Connection
	private static Connection connection = null;
	private static Statement stmt = null;


	//TODO: Remove main and properly initialize connections/tabes from elsewhere
	public static void main(String[] args) {
		createConnection();

		//TODO: GET RID OF THESE DROPTABLES, ONLY IN FOR TESTING
		droptablesForShittyTesting();

		//initialize tables
		initializeProviderTable();
		initializeFloorTable();
		initializeLocationTable();
		initializeOfficeTable();
		initializeNeighborTable();

		//test insertions
		insertFloor(1, "testbuilding", 1);
		insertLocation(11, "testloc", "Room", 0, 0, 1);
		insertLocation(12, "anotherloc", "Room", 1, 1, 1);
		insertProvider(01, "John", "Doe");
		insertOffice(01, 11);
		insertNeighbor(11, 12);
		//TODO: Make neighbor insertion go both ways? could there be benefit to preserving to/from?
		insertNeighbor(12, 11);

		//test get data
		getLocationByID(11);
		getLocationByID(12);
		getProviderByID(01);
		getNeighbors(11);
		getProviderLocations(01);
		getProvidersAtLocation(11);
		getProvidersAtLocation(12); //shouldn't have anything
		getFloorInfo(1);

		modifyFloor(1, "changef1", 2);
		getFloorInfo(1);
		modifyLocation(11, "asdf", "rm", 0, 0, 1);
		getLocationByID(11);
		modifyProvider(01, "sola", "shirai");
		getProviderByID(01);

		shutdown();
	}

	public static void droptablesForShittyTesting(){

		try
		{
			stmt = connection.createStatement();
			// Drop the UnpaidOrder table.
			stmt.execute("DROP TABLE Provider");
			System.out.println("Provider table dropped.");
		} catch (SQLException ex)
		{
			// No need to report an error.
			// The table simply did not exist.
		}
		try
		{
			stmt = connection.createStatement();
			// Drop the UnpaidOrder table.
			stmt.execute("DROP TABLE Location");
			System.out.println("Location table dropped.");
		} catch (SQLException ex)
		{
			// No need to report an error.
			// The table simply did not exist.
		}
		try
		{
			stmt = connection.createStatement();
			// Drop the UnpaidOrder table.
			stmt.execute("DROP TABLE Office");
			System.out.println("Office table dropped.");
		} catch (SQLException ex)
		{
			// No need to report an error.
			// The table simply did not exist.
		}
		try
		{
			stmt = connection.createStatement();
			// Drop the UnpaidOrder table.
			stmt.execute("DROP TABLE Neighbor");
			System.out.println("Neighbor table dropped.");
		} catch (SQLException ex)
		{
			// No need to report an error.
			// The table simply did not exist.
		}
		try
		{
			stmt = connection.createStatement();
			// Drop the UnpaidOrder table.
			stmt.execute("DROP TABLE Floor");
			System.out.println("Floor table dropped.");
		} catch (SQLException ex)
		{
			// No need to report an error.
			// The table simply did not exist.
		}	}

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
			stmt.execute("CREATE TABLE Provider(" +
					"ProviderID INT NOT NULL PRIMARY KEY, " +
					"FirstName VARCHAR(20), " +
					"LastName VARCHAR(20) " +
					")");
			System.out.println("Provider table initialized");
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
			stmt = connection.createStatement();
			stmt.execute("CREATE TABLE Location(" +
					"LocationID INT NOT NULL PRIMARY KEY, " +
					"LocationName VARCHAR(30), " +
					"LocationType VARCHAR(10), " +
					"XCoord INT, " +
					"YCoord INT, " +
					"FloorID INT REFERENCES Floor(FloorID)" +
					")");
			System.out.println("Location table initialized");
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
			stmt.execute("CREATE TABLE Office(" +
					"ProviderID INT NOT NULL REFERENCES Provider(ProviderID), " +
					"LocationID INT REFERENCES Location(LocationID)" +
					")");

			System.out.println("Office table initialized");
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
			stmt.execute("CREATE TABLE Neighbor(" +
					"FromID INT NOT NULL REFERENCES Location(LocationID), " +
					"ToID INT REFERENCES Location(LocationID) " +
					")");
			System.out.println("Neighbor table initialized");
			stmt.close();
		}
		catch (SQLException e){
			e.printStackTrace();
		}
	}

	public static void initializeFloorTable(){
		try
		{
			stmt = connection.createStatement();
			stmt.execute("CREATE TABLE Floor(" +
					"FloorID INT NOT NULL PRIMARY KEY, " +
					"Building VARCHAR(40), " +
					"FloorLevel INT NOT NULL" +
					")");
			System.out.println("Floor table initialized");
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
	public static ConcreteNode getLocationByID(int id){
		try
		{
			stmt = connection.createStatement();

			ResultSet results = stmt.executeQuery("SELECT * FROM Location " +
					"WHERE LocationID = " + id + "");
			//TODO: convert result into a location, or return relevant strings

			String LocName = " ";
			String LocType = " ";
			int XCoord = -1;
			int YCoord = -1;

			while(results.next())
			{
				int LocID = results.getInt(1);
				LocName = results.getString(2);
				LocType = results.getString(3);
				XCoord = results.getInt(4);
				YCoord = results.getInt(5);
				int Floor = results.getInt(6);
				System.out.println(LocID + "\t\t" + LocName + "\t\t" + LocType + "\t\t" + XCoord + YCoord + "\t\t" + Floor);
			}
			ArrayList<String> data = new ArrayList<>();
			data.add(LocName);
			data.add(LocType);
			ConcreteNode node = new ConcreteNode(data, XCoord, YCoord); //Return new node using location's information
			results.close();
			stmt.close();

			return node;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
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

			ResultSet results = stmt.executeQuery("SELECT * FROM Provider " +
					"WHERE ProviderID = " + id + "");
			//TODO: convert result into something, or return relevant strings
			System.out.println("ProviderID: " + id);
			while(results.next())
			{
				String fname = results.getString(2);
				String lname = results.getString(3);
				System.out.println(fname + ", "+ lname);
			}
			results.close();
			stmt.close();
		}
		catch (SQLException e){
			e.printStackTrace();
		}
	}

	/*
	 * Get providers at a specific location
	 * Use LocationID
	 * TODO: Fix return type instead of just printing
	 * TODO: Rename office table?
	 */
	public static void getProvidersAtLocation(int id){
		try
		{
			stmt = connection.createStatement();

			ResultSet results = stmt.executeQuery("SELECT * FROM Office " +
					"WHERE LocationID = " + id + "");
			//TODO: convert result into a provider, or return relevant strings
			System.out.println("Location " + id + " has providerIDs ");
			while(results.next())
			{
				int ProvID = results.getInt(1);
				System.out.println(ProvID + ", ");
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

			ResultSet results = stmt.executeQuery("SELECT * FROM Office " +
					"WHERE ProviderID = " + id + "");
			//TODO: convert result into a provider, or return relevant strings
			System.out.println("Location: " + id + " is at ");
			while(results.next())
			{
				int locID = results.getInt(2);
				System.out.println(locID + ", ");
			}
			results.close();
			stmt.close();
		}
		catch (SQLException e){
			e.printStackTrace();
		}
	}

	/*
	 * Get neighbors of a specific node
	 * TODO: Fix return type instead of just printing
	 */
	public static void getNeighbors(int id){
		try
		{
			stmt = connection.createStatement();

			ResultSet results = stmt.executeQuery("SELECT * FROM Neighbor " +
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

	public static void getFloorInfo(int id){
		try
		{
			stmt = connection.createStatement();

			ResultSet results = stmt.executeQuery("SELECT * FROM Floor " +
					"WHERE FloorID = " + id + "");
			//TODO: convert result into something, or return relevant strings
			System.out.println("FloorID " + id + " info " );
			while(results.next())
			{
				String bld = results.getString(2);
				int lvl = results.getInt(3);
				System.out.println(bld + " " + lvl);
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
	public static void insertNeighbor(int fromid, int toid){
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

	/*
 	* insert new floor
 	*/
	public static void insertFloor(int floorID, String name, int lvl){
		try
		{
			stmt = connection.createStatement();
			stmt.execute("insert into " + floorTable +
					" values (" + floorID + ", '" + name + "', "+ lvl + ")");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/*
	 * delete a single provider
 	 */
	public static void removeProvider(int provID){
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + providerTable +
					" WHERE ProviderID = " + provID + "");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/*
	 * delete a single location
 	 */
	public static void removeLocation(int locID){
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + locationTable +
					" WHERE LocationID = " + locID + "");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/*
	 * delete a single office relationship
	 */
	public static void removeOffice(int provID, int locID){
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + officeTable +
					" WHERE ProviderID = " + provID + " AND LocationID = " + locID + "");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/*
	 * delete all office relationships for a provider
	 */
	public static void removeOfficeByProvider(int provID){
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + officeTable +
					" WHERE ProviderID = " + provID + "");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/*
 	* delete all office relationships for a location
 	*/
	public static void removeOfficeByLocation(int locID){
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + officeTable +
					" WHERE LocationID = " + locID + "");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/*
 	* delete a single neighbor relationship
  	*/
	public static void removeNeighbor(int fromID, int toID){
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + neighborTable +
					" WHERE FromID = " + fromID + " AND ToID = " + toID + "");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/*
 	* delete all neighbor relationships from a certain ID
  	*/
	public static void removeNeighborsFromID(int fromID){
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + neighborTable +
					" WHERE FromID = " + fromID + "");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/*
 	* delete all neighbor relationships to a certain ID
 	 */
	public static void removeNeighborsToID(int toID){
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + neighborTable +
					" WHERE ToID = " + toID + "");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/*
 	* delete all neighbor relationships referencing a certain ID.
 	* Maybe unnecessary? TODO: Look into whether necessary or not
  	*/
	public static void removeAllNeighborsByID(int ID){
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + neighborTable +
					" WHERE FromID = " + ID + "OR ToID = " + ID + "");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/*
 	* delete a floor from the floor table
 	 */
	public static void removeFloorByID(int ID){
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + floorTable +
					" WHERE FloorID = " + ID + "");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/*
	 * modify a provider's entry in the table
	 * TODO: Probably should break this down to modify a single field at a time
	 * TODO: Fix return type?
	 */
	public static void modifyProvider(int ID, String fname, String lname){
		try
		{
			stmt = connection.createStatement();
			stmt.executeUpdate("UPDATE Provider " +
					"SET ProviderID = " + ID + ", " +
					"FirstName = '" + fname + "', " +
					"LastName = '" + lname + "' " +
					"WHERE ProviderID = " + ID +
					"");
			stmt.close();
		}
		catch (SQLException e){
			e.printStackTrace();
		}
	}


	/*
 	* modify a location's entry in the table
 	* TODO: Probably should break this down to modify a single field at a time
 	* TODO: Fix return type?
 	*/
	public static void modifyLocation(int ID, String name, String type, int x, int y, int floor)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.executeUpdate("UPDATE Location " +
					"SET LocationID = " + ID + ", " +
					"LocationName = '" + name + "', " +
					"LocationType = '" + type + "', " +
					"XCoord = " + x + ", " +
					"YCoord = " + y + ", " +
					"FloorID = " + floor + " " +
					"WHERE LocationID = " + ID +
					"");
			stmt.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}


	/*
 	* modify a floor's entry in the table
 	* TODO: Probably should break this down to modify a single field at a time
 	* TODO: Fix return type?
 	*/
	public static void modifyFloor(int ID, String name, int lvl)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.executeUpdate("UPDATE Floor " +
					"SET FloorID = " + ID + ", " +
					"Building = '" + name + "', " +
					"FloorLevel = " + lvl + " " +
					"WHERE FloorID = " + ID +
					"");
			stmt.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

}