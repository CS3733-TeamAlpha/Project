package data;

import pathfinding.ConcreteNode;
import pathfinding.Node;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

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
	private static String residesInTable = "ResidesIn";
	private static String servicesTable = "Services";
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


		//prints results of all tables defined
		printResults(nodeTable);
		printResults(floorTable);
		printResults(providerTable);
		printResults(servicesTable);
		printOffice(officeTable);
		printResidesIn(residesInTable);

		//updateRefInt();

		//******* NEW WORK ******
		initializeAllNodes();
	}

	/**
	 * reset all data in the database to match the hard-coded defaults
	 */
	public static void resetData()
	{
		try
		{
			String DB_URL = "jdbc:derby:FHAlpha;create=true";
			Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
			//Get a connection
			connection = DriverManager.getConnection(DB_URL);
		} catch (Exception except)
		{
			except.printStackTrace();
			//remove this piece
			println("error here");
		}
		try
		{
			stmt = connection.createStatement();
			// Drop the UnpaidOrder table.
			stmt.execute("DROP TABLE Residesin");
			System.out.println("Residesin table dropped.");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		}

		try
		{
			stmt = connection.createStatement();
			// Drop the UnpaidOrder table.
			stmt.execute("DROP TABLE Services");
			System.out.println("Services table dropped.");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		try
		{
			stmt = connection.createStatement();
			// Drop the UnpaidOrder table.
			stmt.execute("DROP TABLE Office");
			System.out.println("Office table dropped.");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		}

		try
		{
			stmt = connection.createStatement();
			// Drop the UnpaidOrder table.
			stmt.execute("DROP TABLE Provider");
			System.out.println("Provider table dropped.");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		}

		try
		{
			stmt = connection.createStatement();
			// Drop the UnpaidOrder table.
			stmt.execute("DROP TABLE Neighbor");
			System.out.println("Neighbor table dropped.");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		try
		{
			stmt = connection.createStatement();
			// Drop the UnpaidOrder table.
			stmt.execute("DROP TABLE Node");
			System.out.println("Node table dropped.");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		}

		try
		{
			stmt = connection.createStatement();
			// Drop the UnpaidOrder table.
			stmt.execute("DROP TABLE Floor");
			System.out.println("Floor table dropped.");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		}



		//initialize tables
		initializeProviderTable();
		initializeFloorTable();
		insertFloor(3, "defaultFloor", 3); //insert default floor for minimal app
		initializeNodeTable();
		initializeOfficeTable();
		initializeNeighborTable();
		initializeServicesTable();
		initializeResidesInTable();

		//Creates hard-coded node values for 3rd floor rooms
		insertNode(1, "Atrium Café", "Service",1138.0, 538.0, 3);
		insertNode(2, "Chapel and Chaplaincy Services", "Service",1012.0, 1148.0, 3);
		insertNode(3, "Gift Shop", "Service",1098.0, 791.0, 3);
		insertNode(4, "Huvos Auditorium", "Service",1087.0, 619.0, 3);
		insertNode(5, "Obstetrics and Gynecology Associates", "Practice",1218.0, 225.0, 3);
		insertNode(6, "Roslindale Pediatric Associates" , "Practice",1013.0, 316.0, 3);
		insertNode(7, "Suburban Eye Specialists" , "Practice",1169.0, 202.0, 3);
		insertNode(8, "Volunteer Services", "Service",1222.0, 1148.0, 3);
		insertNode(9, "Kiosk", "Service",1104.0, 1113.0, 3);
		insertNode(10, "Shutte Pickup", "Service",1117.0, 1218.0, 3);
		insertNode(11, "Hillside Lobby", "Service",1116.0, 1148.0, 3);
		insertNode(12, "Eye Care Specialists" , "Practice",1169.0, 202.0, 3);

		//Creates hard-coded node values for the hallway nodes on 3rd floor
		insertNode(13, "F3Hallway1", "Hallway", 1025.0, 226.0, 3);
		insertNode(14, "F3Hallway2", "Hallway", 1117.0, 208.0, 3);
		insertNode(15, "F3Hallway3", "Hallway", 1117.0, 383.0, 3);
		insertNode(16, "F3Hallway4", "Hallway", 1078.0, 387.0, 3);
		insertNode(17, "F3Hallway5", "Hallway", 1080.0, 542.0, 3);
		insertNode(18, "F3Hallway6", "Hallway", 1107.0, 600.0, 3);
		insertNode(19, "F3Hallway7", "Hallway", 1116.0, 791.0, 3);
		insertNode(20, "F3Hallway8", "Hallway", 1108.0, 1032.0, 3);
		insertNode(21, "F3Hallway9", "Hallway", 1161.0, 1148.0, 3);

		insertServices("Atrium Café", "Service");
		insertServices("Chapel and Chaplaincy Services", "Service");
		insertServices("Gift Shop", "Service");
		insertServices("Huvos Auditorium", "Service");
		insertServices("Obstetrics and Gynecology Associates", "Practice");
		insertServices("Roslindale Pediatric Associates", "Practice");
		insertServices("Suburban Eye Specialists", "Practice");
		insertServices("Volunteer Services", "Service");
		insertServices("Kiosk", "Service");
		insertServices("Shutte Pickup", "Service");
		insertServices("Hillside Lobby", "Service");
		insertServices("Eye Care Specialists", "Practice");

		//Creates hard-coded values for all floors (Faulkner and Belkin)
		insertFloor(1, "Faulkner", 1);
		insertFloor(2, "Faulkner", 2);
		insertFloor(3, "Faulkner", 3);
		insertFloor(4, "Faulkner", 4);
		insertFloor(5, "Faulkner", 5);
		insertFloor(6, "Faulkner", 6);
		insertFloor(7, "Faulkner", 7);
		insertFloor(8, "Belkin", 1);
		insertFloor(9, "Belkin", 2);
		insertFloor(10, "Belkin", 3);
		insertFloor(11, "Belkin", 4);

		//creates hard-coded values for all providers
		insertProvider(1, "Alqueza", " Arnold", "");
		insertProvider(2, "Altschul", " Nomee", "");
		insertProvider(3, "Andromalos", " Laura", "MD");
		insertProvider(4, "Angell", " Trevor", "PhD");
		insertProvider(5, "Ariagno", " Meghan", "LERP");
//		insertProvider(6, "Ash", " Samuel");
//		insertProvider(7, "Bachman", " William");
//		insertProvider(8, "Balash", " Eva");
//		insertProvider(9, "Barbie", " Thanh");
//		insertProvider(10, "Barr", " Joseph Jr.");
//		insertProvider(11, "Batool-Anwar", " Salma");
//		insertProvider(12, "Belkin", " Michael");
//		insertProvider(13, "Berman", " Stephanie");
//		insertProvider(14, "Bernstein", " Carolyn");
//		insertProvider(15, "Bhasin", " Shalender");
//		insertProvider(16, "Bhattacharyya", " Shamik");
//		insertProvider(17, "Blazar", " Phil");
//		insertProvider(18, "Bluman", " Eric");
//		insertProvider(19, "Boatwright", " Giuseppina");
//		insertProvider(20, "Bonaca", " Marc");
//		insertProvider(21, "Bono", " Christopher");
//		insertProvider(22, "Brick", " Gregory");
//		insertProvider(23, "Budhiraja", " Rohit");
//		insertProvider(24, "Burch", " Rebecca");
//		insertProvider(25, "Butler", " Matthew");
//		insertProvider(26, "Byrne", " Jennifer");
//		insertProvider(27, "Cahan", " David");
//		insertProvider(28, "Caplan", " Laura");
//		insertProvider(29, "Cardet", " Juan Carlos");
//		insertProvider(30, "Cardin", " Kristin");
//		insertProvider(31, "Carleen", " Mary Anne");
//		insertProvider(32, "Carty", " Matthew");
//		insertProvider(33, "Caterson", " Stephanie");
//		insertProvider(34, "Chahal", " Katie");
//		insertProvider(35, "Chan", " Walter");
//		insertProvider(36, "Chiodo", " Christopher");
//		insertProvider(37, "Chun", " Yoon Sun");
//		insertProvider(38, "Clark", " Roger");
//		insertProvider(39, "Cochrane", " Thomas");
//		insertProvider(40, "Conant", " Alene");
//		insertProvider(41, "Connell", " Nathan");
//		insertProvider(42, "Copello", " Maria");
//		insertProvider(43, "Corrales", " Carleton Eduardo");
//		insertProvider(44, "Cotter", " Lindsay");
//		insertProvider(45, "Cua", " Christopher");
//		insertProvider(46, "DAmbrosio", " Carolyn");
//		insertProvider(47, "Dave", " Jatin");
//		insertProvider(48, "Davidson", " Paul");
//		insertProvider(49, "Dawson", " Courtney");
//		insertProvider(50, "Divito", " Sherrie");
//		insertProvider(51, "Doherty", " Meghan");
//		insertProvider(52, "Dominici", " Laura");
//		insertProvider(53, "Dowd", " Erin");
//		insertProvider(54, "Drew", " Michael");
//		insertProvider(55, "Drewniak", " Stephen");
//		insertProvider(56, "Duggan", " Margaret");
//		insertProvider(57, "Dyer", " George");
//		insertProvider(58, "Earp", " Brandon");
//		insertProvider(59, "Eatman", " Arlan");
//		insertProvider(60, "Epstein", " Lawrence");
//		insertProvider(61, "Ermann", " Joerg");
//		insertProvider(62, "Fanta", " Christopher");
//		insertProvider(63, "Fitz", " Wolfgang");
//		insertProvider(64, "Frangieh", " George");
//		insertProvider(65, "Frangos", " Jason");
//		insertProvider(66, "Friedman", " Pamela");
//		insertProvider(67, "Fromson", " John");
//		insertProvider(68, "Goldman", " Jill");
//		insertProvider(69, "Greenberg", " James Adam");
//		insertProvider(70, "Groden", " Joseph");
//		insertProvider(71, "Groff", " Michael");
//		insertProvider(72, "Grossi", " Lisa");
//		insertProvider(73, "Haimovici", " Florina");
//		insertProvider(74, "Hajj", " Micheline");
//		insertProvider(75, "Halperin", " Florencia");
//		insertProvider(76, "Halvorson", " Eric");
//		insertProvider(77, "Harris", " Mitchel");
//		insertProvider(78, "Hartigan", " Joseph");
//		insertProvider(79, "Hartman", " Katy");
//		insertProvider(80, "Healey", " Michael");
//		insertProvider(81, "Healy", " Barbara");
//		insertProvider(82, "Hentschel", " Dirk");
//		insertProvider(83, "Hergrueter", " Charles");
//		insertProvider(84, "Higgins", " Laurence");
//		insertProvider(85, "Hinton", " Nadia");
//		insertProvider(86, "Homenko", " Daria");
//		insertProvider(87, "Hoover", " Paul");
//		insertProvider(88, "Horowitz", " Sandra");
//		insertProvider(89, "Howard", " Neal Anthony");
//		insertProvider(90, "Hsu", " Joyce");
//		insertProvider(91, "Humbert", " Timberly");
//		insertProvider(92, "Ingram", " Abbie");
//		insertProvider(93, "Innis", " William");
//		insertProvider(94, "Irani", " Jennifer");
//		insertProvider(95, "Isaac", " Zacharia");
//		insertProvider(96, "Isom", " Kellene");
//		insertProvider(97, "Issa", " Mohammed");
//		insertProvider(98, "Javaheri", " Sogol");
//		insertProvider(99, "Jeselsohn", " Rinath");
//		insertProvider(100, "Johnsen", " Jami");
//		insertProvider(101, "Joyce", " Eileen");
//		insertProvider(102, "Kathrins", " Martin");
//		insertProvider(103, "Keller", " Beth");
//		insertProvider(104, "Keller", " Elisabeth");
//		insertProvider(105, "Kenney", " Pardon");
//		insertProvider(106, "Kessler", " Joshua");
//		insertProvider(107, "Khaodhiar", " Lalita");
//		insertProvider(108, "King", " Tari");
//		insertProvider(109, "Kleifield", " Allison");
//		insertProvider(110, "Kornack", " Fulton");
//		insertProvider(111, "Kramer", " Justine");
//		insertProvider(112, "Lafleur", " Emily");
//		insertProvider(113, "Lahair", " Tracy");
//		insertProvider(114, "Lahive", " Karen");
//		insertProvider(115, "Lai", " Leonard");
//		insertProvider(116, "Laskowski", " Karl");
//		insertProvider(117, "Lauretti", " Linda");
//		insertProvider(118, "Leone", " Amanda");
//		insertProvider(119, "Lilienfeld", " Armin");
//		insertProvider(120, "Lilly", " Leonard Stuart");
//		insertProvider(121, "Lo", " Amy");
//		insertProvider(122, "Loder", " Elizabeth");
//		insertProvider(123, "Lu", " Yi");
//		insertProvider(124, "Malone", " Linda");
//		insertProvider(125, "Malone", " Michael");
//		insertProvider(126, "Mariano", " Timothy");
//		insertProvider(127, "Mason", " William");
//		insertProvider(128, "Mathew", " Paul");
//		insertProvider(129, "Matloff", " Daniel");
//		insertProvider(130, "Matthews", " Robert");
//		insertProvider(131, "Matwin", " Sonia");
//		insertProvider(132, "Matzkin", " Elizabeth");
//		insertProvider(133, "McCarthy", " Rita");
//		insertProvider(134, "McDonald", " Michael");
//		insertProvider(135, "McDonnell", " Marie");
//		insertProvider(136, "McGowan", " Katherine");
//		insertProvider(137, "McKenna", " Robert");
//		insertProvider(138, "McKitrick", " Charles");
//		insertProvider(139, "McMahon", " Gearoid");
//		insertProvider(140, "McNabb-Balter", " Julia");
//		insertProvider(141, "Melnitchouk", " Neyla");
//		insertProvider(142, "Miatto", " Orietta");
//		insertProvider(143, "Micley", " Bruce");
//		insertProvider(144, "Miner", " Julie");
//		insertProvider(145, "Monaghan", " Colleen");
//		insertProvider(146, "Morrison", " Beverly");
//		insertProvider(147, "Morrison-Ma", " Samantha");
//		insertProvider(148, "Mullally", " William");
//		insertProvider(149, "Mutinga", " Muthoka");
//		insertProvider(150, "Nadarajah", " Sarah");
//		insertProvider(151, "Nakhlis", " Faina");
//		insertProvider(152, "Nehs", " Matthew");
//		insertProvider(153, "Nelson", " Ehren");
//		insertProvider(154, "Novak", " Peter");
//		insertProvider(155, "Nuspl", " Kristen");
//		insertProvider(156, "OConnor", " Elizabeth");
//		insertProvider(157, "OHare", " Kitty");
//		insertProvider(158, "OLeary", " Michael");
//		insertProvider(159, "Oliveira", " Nancy");
//		insertProvider(160, "Oliver", " Lynn");
//		insertProvider(161, "Omobomi", " Olabimpe");
//		insertProvider(162, "Owens", " Lisa Michelle");
//		insertProvider(163, "Palermo", " Nadine");
//		insertProvider(164, "Paperno", " Halie");
//		insertProvider(165, "Pariser", " Kenneth");
//		insertProvider(166, "Parker", " Leroy");
//		insertProvider(167, "Parnes", " Aric");
//		insertProvider(168, "Patten", " James");
//		insertProvider(169, "Pavlova", " Milena");
//		insertProvider(170, "Perry", " David");
//		insertProvider(171, "Pilgrim", " David");
//		insertProvider(172, "Pingeton", " Mallory");
//		insertProvider(173, "Preneta", " Ewa");
//		insertProvider(174, "Prince", " Anthony");
//		insertProvider(175, "Quan", " Stuart");
//		insertProvider(176, "Ramirez", " Alberto");
//		insertProvider(177, "Rangel", " Erika");
//		insertProvider(178, "Reil", " Erin");
//		insertProvider(179, "Rizzoli", " Paul");
//		insertProvider(180, "Robinson", " Malcolm");
//		insertProvider(181, "Roditi", " Rachel");
//		insertProvider(182, "Rodriguez", " Claudia");
//		insertProvider(183, "Romano", " Keith");
//		insertProvider(184, "Ruff", " Christian");
//		insertProvider(185, "Ruiz", " Emily");
//		insertProvider(186, "Saldana", " Fidencio");
//		insertProvider(187, "Saluti", " Andrew");
//		insertProvider(188, "Samadi", " Farrah");
//		insertProvider(189, "Samara", " Mariah");
//		insertProvider(190, "Sampson", " Christian");
//		insertProvider(191, "Savage", " Robert");
//		insertProvider(192, "Scheff", " David");
//		insertProvider(193, "Schissel", " Scott");
//		insertProvider(194, "Schmults", " Chrysalyne");
//		insertProvider(195, "Schoenfeld", " Andrew");
//		insertProvider(196, "Schoenfeld", " Paul");
//		insertProvider(197, "Schueler", " Leila");
//		insertProvider(198, "Shah", " Amil");
//		insertProvider(199, "Sharma", " Niraj");
//		insertProvider(200, "Sheth", " Samira");
//		insertProvider(201, "Sheu", " Eric");
//		insertProvider(202, "Shoji", " Brent");
//		insertProvider(203, "Smith", " Benjamin");
//		insertProvider(204, "Smith", " Colleen");
//		insertProvider(205, "Smith", " Jeremy");
//		insertProvider(206, "Smith", " Shannon");
//		insertProvider(207, "Spector", " David");
//		insertProvider(208, "Stacks", " Robert");
//		insertProvider(209, "Steele", " Graeme");
//		insertProvider(210, "Stephens", " Kelly");
//		insertProvider(211, "Stevens", " Erin");
//		insertProvider(212, "Stewart", " Carl");
//		insertProvider(213, "Stone", " Rebecca");
//		insertProvider(214, "Sweeney", " Michael");
//		insertProvider(215, "Tarpy", " Robert");
//		insertProvider(216, "Tavakkoli", " Ali");
//		insertProvider(217, "Taylor", " Cristin");
//		insertProvider(218, "Tenforde", " Adam");
//		insertProvider(219, "Todd", " Derrick");
//		insertProvider(220, "Trumble", " Julia");
//		insertProvider(221, "Tucker", " Kevin");
//		insertProvider(222, "Tunick", " Mitchell");
//		insertProvider(223, "Vardeh", " Daniel");
//		insertProvider(224, "Vernon", " Ashley");
//		insertProvider(225, "Vigneau", " Shari");
//		insertProvider(226, "Viola", " Julianne");
//		insertProvider(227, "Voiculescu", " Adina");
//		insertProvider(228, "Wagle", " Neil");
//		insertProvider(229, "Waldman", " Abigail");
//		insertProvider(230, "Walsh Samp", " Kathy");
//		insertProvider(231, "Warth", " James");
//		insertProvider(232, "Warth", " Maria");
//		insertProvider(233, "Webber", " Anthony");
//		insertProvider(234, "Weisholtz", " Daniel");
//		insertProvider(235, "Welker", " Roy");
//		insertProvider(236, "Wellman", " David");
//		insertProvider(237, "White", " David");
//		insertProvider(238, "Whitlock", " Kaitlyn");
//		insertProvider(239, "Whitman", " Gregory");
//		insertProvider(240, "Wickner", " Paige");
//		insertProvider(241, "Yong", " Jason");
//		insertProvider(242, "Yudkoff", " Benjamin");
//		insertProvider(243, "Yung", " Rachel");
//		insertProvider(244, "Zampini", " Jay");
//
//		//creates hard-coded values for all offices
//		insertOffice(26, 6);
//		insertOffice(72, 6);
//		insertOffice(104, 6);
//		insertOffice(124, 6);
//		insertOffice(146, 6);
//		insertOffice(156, 6);
//		insertOffice(187, 6);
//		insertOffice(192, 6);
//		insertOffice(208, 6);
//		insertOffice(222, 6);
//		insertOffice(226, 6);
//		insertOffice(64, 7);
//		insertOffice(143, 7);
//		insertOffice(168, 7);
//		insertOffice(69, 5);
//		insertOffice(144, 5);
//		insertOffice(150, 5);
//		insertOffice(197, 5);
//		insertOffice(206, 5);
		insertOffice(3, 5);

		insertOffice(3, 3);

		insertOffice(3, 2);
		insertOffice(2, 3);

		//inserts the hard-coded values for the ResidesIn table
		insertResidesIn("Roslindale Pediatric Associates" , 6);
		insertResidesIn("Eye Care Specialists" , 7);
		insertResidesIn("Suburban Eye Specialists" , 7);
		insertResidesIn("Obstetrics and Gynecology Associates", 5);
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
					"LastName VARCHAR(20), " +
					"Title VARCHAR(20) " +
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
					"NodeName VARCHAR(50), " +
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

	/**
	 * generate a new provider for the directory editing tool
	 * @param fname first name of provider
	 * @param lname last name of provider
	 * @param title title of provider
	 * @return a new provider with the greatest id value
	 */
	public static Provider generateNewProvider(String fname, String lname, String title)
	{
		try
		{
			stmt = connection.createStatement();
			ResultSet results = stmt.executeQuery("SELECT * FROM PROVIDER " +
					"ORDER BY ProviderID");

			int newID = -1;
			while (results.next())
			{
				newID = results.getInt(1);
			}
			newID++;
			String FirstName = fname;
			String LastName = lname;
			String Title = title;

			Provider newProvider = new Provider(newID, FirstName, LastName, Title);
			results.close();
			stmt.close();

			return newProvider;
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
			String title = "";

			if (results.next())
			{
				fname = results.getString(2);
				lname = results.getString(3);
				title = results.getString(4);
			} else
			{
				return null;
			}
			results.close();
			stmt.close();
			Provider p = new Provider(id, fname, lname, title);
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

	public static void insertProvider(Provider p)
	{
		int i = p.getID();
		String fn = p.getfName();
		String ln = p.getlName();
		String tit = p.getTitle();
		insertProvider(i, fn, ln, tit);
	}

	/**
	 * Insert new provider into table
	 */
	public static void insertProvider(int provID, String fname, String lname, String title)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.execute("insert into " + providerTable + " values (" + provID + ", '" + fname + "', '" +
					lname + "', '" + title + "')");
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

	public static void modifyProviderTable(Provider p){
		int i = p.getID();
		String fn = p.getfName();
		String ln = p.getlName();
		String tit = p.getTitle();
		modifyProviderTable(i, fn, ln, tit);
	}

	/**
	 * Modify a provider's entry in the table
	 *
	 * @// TODO: Probably should break this down to modify a single field at a time
	 * @// TODO: Fix return type?
	 */
	public static void modifyProviderTable(int ID, String fname, String lname, String title)
	{
		try
		{
			stmt = connection.createStatement();
			stmt.executeUpdate("UPDATE Provider " +
					"SET ProviderID = " + ID + ", " +
					"FirstName = '" + fname + "', " +
					"LastName = '" + lname + "', " +
					"Title = '" + title + "' " +
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

	//prints the string of the inputted table, for hard-coded directory checking
	public static void printResults(String tableName) {
		try
		{
			stmt = connection.createStatement();
			ResultSet results = stmt.executeQuery("SELECT * FROM " + tableName);
			while(results.next()) {
				String acronym = results.getString(1);
				String titleName = results.getString(2);
				System.out.println(acronym + "\t\t " + titleName);
			}
			results.close();
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			if(!sqlExcept.getSQLState().equals("23505")){
				sqlExcept.printStackTrace();
			} else {

				System.out.println("ProviderID already exists");
			}
		}
	}

	//initializes the ResidesIn DB table
	public static void initializeResidesInTable(){
		try
		{
			stmt = connection.createStatement();
			//TODO: delete following line after hard coding is done
			stmt.execute("CREATE TABLE ResidesIn(" +
					"ServiceName VARCHAR(50) NOT NULL PRIMARY KEY REFERENCES Services (ServiceName), " +
					"NodeID INT REFERENCES Node (NodeID)" +
					")");
			System.out.println("ResidesIn table initialized");
			stmt.close();
		}
		catch (SQLException sqlExcept){

			//ToDO: Check if the below value is correct
			if (!sqlExcept.getSQLState().equals("X0Y32"))
			{
				sqlExcept.printStackTrace();
			} else {
				System.out.println("ResidesIn table already exists");
			}
		}
	}


	//initializes the Services DB table
	public static void initializeServicesTable(){
		try
		{
			stmt = connection.createStatement();
			//TODO: delete following line, after hardcoding is done
			stmt.execute("CREATE TABLE Services(" +
					"ServiceName VARCHAR(50) NOT NULL PRIMARY KEY, " +
					"ServiceType VARCHAR(20)" +
					")");
			System.out.println("Services table initialized");
			stmt.close();
		}
		catch (SQLException sqlExcept){

			//ToDO: Check if the below value is correct
			if (!sqlExcept.getSQLState().equals("X0Y32"))
			{
				sqlExcept.printStackTrace();
			} else  {
				System.out.println("Services table already exists");
			}
		}
	}

	//inserts into the ResidesIn Table
	public static void insertResidesIn (String serviceName, int nodeID) {
		try
		{
			stmt = connection.createStatement();
			stmt.execute("insert into " + residesInTable + " values ('" + serviceName + "', " + nodeID + ")");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			if(!sqlExcept.getSQLState().equals("23505")){
				sqlExcept.printStackTrace();
			} else {
				//TODO: handle already exist, perhaps update instead
				System.out.println("ResidesIn already exists");
			}
		}
	}

	//inserts into the Services Table
	public static void insertServices (String serviceName, String serviceType) {
		try
		{
			stmt = connection.createStatement();
			stmt.execute("insert into " + servicesTable + " values ('" + serviceName + "', '" + serviceType + "')");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			if(!sqlExcept.getSQLState().equals("23505")){
				sqlExcept.printStackTrace();
			} else {
				//TODO: handle already exist, perhaps update instead
				System.out.println("Services already exists");
			}
		}
	}

	//prints the string of the inputted table, for hard-coded directory checking
	public static void printOffice(String name) {
		try
		{
			stmt = connection.createStatement();
			ResultSet results = stmt.executeQuery("SELECT * FROM " +name );
			while(results.next()) {
				int acronym = results.getInt(1);
				int titleName = results.getInt(2);
				System.out.println(acronym + "\t\t " + titleName);
			}
			results.close();
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			if(!sqlExcept.getSQLState().equals("23505")){
				sqlExcept.printStackTrace();
			} else {

				System.out.println("ProviderID already exists");
			}
		}
	}

	public static void printResidesIn(String name) {
		try
		{
			stmt = connection.createStatement();
			ResultSet results = stmt.executeQuery("SELECT * FROM " +name );
			while(results.next()) {
				String acronym = results.getString(1);
				String titleName = results.getString(2);
				System.out.println(acronym + "\t\t " + titleName);
			}
			results.close();
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			if(!sqlExcept.getSQLState().equals("23505")){
				sqlExcept.printStackTrace();
			} else {

				System.out.println("ProviderID already exists");
			}
		}
	}

	//Added in order to fix VARCHAR value for the Node table for value NodeName
	public static void alterNode() {
		try
		{
			stmt = connection.createStatement();
			stmt.execute("ALTER TABLE Node " +
					"ALTER COLUMN NodeName SET DATA TYPE VARCHAR(50)");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			if(!sqlExcept.getSQLState().equals("23505")){
				sqlExcept.printStackTrace();
			} else {

				System.out.println("Issue with Node Table Alter");
			}
		}
	}

	public static void updateRefInt() {
		try
		{
			stmt = connection.createStatement();
			stmt.execute("ALTER TABLE ResidesIn ADD CONSTRAINT NODE_REF " +
					"FOREIGN KEY(NodeID) REFERENCES Node (NodeID)");

			stmt.execute("ALTER TABLE ResidesIn ADD CONSTRAINT SERV_REF " +
					"FOREIGN KEY(ServiceName) REFERENCES Services (ServiceName)");

			stmt.execute("ALTER TABLE Node ADD CONSTRAINT NODE_FLOOR_REF " +
					"FOREIGN KEY(FloorID) REFERENCES Floor (FloorID)");

			stmt.execute("ALTER TABLE Office ADD CONSTRAINT PROVIDER_REF " +
					"FOREIGN KEY(ProviderID) REFERENCES Provider (ProviderID)");

			stmt.execute("ALTER TABLE Office ADD CONSTRAINT PROVIDER_REF " +
					"FOREIGN KEY(ProviderID) REFERENCES Provider (ProviderID)");

			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			if(!sqlExcept.getSQLState().equals("23505")){
				sqlExcept.printStackTrace();
			} else {

				System.out.println("Issue with Node Table Alter");
			}
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
