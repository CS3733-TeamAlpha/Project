package ui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import data.*;
import javafx.stage.WindowEvent;
import pathfinding.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.ResourceBundle;

import static java.awt.SystemColor.window;

public class MapEditorToolController
{
    //Arraylist of all lines drawn from a node to its neighbors
    private HashMap<Node, ArrayList<Group>> lineGroups = new HashMap<Node, ArrayList<Group>>();

    //link buttons to node objects
    private HashMap<Button, Node> nodeButtonLinks = new HashMap<Button, Node>();

    //arraylist of new nodes
    //IMPORTANT: this list is distinct from the nodes in DatabaseController.nodeList
    private ArrayList<Node> newNodesList = new ArrayList<Node>();

    //currently selected node and button
    private Node currentNode = null;
    private Button currentButton = null;

    //arraylist of nodes loaded by DatabaseController that have been modified
    //we will use this array to check for any updates that need to be made
    // to the database (DatabaseController.modifyXTable)
    private ArrayList<Node> modifiedNodesList = new ArrayList<Node>();

    //X and Y offsets, for button placement.
    //TODO: fine tune offsets to make button placement visuals better
    private static int XOFFSET = 12;
    private static int YOFFSET = 12;

    private boolean addingNeighbor = false; //currently adding a neighbor?
    private boolean removingNeighbor = false; //currently removing a neighbor?
    private boolean makingNew = false; //currently making a new node?
    private boolean modifyingLocation = false; //currently moving a node?
    private boolean showingDetails = false;

    private int FLOORID = 3; //Default floor id for minimal application


    static
    {
        //initialize connection and floor/node/provider lists right away
        //DatabaseController will hold onto these lists
        DatabaseController.createConnection();
        DatabaseController.initializeAllFloors();
        DatabaseController.initializeAllNodes();
        DatabaseController.initializeAllProviders();
    }

    public MapEditorToolController(){}
    {
        //TODO: make editortool load all nodes without making everything static
        //currently acheived by pressing a button
       // loadNodesFromDatabase();
    }


    @FXML
    private AnchorPane editingFloor;

    @FXML
    private ImageView floorImage;

    @FXML
    private Button newNodeButton;

    @FXML
    private TextField nameField;

    @FXML
    private TextField typeField;

    @FXML
    private TextField xField;

    @FXML
    private TextField yField;

    @FXML
    private Button pushChanges;

    @FXML
    private Button clickModLocation;

    @FXML
    private Button addConnectionButton;

    @FXML
    private Button removeConnectionButton;



    @FXML
    /**
     * Function used to either add a new node at the clicked location
     * or move an existing node to the clicked location.
     * Choose which event to do based on makingNew or modifyingLocation bool
     */
    void addNodeHere(MouseEvent e)
    {
        if(e.isStillSincePress())
        {
            if(makingNew) //making a new button/node
                {
                Node newNode = null;
                if (newNodesList.size() == 0)
                {
                    //if we haven't made any new nodes yet, call databasecontroller so that
                    //we have a baseline nodeID to start with.
                    //This nodeID is going the be the greatest int nodeID in the existing nodes
                    //IMPORTANT: this doesn't actually add the new node to the database tables
                    newNode = DatabaseController.generateNewNode("newNode", "default", e.getX(), e.getY(), FLOORID);
                } else
                {
                    //otherwise use our own function to generate a new node.
                    newNode = editorGenerateNewNode("newNode", "default", e.getX(), e.getY(), FLOORID);
                }
                newNodesList.add(newNode);
                //make a new button to associate with the node
                Button nodeB = new Button("+");
                nodeB.setLayoutX(e.getX() - XOFFSET);
                nodeB.setLayoutY(e.getY() - YOFFSET);

                //on button click show node details on the righthand panel
                nodeB.setOnAction(new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        showNodeDetails(nodeB);
                    }
                });
                nodeButtonLinks.put(nodeB, newNode);

                //add the new button to the scene
                editingFloor.getChildren().add(1, nodeB);

                //modify bool and change button text
                makingNew = false;
                //change new node button text back to original
                newNodeButton.setText("Add a New Node");
            }
            else if(modifyingLocation) //modifying the location of an existing node
            {
                if(currentButton != null && currentNode != null) //check that a button/node is actually selected
                {
                    //modify button text
                    clickModLocation.setText("Modify Location by Click");
                    modifyingLocation = false;

                    //update button and node location
                    currentButton.setLayoutX(e.getX() - XOFFSET);
                    currentButton.setLayoutY(e.getY() - YOFFSET);
                    currentNode.setX(e.getX());
                    currentNode.setY(e.getY());

                    //TODO: implement some tracking that this node has been modified, if it is a node
                    //TODO: loaded in from the databsecontroller
                } else {
                    hideNodeDetails();
                    modifyingLocation = false;
                }
            }
            else
            {
                //if not making or moving nodes, hide node details.
                hideNodeDetails();
            }
        }
        addingNeighbor = false;
        removingNeighbor = false;

    }


    /**
     * Load all nodes from the databasecontroller's list of nodes onto our scene
     */
    public void loadNodesFromDatabase()
    {
        for(Node n: DatabaseController.getAllNodes())
        {
            loadNode(n);
            drawToNeighbors(n);
        }
    }

    /**
     * Create a button on the scene and associate it with a node
     * @param n the node to load into the scene
     */
    private void loadNode(Node n)
    {
        //new button
        Button nodeB = new Button("+");
        //set button XY coordinates
        nodeB.setLayoutX(n.getX()-XOFFSET);
        nodeB.setLayoutY(n.getY()-YOFFSET);

        //on button click display the node's details
        nodeB.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showNodeDetails(nodeB);
            }
        });
        nodeButtonLinks.put(nodeB, n);
        //add button to scene
        editingFloor.getChildren().add(1, nodeB);
    }

    /**
     * The editortool can generate its own new nodes once we have a
     * baseline nodeID to start counting up from
     * @param name node name, placeholder
     * @param type node type, placeholder
     * @param x node x coordinate
     * @param y node y coordinate
     * @param floorid id of floor this node is on
     * @return a new Node object
     */
    private Node editorGenerateNewNode(String name, String type, double x, double y, int floorid)
    {
        //add 1 to greatest node ID value, which should be last item in the list
        int newID = newNodesList.get(newNodesList.size()-1).getID()+1;

        //initialize data
        ArrayList<String> data = new ArrayList<String>();
        data.add(name);
        data.add(type);
        //create new concrete node
        Node newNode = new ConcreteNode(newID, data, x, y, DatabaseController.getFloorByID(floorid));
        return newNode;
    }

    /**
     * display node details when a node's button is clicked.
     *
     * This function is also used to add/remove node neighbor relations (see inner comments)
     * @param nodeB the button clicked. associated to a node
     */
    private void showNodeDetails(Button nodeB){
        //get node linked to this button
        Node linkedNode = nodeButtonLinks.get(nodeB);

        /**
         * IMPORTANT: showNodeDetails is called by node buttons when clicked.
         * To add/remove a neighbor, we need to click a node button.
         * Therefore at the beginning of this function we check whether the
         * addingNeighbor or removingNeighbor booleans have been tagged true, and if so
         * we add/remove the currently clicked node from the original node's neighborlist,
         * and then update the lines by calling DrawToNeighbors.
         */
        if (addingNeighbor)
        {
            //add neighbor
            currentNode.addNeighbor(linkedNode);
            //add currentNode (not the node that has just been clicked) to the modifiedlist
            if(!modifiedNodesList.contains(currentNode)){
                modifiedNodesList.add(currentNode);
            }
            addingNeighbor = false;
            //redraw lines
            drawToNeighbors(currentNode);
        }
        else if (removingNeighbor)
        {
            //remove neighbor
            currentNode.removeNeighbor(linkedNode);
            //add currentNode (not the node that has just been clicked) to the modifiedlist
            if(!modifiedNodesList.contains(currentNode)){
                modifiedNodesList.add(currentNode);
            }
            removingNeighbor = false;
            //redraw lines
            drawToNeighbors(currentNode);
        }
        else
        {
            //modify text fields to display node info
            nameField.setText(linkedNode.getData().get(0));
            typeField.setText(linkedNode.getData().get(1));
            xField.setText(Double.toString(linkedNode.getX()));
            yField.setText(Double.toString(linkedNode.getY()));

            //set current node/button
            currentNode = linkedNode;
            //TODO: make it so that we can visually see which node is selected
            currentButton = nodeB;
        }

    }

    /**
     * hide node details sidebar and deselect current node/button
     */
    private void hideNodeDetails()
    {
        currentNode = null;
        currentButton = null;

        nameField.setText("");
        typeField.setText("");
        xField.setText("");
        yField.setText("");
    }

    /**
     * Create a new node on the map. alternatively, cancel new node creation
     */
    public void createNewNode()
    {
        if(newNodeButton.getText().equals("Add a New Node"))
        {
            makingNew = true;
            newNodeButton.setText("Cancel new node creation");
        } else {
            makingNew = false;
            newNodeButton.setText("Add a New Node");
        }
    }

    @FXML
    /**
     * modify a node's location on the map. alternately, cancel node location modification
     */
    void modNodeLocation(ActionEvent event)
    {
        if(!modifyingLocation)
        {
            clickModLocation.setText("Cancel");
            //set booleans to track which state we are in right now
            modifyingLocation = true;
            addingNeighbor = false;
            removingNeighbor = false;
            makingNew = false;
        }
        else
        {
            clickModLocation.setText("Modify Location by Click");
            //set booleans to track which state we are in right now
            modifyingLocation = false;
            addingNeighbor = false;
            removingNeighbor = false;
            makingNew = false;
        }
    }

    @FXML
    /**
     * update a node's X coordinate, both visually and in the node's properties
     */
    void updateNodeX(ActionEvent event)
    {
        try
        {
            currentButton.setLayoutX(Double.parseDouble(xField.getText()));
            currentNode.setX(Double.parseDouble(xField.getText()));
            //track that this node has been modified
            if(!modifiedNodesList.contains(currentNode)){
                modifiedNodesList.add(currentNode);
            }
        }
        catch (NumberFormatException e)
        {
            //TODO: need more exception handling?
            System.out.println("Not a double");
        }
    }

    @FXML
    /**
     * update a node's Y coordinate, both visually and in the node's properties
     */
    void updateNodeY(ActionEvent event) {
        try
        {
            if(currentButton != null && currentNode != null)
            {
                currentButton.setLayoutY(Double.parseDouble(yField.getText()));
                currentNode.setY(Double.parseDouble(yField.getText()));
                //track that this node has been modified
                if(!modifiedNodesList.contains(currentNode)){
                    modifiedNodesList.add(currentNode);
                }
            }
        } catch (NumberFormatException e){
            //TODO: need more exception handling?
            System.out.println("Not a double");
        }
    }

    @FXML
    /**
     * update a node's Name string
     */
    void updateNodeData(ActionEvent event)
    {
        try
        {
            if(currentButton != null && currentNode != null)
            {
                ArrayList<String> data = new ArrayList<String>();
                data.add(nameField.getText());
                data.add(typeField.getText());
                currentNode.setData(data);
                //track that this node has been modified
                if(!modifiedNodesList.contains(currentNode))
                {
                    modifiedNodesList.add(currentNode);
                }
            }
        }
        catch (NumberFormatException e)
        {
            //TODO: need more exception handling?
            System.out.println("Not a double");
        }
    }


    @FXML
    /**
     * Push newly created nodes into the databasecontroller's node table.
     *
     * This function will:
     *  - insert all newly created nodes into the database
     *  - update all modified nodes that were loaded in from the database
     *  - insert all new neighbor relations that have been created
     *  - delete all neighbor relations that have been deleted
     * TODO: nodes loaded in from the table and modified should also be pushed.
     */
    void pushChangesToDatabase(ActionEvent event)
    {
        for(Node n: newNodesList)
        {
            //TODO: change types of things to be concretenode instead of node?
            //sloppy to cast like this I assume
            ConcreteNode newNode = (ConcreteNode)n;
            DatabaseController.insertNode(newNode);
        }

        //modifyNodes will update Node tables for all nodes in the list
        DatabaseController.modifyNodes(modifiedNodesList);

        //for each newly created node, insert it's neighbor relations into the table
        for(Node n: newNodesList)
        {
            //Also has sloppy casting here
            ConcreteNode newNode = (ConcreteNode)n;
            for(Node neighborNode: newNode.getNeighbors()) {
                //neighbor relation from newNode to neighborNode
                DatabaseController.insertNeighbor(newNode, (ConcreteNode) neighborNode);
            }
        }

        //TODO: ADD SOME SAUCE TO THIS SPAGHETTI
        //reinitialize all nodes in the databasecontroller
        //We are doing this because the nodes in modifiedNodesList reference the objects
        //in databasecontroller.nodeList. This means that changes we made in the editor also have
        //been changed in databasecontroller's nodeList. nodeList stores objects, but those objects
        //don't directly affect the database's tables.

        //THEREFORE: we reinitialize all nodes so that we have an accurate and updated picture
        //of what nodes and neighbors are currently in the database
        DatabaseController.initializeAllNodes();

        //for each node that has been modified
        for(Node n: modifiedNodesList)
        {
            //initialize a collection of all neighbors for node N that the database knows about
            Collection<Node> sourceNeighbors = DatabaseController.getNodeByID(n.getID()).getNeighbors();
            //get neighbors from node N as the editortool knows about
            Collection<Node> modNeighbors = n.getNeighbors();

            //arrays of nodes that will be used to store which nodes are common to both sourceNeighbors and modNeighbors
            ArrayList<Node> toDeleteSourceNeighbors = new ArrayList<Node>();
            ArrayList<Node> toDeleteModNeighbors = new ArrayList<Node>();

            //for each neighbor editor knows about
            for(Node modNeighbor: modNeighbors)
            {
                //for each neighbor database knows about
                for(Node sourceNeighbor: sourceNeighbors)
                {
                    //if ID is same, they are the same node.
                    if(sourceNeighbor.getID() == modNeighbor.getID())
                    {
                        //store the node in toDelete arrays, to indicate both source and mod
                        //know about this neighbor relationship
                        toDeleteModNeighbors.add(modNeighbor);
                        toDeleteSourceNeighbors.add(sourceNeighbor);
                    }
                }
            }

            //remove all nodes that are common to mod and source.
            //this will leave just the neighbor nodes that are different.
            //any neighbor nodes in modNeighbors but not in source are newly created neighbors.
            //any neighbor nodes in sourceNeighbors but not in mod are old neighbors that have been deleted.
            modNeighbors.removeAll(toDeleteModNeighbors);
            sourceNeighbors.removeAll(toDeleteSourceNeighbors);

            //insert new neighbor relations
            for(Node modNeighbor: modNeighbors)
            {
                 DatabaseController.insertNeighbor(n.getID(), modNeighbor.getID());
            }
            //remove all neighbor relations that have been removed by the editor
            for(Node sourceNeighbor: sourceNeighbors)
            {
                DatabaseController.removeNeighbor(n.getID(), sourceNeighbor.getID());
            }
        }
    }


    @FXML
    /**
     * Create lines from a node (source) to all of the node's neighbors.
     *
     * This will need to be called many times to properly and fully update drawn lines
     * because this only updates lines coming from a Node.
     */
    void drawToNeighbors(Node source) {

        // Remove all existing lines coming from source Node
        // Check if source has existing lines
        if (lineGroups.containsKey(source))
        {
            ArrayList<Group> oldGroups = lineGroups.get(source);
            for (Group g: oldGroups)
            {
                // Removes all lines from oldLines from the UI
                ((AnchorPane)g.getParent()).getChildren().remove(g);
            }
            lineGroups.remove(source);
        }

        //keep track of lines created for this node
        ArrayList<Group> groups = new ArrayList<Group>();
        Collection<Node> neighbors = source.getNeighbors();

        //for each neighbor associated with source Node, draw a line.
        for(Node neighbor: neighbors) {
            Line line = new Line();
            line.setStartX(source.getX());
            line.setStartY(source.getY());
            line.setEndX(neighbor.getX());
            line.setEndY(neighbor.getY());

            //some shape creation magic for making arrow lines

            //get difference in xy position
            double diffY = neighbor.getY()-source.getY();
            double diffX = neighbor.getX()-source.getX();


            //we will use pathShift to place the line's arrow somewhere in the middle of the line.
            //this number should be <1. for example, a value of 0.5 should put the arrow in the middle of the line.
            double pathShift = 0.8;

            //make a new triangle
            Polygon arrowTriangle = new Polygon();
            arrowTriangle.getPoints().addAll(new Double[]{
                    0.0, 0.0,
                    -10.0, 10.0,
                    10.0, 10.0
            });

            //get angle the angle of the line we'll be making in degrees
            double slopeAngle = Math.toDegrees(Math.atan2(diffY, diffX));
            //reotate the triangle we made
            //simple correction rotation angle
            double correction = 90;
            arrowTriangle.getTransforms().add(new Rotate(slopeAngle+correction, 0, 0));
            //position the triangle onto the line
            arrowTriangle.setLayoutX(source.getX()+pathShift*diffX);
            arrowTriangle.setLayoutY(source.getY()+pathShift*diffY);

            //group together the line and triangle for future referencing
            Group g = new Group();
            g.getChildren().add(line);
            g.getChildren().add(arrowTriangle);
            editingFloor.getChildren().add(1, g);
            //add this line into the lines array
            groups.add(g);
        }
        //store the array of lines for this source node into the hashmap
        //to be used to delete all lines later when redrawing
        lineGroups.put(source, groups);
    }

    @FXML
    /**
     * Set controller to addingNeighbor state
     */
    void addNeighbor(ActionEvent event)
    {
        if (currentNode != null)//Don't do anything unless a node is selected
        {
            //set boolean to track state of editor
            addingNeighbor = true;
            removingNeighbor = false;
            modifyingLocation = false;
            makingNew = false;
        }
    }

    @FXML
    /**
     * Set controller to removingNeighbor state
     */
    void removeNeighbor(ActionEvent event)
    {
        if (currentNode != null)  //Don't do anything unless a node is selected
        {
            //set boolean to track state of editor
            removingNeighbor = true;
            addingNeighbor = false;
            modifyingLocation = false;
            makingNew = false;
        }
    }

    @FXML
    /**
     *  Go back to startup screen
     */
    void goBack(){
        Main.loadFXML("/fxml/Startup.fxml");
    }

}
