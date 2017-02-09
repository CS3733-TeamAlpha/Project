package data;

/**
 * Floor currently does nothing other than exist.
 */
public class Floor
{

	private int FloorID;
	private String FloorName;
	private int FloorLevel;

	public Floor()
	{
	}

	public Floor(int id, String name, int lvl)
	{
		FloorID = id;
		FloorName = name;
		FloorLevel = lvl;
	}

	public int getID()
	{
		return FloorID;
	}

	public String getName()
	{
		return FloorName;
	}

	public void setName(String n)
	{
		FloorName = n;
	}

	public int getLevel()
	{
		return FloorLevel;
	}

	public void setLevel(int l)
	{
		FloorLevel = l;
	}
}
