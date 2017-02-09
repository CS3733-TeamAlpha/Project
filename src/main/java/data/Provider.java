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
    private String title;

    public Provider()
    {
    }

    public Provider(int id, String f, String l, String t)
    {
        providerID = id;
        fName = f;
        lName = l;
        title = t;
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

    public void removeLocation(Node locationNode)
    {
        if(locations.contains(locationNode))
        {
            locations.remove(locationNode);
        }
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

    public String getTitle() { return title; }

    public void setTitle(String t){ title = t; }

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
