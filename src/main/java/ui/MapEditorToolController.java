package ui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
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
    private HashMap<Node, ArrayList<Line>> neighborLines = new HashMap<Node, ArrayList<Line>>();

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
        //TODO: make editortool load all nodes without making evrything static
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
    void addNodeHere(MouseEvent e) {
        if(e.isStillSincePress())
        { if(makingNew)
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

            //on button click...
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
            newNodeButton.setText("Add a New Node");
        } else if(modifyingLocation)
        {
            if(currentButton != null && currentNode != null)
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
    public void loadNodesFromDatabase(){
        for(Node n: DatabaseController.getAllNodes()){
            loadNode(n);
            drawToNeighbors(n);
        }


    }

    /**
     * Create a button on the scene and associate it with a node
     * @param n the node to load into the scene
     */
    private void loadNode(Node n){
        //new button
        Button nodeB = new Button("+");
        //set button XY coordinates
        nodeB.setLayoutX(n.getX()-XOFFSET);
        nodeB.setLayoutY(n.getY()-YOFFSET);

        //on button click...
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
    private Node editorGenerateNewNode(String name, String type, double x, double y, int floorid){
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
     * display node details when a node's button is clicked
     * @param nodeB the button clicked. associated to a node
     */
    private void showNodeDetails(Button nodeB){
        //get node linked to this button
        Node linkedNode = nodeButtonLinks.get(nodeB);

        if (addingNeighbor) {
            currentNode.addNeighbor(linkedNode);
            if(!modifiedNodesList.contains(currentNode)){
                modifiedNodesList.add(currentNode);
            }
            addingNeighbor = false;
            drawToNeighbors(currentNode);
        } else if (removingNeighbor) {
            currentNode.removeNeighbor(linkedNode);
            if(!modifiedNodesList.contains(currentNode)){
                modifiedNodesList.add(currentNode);
            }
            removingNeighbor = false;
            drawToNeighbors(currentNode);
        } else {
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
    private void hideNodeDetails(){
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
            newNodeButton.setText("Click here to cancel new node creation");
        } else {
            makingNew = false;
            newNodeButton.setText("Add a New Node");
        }
    }

    @FXML
    /**
     * modify a node's location on the map. alternately, cancel node location modification
     */
    void modNodeLocation(ActionEvent event){
        if(!modifyingLocation)
        {
            clickModLocation.setText("Cancel");
            modifyingLocation = true;
        }
        else
        {
            clickModLocation.setText("Modify Location by Click");
            modifyingLocation = false;
        }
    }

    @FXML
    /**
     * update a node's X coordinate, both visually and in the node's properties
     */
    void updateNodeX(ActionEvent event) {
        try
        {
            currentButton.setLayoutX(Double.parseDouble(xField.getText()));
            currentNode.setX(Double.parseDouble(xField.getText()));
            //track that this node has been modified
            if(!modifiedNodesList.contains(currentNode)){
                modifiedNodesList.add(currentNode);
            }
        } catch (NumberFormatException e){
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
    void updateNodeData(ActionEvent event) {
        try
        {
            if(currentButton != null && currentNode != null)
            {
                ArrayList<String> data = new ArrayList<String>();
                data.add(nameField.getText());
                data.add(typeField.getText());
                currentNode.setData(data);
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
     * Push newly created nodes into the databasecontroller's node table.
     * TODO: nodes loaded in from the table and modified should also be pushed.
     */
    void pushChangesToDatabase(ActionEvent event) {
        for(Node n: newNodesList){
            //TODO: change types of things to be concretenode instead of node?
            //sloppy to cast like this I assume
            ConcreteNode cn = (ConcreteNode)n;
            DatabaseController.insertNode(cn);
        }
        DatabaseController.modifyNodes(modifiedNodesList);

        for(Node n: newNodesList) { // Insert neighbor relationships to database
            //Also has sloppy casting here
            ConcreteNode cn = (ConcreteNode)n;
            for(Node nn: cn.getNeighbors()) {
                DatabaseController.insertNeighbor(cn, (ConcreteNode) nn);
            }
        }

        //TODO: MAJOR NEED FOR DOCUMENTATION
        //TODO: ADD SOME SAUCE TO THIS SPAGHETTI
        DatabaseController.initializeAllNodes();
        for(Node n: modifiedNodesList) {

        //    if (n.getNeighbors() != DatabaseController.getNodeByID(n.getID()).getNeighbors()){
                Collection<Node> sourceNeighbors = DatabaseController.getNodeByID(n.getID()).getNeighbors();
                Collection<Node> modNeighbors = n.getNeighbors();
                ArrayList<Node> toDeleteSourceNeighbors = new ArrayList<Node>();
                ArrayList<Node> toDeleteModNeighbors = new ArrayList<Node>();

                for(Node nn: modNeighbors){
                    for(Node sn: sourceNeighbors){
                        if(sn.getID() == nn.getID()){
                            toDeleteModNeighbors.add(nn);
                            toDeleteSourceNeighbors.add(sn);
                        }
                    }
                }
                modNeighbors.removeAll(toDeleteModNeighbors);
                sourceNeighbors.removeAll(toDeleteSourceNeighbors);
                for(Node nn: modNeighbors){
                    DatabaseController.insertNeighbor(n.getID(), nn.getID());
                }
                for(Node sn: sourceNeighbors){
                    System.out.println(sn.getID());
                    DatabaseController.removeNeighbor(n.getID(), sn.getID());
                }
        //    }
        }
    }


    @FXML
    /**
     * Create lines from a node (source) to all of the node's neighbors
     */
    void drawToNeighbors(Node source) {
        // Remove all existing lines from source at beginning
        if (neighborLines.containsKey(source)) { // Check if source has existing lines
            ArrayList<Line> oldLines = neighborLines.get(source);
            for (Line l: oldLines) { // Removes all lines from oldLines from the UI
                ((AnchorPane)l.getParent()).getChildren().remove(l);
            }
            neighborLines.remove(source);
        }

        ArrayList<Line> lines = new ArrayList<Line>();
        Collection<Node> neighbors = source.getNeighbors();
        for(Node neighbor: neighbors) {
            Line line = new Line();
            line.setStartX(source.getX());
            line.setStartY(source.getY());
            line.setEndX(neighbor.getX());
            line.setEndY(neighbor.getY());
            editingFloor.getChildren().add(1, line);
            lines.add(line);
        }
        neighborLines.put(source, lines);
    }

    @FXML
    /**
     * Set controller to addingNeighbor state
     */
    void addNeighbor(ActionEvent event) {
        if (currentNode != null) { //Don't do anything unless a node is selected
            addingNeighbor = true;
            removingNeighbor = false;
        }
    }

    @FXML
    /**
     * Set controller to removingNeighbor state
     */
    void removeNeighbor(ActionEvent event) {
        if (currentNode != null) {
            removingNeighbor = true;
            addingNeighbor = false;
        }
    }

}
