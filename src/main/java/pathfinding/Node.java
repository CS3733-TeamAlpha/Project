package pathfinding;

import java.util.Collection;

public interface Node
{
	boolean containsData(String data);
	void addData(Collection<String> newData);
	float distance(Node node);
	float distance(float nodeX, float nodeY);

	Collection<Node> getNeighbors();
	void addNeighbors(Collection<Node> newNeighbors);
	float getX();
	float getY();
}
