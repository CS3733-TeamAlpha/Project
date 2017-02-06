package data;

import pathfinding.Node;

import java.util.ArrayList;
import java.util.Collection;

public class Provider
{

    private ArrayList<Node> locations;
    private int providerID;
    private String fName;
    private String lName;

    public Provider()
    {
    }

    public Provider(int id, String f, String l)
    {
        providerID = id;
        fName = f;
        lName = l;
        locations = new ArrayList<Node>();
    }

    public boolean atLocation(Node locationNode)
    {
        return locations.contains(locationNode);
    }

    public ArrayList<Node> getLocations()
    {
        return locations;
    }

    public void addLocations(Collection<Node> locationNodes)
    {
        locations.addAll(locationNodes);
    }

    public void addLocation(Node locationNode)
    {
        locations.add(locationNode);
    }

    public String getfName()
    {
        return fName;
    }

    public String getlName()
    {
        return lName;
    }

    public void setfName(String f)
    {
        fName = f;
    }

    public void setlName(String l)
    {
        lName = l;
    }

    public int getID()
    {
        return providerID;
    }
}
