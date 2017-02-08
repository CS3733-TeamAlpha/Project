package pathfinding;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class ConcreteGraphTest
{

	@Test
	public void findPath() throws Exception
	{
		ConcreteNode[] straightNodes = new ConcreteNode[100];
		ConcreteNode[][] gridNodes = new ConcreteNode[100][100];

		//Ugh... init
		for (int i = 0; i < straightNodes.length; i++)
			straightNodes[i] = new ConcreteNode();

		for (int i = 0; i < 100; i++)
			for (int j = 0; j < 100; j++)
				gridNodes[i][j] = new ConcreteNode();

		//Create a simple straightshot array of nodes to idiot-test the pathfinding
		for (int i = 0; i < straightNodes.length; i++)
		{
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

		Graph graph = new ConcreteGraph();

		//Straight shot pathing test
		assertNotNull(graph.findPath(straightNodes[0], straightNodes[straightNodes.length - 1]));
		assertEquals(straightNodes.length, graph.findPath(straightNodes[0], straightNodes[straightNodes.length - 1]).size());

		//Grid pathing test
		assertNotNull(graph.findPath(gridNodes[0][0], gridNodes[99][99]));
		assertEquals(100, graph.findPath(gridNodes[0][0], gridNodes[0][99]).size());
		assertNotNull(graph.findPath(gridNodes[0][0], gridNodes[99][99]));
		assertEquals(199, graph.findPath(gridNodes[0][0], gridNodes[99][99]).size());

		//***EDGE CASES***
		assertNull(graph.findPath(null, null));
		assertNull(graph.findPath(gridNodes[0][0], straightNodes[0]));    //No path
		Node emptyNode = new ConcreteNode();
		assertNull(graph.findPath(emptyNode, gridNodes[0][0]));
		assertNull(graph.findPath(gridNodes[0][0], emptyNode));

		//Ordering tests
		ArrayList<Node> orderedSolution = graph.findPath(straightNodes[0], straightNodes[straightNodes.length - 1]);
	}
}