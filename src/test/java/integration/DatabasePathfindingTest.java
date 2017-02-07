package integration;

import data.*;
import pathfinding.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;

/**
 * THIS IS AWFUL!
 * THERE HAS TO BE A BETTER WAY OF DOING INTEGRATION TESTS
 */
public class DatabasePathfindingTest
{
	Graph graph;

	@Before
	public void setUp()
	{
		graph = new ConcreteGraph();
	}

	@Test
	public void testLinkPersistance(){
		//Test to make sure that connections between two nodes are maintained after being stored in the database
		//TODO: Either these idiot checks are redundant or they need to be moved into a more suitable function
		ConcreteNode node1 = new ConcreteNode(0, new ArrayList<String>(), 1, 7, null);
		ConcreteNode node2 = new ConcreteNode(1, new ArrayList<String>(), 0, 1, null);
		node1.getData().add("Hello"); //TODO: Remove this once issue #7 is fixed
		node1.getData().add("World");
		node2.getData().add("dlroW");
		node2.getData().add("olleH");
		assertNull(graph.findPath(node1, node2)); //Idiot check... you never know when programs drop 100 IQ on the spot

		node1.addNeighbor(node2);
		node2.addNeighbor(node1);
		assertNotNull(graph.findPath(node1, node2)); //Idiot check round 2

		//Add these nodes to the database
		DatabaseController.insertNode(node1);
		DatabaseController.insertNode(node2);

		//Now get the nodes back out and check their data to make sure that it is IDENTICAL
		Node dNode1 = DatabaseController.getNodeByID(0);
		Node dNode2 = DatabaseController.getNodeByID(1);
		assertNotNull(dNode1);
		assertNotNull(dNode2);
		assertEquals(1, dNode1.getX(), 0);
		assertEquals(7, dNode1.getY(), 0);
		assertEquals(0, dNode2.getX(), 0);
		assertEquals(1, dNode2.getY(), 0);
		assertNull(dNode1.getOnFloor());
		assertNull(dNode1.getOnFloor()); //getOnFloor()? that's awkwardly named...
		assertNotNull(dNode1.getData());
		assertNotNull(dNode2.getData());

		//Now verify that these two nodes are actually linked together, the database should've preserved their relationship
		assertTrue(dNode1.getNeighbors().contains(dNode2));
		assertTrue(dNode2.getNeighbors().contains(dNode1));
	}
}
