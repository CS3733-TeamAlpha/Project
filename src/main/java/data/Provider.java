package data;

import pathfinding.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a service provider.
 */
public class Provider
{
	private String firstName, lastName, title;
	private String uuid;

	private HashMap<String, Node> locations;

	/**
	 * Creates a provider, and makes sure all related nodes are aware the provider is assigned to them
	 * @param firstName
	 * @param lastName
	 * @param uuid
	 * @param title
	 * @param locations
	 * @return
	 */
	public static Provider newInstance(String firstName, String lastName, String uuid, String title, HashMap<String, Node> locations)
	{
		Provider p = new Provider(firstName, lastName, uuid, title, locations);

		locations.forEach((id, node) -> node.addProvider(p));

		return p;
	}

	public static Provider newInstance(String firstName, String lastName, String uuid, String title, ArrayList<Node> locations)
	{
		HashMap<String, Node> map = new HashMap<>();
		for(Node n : locations)
		{
			map.put(n.getID(), n);
		}
		return newInstance(firstName, lastName, uuid, title, map);
	}

	private Provider(String firstName, String lastName, String uuid, String title, HashMap<String, Node> locations)
	{
		this.firstName = firstName;
		this.lastName = lastName;
		this.uuid = uuid;
		this.title = title;
		this.locations = locations;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public List<String> getLocationIds()
	{
		return locations.keySet().stream().collect(Collectors.toList());
	}

	public void addLocation(Node n)
	{
		locations.put(n.getID(), n);
		n.addProvider(this);
	}

	public void removeLocation(String id)
	{
		locations.get(id).delProvider(this);
		locations.remove(id);
	}

	public List<String> getLocationStringArray()
	{
		//Sorry for the extended syntax. This line extracts all the node names out of the locations hashmap, and returns a string array
		return locations.values().stream().map(Node::getName).collect(Collectors.toList());
	}

	public String getStringLocations()
	{
		StringBuilder build = new StringBuilder();
		for(int i = 0; i < locations.size(); i++)
		{
			build.append(locations.get(i).getName());
			if(i != locations.size()-1)
				build.append(", ");
		}
		return build.toString();
	}

	public HashMap<String, String> getNodeIdNameMap()
	{
		HashMap<String, String> map = new HashMap<>();
		for(String key : locations.keySet())
		{
			map.put(key, locations.get(key).getName());
		}
		return map;
	}
}
