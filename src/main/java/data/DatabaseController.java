package data;

import pathfinding.ConcreteNode;
import pathfinding.Node;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

import pathfinding.Node;
import pathfinding.ConcreteNode;

import static java.sql.DriverManager.println;

public class DatabaseController
{

	//private static String dbURL = "jdbc:derby://localhost:1527/myDB;create=true;user=me;password=mine";
	static final String DB_URL = "jdbc:derby:FHAlpha;create=true";
	static final String DB_TEST_URL = "jdbc:derby:TestFHAlpha;create=true";
	private static String providerTable = "Provider";
	private static String nodeTable = "Node";
	private static String officeTable = "Office";
	private static String neighborTable = "Neighbor";
	private static String floorTable = "Floor";
	// jdbc Connection
	private static Connection connection = null;
	//private static Connection testConnection = null;
	private static Statement stmt = null;
	private static ArrayList<Node> nodeList = new ArrayList<Node>();
	private static ArrayList<Provider> providerList = new ArrayList<Provider>();
	private static ArrayList<Floor> floorList = new ArrayList<Floor>();

	static
	{
		createConnection();

		//initialize tables
		initializeProviderTable();
		initializeFloorTable();
		insertFloor(3, "defaultFloor", 3); //insert default floor for minimal app
		initializeNodeTable();
		initializeOfficeTable();
		initializeNeighborTable();
	}

	public static void createTestConnection()
	{
		//shutdown the standard connection
		shutdown();
		try
		{
			Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
			//Get a connection
			connection = DriverManager.getConnection(DB_TEST_URL);
		} catch (Exception except)
		{
			except.printStackTrace();
			//remove this piece
			println("error here");
		}

	}

	public static void shutdownTest()
	{
		try
		{
			if (stmt != null)
			{
				stmt.close();
			}
			if (connection != null)
			{
				DriverManager.getConnection(DB_TEST_URL + ";shutdown=true");
				connection.close();
				//delete the test database contents and folder
				File index = new File("TESTFHAlpha");
				if (index.exists()) {
					String[]entries = index.list();
					for(String s: entries){
						File currentFile = new File(index.getPath(),s);
						currentFile.delete();
					}
					index.delete();
				}
			}
		} catch (SQLException sqlExcept)
		{

		}
	}

	/**
	 * Creates a connection to the DB.
	 */
	public static void createConnection()
	{

		try
		{
			Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
			//Get a connection
			connection = DriverManager.getConnection(DB_URL);
		} catch (Exception except)
		{
			except.printStackTrace();
			//remove this piece
			println("error here");
		}
	}

	/**
	 * Shuts down the statement
	 */
	protected static void shutdown()
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
		} catch (SQLException sqlExcept)
		{

		}

	}

	/**
	 * Intiailize the provider table if not already initialized
	 */
	public static void initializeProviderTable()
	{
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
		} catch (SQLException sqlExcept)
		{
			if (!sqlExcept.getSQLState().equals("X0Y32")) //TODO: Remove magic numbers
			{
				sqlExcept.printStackTrace();
			} else
			{
				System.out.println("Provider table already exists");
			}
		}
	}

	/**
	 * Initialize the node table if not already initialized
	 */
	public static void initializeNodeTable()
	{
		try
		{
			//TODO: Node type is ok as a string? or change?
			stmt = connection.createStatement();
			stmt.execute("CREATE TABLE Node(" +
					"NodeID INT NOT NULL PRIMARY KEY, " +
					"NodeName VARCHAR(30), " +
					"NodeType VARCHAR(10), " +
					"XCoord DOUBLE, " +
					"YCoord DOUBLE, " +
					"FloorID INT REFERENCES Floor(FloorID)" +
					")");
			System.out.println("Node table initialized");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			if (!sqlExcept.getSQLState().equals("X0Y32"))
			{
				sqlExcept.printStackTrace();
			} else
			{
				System.out.println("Node table already exists");
			}
		}
	}

	/**
	 * Initialize the office table, if not already created
	 */
	public static void initializeOfficeTable()
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("CREATE TABLE Office(" +
					"ProviderID INT NOT NULL REFERENCES Provider(ProviderID), " +
					"NodeID INT REFERENCES Node(NodeID), " +
					"CONSTRAINT UQ_OFFICE UNIQUE(ProviderID, NodeID)" +
					")");

			System.out.println("Office table initialized");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			if (!sqlExcept.getSQLState().equals("X0Y32"))
			{
				sqlExcept.printStackTrace();
			} else
			{
				System.out.println("Office table already exists");
			}
		}
	}

	/**
	 * Initialize the neighbor table, if not already created
	 */
	public static void initializeNeighborTable()
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("CREATE TABLE Neighbor(" +
					"FromID INT NOT NULL REFERENCES Node(NodeID), " +
					"ToID INT REFERENCES Node(NodeID) " +
					")");
			System.out.println("Neighbor table initialized");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			if (!sqlExcept.getSQLState().equals("X0Y32"))
			{
				sqlExcept.printStackTrace();
			} else
			{
				System.out.println("Neighbor table already exists");
			}
		}
	}

	/**
	 * Initialize the floor table, if not already created
	 */
	public static void initializeFloorTable()
	{
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
		} catch (SQLException sqlExcept)
		{
			if (!sqlExcept.getSQLState().equals("X0Y32"))
			{
				sqlExcept.printStackTrace();
			} else
			{
				System.out.println("Floor table already exists");
			}
		}
	}

	//make floor objects from all entries in the floor table
	public static void initializeAllFloors()
	{
		try
		{
			floorList.clear(); //sanitize floorlist
			stmt = connection.createStatement();

			ResultSet results = stmt.executeQuery("SELECT * FROM Floor " + "");

			ArrayList<Integer> floorIDs = new ArrayList<Integer>();
			while (results.next())
			{
				int FloorID = results.getInt(1);
				floorIDs.add(FloorID);
			}
			for (int i = 0; i < floorIDs.size(); i++)
			{
				floorList.add(makeFloorByID(floorIDs.get(i)));
			}
			results.close();
			stmt.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	//make node objects from all entries in the node table
	//dependent on floorlist already being initialized
	public static void initializeAllNodes()
	{
		try
		{
			nodeList.clear(); //sanitize nodelist
			stmt = connection.createStatement();

			ResultSet results = stmt.executeQuery("SELECT * FROM Node " + "");

			ArrayList<Integer> nodeIDs = new ArrayList<Integer>();
			while (results.next())
			{
				int NodeID = results.getInt(1);
				nodeIDs.add(NodeID);
			}
			for (int i = 0; i < nodeIDs.size(); i++)
			{
				nodeList.add(makeNodeByID(nodeIDs.get(i)));
			}
			for (int i = 0; i < nodeList.size(); i++)
			{
				nodeList.get(i).addNeighbors(getNeighbors(nodeList.get(i).getID()));
			}
			results.close();
			stmt.close();

		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	//make provider objects from all entries in the provider table
	//dependent on providerlist already being initialized
	public static void initializeAllProviders()
	{
		try
		{
			providerList.clear(); //sanitize providerList
			stmt = connection.createStatement();

			ResultSet results = stmt.executeQuery("SELECT * FROM Provider " + "");

			ArrayList<Integer> provIDs = new ArrayList<Integer>();
			while (results.next())
			{
				int NodeID = results.getInt(1);
				provIDs.add(NodeID);
			}
			for (int i = 0; i < provIDs.size(); i++)
			{
				providerList.add(makeProviderByID(provIDs.get(i)));
			}
			results.close();
			stmt.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Create a new node based on xy coordinates and the floor it's on
	 *
	 * @param x       new node's x coordinate
	 * @param y       new node's y coordinate
	 * @param floorid new node's floor
	 * @return the newly created node
	 */
	public static Node generateNewNode(String name, String type, double x, double y, int floorid)
	{
		try
		{
			stmt = connection.createStatement();
			ResultSet results = stmt.executeQuery("SELECT * FROM NODE " +
					"ORDER BY NodeID");

			int newID = -1;
			while (results.next())
			{
				newID = results.getInt(1);
			}
			newID++;
			String NodeName = name;
			String NodeType = type;
			double XCoord = x;
			double YCoord = y;
			Floor flr = getFloorByID(floorid);

			ArrayList<String> data = new ArrayList<>();
			data.add(NodeName);
			data.add(NodeType);
			ConcreteNode node = new ConcreteNode(newID, data, XCoord, YCoord, flr); //Return new node using node's information
			results.close();
			stmt.close();

			return node;
		} catch (SQLException e)
		{
			//TODO: properly handle exceptions
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * make a single node by NodeID
	 * @param id NodeID of node object to be created
	 */
	public static ConcreteNode makeNodeByID(int id)
	{
		try
		{
			stmt = connection.createStatement();

			ResultSet results = stmt.executeQuery("SELECT * FROM Node " +
					"WHERE NodeID = " + id + "");

			String NodeName = " ";
			String NodeType = " ";
			int XCoord = -1;
			int YCoord = -1;
			Floor flr = null;

			if (results.next())
			{
				NodeName = results.getString(2);
				NodeType = results.getString(3);
				XCoord = results.getInt(4);
				YCoord = results.getInt(5);
				flr = getFloorByID(results.getInt(6));
			} else
			{
				return null;
			}
			ArrayList<String> data = new ArrayList<>();
			data.add(NodeName);
			data.add(NodeType);
			ConcreteNode node = new ConcreteNode(id, data, XCoord, YCoord, flr); //Return new node using node's information
			results.close();
			stmt.close();

			return node;
		} catch (SQLException e)
		{
			//TODO: properly handle exceptions
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * retrieve a node from nodeList based on id
	 * @param id NodeID of desired node
	 */
	public static Node getNodeByID(int id)
	{
		for (Node n : nodeList)
		{
			if (n.getID() == id)
			{
				return n;
			}
		}
		//TODO: Handle when no match was found
		return null;
	}

	/*
	 * get a single node by xy coordinates. assuming only one node can exist at a coordinate
	 * TODO: fix if multiple nodes in same location
	 */
	public static Node getNodeByXY(int x, int y)
	{
		for (Node n : nodeList)
		{
			if (n.getX() == x && n.getY() == y)
			{
				return n;
			}
		}
		//TODO: handle if no node found at xy coordinate
		return null;
	}

	/*
	 * get nodes based on their NodeName, contained in data
	 * @param name The name of the node to find
	 */
	public static Node getNodeByName(String name)
	{
		for (Node n : nodeList)
		{
			if (n.containsData(name))
			{
				//TODO: This just returns the first match. what if multiple matches?
				return n;
			}
		}
		//TODO: handle when no match is found
		return null;
	}

	/**
	 * Get the nearest node given X and Y coordinates.
	 * Generates a node and pass it to the getNearestNode(Node) function
	 *
	 * @param x x coordinate to find nearest node from
	 * @param y y coordinate ||
	 * @return the Node object closest to the given xy coordinates
	 */
	public static Node getNearestNode(double x, double y)
	{
		//initialize a placeholder node and pass to GetNearestNode(node) function
		Node fakeNode = new ConcreteNode(-1, null, x, y, getFloorByID(3));
		return getNearestNode(fakeNode);
	}

	/**
	 * Get a node closest to a source node.
	 * currently assuming nodes can't have the exact same XY coordinates
	 * Check floorID is different to discern between different building/floors
	 *
	 * @param source The souce node from which we want to find the nearest node
	 */
	public static Node getNearestNode(Node source)
	{
		double min = -1;
		Node nearest = null;

		for (Node n : nodeList)
		{
			if (n.getX() == source.getX() && n.getY() == source.getY())
			{ //todo: something for same location?
			} else if (n.getOnFloor().getID() == source.getOnFloor().getID()) //check node is on same floor
			{
				double dist = source.distance(n);
				if (dist < min || min == -1)
				{
					min = dist;
					nearest = n;
				}
			}
		}
		return nearest;
	}

	/**
	 * Make a single provider by ID.
	 * Relies on nodeList already being initialized
	 *
	 * @param id ProviderID
	 */
	public static Provider makeProviderByID(int id)
	{
		try
		{
			stmt = connection.createStatement();

			ResultSet results = stmt.executeQuery("SELECT * FROM Provider " +
					"WHERE ProviderID = " + id + "");
			String fname = "";
			String lname = "";

			if (results.next())
			{
				fname = results.getString(2);
				lname = results.getString(3);
			} else
			{
				return null;
			}
			results.close();
			stmt.close();
			Provider p = new Provider(id, fname, lname);
			p.addLocations(getProviderNodes(id));
			return p;
		} catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get a single provider by id
	 *
	 * @param id ProviderID
	 */
	public static Provider getProviderByID(int id)
	{
		for (Provider p : providerList)
		{
			if (p.getID() == id)
			{
				return p;
			}
		}
		//TODO: handle when no provider matches
		return null;
	}

	/**
	 * Get providers by name. expect possibility of duplicates since no uniqueness constraints
	 *
	 * @param f FirstName of provider
	 * @param l LastName of provider
	 */
	public static ArrayList<Provider> getProvidersByFullName(String f, String l)
	{
		ArrayList<Provider> provList = new ArrayList<Provider>();
		for (Provider p : providerList)
		{
			if (p.getfName().equals(f) && p.getlName().equals(l))
			{
				provList.add(p);
			}
		}
		return provList;
	}

	/**
	 * Get providers at a specific node
	 *
	 * @param id NodeID to get providers from
	 */
	public static ArrayList<Provider> getProvidersAtNode(int id)
	{
		ArrayList<Provider> pList = new ArrayList<Provider>();
		Node target = getNodeByID(id);
		for (Provider p : providerList)
		{
			if (p.atLocation(target))
			{
				pList.add(p);
			}
		}
		//TODO: Handle no providers at a node
		return pList;
	}

	/**
	 * Get nodes a provider is associated with
	 *
	 * @param id ProviderID of provider for which we are looking for linked nodes
	 */
	public static ArrayList<Node> getProviderNodes(int id)
	{
		try
		{
			stmt = connection.createStatement();

			ResultSet results = stmt.executeQuery("SELECT * FROM Office " +
					"WHERE ProviderID = " + id + "");

			ArrayList<Node> provNodes = new ArrayList<Node>();
			while (results.next())
			{
				int nodeID = results.getInt(2);
				if (!provNodes.contains(getNodeByID(nodeID)))
				{
					provNodes.add(getNodeByID(nodeID));
				}
			}
			results.close();
			stmt.close();
			return provNodes;
		} catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get neighbors of a specific node
	 *
	 * @param id NodeID from which we are getting neighbors
	 */
	public static ArrayList<Node> getNeighbors(int id)
	{
		try
		{
			stmt = connection.createStatement();
			ArrayList<Node> neighbors = new ArrayList<Node>();

			ResultSet results = stmt.executeQuery("SELECT * FROM Neighbor " +
					"WHERE FromID = " + id + "");
			while (results.next())
			{
				int ToID = results.getInt(2);
				//TODO: Handle null nodes being added or fix getNodeByID
				neighbors.add(getNodeByID(ToID));
			}
			results.close();
			stmt.close();
			return neighbors;
		} catch (SQLException e)
		{
			e.printStackTrace();
			//TODO: Handle exceptions
			return null;
		}
	}

	/**
	 * Make a floor from floor table
	 *
	 * @param id FloorID of the floor to make
	 */
	public static Floor makeFloorByID(int id)
	{
		try
		{
			stmt = connection.createStatement();

			ResultSet results = stmt.executeQuery("SELECT * FROM Floor " +
					"WHERE FloorID = " + id + "");
			String bld = "";
			int lvl = 0;

			if (results.next())
			{
				bld = results.getString(2);
				lvl = results.getInt(3);
			} else
			{
				return null;
			}
			results.close();
			stmt.close();

			return new Floor(id, bld, lvl);
		} catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get a floor from floorList
	 *
	 * @param id FloorID of floor to search for
	 */
	public static Floor getFloorByID(int id)
	{
		for (Floor f : floorList)
		{
			if (f.getID() == id)
			{
				return f;
			}
		}
		//TODO: handle when no match is found
		return null;
	}

	/**
	 * Insert new provider into table
	 */
	public static void insertProvider(int provID, String fname, String lname)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("insert into " + providerTable + " values (" + provID + ", '" + fname + "', '" + lname + "')");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			if (!sqlExcept.getSQLState().equals("23505"))
			{
				sqlExcept.printStackTrace();
			} else
			{
				//TODO: handle already exist, perhaps update instead
				System.out.println("ProviderID already exists");
			}
		}
	}

	/**
	 * insert a list of new nodes into the database, as well as their neighbor relationships.
	 * Currently completely unoptimized. goes through every node and inserts it into database, then
	 * goes through every node agian and adds its neighbor relations to the neighbor table.
	 * <p>
	 * Neighbor relations need to be added after all nodes have been inserted so that no
	 * uninitialized nodes are being referenced.
	 *
	 * @param nodes List of nodes to insert
	 */
	public static void insertNodeList(ArrayList<Node> nodes)
	{
		for (Node n : nodes)
		{
			ConcreteNode newNode = (ConcreteNode) n;
			insertNode(newNode);
		}
		for (Node n : nodes)
		{
			ConcreteNode newNode = (ConcreteNode) n;
			for (Node nn : newNode.getNeighbors())
			{
				ConcreteNode nnn = (ConcreteNode) nn;
				insertNeighbor(newNode, nnn);
			}
		}
	}

	/**
	 * Insert new node from a concrete node into table
	 * IMPORTANT this doesn't add neighbor relationships
	 * <p>
	 * Node floor is defaulted to 3 for now since it is our default floor.
	 *
	 * @param newNode
	 */
	public static void insertNode(ConcreteNode newNode)
	{
		int id = newNode.getID();
		String name = "";
		String type = "";
		if (newNode.getData().size() >= 2)
		{
			name = newNode.getData().get(0);
			type = newNode.getData().get(1);
		}
		double x = newNode.getX();
		double y = newNode.getY();
		int floor = 3; //TODO: default floor to 3 since first iteration is just on 3rd floor
		insertNode(id, name, type, x, y, floor);
	}

	/**
	 * Insert new node into table
	 */
	public static void insertNode(int nodeID, String name, String type, double x, double y, int floor)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("insert into " + nodeTable +
					" values (" + nodeID + ", '" + name + "', '" + type + "', " + x + ", " + y + ", " + floor + ")");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			if (!sqlExcept.getSQLState().equals("23505"))
			{
				sqlExcept.printStackTrace();
			} else
			{
				//TODO: Handle node already existing, perhaps update instead
				System.out.println("NodeID already exists");
			}
		}
	}

	/**
	 * Insert new neighbor from ConcreteNodes
	 * This will extract the nodeIDs and pass those to the insertNeighbor(int, int) function.
	 * <p>
	 * This only inserts a neighbor relationship from node1 to node2, not both ways.
	 * Insertneighbor must be called with the argument order switched if you want bidirectional
	 * neighbor relationship in the database
	 */
	public static void insertNeighbor(ConcreteNode node1, ConcreteNode node2)
	{
		insertNeighbor(node1.getID(), node2.getID());
	}

	/**
	 * Insert new node neighbor
	 * Neighbore relationship is created as fromid to toid.
	 * ID refers to NodeID
	 * neighbor relationships are not bidirectional.
	 */
	public static void insertNeighbor(int fromid, int toid)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("insert into " + neighborTable +
					" values (" + fromid + ", " + toid + ")");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			if (!sqlExcept.getSQLState().equals("23505"))
			{
				sqlExcept.printStackTrace();
			} else
			{
				System.out.println("Neighbor relation already exists");
			}
		}
	}

	/**
	 * Insert new provider node relationship (office) into table
	 */
	public static void insertOffice(int provID, int nodeID)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("insert into " + officeTable +
					" values (" + provID + ", " + nodeID + ")");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			if (!sqlExcept.getSQLState().equals("23505"))
			{
				sqlExcept.printStackTrace();
			} else
			{
				//TODO: handle already existing, perhaps update instead
				System.out.println("Office relation already exists");
			}
		}
	}

	/**
	 * Insert new floor into table
	 */
	public static void insertFloor(int floorID, String name, int lvl)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("insert into " + floorTable +
					" values (" + floorID + ", '" + name + "', " + lvl + ")");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			if (!sqlExcept.getSQLState().equals("23505"))
			{
				sqlExcept.printStackTrace();
			} else
			{
				//TODO: handle floor already existing, perhaps update instead
				System.out.println("FloorID already exists");
			}
		}
	}

	/**
	 * Delete a single provider from tables
	 */
	public static void removeProvider(int provID)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + providerTable +
					" WHERE ProviderID = " + provID + "");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/**
	 * Delete a single node from tables
	 */
	public static void removeNode(int nodeID)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + nodeTable +
					" WHERE NodeID = " + nodeID + "");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/**
	 * Delete a single office relationship from tables
	 */
	public static void removeOffice(int provID, int nodeID)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + officeTable +
					" WHERE ProviderID = " + provID + " AND NodeID = " + nodeID + "");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/**
	 * Delete all office relationships for a provider from tables
	 */
	public static void removeOfficeByProvider(int provID)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + officeTable +
					" WHERE ProviderID = " + provID + "");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/**
	 * Delete all office relationships for a node from tables
	 */
	public static void removeOfficeByNode(int nodeID)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + officeTable +
					" WHERE NodeID = " + nodeID + "");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/**
	 * Delete a single neighbor relationship from tables
	 */
	public static void removeNeighbor(int fromID, int toID)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + neighborTable +
					" WHERE FromID = " + fromID + " AND ToID = " + toID + "");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/**
	 * Delete all neighbor relationships from a certain ID from tables
	 */
	public static void removeNeighborsFromID(int fromID)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + neighborTable +
					" WHERE FromID = " + fromID + "");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/**
	 * Delete all neighbor relationships to a certain ID from tables
	 */
	public static void removeNeighborsToID(int toID)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + neighborTable +
					" WHERE ToID = " + toID + "");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/**
	 * Delete all neighbor relationships referencing a certain ID.
	 * Maybe unnecessary? T
	 *
	 * @// TODO: Look into whether necessary or not
	 */
	public static void removeAllNeighborsByID(int ID)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + neighborTable +
					" WHERE FromID = " + ID + "OR ToID = " + ID + "");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/**
	 * Delete a floor from the floor table
	 */
	public static void removeFloorByID(int ID)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("DELETE FROM " + floorTable +
					" WHERE FloorID = " + ID + "");
			stmt.close();
		} catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/**
	 * Modify a provider's entry in the table
	 *
	 * @// TODO: Probably should break this down to modify a single field at a time
	 * @// TODO: Fix return type?
	 */
	public static void modifyProviderTable(int ID, String fname, String lname)
	{
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
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Modify a node's entry in the table from a ConcreteNode
	 */
	public static void modifyNodeTable(ConcreteNode modNode)
	{
		int id = modNode.getID();
		String name = "";
		String type = "";
		if (modNode.getData().size() >= 2)
		{
			name = modNode.getData().get(0);
			type = modNode.getData().get(1);
		}
		double x = modNode.getX();
		double y = modNode.getY();
		int floor = 3; //TODO: default floor to 3 since first iteration is just on 3rd floor
		System.out.println("Modifying");
		modifyNodeTable(id, name, type, x, y, floor);
	}

	/**
	 * parse a list of nodes that have been modified and run the modifyNodeTable for each
	 *
	 * @param modNodes List of modified Nodes
	 */
	public static void modifyNodes(ArrayList<Node> modNodes)
	{
		for (Node n : modNodes)
		{
			//TODO: do something better than hard cast to ConcreteNode?
			ConcreteNode cn = (ConcreteNode) n;
			modifyNodeTable(cn);
		}
	}

	/*
	 * modify a node's entry in the table
	 * TODO: Probably should break this down to modify a single field at a time
	 * TODO: Fix return type?
	 */
	public static void modifyNodeTable(int ID, String name, String type, double x, double y, int floor)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.executeUpdate("UPDATE Node " +
					"SET NodeID = " + ID + ", " +
					"NodeName = '" + name + "', " +
					"NodeType = '" + type + "', " +
					"XCoord = " + x + ", " +
					"YCoord = " + y + ", " +
					"FloorID = " + floor + " " +
					"WHERE NodeID = " + ID +
					"");
			stmt.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Modify a floor's entry in the table
	 *
	 * @// TODO: Probably should break this down to modify a single field at a time
	 * @// TODO: Fix return type?
	 */
	public static void modifyFloorTable(int ID, String name, int lvl)
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

	public static ArrayList<Node> getAllNodes()
	{
		return nodeList;
	}

	public static ArrayList<Provider> getAllProviders()
	{
		return providerList;
	}

	public static ArrayList<Floor> getAllFloors()
	{
		return floorList;
	}

}
