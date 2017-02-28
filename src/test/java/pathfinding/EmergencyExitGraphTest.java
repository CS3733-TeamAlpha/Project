package pathfinding;

import data.Node;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EmergencyExitGraphTest
{
	@Test
	public void EmergencyExitGraphTest()
	{
		Node[] nodes = new Node[10];

		for (int i = 0; i < nodes.length; i++)
			nodes[i] = new Node();

		for (int i = 0; i < nodes.length; i++)
		{
			if (i > 0)
				nodes[i].addNeighbor(nodes[i - 1]);
			if (i < nodes.length - 1)
				nodes[i].addNeighbor(nodes[i + 1]);
		}
		nodes[7].setType(6);
		nodes[9].setType(6);

		EmergencyExitGraph graph = new EmergencyExitGraph();

		assertNotNull(graph.findPath(nodes[0], null, true));
		assertEquals(nodes[7], graph.findPath(nodes[0], null, true).get(0));

	}

}

