package integration;

import data.*;
import pathfinding.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * THIS IS AWFUL!
 * THERE HAS TO BE A BETTER WAY OF DOING INTEGRATION TESTS
 */

public class DatabasePathfindingTest
{
	Graph graph;
	Database database;

	@Before
	public void setUp()
	{
		graph = new ConcreteGraph();
		database = new data.Database("junit_testing_db");
	}

	@After
	public void tearDown()
	{
		database.disconnect();
	}

	@Test
	public void testLinkPersistence(){
		Node node1 = new Node();
		Node node2 = new Node();
		assertNull(graph.findPath(node1, node2)); //Idiot check... you never know when programs drop 100 IQ on the spot
		node1.addNeighbor(node2);
		node2.addNeighbor(node1);
		assertNotNull(graph.findPath(node1, node2)); //Idiot check round 2

		//Add these nodes to the database
		database.insertNode(node1);
		database.insertNode(node2);

		//Flush the node cache - we want to verify that pathable nodes can actually be loaded
		database.disconnect();
		database.connect();

		//Now get the nodes back out...
		Node dNode1 = database.getNodeByUUID(node1.getID());
		Node dNode2 = database.getNodeByUUID(node2.getID());

		//Now verify that these two nodes are actually linked together, the database should've preserved their relationship
		assertTrue(dNode1.getNeighbors().contains(dNode2));
		assertTrue(dNode2.getNeighbors().contains(dNode1));

		//Graph test
		assertNotNull(graph.findPath(dNode1, dNode2));

		//Clean up
		database.deleteNodeByUUID(node1.getID());
		database.deleteNodeByUUID(node2.getID());
	}

	@Test
	public void databaseTortureTest()
	{
		//Lets have a little fun with the database by giving it 100 nodes to manage
		//Start by initializing and linking 100x100 nodes together
		System.out.println("Beginning graph creation");
		Node[][] gridNodes = new Node[10][10];
		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				gridNodes[i][j] = new Node();
				gridNodes[i][j].setX(i);
				gridNodes[i][j].setY(j);
			}
		}

		System.out.println("Finished initializing graph");
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
		System.out.println("Finished constructing graph");

		//Make sure we can find a path between upper left and lower right. If this fails, the below tests will too
		assertNotNull(graph.findPath(gridNodes[0][0], gridNodes[9][9]));
		assertEquals(19, graph.findPath(gridNodes[0][0], gridNodes[9][9]).size());
		System.out.println("Finished base test of graph");

		//Now put them all in the database...
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				database.insertNode(gridNodes[i][j]);
		System.out.println("Finished inserting nodes into database");

		//Flush cache forcing reload from database (who needs reloadCache anyways?)
		database.disconnect();
		database.connect();

		//...and get them all back out again!
		Node[][] dGridNodes = new Node[10][10];
		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				dGridNodes[i][j] = database.getNodeByUUID(gridNodes[i][j].getID());
				assertNotNull(dGridNodes[i][j]);
				assertTrue(dGridNodes[i][j].getNeighbors().size() >= 2);
			}
		}
		System.out.println("Finished retrieving nodes from database");
		//Now try and find the same path found above using these nodes extracted from the database
		assertNotNull(graph.findPath(dGridNodes[0][0], dGridNodes[9][9]));
		assertEquals(19, graph.findPath(dGridNodes[0][0], dGridNodes[9][9]).size());

		//Now just clean up
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				database.deleteNodeByUUID(gridNodes[i][j].getID());
	}
}
