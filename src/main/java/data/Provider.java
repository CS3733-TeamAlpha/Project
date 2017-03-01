package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.stream.Collectors;

/**
 * Represents a service provider.
 */
public class Provider extends Observable
{
	private String firstName, lastName, title, uuid;
	HashMap<String, Node> locations;

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
		this (firstName, lastName, uuid, title, new HashMap<>());
	}

	public String getTitle()
	{
		return title;
	}

	public void setAll(String newFName, String newLName, String newTitle)
	{
		firstName = newFName;
		lastName = newLName;
		title = newTitle;
		setChanged();
		notifyObservers();
	}

	public void setTitle(String title)
	{
		this.title = title;
		setChanged();
		notifyObservers();
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
		setChanged();
		notifyObservers();
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
		setChanged();
		notifyObservers();
	}

	public String getUUID()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
		setChanged();
		notifyObservers();
	}

	public List<String> getLocationIds()
	{
		return locations.keySet().stream().collect(Collectors.toList());
	}

	public List<Node> getLocations()
	{
		ArrayList<Node> ret = new ArrayList<>();
		locations.forEach((id, node) -> ret.add(node));
		return ret;
	}

	public void addLocation(Node n)
	{
		if (!locations.containsKey(n.getID()))
		{
			locations.put(n.getID(), n);
			n.addProvider(this); //introducing looper 2...
			setChanged();
			notifyObservers();
		}
	}

	public void removeLocation(String id)
	{
		if (locations.containsKey(id))
		{
			locations.get(id).delProvider(this);
			locations.remove(id);
			setChanged();
			notifyObservers();
		}
	}

	public String getStringLocations()
	{
		StringBuilder build = new StringBuilder();
		Object[] keySet = locations.keySet().toArray();
		for(int i = 0; i < keySet.length; i++)
		{
			build.append(locations.get(keySet[i]).getName());
			if(i != locations.size() - 1)
				build.append("\n");
		}
		return build.toString();
	}
}
