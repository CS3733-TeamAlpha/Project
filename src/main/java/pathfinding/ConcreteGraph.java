package pathfinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;

public class ConcreteGraph implements Graph {

	/**
	 * {@inheritDoc}
	 * @implNote This implementation returns an ArrayList.
	 */
	public Collection<Node> findPath(Node start, Node end) {
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

				//Check to see if we've found the end node yet
				if (expTempNode == end)
				{
					complete = true;
					astEnd.node = expTempNode;
					astEnd.parent = curNode.parent;
					break;
				}

				ASTNode expNode = new ASTNode(expTempNode, curNode.g + 1.0);
				expNode.f = expNode.g + expTempNode.distance(end); //Compute f value using g and h
				expNode.parent = curNode;
				openList.add(expNode);
			}
			closedList.add(curNode.node);
		}

		if (!complete)
			return null;

		//Backtrack from the end node, assembling an ordered list as we go
		ArrayList<Node> path = new ArrayList<Node>();
		ASTNode curNode = astEnd;
		while (curNode != null)
		{
			path.add(curNode.node);
			curNode = curNode.parent;
		}
		path.add(start);
		return path;
	}

	//This is the only class I bothered to fully implement, for now. Isn't it cute though?
	private static class ASTNode implements Comparable<ASTNode> {
		double f;
		double g;
		ASTNode parent;
		Node node;

		ASTNode(Node newNode, double newG) {
			f = 0;
			g = newG;
			node = newNode;
		}

		public int compareTo(ASTNode node) {
			//Inverted ordering to trick PriorityQueue into thinking that lower valued nodes should be at the top of
			//the queue
			if (f < node.f)
				return -1;
			if (f > node.f)
				return 1;
			return 0;
		}
	}
}
