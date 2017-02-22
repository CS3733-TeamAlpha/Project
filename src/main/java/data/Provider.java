package data;

import pathfinding.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	 * Creates a new provider using given data. Makes sure to assign this provider to the given locations.
	 * @param firstName First name of provider.
	 * @param lastName Last name of provider.
	 * @param uuid UUID of provider.
	 * @param title Title of provider.
	 * @param locations ArrayList of this provider's locations. Each location will have this provider object added to it.
	 */
	public Provider(String firstName, String lastName, String uuid, String title, ArrayList<Node> locations)
	{
		this.firstName = firstName;
		this.lastName = lastName;
		this.uuid = uuid;
		this.title = title;
		this.locations = new HashMap<>();
		locations.forEach((node) -> this.locations.put(node.getID(), node));
		locations.forEach((node) -> node.addProvider(this));
	}

	/**
	 * Creates a new provider using given data. Makes sure to assign this provider to the given locations.
	 * @param firstName First name of provider.
	 * @param lastName Last name of provider.
	 * @param uuid UUID of provider.
	 * @param title Title of provider.
	 * @param locations HashMap of this provider's locations. Each location will have this provider object added to it.
	 */
	public Provider(String firstName, String lastName, String uuid, String title, HashMap<String, Node> locations)
	{
		this.firstName = firstName;
		this.lastName = lastName;
		this.uuid = uuid;
		this.title = title;
		this.locations = locations;
		locations.forEach((id, node) -> node.addProvider(this));
	}

	/**
	 * Creates a new provider using given data, but initializes it with a new (blank) hashmap. Provided so that provider
	 * objects may be created independently of nodes without having to worry about nullptrs.
	 * @param firstName First name of provider.
	 * @param lastName Last name of provider.
	 * @param uuid UUID of provider.
	 * @param title Title of provider.
	 */
	public Provider(String firstName, String lastName, String uuid, String title)
	{
		this (firstName, lastName, uuid, title, new HashMap<String, Node>());
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
		Object[] keySet = locations.keySet().toArray();
		for(int i = 0; i < keySet.length; i++)
		{
			build.append(locations.get(keySet[i]).getName());
			if(i != locations.size()-1)
				build.append("\n");
		}
		return build.toString();
	}

	/**
	 * The point in having this is preventing direct access to our Node List, since we want
	 * to be the one responsible for keeping it updated
	 * @return A Hashmap relating Node IDs to Node Names
	 */
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
