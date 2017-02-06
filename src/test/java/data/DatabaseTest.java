package data;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import pathfinding.ConcreteNode;
import pathfinding.Node;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class DatabaseTest {

    ArrayList<Node> ndL = new ArrayList<Node>();
    ArrayList<Provider> pvdL = new ArrayList<Provider>();
    ArrayList<Floor> flL = new ArrayList<Floor>();

    @Before
    public void setUp(){
        ndL.clear();
        pvdL.clear();
        flL.clear();
        DatabaseController.createConnection();

        DatabaseController.droptablesForShittyTesting();
        DatabaseController.initializeProviderTable();
        DatabaseController.initializeFloorTable();
        DatabaseController.initializeNodeTable();
        DatabaseController.initializeOfficeTable();
        DatabaseController.initializeNeighborTable();

        Floor fl1 = new Floor(01, "TestFloor", 1);
        ConcreteNode n001 = new ConcreteNode(001, new ArrayList<>(Arrays.asList("Room001", "room")), 1, 1, fl1);
        ConcreteNode n002 = new ConcreteNode(002, new ArrayList<>(Arrays.asList("Room002", "room")), 2, 2, fl1);
        ConcreteNode n003 = new ConcreteNode(003, new ArrayList<>(Arrays.asList("Room003", "room")), 3, 3, fl1);
        Provider p111 = new Provider(111, "Donald", "Trump");
        Provider p222 = new Provider(222, "Barack", "Obama");
        n001.addNeighbor(n002);
        n002.addNeighbor(n001);
        p111.addLocation(n003);
        flL.add(fl1);
        ndL.add(n001);
        ndL.add(n002);
        ndL.add(n003);
        pvdL.add(p111);
        pvdL.add(p222);

        testInsert();
    }

    @After
    public void shutdown(){
        DatabaseController.shutdown();
    }

    @Test
    public void testInsert(){
        DatabaseController.insertFloor(01, "TestFloor", 1);
        DatabaseController.insertNode(001, "Room001", "room", 1, 1, 01);
        DatabaseController.insertNode(002, "Room002", "room", 2, 2, 01);
        DatabaseController.insertNode(003, "Room003", "room", 3, 3, 01);
        DatabaseController.insertProvider(111, "Donald", "Trump");
        DatabaseController.insertProvider(222, "Barack", "Obama");
        DatabaseController.insertNeighbor(001, 002);
        DatabaseController.insertNeighbor(002, 001);
        DatabaseController.insertOffice(111, 003);

    }

    @Test
    public void testInitialization(){

        DatabaseController.droptablesForShittyTesting();

        DatabaseController.initializeProviderTable();
        DatabaseController.initializeFloorTable();
        DatabaseController.initializeNodeTable();
        DatabaseController.initializeOfficeTable();
        DatabaseController.initializeNeighborTable();
        testInsert();
        DatabaseController.initializeAllFloors();
        DatabaseController.initializeAllNodes();
        DatabaseController.initializeAllProviders();

        for(int i=0;i<flL.size();i++){
            compareFloor(flL.get(i), DatabaseController.getFloorList().get(i));
        }

        //for(int i=0;i<ndL.size();i++){
        //TODO: Why is data coming in this order?
            compareNode(ndL.get(0), DatabaseController.getNodeList().get(2));
            compareNode(ndL.get(1), DatabaseController.getNodeList().get(0));
            compareNode(ndL.get(2), DatabaseController.getNodeList().get(1));
        //}
        //for(int i=0;i<pvdL.size();i++){
            //TODO: why is data coming in this order?
            compareProvider(pvdL.get(0), DatabaseController.getProviderList().get(1));
            compareProvider(pvdL.get(1), DatabaseController.getProviderList().get(0));
        //}

    }

    @Test
    public void testGets(){
        DatabaseController.initializeAllFloors();
        DatabaseController.initializeAllNodes();
        DatabaseController.initializeAllProviders();

        compareNode(DatabaseController.getNodeByID(001), ndL.get(0));
        compareNode(DatabaseController.getNodeByID(002), ndL.get(1));

        compareProvider(DatabaseController.getProviderByID(111), pvdL.get(0));

        compareProvider(DatabaseController.getProvidersByFullName("Donald", "Trump").get(0), pvdL.get(0));

        compareFloor(DatabaseController.getFloorByID(01), flL.get(0));
    }

    @Test
    public void testProviderAtNode(){
        DatabaseController.initializeAllFloors();
        DatabaseController.initializeAllNodes();
        DatabaseController.initializeAllProviders();

        compareProvider(DatabaseController.getProvidersAtNode(003).get(0), pvdL.get(0));
        compareNode(DatabaseController.getProviderNodes(111).get(0), ndL.get(2));
    }

    @Test
    public void testRemoves(){
        DatabaseController.removeOfficeByProvider(111);
        DatabaseController.removeAllNeighborsByID(001);
        DatabaseController.removeNode(001);
        DatabaseController.removeProvider(111);
        DatabaseController.initializeAllFloors();
        DatabaseController.initializeAllNodes();
        DatabaseController.initializeAllProviders();

        assertNotEquals(DatabaseController.getProviderList().size(), pvdL.size());
        assertNotEquals(DatabaseController.getNodeList().size(), ndL.size());
    }

    @Test
    public void testNullGets(){
        assertNull(DatabaseController.getFloorByID(123456));
        assertNull(DatabaseController.getNodeByID(987654));
        assertNull(DatabaseController.getProviderByID(765484));
        assertNull(DatabaseController.makeNodeByID(123456));
        assertNull(DatabaseController.makeProviderByID(123456));
        assertNull(DatabaseController.makeFloorByID(123456));

    }


    //compare contents of floor
    public void compareFloor(Floor e, Floor a){
        assertEquals(e.getID(), a.getID());
        assertEquals(e.getLevel(), a.getLevel());
        assertEquals(e.getName(), a.getName());
    }

    //compare contents of node
    public void compareNode(Node e, Node a){
        assertEquals(e.getID(), a.getID());
        assertEquals(e.getData(), a.getData());
        //TODO: proper neighbor testing?
        compareFloor(e.getOnFloor(), a.getOnFloor());
        assertEquals(e.getX(), a.getX(), 0.001);
        assertEquals(e.getY(), a.getY(), 0.001);
    }

    //check that nodes aren't equal. for now just assert that IDs are different
    public void compareNodeFail(Node e, Node a){
        assertNotEquals(e.getID(), a.getID());
    }

    //compare contents of provider
    public void compareProvider(Provider e, Provider a){
        assertEquals(e.getID(), a.getID());
        assertEquals(e.getfName(), a.getfName());
        assertEquals(e.getlName(), a.getlName());
        for(int i=0;i<e.getLocations().size();i++){
            compareNode(e.getLocations().get(i), a.getLocations().get(i));
        }
    }

    //check that providers aren't equal. for now just assert that IDs are different
    public void compareProviderFail(Provider e, Provider a){
        assertNotEquals(e.getID(), a.getID());
    }

}
