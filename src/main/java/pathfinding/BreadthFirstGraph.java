package pathfinding;

import data.Node;
import data.NodeTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class BreadthFirstGraph extends Graph
{

	public ArrayList<Node> findPath(Node start, Node end, boolean useStairs)
	{
		System.out.println("Activating breadth first search");
		ArrayList<Node> result = new ArrayList<Node>();
		HashMap<Node, Node> parentMap = new HashMap<Node, Node>();
		parentMap.put(start, null);
		LinkedList<Node> nodeQueue = new LinkedList<Node>();

		if (start == null || end == null)
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
			if (useStairs && temp.getType() == NodeTypes.ELEVATOR.val())
				continue;
			if (!useStairs && temp.getType() == NodeTypes.STAIRWAY.val())
				continue;

			if (temp == end)
			{
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