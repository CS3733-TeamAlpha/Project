package pathfinding;

import data.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Adam B. White on 2/27/2017.
 */
public class EmergencyExitGraph extends Graph{
    /**
     * Finds path to the nearest Emergency exit.
     * @param start Node to start pathing from.
     * @param end   Ignored
     * @return      Path
     */
    @Override
    public ArrayList<Node> findPath(Node start, Node end) {
        System.out.println("Activating breadth first search");
        ArrayList<Node> result = new ArrayList<Node>();
        HashMap<Node, Node> parentMap = new HashMap<Node, Node>();
        parentMap.put(start, null);
        LinkedList<Node> nodeQueue = new LinkedList<Node>();

        if (start == null)
            return null;

        if (start == end)
        {
            ArrayList<Node> ret = new ArrayList<>();
            ret.add(start);
            return ret;
        }

        for (Node n : start.getNeighbors())
        {
            nodeQueue.add(n);
            parentMap.put(n, start);
        }

        while (!nodeQueue.isEmpty())
        {
            Node temp = nodeQueue.poll();
            if (temp.getType() >= 6 && temp.getType() <= 19)
            {
                end = temp; //so we don't nullptr on the filter node thingy
                while (parentMap.get(temp) != null)
                {
                    if (filterNode(start, end, temp))
                        result.add(temp);
                    temp = parentMap.get(temp);
                }
                result.add(start);
                return result;
            } else
            {
                for (Node n : temp.getNeighbors())
                {
                    if (!parentMap.containsKey(n))
                    {
                        nodeQueue.add(n);
                        parentMap.put(n, temp);
                    }
                }
            }
        }
        return null; //no path found
    }
}
