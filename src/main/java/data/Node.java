package data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Observable;
import java.util.UUID;

public class Node extends Observable
{
	ArrayList<Provider> providers;
	ArrayList<String> services;
	ArrayList<Node> neighbors;
	String id;
	String name;
	String building;
	double x;
	double y;
	int type;
	int floor;

	public Node()
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

	public Node(String newID, String newName, String newBuilding, double newPosX, double newPosY, int newType, int newFloor)
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
	public double distance(double nodeX, double nodeY)
	{

		return Math.sqrt(Math.pow(x - nodeX, 2) + Math.pow(y - nodeY, 2));
	}

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
	public int angle(Node pivot, Node dest)
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
		angle = (angleRad * 180 / Math.PI);
		if (angle < 0)
			angle += 360;
		if (0 <= angle && angle < 60)
			return 0;
		else if (60 <= angle && angle < 120)
			return 1;
		else if (120 <= angle && angle < 165)
			return 2;
		else if (165 <= angle && angle < 195)
			return 3;
		else if (195 <= angle && angle < 240)
			return 4;
		else if (240 <= angle && angle < 300)
			return 5;
		else if (300 <= angle && angle < 360)
			return 6;
		else return -1;
	}

	/*Getters and setters*/

	public void addProvider(Provider newProvider)
	{
		//Only add the provider if they aren't already added here
		if (!providers.contains(newProvider))
		{
			providers.add(newProvider);
			newProvider.addLocation(this); //this could get fun and loopy
			setChanged();
			notifyObservers();
		}
	}

	public void delProvider(Provider oldProvider)
	{
		if (providers.contains(oldProvider))
		{
			providers.remove(oldProvider);
			oldProvider.removeLocation(this.getID()); //todo: why am I removing this by ID? This could get fun and loopy
			setChanged();
			notifyObservers();
		}
	}

	public void addService(String newService)
	{
		if (!services.contains(newService))
		{
			services.add(newService);
			setChanged();
			notifyObservers();
		}
	}

	public void delService(String oldService)
	{
		if (services.contains(oldService))
		{
			services.remove(oldService);
			setChanged();
			notifyObservers();
		}
	}

	public void addNeighbor(Node newNeighbor)
	{
		if (!neighbors.contains(newNeighbor))
		{
			neighbors.add(newNeighbor);
			setChanged();
			notifyObservers();
		}
	}

	public void delNeighbor(Node oldNeighbor)
	{
		if (neighbors.contains(oldNeighbor))
		{
			neighbors.remove(oldNeighbor);
			setChanged();
			notifyObservers();
		}
	}

	public void setID(String newID)
	{
		id = newID;
		setChanged();
		notifyObservers();
	}

	public void setName(String newName)
	{
		name = newName;
		setChanged();
		notifyObservers();
	}

	public void setBuilding(String newBuilding)
	{
		building = newBuilding;
		setChanged();
		notifyObservers();
	}

	public void setXQuiet(double newX)
	{
		setChanged();
		x = newX;
	}

	public void setX(double newX)
	{
		x = newX;
		setChanged();
		notifyObservers();
	}

	public void setYQuiet(double newY)
	{
		setChanged();
		y = newY;
	}

	public void setY(double newY)
	{
		y = newY;
		setChanged();
		notifyObservers();
	}

	public void setType(int newType)
	{
		type = newType;
		setChanged();
		notifyObservers();
	}

	public void setFloor(int newFloor)
	{
		floor = newFloor;
		setChanged();
		notifyObservers();
	}

	public ArrayList<Provider> getProviders()
	{
		return providers;
	}

	public ArrayList<String> getServices()
	{
		return services;
	}

	public Collection<Node> getNeighbors()
	{
		return neighbors;
	}

	public String getID()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getBuilding()
	{
		return building;
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public int getType()
	{
		return type;
	}

	public int getFloor()
	{
		return floor;
	}
}
