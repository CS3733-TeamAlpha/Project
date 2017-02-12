package data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pathfinding.*;
import sun.awt.AWTIcon32_java_icon16_png;

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
		Node testNode = new ConcreteNode("00000000-0000-0000-0000-000000000000",
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
		ConcreteNode testNode = new ConcreteNode();
		database.insertNode(testNode);
		assertNotNull(database.getNodeByUUID(testNode.getID()));
		database.deleteNodeByUUID(testNode.getID());
		assertNull(database.getNodeByUUID(testNode.getID()));
	}

	@Test
	public void testLinkage()
	{
		//Stolen from pathfinding integration test
		ConcreteNode[][] gridNodes = new ConcreteNode[10][10];
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				gridNodes[i][j] = new ConcreteNode();

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
		Node[][] testNodes = new ConcreteNode[10][10];
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
		ConcreteNode[] nodes = new ConcreteNode[50];
		for (int i = 0; i < nodes.length; i++)
		{
			nodes[i] = new ConcreteNode();
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
		for (ConcreteNode node : nodes)
			database.deleteNodeByUUID(node.getID());
	}

	@Test
	public void testGetByBuilding()
	{
		//Create a new building with random UUID
		database.addBuilding(UUID.randomUUID().toString(), "Starfleet Headquarters");

		//Verify that the building was actually inserted
		final String uuid = database.getBuildingUUID("Starfleet Headquarters");
		assertEquals(36, uuid.length());

		//Verify that we can't get an invalid building
		assertTrue(database.getBuildingUUID("Dominion Headquarters").isEmpty());

		//Now add some nodes to Starfleet Headquarters
		ConcreteNode[] nodes = new ConcreteNode[5];
		for (Node node : nodes)
		{
			node = new ConcreteNode();
			node.setFloor(1701);
			node.setBuilding(uuid);
			database.insertNode(node);
		}

		//Try getting those nodes out again and verify situations that do and don't work
		assertEquals(5, database.getNodesInBuildingFloor(uuid, 1701).size()); //Correct uuid, correct floor
		assertEquals(0, database.getNodesInBuildingFloor(uuid, 1702).size()); //Correct uuid, incorrect floor
		assertEquals(0, database.getNodesInBuildingFloor("00", 1701).size()); //Incorrect uuid, correct floor
		assertEquals(0, database.getNodesInBuildingFloor("00", 1702).size()); //Incorrect uuid, incorrect floor

		//And clean up the database by deleting the whole building!
		database.deleteBuilding(uuid);
		assertEquals(0, database.getNodesInBuildingFloor(uuid, 1701).size());
	}

	@Test
	public void testUpdateNode()
	{
		//Create a pair of unlinked nodes, put them in the database. Create a 1->2 edge between them, insert the edge,
		//shutdown the database, restart it, and grab the two nodes out again. Verify that they're still connected.
		Node node1 = new ConcreteNode();
		Node node2 = new ConcreteNode();
		database.insertNode(node1);
		database.insertNode(node2);
		node1.addNeighbor(node2);

		database.updateNode(node1);
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
		/*
		//Create a test node, put it in the database.
		ConcreteNode node = new ConcreteNode();
		database.insertNode(node);

		//Create a provider, put it in, get it out
		database.addProvider(UUID.randomUUID().toString(), "Picard, Jean-Luc; Captain");
		assertEquals(36, database.getProviderUUID("Picard, Jean-Luc; Captain").length());

		//Get another provider, verify that two are now stored
		database.addProvider(UUID.randomUUID().toString(), "Sisko, Benjamin");
		assertEquals(2, database.getProviderNames().size());

		//Tie a provider to node
		database.addProviderOffice(database.getProviderUUID("Picard, Jean-Luc; Captain"), node.getID());
		*/
	}
}