package pathfinding;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class BreadthFirstTest {

    @Test
    public void findPath() {
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
        Graph graph = new BreadthFirstGraph();
        graph.findPath(testNodes[0], testNodes[5]);
    }
}
