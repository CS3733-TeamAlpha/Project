package pathfinding;

import data.Node;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EmergencyExitFinderTest
{
	@Test
	public void findExitTest()
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

		EmergencyExitFinder exitFinder = new EmergencyExitFinder();

		assertNotNull(exitFinder.findExit(nodes[0]));
		assertEquals(nodes[7], exitFinder.findExit(nodes[0]));
	}
}

