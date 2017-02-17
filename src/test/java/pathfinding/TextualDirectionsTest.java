package pathfinding;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TextualDirectionsTest {

    @Test
    public void findAngle() {
        String one = "Sharp right";
        String two = "Turn right";
        String three = "Bear right";
        String four = "Bear left";
        String five = "Turn left";
        String six = "Sharp left";
        ConcreteNode pivot = new ConcreteNode(); pivot.setX(10); pivot.setY(10); //nonstandard line breaks!
        ConcreteNode start = new ConcreteNode(); start.setX(10); start.setY(0);
        ConcreteNode oneN = new ConcreteNode(); oneN.setX(11); oneN.setY(0);
        ConcreteNode twoN = new ConcreteNode(); twoN.setX(20); twoN.setY(10);
        ConcreteNode threeN = new ConcreteNode(); threeN.setX(11); threeN.setY(20);
        ConcreteNode fourN = new ConcreteNode(); fourN.setX(9); fourN.setY(20);
        ConcreteNode fiveN = new ConcreteNode(); fiveN.setX(0); fiveN.setY(10);
        ConcreteNode sixN = new ConcreteNode(); sixN.setX(9); sixN.setY(0);
        String testOne = start.angle(pivot, oneN);
        String testTwo = start.angle(pivot, twoN);
        String testThree = start.angle(pivot, threeN);
        String testFour = start.angle(pivot, fourN);
        String testFive = start.angle(pivot, fiveN);
        String testSix = start.angle(pivot, sixN);
        assert(one.equals(testOne));
        assert(two.equals(testTwo));
        assert(three.equals(testThree));
        assert(four.equals(testFour));
        assert(five.equals(testFive));
        assert(six.equals(testSix));
    }

    @Test
	public void textDirections() {
		ConcreteNode[] testNodes = new ConcreteNode[6];
		for (int i = 0; i < testNodes.length; i++)
			testNodes[i] = new ConcreteNode();
		for (int i = 0; i < testNodes.length; i++) {
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
		assertEquals("Walk 50 feet", directions.get(1));
		assertEquals("Walk 100 feet", directions.get(7));
		assertEquals("Turn left, then", directions.get(6));
		assertEquals("You have reached your destination!", directions.get(10));
	}
}
