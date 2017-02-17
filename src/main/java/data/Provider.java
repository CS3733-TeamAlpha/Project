package data;

import pathfinding.Node;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a service provider.
 */
public class Provider
{
	public String name;
	public String uuid;
	public ArrayList<Node> locations;

	public Provider(String newName, String newUUID)
	{
		name = newName;
		uuid = newUUID;
		locations = new ArrayList<>();
	}
}
