package data;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Stack;
import pathfinding.Node;
import pathfinding.ConcreteNode;

public class NodeRepository
{
	private ArrayList<Node> nodes; //Database guys: switch this to whatever you want. ArrayList is just a placeholder!
	private Stack<Node> resultStack; //Same goes for this too

	public NodeRepository()
	{
	}

	public Node getNearestNode(float x, float y)
	{
		return null;
	}

	public Node getNodeByData(String data)
	{
		return null;
	}

	public Stack<Node> getRecentSearches()
	{
		return null;
	}

	public void addNodes(Collection<Node> newNodes)
	{

	}

	public void deleteNode(Node node)
	{

	}

	public Collection<Node> getAll()
	{
		return null;
	}
}
