package pathfinding;

import java.util.Collection;

public interface Node
{
	boolean containsData(String data);
	void addData(Collection<String> newData);
	double distance(Node node);
	double distance(double nodeX, double nodeY);

	Collection<Node> getNeighbors();
	void addNeighbors(Collection<Node> newNeighbors);
	void addNeighbor(Node newNeighbor);
	double getX();
	double getY();
	void setX(double newX);
	void setY(double newY);
}
