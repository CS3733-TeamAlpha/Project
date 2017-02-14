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
	private int nodeID; //unique

	public ConcreteNode()
	{
		data = new ArrayList<String>();
		neighbors = new ArrayList<Node>();
	}

	public ConcreteNode (int ID, ArrayList<String> newData, double newX, double newY, Floor flr)
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

	/**
	 * uses BLACK MAGIC to calculate the angle between 3 nodes, the first being the node
	 * that this method is called on. Doesn't actually return the angle; returns an approximate
	 * direction
	 * @param pivot the second node in a set of three nodes
	 * @param dest the third node in a set of three nodes
	 */
	public String angle(Node pivot, Node dest)
	{
		double angle;
		// Google atan2 if you want to understand this; I sure don't
		double aX = this.x - pivot.getX();
		double aY = this.y - pivot.getY();
		double bX = dest.getX() - pivot.getX();
		double bY = dest.getY() - pivot.getY();
		double thetaA = Math.atan2(aY, aX);
		double thetaB = Math.atan2(bY, bX);
		double angleRad = (thetaB - thetaA);
		angle = (angleRad*180/Math.PI);
		if (angle < 0)
			angle += 360;
		if (0 <= angle && angle < 60) //TODO: can this be done with switch statements?
			return "Sharp right";
		else if (60 <= angle && angle < 120)
			return "Turn right";
		else if (120 <= angle && angle < 180)
			return "Bear right";
		else if (180 <= angle && angle < 240)
			return "Bear left";
		else if (240 <= angle && angle < 300)
			return "Turn left";
		else if (300 <= angle && angle < 360)
			return "Sharp left";
		else return null;
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

	public int getID(){ return nodeID; }

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
