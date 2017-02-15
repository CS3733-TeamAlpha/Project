package pathfinding;

import java.util.Collection;
import java.util.ArrayList;
import java.util.UUID;

public class ConcreteNode implements Node
{
	private ArrayList<String> providers;
	private ArrayList<String> services;
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
		providers = new ArrayList<>();
		services = new ArrayList<>();
		neighbors = new ArrayList<>();
		id = UUID.randomUUID().toString();
		name = "noname";
		building = "00000000-0000-0000-0000-000000000000";
		x = 0;
		y = 0;
		type = -1;
		floor = 1;
	}

	public ConcreteNode(String newID, String newName, String newBuilding, double newPosX, double newPosY, int newType, int newFloor)
	{
		providers = new ArrayList<>();
		services = new ArrayList<>();
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

	/**
	 * {@inheritDoc}
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

	/*Getters and setters*/

	@Override
	public void addProvider(String newProvider)
	{
		if (!providers.contains(newProvider))
			providers.add(newProvider);
	}

	@Override
	public void delProvider(String oldProvider)
	{
		providers.remove(oldProvider);
	}

	@Override
	public void addService(String newService)
	{
		if (!services.contains(newService))
			services.add(newService);
	}

	@Override
	public void delService(String oldService)
	{
		services.remove(oldService);
	}

	@Override
	public void addNeighbor(Node newNeighbor)
	{
		if (!neighbors.contains(newNeighbor))
			neighbors.add(newNeighbor);
	}

	@Override
	public void delNeighbor(Node oldNeighbor)
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
	public ArrayList<String> getProviders()
	{
		return providers;
	}

	@Override
	public ArrayList<String> getServices()
	{
		return services;
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
