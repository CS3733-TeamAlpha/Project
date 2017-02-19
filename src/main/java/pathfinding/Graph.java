package pathfinding;

import java.util.ArrayList;

public interface Graph
{
	/**
	 * Find a path between start and end using
	 *
	 * @param start Node to start pathing from.
	 * @param end   Node to attempt to path to.
	 * @return Ordered collection of nodes forming a complete path from start to finish, or null upon error/no-path.
	 */
	ArrayList<Node> findPath(Node start, Node end);
}