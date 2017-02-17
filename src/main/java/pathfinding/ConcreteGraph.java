package pathfinding;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class ConcreteGraph implements Graph
{

	/**
	 * {@inheritDoc}
	 *
	 * @implNote This implementation returns an ArrayList.
	 * @// TODO: 2/6/17 Implement straight-shot optimization for traversing multiple floors
	 */
	public ArrayList<Node> findPath(Node start, Node end)
	{
		if (start == null || end == null)
			return null; //idiot check

		//Init: add the first node to the open list
		ASTNode astStart = new ASTNode(start, 0);
		ASTNode astEnd = new ASTNode(end, -1);
		astStart.f = start.distance(end);

		PriorityQueue<ASTNode> openList = new PriorityQueue<ASTNode>();
		ArrayList<Node> closedList = new ArrayList<Node>();
		openList.add(astStart);

		boolean complete = false;
		while (openList.size() > 0 && !complete)
		{
			//Pop off the lowest f-val node from the open list
			ASTNode curNode = openList.poll();

			//Explore curNode's children
			for (Node expTempNode : curNode.node.getNeighbors())
			{
				if (closedList.contains(expTempNode))
					continue; //Don't explore nodes that have already been explored

				//Straight shot optimization -- if curNode isn't on the same floor as start or end, this node must be
				//on a different floor than curNode. Otherwise, skip.
				//if ((curNode.node.getFloor() != start.getFloor() || curNode.node.getFloor() != end.getFloor())
				//		&& expTempNode.getFloor() == curNode.node.getFloor())
				//	continue;

				//Check to see if we've found the end node yet
				if (expTempNode == end)
				{
					complete = true;
					astEnd.parent = curNode;
					break;
				}

				ASTNode expNode = new ASTNode(expTempNode, curNode.g + 1.0);
				expNode.f = expNode.g + expTempNode.distance(end); //Compute f value using g and h
				expNode.parent = curNode;

				//Try to add the newly found node to the list
				boolean hasNode = false;
				for (ASTNode node : openList)
				{
					if (node.node == expTempNode)
					{
						if (node.f > expNode.f)
						{
							openList.remove(node);
							break; //There will only ever be one copy of the same none on the openlist
						}
						hasNode = true;
						break;
					}
				}
				if (!hasNode)
					openList.offer(expNode);
			}
			closedList.add(curNode.node);
		}

		if (!complete)
			return null;

		//Backtrack from the end node, assembling an ordered list as we go
		ArrayList<Node> path = new ArrayList<Node>();
		for (ASTNode node = astEnd; node != null; node = node.parent)
			if (node.node.getFloor() == start.getFloor() || node.node.getFloor() == end.getFloor())
				path.add(node.node);
		return path;
	}

	private static class ASTNode implements Comparable<ASTNode>
	{
		double f;
		double g;
		ASTNode parent;
		Node node;

		ASTNode(Node newNode, double newG)
		{
			f = 0;
			g = newG;
			node = newNode;
		}

		public int compareTo(ASTNode node)
		{
			if (f < node.f)
				return -1;
			if (f > node.f)
				return 1;
			return 0;
		}
	}

	/**
	 * returns an arraylist of all the textual directions, in string form, to get from one node to another.
	 * assumes a path exists
	 *
	 * @param scaleFactor this is how we'll convert coordinates to feet
	 */
	//TODO: edge cases like only two nodes
	//TODO: test
	public ArrayList<String> textDirect(Node start, Node end, int scaleFactor)
	{
		ArrayList<Node> path = findPath(start, end);
		Node hold;
		for (int j = 0; j < path.size()/2; j++)
		{
			hold = path.get(j);
			path.set(j, path.get(path.size()-1 - j));
			path.set(path.size()-1 - j, hold);
		}
		if (path == null)
		    return null;
		ArrayList<String> temp = new ArrayList<>();
		for (int i = 0; i < path.size() - 2; i++)
		{
			temp.add("Walk " + Math.round(scaleFactor*path.get(i).distance(path.get(i+1))) + " feet");
			if (path.get(i).getFloor() != path.get(i+1).getFloor())
				temp.add("Take the elevator to floor " + path.get(i+1).getFloor() + ", then");
			else
			temp.add(path.get(i).angle(path.get(i+1), path.get(i+2)) + ", then");
		}
		temp.add("Walk " + Math.round(scaleFactor*path.get(path.size()-2).distance(path.get(path.size()-1))) + " feet");
		temp.add("You have reached your destination!");
		return temp;
	}
}
