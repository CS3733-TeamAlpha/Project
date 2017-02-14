package pathfinding;

        import org.junit.Test;

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
}
