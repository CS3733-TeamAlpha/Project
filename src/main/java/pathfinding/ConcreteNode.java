package pathfinding;

import java.util.Collection;
import java.util.ArrayList;
import data.Floor;

public class ConcreteNode implements Node
{
	private ArrayList<String> data; // name, type
	private ArrayList<Node> neighbors;
	private Floor onFloor;
	private double x;
	private double y;
	private String nodeID; //unique

	public ConcreteNode()
	{
		data = new ArrayList<String>();
		neighbors = new ArrayList<Node>();
	}

	public ConcreteNode (String ID, ArrayList<String> newData, double newX, double newY, Floor flr)
	{
		nodeID = ID;
		data = newData;
		neighbors = new ArrayList<Node>();
		x = newX;
		y = newY;
		onFloor = flr;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsData(String data)
	{
		return this.data.contains(data);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addData(Collection<String> newData)
	{
		data.addAll(newData);
	}

	/**
	 * {@inheritDoc}
	 */
	public double distance(Node node)
	{
		return Math.sqrt(Math.pow(x - node.getX(), 2) + Math.pow(y - node.getY(), 2));
	}

	/**
	 * {@inheritDoc}
	 */
	public double distance(double nodeX, double nodeY)
	{
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

	public void removeNeighbor(Node oldNeighbor)
	{
		if (neighbors.contains(oldNeighbor)) {
			neighbors.remove(oldNeighbor);
		}
	}

	public Floor getOnFloor(){ return onFloor; }

	public void setOnFloor(Floor flr){ onFloor = flr; }

	public String getID(){ return nodeID; }

	public ArrayList<String> getData(){ return data; }

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

	public void setData(ArrayList<String> newData){ data = newData; }
}
