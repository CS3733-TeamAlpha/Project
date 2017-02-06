package data;

import pathfinding.Node;

import java.util.ArrayList;
import java.util.Collection;

public class Provider {

    private ArrayList<Node> locations;
    private int providerID;
    private String fName;
    private String lName;

    public Provider(){}

    public Provider(int id, String f, String l){
        providerID = id;
        fName = f;
        lName = l;
    }

    public boolean atLocation(Node locationNode){ return locations.contains(locationNode); }

    public Collection<Node> getLocations()
    {
        return locations;
    }

    public void addLocations(Collection<Node> newNeighbors)
    {
        locations.addAll(newNeighbors);
    }

    public void addLocation(Node newNeighbor)
    {
        locations.add(newNeighbor);
    }

    public String getfName(){ return fName; }

    public String getlName(){ return lName; }

    public void setfName(String f){ fName = f; }

    public void setlName(String l){ lName = l; }

    public int getID(){ return providerID; }
}
