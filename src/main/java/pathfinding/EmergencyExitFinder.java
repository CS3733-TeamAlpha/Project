package pathfinding;

import data.Node;
import data.NodeTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class EmergencyExitFinder
{
	/**
	 * Finds an exit nearest to the current node that can be accessed using only stairs.
	 *
	 * @param start Node to start searching from.
	 * @return Nearest exit accessible by stairs only
	 */
	public Node findExit(Node start)
	{
		System.out.println("Activating breadth first search");
		ArrayList<Node> result = new ArrayList<Node>();
		HashMap<Node, Node> parentMap = new HashMap<Node, Node>();
		parentMap.put(start, null);
		LinkedList<Node> nodeQueue = new LinkedList<Node>();

		if (start == null)
			return null;

		for (Node n : start.getNeighbors())
		{
			nodeQueue.add(n);
			parentMap.put(n, start);
		}

		while (!nodeQueue.isEmpty())
		{
			Node temp = nodeQueue.poll();
			if (temp.getType() >= 6 && temp.getType() <= 19)
				return temp;
			else
			{
				for (Node n : temp.getNeighbors())
				{
					if (!parentMap.containsKey(n) && n.getType() != NodeTypes.ELEVATOR.val())
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
