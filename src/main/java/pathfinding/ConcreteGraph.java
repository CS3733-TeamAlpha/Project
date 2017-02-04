package pathfinding;

import java.util.Collection;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class ConcreteGraph implements Graph
{
	private PriorityQueue<ASTNode> openList;
	private ArrayList<Node> closedList;

	public ConcreteGraph(){};

	public Collection<Node> findPath(Node start, Node end)
	{
		return null;
	}

	//This is the only class I bothered to fully implement, for now. Isn't it cute though?
	private static class ASTNode implements Comparable<ASTNode>
	{
		public float f;
		public float g;
		public Node parent;
		public Node node;

		public ASTNode()
		{
			f = 0;
			g = 0;
			node = new ConcreteNode();
		}

		public int compareTo(ASTNode node)
		{
			//Inverted ordering to trick PriorityQueue into thinking that lower valued nodes should be at the top of
			//the queue
			if (f < node.f)
				return 1;
			if (f> node.f)
				return -1;
			return 0;
		}
	}
}
