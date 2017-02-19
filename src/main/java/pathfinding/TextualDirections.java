package pathfinding;

import java.util.ArrayList;

public class TextualDirections
{
	/**
	 * returns an arraylist of all the textual directions, in string form, to get from one node to another.
	 * assumes a path exists
	 *
	 * @param scaleFactor this is how we'll convert coordinates to feet
	 */
	//TODO: edge cases like only two nodes
	//TODO: test
	public static ArrayList<String> getDirections(ArrayList<Node> path, double scaleFactor)
	{
		ArrayList<String> ret = new ArrayList<>();
		if (path == null)
			return ret;

		Node hold;
		for (int j = 0; j < path.size() / 2; j++)
		{
			hold = path.get(j);
			path.set(j, path.get(path.size() - 1 - j));
			path.set(path.size() - 1 - j, hold);
		}

		for (int i = 0; i < path.size() - 2; i++)
		{
			ret.add("Walk " + Math.round(scaleFactor * path.get(i).distance(path.get(i + 1))) + " feet");
			if (path.get(i).getFloor() != path.get(i + 1).getFloor())
				ret.add("Take the elevator to floor " + path.get(i + 1).getFloor() + ", then");
			else
				ret.add(path.get(i).angle(path.get(i + 1), path.get(i + 2)) + ", then");
		}
		ret.add("Walk " + Math.round(scaleFactor * path.get(path.size() - 2).distance(path.get(path.size() - 1))) + " feet");
		ret.add("You have reached your destination!");
		return ret;
	}
}