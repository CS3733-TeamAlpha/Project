package data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.*;

public class DatabaseTest
{
	private static final String TEST_DB = "junit_testing_db";

	Database database;

	@Before
	public void setUp() throws Exception
	{
		database = new Database(TEST_DB);
	}

	@After
	public void tearDown() throws Exception
	{
		database.disconnect();

		//Delete the test database, stored in a folder $TEST_DB
		//deleteFolder(TEST_DB);
	}

	/**
	 * Recursively deletes a folder, because java is stupid and `rm -rf $fname` isn't platform independent.
	 * @param fname
	 */
	private void deleteFolder(String fname)
	{
		File folder = new File(fname);
		for (String s : folder.list())
		{
			File curFile = new File(folder.getPath(), s);
			if (curFile.isDirectory())
				deleteFolder(curFile.getPath());
			curFile.delete();
		}
		folder.delete();
	}

	@Test
	public void testConnection()
	{
		assertTrue(database.isConnected());
	}

	@Test
	public void testInsertAndRetrieval()
	{
		Node testNode = new Node("00000000-0000-0000-0000-000000000000",
				"Test Node", "00000000-0000-0000-0000-000000000000", 1, 2, 3, 1701);

		database.insertNode(testNode); //Insert the node...
		database.disconnect();
		database.connect();
		Node retNode = database.getNodeByUUID(testNode.getID());
		assertTrue(testNode.equals(retNode));
		database.deleteNodeByUUID(retNode.getID());
	}

	@Test
	public void testDelete()
	{
		Node testNode = new Node();
		database.insertNode(testNode);
		assertNotNull(database.getNodeByUUID(testNode.getID()));
		database.deleteNodeByUUID(testNode.getID());
		assertEquals(0, database.getAllNodes().size());
		assertNull(database.getNodeByUUID(testNode.getID()));
	}

	@Test
	public void testLinkage()
	{
		//Stolen from pathfinding integration test
		Node[][] gridNodes = new Node[10][10];
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				gridNodes[i][j] = new Node();

		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				if (i > 0)
					gridNodes[i][j].addNeighbor(gridNodes[i-1][j]);
				if (i < 9)
					gridNodes[i][j].addNeighbor(gridNodes[i+1][j]);
				if (j > 0)
					gridNodes[i][j].addNeighbor(gridNodes[i][j-1]);
				if (j < 9)
					gridNodes[i][j].addNeighbor(gridNodes[i][j+1]);
			}
		}

		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				database.insertNode(gridNodes[i][j]);

		database.disconnect();
		database.connect();

		//Now get all those nodes back out again
		Node[][] testNodes = new Node[10][10];
		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				testNodes[i][j] = database.getNodeByUUID(gridNodes[i][j].getID());
				assertNotNull(testNodes[i][j]);
			}
		}

		//Verify the integrity of the node grid just retrieved
		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				if (i > 0)
					assertTrue(gridNodes[i][j].getNeighbors().contains(gridNodes[i-1][j]));
				if (i < 9)
					assertTrue(gridNodes[i][j].getNeighbors().contains(gridNodes[i+1][j]));
				if (j > 0)
					assertTrue(gridNodes[i][j].getNeighbors().contains(gridNodes[i][j-1]));
				if (j < 9)
					assertTrue(gridNodes[i][j].getNeighbors().contains(gridNodes[i][j+1]));
			}
		}

		//And finally clean up, we don't want a ton of spare nodes hanging around in the database
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				database.deleteNodeByUUID(testNodes[i][j].getID());
	}

	@Test
	public void testGetByFloor()
	{
		//Create some nodes on 10 different floors
		Node[] nodes = new Node[50];
		for (int i = 0; i < nodes.length; i++)
		{
			nodes[i] = new Node();
			nodes[i].setFloor(i / 5);
			database.insertNode(nodes[i]);
		}

		//Get nodes by floor now
		for (int i = 0; i < 10; i++)
		{
			ArrayList<Node> ret = database.getNodesByFloor(i);
			assertNotNull(ret);
			assertEquals(5, ret.size());

			//Make sure that the expected nodes were returned
			for (Node node : nodes)
			{
				if (node.getFloor() == i)
					assertTrue(ret.contains(node));
			}
		}

		//And now clean up
		for (Node node : nodes)
			database.deleteNodeByUUID(node.getID());
	}

	@Test
	public void testGetByBuilding()
	{
		//Drop the default building
		database.deleteBuilding("00000000-0000-0000-0000-000000000000");
		assertEquals(0, database.getBuildings().size());

		//Create a new building with random UUID
		database.insertBuilding(UUID.randomUUID().toString(), "Starfleet Headquarters");

		//Verify that the building was actually inserted
		final String uuid = database.getBuildingUUID("Starfleet Headquarters");
		assertEquals(36, uuid.length());
		assertEquals(1, database.getBuildings().size());

		//Verify that we can't get a bad building
		assertTrue(database.getBuildingUUID("Dominion Headquarters").isEmpty());

		//Now add some nodes to Starfleet Headquarters
		Node[] nodes = new Node[5];
		for (Node node : nodes)
		{
			node = new Node();
			node.setFloor(1701);
			node.setBuilding(uuid);
			database.insertNode(node);
		}

		//Try getting those nodes out again and verify situations that do and don't work
		assertEquals(5, database.getNodesInBuildingFloor(uuid, 1701).size()); //Correct uuid, correct floor
		assertEquals(0, database.getNodesInBuildingFloor(uuid, 1702).size()); //Correct uuid, incorrect floor
		assertEquals(0, database.getNodesInBuildingFloor("00", 1701).size()); //Incorrect uuid, correct floor
		assertEquals(0, database.getNodesInBuildingFloor("00", 1702).size()); //Incorrect uuid, incorrect floor

		//And clean up the database by deleting the whole building and re-adding default!
		database.deleteBuilding(uuid);
		assertEquals(0, database.getNodesInBuildingFloor(uuid, 1701).size());
		database.insertBuilding("00000000-0000-0000-0000-000000000000", "outdoors");
	}

	@Test
	public void testUpdateNode()
	{
		//Create a pair of unlinked nodes, put them in the database. Create a 1->2 edge between them, insert the edge,
		//shutdown the database, restart it, and grab the two nodes out again. Verify that they're still connected.
		Node node1 = new Node();
		Node node2 = new Node();
		database.insertNode(node1);
		database.insertNode(node2);
		node1.addNeighbor(node2);

		database.disconnect(); //Clears the node cache
		database.connect();

		Node testNode1 = database.getNodeByUUID(node1.getID());
		Node testNode2 = database.getNodeByUUID(node2.getID());
		assertTrue(testNode1.getNeighbors().contains(testNode2));

		//Clean up
		database.deleteNodeByUUID(testNode1.getID());
		database.deleteNodeByUUID(testNode2.getID());
	}

	@Test
	public void testProviderOperations()
	{
		//Create a test node with a test provider, put it in the database.
		Node node = new Node();
		Provider picard = new Provider("Jean Luc", "Picard", UUID.randomUUID().toString(), "Captain");
		node.addProvider(picard);
		database.insertNode(node);

		//Reload from the database
		database.disconnect();
		database.connect();

		//Verify the presence of a provider
		assertEquals(1, database.getProviders().size());

		//Add a second provider to the node and verify the transaction
		Provider sisko = new Provider("Benjamin", "Sisko", UUID.randomUUID().toString(), "Captain");
		node.addProvider(sisko);
		assertEquals(2, database.getProviders().size());
		assertEquals(2, node.getProviders().size());

		//Delete picard and verify his deletion
		database.deleteProvider(picard);
		assertEquals(1, database.getProviders().size());
		assertEquals(1, node.getProviders().size());
		System.out.printf("Remaining provider " + database.getProviders().get(0).getLastName());

		//Clean up
		database.deleteNodeByUUID(node.getID());
		assertEquals(1, node.getProviders().size());
		assertEquals(0, database.getAllNodes().size());
		assertEquals(1, database.getProviders().size());
		database.deleteProvider(sisko);
		assertEquals(0, node.getProviders().size());
		assertEquals(0, database.getProviders().size());
	}

	@Test
	public void testServices()
	{
		//Create a test node with a service, put it in the database
		Node node = new Node();
		node.addService("tenforward");
		database.insertNode(node);
		assertEquals(1, database.getServices().size());

		node.addService("quarks");
		assertEquals(2, database.getServices().size());

		node.delService("quarks");
		assertEquals(1, database.getServices().size());

		//Verify that two nodes cannot have a service by the same name - nodes and services are 1-1
		//Nodes should still be inserted, however.
		Node node2 = new Node();
		node2.addService("tenforward");
		database.insertNode(node2);
		assertEquals(1, database.getServices().size());
		assertNotNull(database.getNodeByUUID(node2.getID()));

		//Verify location finding
		assertEquals(node, database.getServiceLocation("tenforward"));

		//Clean up and verify that services don't get deleted along with their nodes
		database.deleteNodeByUUID(node.getID());
		assertEquals(1, database.getServices().size());
		database.delService("tenforward");
		assertEquals(0, database.getServices().size());
	}
}