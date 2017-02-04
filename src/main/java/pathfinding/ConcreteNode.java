package pathfinding;

import java.util.Collection;
import java.util.ArrayList;

public class ConcreteNode implements Node
{
	private ArrayList<String> data;
	private ArrayList<Node> neighbors;
	private float x;
	private float y;

	public ConcreteNode(){}

	public boolean containsData(String data)
	{
		return false;
	}

	public void addData(Collection<String> newData)
	{

	}

	public float distance(Node node)
	{
		return 0;
	}

	public float distance(float nodeX, float nodeY)
	{
		return 0;
	}


	public Collection<Node> getNeighbors()
	{
		return null;
	}

	public void addNeighbors(Collection<Node> newNeighbors)
	{

	}

	public float getX()
	{
		return 0;
	}

	public float getY()
	{
		return 0;
	}
}
