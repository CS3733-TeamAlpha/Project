package pathfinding;

import java.util.ArrayList;
import java.util.Collection;

public interface Node
{

	/**
	 * Calculate distance to another node using simple pythagorean theorem
	 * @param node Second node
	 * @return Distance
	 */
	double distance(Node node);

	/**
	 * Calculate distance from node to given coordinates
	 * @param nodeX X coordinate
	 * @param nodeY Y coordinate
	 * @return Distance to node
	 */
	double distance(double nodeX, double nodeY);

	boolean equals(Node node);

	//Documentation on getters/setters? NEVER!
	void addProvider(String newProvider);
	void delProvider(String oldProvider);
	void addService(String newService);
	void delService(String oldService);
	void addNeighbor(Node newNeighbor);
	void delNeighbor(Node oldNeighbor);
	void setID(String newID);
	void setName(String newName);
	void setBuilding(String newBuilding);
	void setX(double newX);
	void setY(double newY);
	void setType(int newType);
	void setFloor(int newFloor);

	ArrayList<String> getProviders();
	ArrayList<String> getServices();
	Collection<Node> getNeighbors();
	String getID();
	String getName();
	String getBuilding();
	double getX();
	double getY();
	int getType();
	int getFloor();
}
