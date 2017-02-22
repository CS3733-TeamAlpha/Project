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
}
