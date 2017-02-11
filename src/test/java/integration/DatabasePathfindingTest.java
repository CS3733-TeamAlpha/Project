package integration;

import data.*;
import pathfinding.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.image.renderable.ContextualRenderedImageFactory;
import java.util.ArrayList;

/**
 * THIS IS AWFUL!
 * THERE HAS TO BE A BETTER WAY OF DOING INTEGRATION TESTS
 */

/*
public class DatabasePathfindingTest
{
	Graph graph;

	@Before
	public void setUp()
	{
		graph = new ConcreteGraph();
		//DatabaseController.createTestConnection();
	}

	@After
	public void tearDown()
	{
	//	DatabaseController.shutdownTest();
	}

	@Test
	public void testLinkPersistence(){
		//Test to make sure that connections between two nodes are maintained after being stored in the database
		//TODO: Either these idiot checks are redundant or they need to be moved into a more suitable function
		ConcreteNode node1 = new ConcreteNode(100, new ArrayList<String>(), 1, 7, null);
		ConcreteNode node2 = new ConcreteNode(101, new ArrayList<String>(), 0, 1, null);
		assertNull(graph.findPath(node1, node2)); //Idiot check... you never know when programs drop 100 IQ on the spot

		node1.addNeighbor(node2);
		node2.addNeighbor(node1);
		assertNotNull(graph.findPath(node1, node2)); //Idiot check round 2

		//Add these nodes to the database using insertNodeList
		ArrayList<Node> tempList = new ArrayList<Node>();
		tempList.add(node1);
		tempList.add(node2);
	//	DatabaseController.insertNodeList(tempList);
	//	DatabaseController.initializeAllNodes();

		//Now get the nodes back out and check their data to make sure that it is IDENTICAL
	//	Node dNode1 = DatabaseController.getNodeByID(100);
	//	Node dNode2 = DatabaseController.getNodeByID(101);
	//	assertNotNull(dNode1);
	//	assertNotNull(dNode2);
	//	assertEquals(1, dNode1.getX(), 0);
	//	assertEquals(7, dNode1.getY(), 0);
	//	assertEquals(0, dNode2.getX(), 0);
	//	assertEquals(1, dNode2.getY(), 0);
	//	assertNotNull(dNode1.getData());
	//	assertNotNull(dNode2.getData());

		//Now verify that these two nodes are actually linked together, the database should've preserved their relationship
	//	assertTrue(dNode1.getNeighbors().contains(dNode2));
	//	assertTrue(dNode2.getNeighbors().contains(dNode1));
	}

	@Test
	public void databaseTortureTest()
	{
		//Lets have a little fun with the database by giving it 100 nodes to manage
		//Start by initializing and linking 100x100 nodes together
		System.out.println("Beginning graph creation");
		ConcreteNode[][] gridNodes = new ConcreteNode[10][10];
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				gridNodes[i][j] = new ConcreteNode(i * 100 + j + 400, new ArrayList<String>(), i, j, null);

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
		ArrayList<Node> tempList = new ArrayList<Node>();
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				tempList.add(gridNodes[i][j]);
	//	DatabaseController.insertNodeList(tempList);
	//	DatabaseController.initializeAllNodes();
		System.out.println("Finished inserting nodes into database");

		//...and get them all back out again!
		Node[][] dGridNodes = new ConcreteNode[10][10];
		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 10; j++)
			{
	//			dGridNodes[i][j] = DatabaseController.getNodeByID(i * 100 + j + 400); //500 is used as offset for... reasons.
				assertNotNull(dGridNodes[i][j]);
				assertTrue(dGridNodes[i][j].getNeighbors().size() >= 2);
			}
		}
		System.out.println("Finished retrieving nodes from database");
		//Now try and find the same path found above using these nodes extracted from the database
		assertNotNull(graph.findPath(dGridNodes[0][0], dGridNodes[9][9]));
		assertEquals(19, graph.findPath(dGridNodes[0][0], dGridNodes[9][9]).size());
	}
}*/
