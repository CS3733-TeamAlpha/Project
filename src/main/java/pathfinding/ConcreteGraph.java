package pathfinding;

import data.Node;

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

		//Idiot check for trying to path from point A to point
		if (start == end)
		{
			ArrayList<Node> ret = new ArrayList<>();
			ret.add(start);
			return ret;
		}

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
	public ArrayList<String> textDirect(Node start, Node end, double scaleFactor)
	{
		ArrayList<Node> path = findPath(start, end);
		if(path == null)
		{
			return null;
		}
		ArrayList<String> toReturn = new ArrayList<>();
		int length = path.size()-1;

		String[] angles = {"Sharp left","Turn left","Bear left","Continue straight","Bear right","Turn right","Sharp right"};

		toReturn.add("Leave kiosk and " + angles[path.get(length).angle(path.get(length-1),path.get(length-2))].toLowerCase());
		String lastName = "";
		int lastAngle = -1;
		for (int i = length-2; i>=2 ; i--)
		{
			String tempN = getNearbyName(path.get(i-2));
			int tempA = path.get(i).angle(path.get(i-1),path.get(i-2));
			if(tempN.equals(lastName) && tempA==lastAngle && tempA==3)
			{
			}else if(path.get(i).getType()==2 && path.get(i-1).getType()==2){
				toReturn.add("Take the elevator to floor " + path.get(i-1).getFloor());
				i--;
			}else if(tempN.equals("the hallway"))
			{
				toReturn.add(angles[tempA] + " and walk down " + tempN);
			}else{
				toReturn.add(angles[tempA] + " towards the " +tempN);
			}
			lastName = tempN;
			lastAngle = tempA;
		}

		toReturn.add("You have arrived at " + path.get(0).getName());

		return toReturn;
	}

	private String getNearbyName(Node next)
	{
		if(next.getType()!=0){
			return next.getName();
		}else{
			for (Node n:next.getNeighbors())
			{
				if(n.getType()!=0)
				{
					return n.getName();
				}
			}
		}
		return "the hallway";
	}
}
