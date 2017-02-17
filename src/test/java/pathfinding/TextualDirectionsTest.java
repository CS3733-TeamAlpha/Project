package pathfinding;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TextualDirectionsTest
{

	@Test
	public void findAngle()
	{
		ConcreteNode nodes[] = new ConcreteNode[6];
		ConcreteNode pivot = new ConcreteNode();
		pivot.setX(10);
		pivot.setY(10); //nonstandard line breaks!
		ConcreteNode start = new ConcreteNode();
		start.setX(10);
		start.setY(0);
		for (int i = 0; i < 6; i++)
			nodes[i] = new ConcreteNode();

		nodes[0].setX(11);
		nodes[0].setY(0);
		nodes[1].setX(20);
		nodes[1].setY(10);
		nodes[2].setX(11);
		nodes[2].setY(20);
		nodes[3].setX(9);
		nodes[3].setY(20);
		nodes[4].setX(0);
		nodes[4].setY(10);
		nodes[5].setX(9);
		nodes[5].setY(0);

		assertEquals("Sharp left", start.angle(pivot, nodes[0]));
		assertEquals("Turn left", start.angle(pivot, nodes[1]));
		assertEquals("Bear left", start.angle(pivot, nodes[2]));
		assertEquals("Bear right", start.angle(pivot, nodes[3]));
		assertEquals("Turn right", start.angle(pivot, nodes[4]));
		assertEquals("Sharp right", start.angle(pivot, nodes[5]));
	}

	@Test
	public void textDirections()
	{
		ConcreteNode[] testNodes = new ConcreteNode[6];
		for (int i = 0; i < testNodes.length; i++)
			testNodes[i] = new ConcreteNode();
		for (int i = 0; i < testNodes.length; i++)
		{
			if (i > 0)
				testNodes[i].addNeighbor(testNodes[i - 1]);
			if (i < testNodes.length - 1)
				testNodes[i].addNeighbor(testNodes[i + 1]);
		}

		testNodes[0].setX(50);
		testNodes[0].setY(50);
		testNodes[1].setX(60);
		testNodes[1].setY(50);
		testNodes[2].setX(60);
		testNodes[2].setY(40);
		testNodes[3].setX(70);
		testNodes[3].setY(40);
		testNodes[4].setX(70);
		testNodes[4].setY(60);
		testNodes[5].setX(75);
		testNodes[5].setY(60);

		ConcreteGraph test = new ConcreteGraph();
		ArrayList<String> directions = test.textDirect(testNodes[0], testNodes[5], 5);
		for (String s : directions)
			System.out.println(s);
		assertEquals("Walk 50 feet", directions.get(0));
		assertEquals("Walk 100 feet", directions.get(6));
		assertEquals("Turn left, then", directions.get(7));
		assertEquals("You have reached your destination!", directions.get(directions.size() - 1));
	}
}
