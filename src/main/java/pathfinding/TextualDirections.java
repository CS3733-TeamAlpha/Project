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
	public static ArrayList<String> getDirections(ArrayList<Node> path, double scaleFactor, ArrayList<Node> totalPath)
	{
		String outdoors = "00000000-0000-0000-0000-222222222222";

		if(path == null || path.size() == 0)
		{
			return null;
		}
		ArrayList<String> toReturn = new ArrayList<>();
		int length = path.size()-1;
		//special case we are already at destination
		if(length <= 1)
		{
			if(length == 1)
			{
				toReturn.add("Leave " + path.get(1).getName());
			}
			toReturn.add("You have arrived at " + path.get(0).getName());
			return toReturn;
		}

		String[] angles = {"Sharp left","Turn left","Bear left","Continue straight","Bear right","Turn right","Sharp right"};

		toReturn.add("Leave " + path.get(length).getName() + " and " + angles[path.get(length).angle(path.get(length-1),path.get(length-2))].toLowerCase());
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
			}else if(path.get(i).getType()==20 && path.get(i-1).getType()==20){
				toReturn.add("Take the stairs to floor " + path.get(i-1).getFloor());
			}
			else if(tempN.equals("the hallway"))
			{
				//change text to be sidewalk if we are outside
				if(path.get(i-2).getBuilding().equals(outdoors))
				{
					toReturn.add(angles[tempA] + " and walk down the sidewalk.");
				}
				else
				{
					toReturn.add(angles[tempA] + " and walk down " + tempN);
				}
			}else{
				toReturn.add(angles[tempA] + " towards \"" +tempN + "\"");
			}
			lastName = tempN;
			lastAngle = tempA;
		}

		//Case to print for the last item in the trimmed path
		if(path.get(0) == totalPath.get(0)) //at destination
		{
			toReturn.add("You have arrived at " + path.get(0).getName());
		} else if (path.get(0).getType() == 2) { //elevator
			Integer lastIndex = totalPath.indexOf(path.get(0));
			toReturn.add("Take the elevator to floor " + totalPath.get(lastIndex-1).getFloor());

		} else if (path.get(0).getType() == 20)
		{ //stairs
			Integer lastIndex = totalPath.indexOf(path.get(0));
			toReturn.add("Take the stairs to floor " + totalPath.get(lastIndex - 1).getFloor());
		}else if (path.get(0).getType() > 5 && path.get(0).getType() < 20) //building entrance/exit
		{
			if(path.get(0).getBuilding().equals(outdoors))
			{
				toReturn.add("Proceed to enter the building.");
			} else
			{
				toReturn.add("Proceed to the exit the building.");
			}
		}

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