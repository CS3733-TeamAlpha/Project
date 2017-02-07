package ui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import data.*;
import pathfinding.*;

import java.util.ArrayList;
import java.util.HashMap;

public class MapEditorToolController
{
    //link buttons to node objects
    private HashMap<Button, Node> nodeButtonLinks = new HashMap<Button, Node>();
    //arraylist of newly created nodes
    private ArrayList<Node> newNodesList = new ArrayList<Node>();
    private Node currentNode = null;
    private Button currentButton = null;
    private ArrayList<Node> modifiedNodesList = new ArrayList<Node>();
    private static int XOFFSET = 12; // to be used to modify button placement
    private static int YOFFSET = 12;

    static
    {
        DatabaseController.createConnection();
        DatabaseController.initializeAllFloors();
        DatabaseController.initializeAllNodes();
    }

    private boolean makingNew = false;
    private boolean modifyingLocation = false;
    private boolean showingDetails = false;

    private int FLOORID = 3; //Default floor id for minimal application

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
    void addNodeHere(MouseEvent e) {
        if(e.isStillSincePress())
        { if(makingNew)
        {
            Node newNode = null;
            if (newNodesList.size() == 0)
            {
                newNode = DatabaseController.generateNewNode("newNode", "default", e.getX(), e.getY(), FLOORID);
            } else
            {
                newNode = editorGenerateNewNode("newNode", "default", e.getX(), e.getY(), FLOORID);
            }
            newNodesList.add(newNode);
            Button nodeB = new Button("+");
            nodeB.setLayoutX(e.getX() - XOFFSET);
            nodeB.setLayoutY(e.getY() - YOFFSET);
            nodeB.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent event)
                {
                    showNodeDetails(nodeB);
                }
            });
            nodeButtonLinks.put(nodeB, newNode);
            editingFloor.getChildren().add(1, nodeB);
            makingNew = false;
            newNodeButton.setText("Add a New Node");
        } else if(modifyingLocation)
        {
            clickModLocation.setText("Modify Location by Click");
            modifyingLocation = false;
            currentButton.setLayoutX(e.getX() - XOFFSET);
            currentButton.setLayoutY(e.getY() - YOFFSET);
            currentNode.setX(e.getX());
            currentNode.setY(e.getY());
        }
        else
        {
            hideNodeDetails();
        }
        }
    }


    public void loadNodesFromDatabase(){
        for(Node n: DatabaseController.getAllNodes()){
            loadNode(n);
        }
    }

    private void loadNode(Node n){
        Button nodeB = new Button("+");
        nodeB.setLayoutX(n.getX()-XOFFSET);
        nodeB.setLayoutY(n.getY()-YOFFSET);
        nodeB.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showNodeDetails(nodeB);
            }
        });
        nodeButtonLinks.put(nodeB, n);
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
        int newID = newNodesList.get(newNodesList.size()-1).getID()+1;
        ArrayList<String> data = new ArrayList<String>();
        data.add(name);
        data.add(type);
        Node newNode = new ConcreteNode(newID, data, x, y, DatabaseController.getFloorByID(floorid));
        return newNode;
    }

    private void showNodeDetails(Button nodeB){
        Node linkedNode = nodeButtonLinks.get(nodeB);
        nameField.setText(linkedNode.getData().get(0));
        typeField.setText(linkedNode.getData().get(1));
        xField.setText(Double.toString(linkedNode.getX()));
        yField.setText(Double.toString(linkedNode.getY()));
        currentNode = linkedNode;
        //TODO: make it so that we can visually see which node is selected
        currentButton = nodeB;
    }

    private void hideNodeDetails(){
        currentNode = null;

        nameField.setText("");
        typeField.setText("");
        xField.setText("");
        yField.setText("");
    }


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
    void updateNodeX(ActionEvent event) {
        try
        {
            currentButton.setLayoutX(Double.parseDouble(xField.getText()));
            currentNode.setX(Double.parseDouble(xField.getText()));
        } catch (NumberFormatException e){
            //TODO: need more exception handling?
            System.out.println("Not a double");
        }
    }

    @FXML
    void updateNodeY(ActionEvent event) {
        try
        {
            if(currentButton != null && currentNode != null)
            {
                currentButton.setLayoutY(Double.parseDouble(yField.getText()));
                currentNode.setY(Double.parseDouble(yField.getText()));
            }
        } catch (NumberFormatException e){
            //TODO: need more exception handling?
            System.out.println("Not a double");
        }
    }


    @FXML
    void pushChangesToDatabase(ActionEvent event) {
        for(Node n: newNodesList){
            //TODO: change types of things to be concretenode instead of node?
            //sloppy to cast like this I assume
            ConcreteNode cn = (ConcreteNode)n;
            DatabaseController.insertNode(cn);
        }
    }

}
