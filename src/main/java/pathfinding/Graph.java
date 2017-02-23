package pathfinding;

import data.Node;

import java.util.ArrayList;

public abstract class Graph
{
	/**
	 * Find a path between start and end using
	 *
	 * @param start Node to start pathing from.
	 * @param end   Node to attempt to path to.
	 * @return Ordered collection of nodes forming a complete path from start to finish, or null upon error/no-path.
	 */
	public abstract ArrayList<Node> findPath(Node start, Node end);

	/**
	 * For internal use by Graph subclasses only. Given a start node and an end node, this will return true if the
	 * current node should be added, and false otherwise.
	 *
	 * @param start Start node on the WIP path
	 * @param end End node on the WIP path
	 * @param cur Node that may or may not be added to path
	 * @return True if the node should be added, false otherwise.
	 */
	protected boolean filterNode(Node start, Node end, Node cur)
	{
		//This used to be three if statements until I realized that we might as well just do this in a single boolean
		//expression. Hence, the monstrosity below.
		return (cur.getFloor() == 1) ||
				(cur.getFloor() == start.getFloor() && cur.getBuilding().equals(start.getBuilding())) ||
				(cur.getFloor() == end.getFloor() && cur.getBuilding().equals(end.getBuilding()));
	}
}