package pathfinding;

import java.util.Collection;

public interface Graph
{
	Collection<Node> findPath(Node start, Node end);
}