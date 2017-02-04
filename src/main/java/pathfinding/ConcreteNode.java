package pathfinding;

import java.util.Collection;
import java.util.ArrayList;

public class ConcreteNode implements Node
{
	private ArrayList<String> data;
	private ArrayList<Node> neighbors;
	private double x;
	private double y;

	public ConcreteNode()
	{
		data = new ArrayList<String>();
		neighbors = new ArrayList<Node>();
	}

	public boolean containsData(String data)
	{
		return this.data.contains(data);
	}

	public void addData(Collection<String> newData)
	{
		data.addAll(newData);
	}

	public double distance(Node node)
	{
		//TODO: Writes units tests... just because.
		return Math.sqrt(Math.pow(x - node.getX(), 2) + Math.pow(y - node.getY(), 2));
	}

	public double distance(double nodeX, double nodeY)
	{
		//TODO: Write unit tests
		return Math.sqrt(Math.pow(x - nodeX, 2) + Math.pow(y - nodeY, 2));
	}


	public Collection<Node> getNeighbors()
	{
		return neighbors;
	}

	public void addNeighbors(Collection<Node> newNeighbors)
	{
		neighbors.addAll(newNeighbors);
	}

	public void addNeighbor(Node newNeighbor)
	{
		neighbors.add(newNeighbor);
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public void setX(double newX)
	{
		x = newX;
	}

	public void setY(double newY)
	{
		y = newY;
	}
}