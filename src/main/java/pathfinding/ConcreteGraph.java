package pathfinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;

public class ConcreteGraph implements Graph {
	private PriorityQueue<ASTNode> openList;

	public ConcreteGraph() {
		openList = new PriorityQueue<ASTNode>();
	}

	public Collection<Node> findPath(Node start, Node end) {
		//Init: add the first node to the open list
		ASTNode astStart = new ASTNode(start, 0);
		ASTNode astEnd = new ASTNode(end, -1);
		astStart.f = start.distance(end);
		openList.add(astStart);

		boolean complete = false;
		while (openList.size() > 0 && !complete)
		{
			//Pop off the lowest f-val node from the open list
			ASTNode curNode = openList.poll();

			//Explore curNode's children
			for (Node expTempNode : curNode.node.getNeighbors())
			{
				//Check to see if we've found the end node yet
				if (expTempNode == end)
				{
					complete = true;
					astEnd.node = expTempNode;
					astEnd.parent = curNode.parent;
					break;
				}

				ASTNode expNode = new ASTNode(expTempNode, curNode.g + 1);
				expNode.f = expNode.g + expTempNode.distance(curNode.node); //Compute f value using g and h
				expNode.parent = curNode;

				//Add the newly explored node to the open list. Don't bother checking for dupes, it won't matter because
				//we're using a priority queue for openList.
				openList.add(expNode);
			}
		}

		openList.clear(); //Clean up so this whole algorithm stays stateless

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
		return path;
	}

	//This is the only class I bothered to fully implement, for now. Isn't it cute though?
	private static class ASTNode implements Comparable<ASTNode> {
		double f;
		int g;
		ASTNode parent;
		Node node;

		ASTNode(Node newNode, int newG) {
			f = 0;
			g = newG;
			node = newNode;
		}

		public int compareTo(ASTNode node) {
			//Inverted ordering to trick PriorityQueue into thinking that lower valued nodes should be at the top of
			//the queue
			if (f < node.f)
				return 1;
			if (f > node.f)
				return -1;
			return 0;
		}
	}
}
