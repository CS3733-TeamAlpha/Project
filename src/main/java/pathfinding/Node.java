package pathfinding;

import data.Floor;

import java.util.ArrayList;
import java.util.Collection;

public interface Node
{
	/**
	 * Determine whether the node contains some piece of data
	 * @param data Data as a string
	 * @return True if the node contains given data, false otherwise.
	 */
	boolean containsData(String data);

	/**
	 * Merge given data into current data
	 * @param newData Collection of data to merge in
	 */
	void addData(Collection<String> newData);

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

	//Documentation on getters/setters? NEVER!
	Collection<Node> getNeighbors();
	void addNeighbors(Collection<Node> newNeighbors);
	void addNeighbor(Node newNeighbor);
	void removeNeighbor(Node oldNeighbor);
	Floor getOnFloor();
	void setOnFloor(Floor flr);
	int getID();
	double getX();
	double getY();
	void setX(double newX);
	void setY(double newY);
	ArrayList<String> getData();
	void setData(ArrayList<String> newData);
	String angle(Node pivot, Node dest);
}
