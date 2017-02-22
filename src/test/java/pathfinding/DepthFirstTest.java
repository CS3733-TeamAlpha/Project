package pathfinding;

import data.Node;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class DepthFirstTest
{

	@Test
	public void findPath()
	{
		Node[] straightNodes = new Node[100];
		Node[][] gridNodes = new Node[100][100];

		//Ugh... init
		for (int i = 0; i < straightNodes.length; i++)
			straightNodes[i] = new Node();

		for (int i = 0; i < 100; i++)
			for (int j = 0; j < 100; j++)
				gridNodes[i][j] = new Node();

		//Create a simple straightshot array of nodes to idiot-test the pathfinding
		for (int i = 0; i < straightNodes.length; i++)
		{
			straightNodes[i].setX(i);
			if (i > 0)
				straightNodes[i].addNeighbor(straightNodes[i - 1]);
			if (i < straightNodes.length - 1)
				straightNodes[i].addNeighbor(straightNodes[i + 1]);
		}

		//Create a more complicated grid of nodes to check for actual pathfinding ability where many choices exist
		for (int i = 0; i < 100; i++)
		{
			for (int j = 0; j < 100; j++)
			{
				gridNodes[i][j].setX(i);
				gridNodes[i][j].setY(j);
				if (i > 0)
					gridNodes[i][j].addNeighbor(gridNodes[i - 1][j]);
				if (i < 99)
					gridNodes[i][j].addNeighbor(gridNodes[i + 1][j]);
				if (j > 0)
					gridNodes[i][j].addNeighbor(gridNodes[i][j - 1]);
				if (j < 99)
					gridNodes[i][j].addNeighbor(gridNodes[i][j + 1]);
			}
		}

		Graph graph = new DepthFirstGraph();

		//Straight shot pathing test
		ArrayList<Node> path = graph.findPath(straightNodes[0], straightNodes[straightNodes.length - 1]);
		assertNotNull(path);
		assertTrue(path.contains(straightNodes[0]));
		assertTrue(path.contains(straightNodes[straightNodes.length - 1]));
		assertEquals(straightNodes.length, path.size());

		//Grid pathing test
		assertNotNull(graph.findPath(gridNodes[0][0], gridNodes[99][99]));
		assertTrue(199 < graph.findPath(gridNodes[0][0], gridNodes[99][99]).size());

		//***EDGE CASES***
		assertNull(graph.findPath(null, null));
		assertNull(graph.findPath(gridNodes[0][0], straightNodes[0]));    //No path
		Node emptyNode = new Node();
		assertNull(graph.findPath(emptyNode, gridNodes[0][0]));
		assertNull(graph.findPath(gridNodes[0][0], emptyNode));
		assertEquals(1, graph.findPath(straightNodes[0], straightNodes[0]).size());
		assertTrue(straightNodes[0] == graph.findPath(straightNodes[0], straightNodes[0]).get(0));

		//Ordering + path integrity test
		ArrayList<Node> orderedSolution = graph.findPath(straightNodes[0], straightNodes[straightNodes.length - 1]);
		assertNotNull(orderedSolution);
		for (int i = 0; i < 100; i++)
			assertEquals(orderedSolution.get(i), straightNodes[99 - i]);
	}

	@Test
	public void straitShotOptimization()
	{
		//Straight shot optimization test - test to make sure that intermediate floors are skipped
		Node[] nodes = new Node[3];
		for (int i = 0; i < nodes.length; i++)
		{
			nodes[i] = new Node();
			nodes[i].setFloor(i);
		}

		for (int i = 0; i < nodes.length - 1; i++)
		{
			if (i > 0)
				nodes[i].addNeighbor(nodes[i - 1]);
			if (i < nodes.length - 1)
				nodes[i].addNeighbor(nodes[i + 1]);
		}

		Graph graph = new DepthFirstGraph();
		ArrayList<Node> path = graph.findPath(nodes[0], nodes[nodes.length - 1]);
		assertEquals(2, path.size());
	}
}
