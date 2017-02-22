package pathfinding;

import data.Node;

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
		if(path == null)
		{
			return null;
		}
		ArrayList<String> toReturn = new ArrayList<>();
		int length = path.size()-1;

		String[] angles = {"Sharp left","Turn left","Bear left","Continue straight","Bear right","Turn right","Sharp right"};

		toReturn.add("Leave kiosk and " + angles[path.get(length).angle(path.get(length-1),path.get(length-2))].toLowerCase());
		String lastName = "";
		int lastAngle = -1;
		for (int i = length-2; i>=2 ; i--)
		{
			String tempN = getNearbyName(path.get(i-2));
			int tempA = path.get(i).angle(path.get(i-1),path.get(i-2));
			if(tempN.equals(lastName) && tempA==lastAngle && tempA==3)
			{
			}else if(path.get(i).getType()==2 && path.get(i-1).getType()==2){
				toReturn.add("Take the elevator to floor " + path.get(i-1).getFloor());
				i--;
			}else if(tempN.equals("the hallway"))
			{
				toReturn.add(angles[tempA] + " and walk down " + tempN);
			}else{
				toReturn.add(angles[tempA] + " towards the " +tempN);
			}
			lastName = tempN;
			lastAngle = tempA;
		}

		toReturn.add("You have arrived at " + path.get(0).getName());

		return toReturn;
	}

	private static String getNearbyName(Node next)
	{
		if(next.getType()!=0){
			return next.getName();
		}else{
			for (Node n:next.getNeighbors())
			{
				if(n.getType()!=0)
				{
					return n.getName();
				}
			}
		}
		return "the hallway";
	}
}