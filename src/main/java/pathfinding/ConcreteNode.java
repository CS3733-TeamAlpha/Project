package pathfinding;

import com.sun.corba.se.impl.logging.POASystemException;

import java.util.Collection;
import java.util.ArrayList;
import java.util.UUID;

public class ConcreteNode implements Node
{
	private ArrayList<Node> neighbors;
	private String id;
	private String name;
	private String building;
	private double x;
	private double y;
	private int type; //TODO: Create a better type field, probably an enum
	private int floor;

	public ConcreteNode()
	{
		neighbors = new ArrayList<>();
		id = UUID.randomUUID().toString();
		name = "noname";
		building = "nobuilding";
		x = 0;
		y = 0;
		type = 0;
		floor = 1;
	}

	public ConcreteNode(String newID, String newName, String newBuilding, double newPosX, double newPosY, int newType, int newFloor)
	{
		neighbors = new ArrayList<>();
		id = newID;
		name = newName;
		building = newBuilding;
		x = newPosX;
		y = newPosY;
		type = newType;
		floor = newFloor;
	}

	/**
	 * Calculate distance to another node using simple pythagorean theorem
	 *
	 * @param node Second node
	 * @return Distance
	 */
	@Override
	public double distance(Node node)
	{
		return distance(node.getX(), node.getY());
	}

	/**
	 * Calculate distance from node to given coordinates
	 *
	 * @param nodeX X coordinate
	 * @param nodeY Y coordinate
	 * @return Distance to node
	 */
	@Override
	public double distance(double nodeX, double nodeY)
	{

		return Math.sqrt(Math.pow(x - nodeX, 2) + Math.pow(y - nodeY, 2));
	}

	@Override
	public boolean equals(Node node)
	{
		return id.equals(node.getID()) &&
				name.equals(node.getName()) &&
				building.equals(node.getBuilding()) &&
				x == node.getX() &&
				y == node.getY() &&
				type == node.getType() &&
				floor == node.getFloor();
	}

	/*Getters and setters*/

	@Override
	public void addNeighbor(Node newNeighbor)
	{
		neighbors.add(newNeighbor);
	}

	@Override
	public void removeNeighbor(Node oldNeighbor)
	{
		neighbors.remove(oldNeighbor);
	}

	@Override
	public void setID(String newID)
	{
		id = newID;
	}

	@Override
	public void setName(String newName)
	{
		name = newName;
	}

	@Override
	public void setBuilding(String newBuilding)
	{
		building = newBuilding;
	}

	@Override
	public void setX(double newX)
	{
		x = newX;
	}

	@Override
	public void setY(double newY)
	{
		y = newY;
	}

	@Override
	public void setType(int newType)
	{
		type = newType;
	}

	@Override
	public void setFloor(int newFloor)
	{
		floor = newFloor;
	}

	@Override
	public Collection<Node> getNeighbors()
	{
		return neighbors;
	}

	@Override
	public String getID()
	{
		return id;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getBuilding()
	{
		return building;
	}

	@Override
	public double getX()
	{
		return x;
	}

	@Override
	public double getY()
	{
		return y;
	}

	@Override
	public int getType()
	{
		return type;
	}

	@Override
	public int getFloor()
	{
		return floor;
	}
}
