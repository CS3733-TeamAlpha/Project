package ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import pathfinding.ConcreteNode;
import pathfinding.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MapEditorToolController extends AbstractController
{
	//Arraylist of all lines drawn from a node to its neighbors
	private HashMap<Node, ArrayList<Group>> lineGroups = new HashMap<Node, ArrayList<Group>>();

	//link buttons to node objects
	private HashMap<Button, Node> nodeButtonLinks = new HashMap<Button, Node>();

    //currently selected node and button
    private Node currentNode = null;
    private Button currentButton = null;

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

	public MapEditorToolController()
	{
		//TODO: make editortool load all nodes without making everything static
		//currently achieved by pressing a button
		// loadNodesFromDatabase();
		super();
	}

	@FXML
	public void initialize()
	{
		if(Accessibility.isHighContrast())
			floorImage.setImage(new Image(Accessibility.HIGH_CONTRAST_MAP_PATH));
		loadNodesFromDatabase();
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
	private TextField typeField; //TODO: Turn this into a dropdown menu?

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

	/**
	 * Used to either add a new node at the clicked location
	 * or move an existing node to the clicked location.
	 * Choose which event to do based on makingNew or modifyingLocation bool
	 */
	@FXML
	void addNodeHere(MouseEvent e)
	{
		if (e.isStillSincePress())
		{
			if (makingNew) //making a new button/node
			{
				Node newNode = null;
				newNode = new ConcreteNode();
				newNode.setName("newNode");
				newNode.setType(-1); //default
				newNode.setX(e.getX());
				newNode.setY(e.getY());
				newNode.setFloor(FLOORID);
				database.insertNode(newNode);
				//make a new button to associate with the node
				Button nodeB = new Button("+");
				nodeB.setLayoutX(e.getX() - XOFFSET);
				nodeB.setLayoutY(e.getY() - YOFFSET);

				//on button click show node details on the righthand panel
				nodeB.setOnAction(event -> showNodeDetails(nodeB));
				nodeButtonLinks.put(nodeB, newNode);

				//add the new button to the scene
				editingFloor.getChildren().add(1, nodeB);

				//modify bool and change button text
				makingNew = false;
				//change new node button text back to original
				newNodeButton.setText("Add a New Node");
			}
			else if (modifyingLocation) //modifying the location of an existing node
			{
				if (currentButton != null && currentNode != null) //check that a button/node is actually selected
				{
					//modify button text
					clickModLocation.setText("Modify Location by Click");
					modifyingLocation = false;

					//update button and node location
					currentButton.setLayoutX(e.getX() - XOFFSET);
					currentButton.setLayoutY(e.getY() - YOFFSET);
					currentNode.setX(e.getX());
					currentNode.setY(e.getY());

                    //redraw lines for any node that has currentNode as a neighbor
                    //store nodes that need to be redrawn in a list as a workaround
                    //for concurrentmodificationexception
                    ArrayList<Node> toRedraw = new ArrayList<Node>();
                    for(Node n: lineGroups.keySet())
                        if(n.getNeighbors().contains(currentNode))
							toRedraw.add(n);

                    for(Node n : toRedraw)
                        drawToNeighbors(n);

                    drawToNeighbors(currentNode);

					database.updateNode(currentNode);
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
		for (Node n : database.getAllNodes())
		{
			loadNode(n);
			drawToNeighbors(n);
		}
	}

	/**
	 * Create a button on the scene and associate it with a node
	 *
	 * @param n the node to load into the scene
	 */
	private void loadNode(Node n)
	{
		//new button
		Button nodeB = new Button("+");
		//set button XY coordinates
		nodeB.setLayoutX(n.getX() - XOFFSET);
		nodeB.setLayoutY(n.getY() - YOFFSET);

		//on button click display the node's details
		nodeB.setOnAction(event -> showNodeDetails(nodeB));
		nodeButtonLinks.put(nodeB, n);
		//add button to scene
		editingFloor.getChildren().add(1, nodeB);
	}

	/**
	 * display node details when a node's button is clicked.
	 * <p>
	 * This function is also used to add/remove node neighbor relations (see inner comments)
	 *
	 * @param nodeB the button clicked. associated to a node
	 */
	private void showNodeDetails(Button nodeB)
	{
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
			database.updateNode(currentNode);

			addingNeighbor = false;
			//redraw lines
			drawToNeighbors(currentNode);
		}
		else if (removingNeighbor)
		{
			//remove neighbor
			currentNode.delNeighbor(linkedNode);
			database.updateNode(currentNode);

			removingNeighbor = false;
			//redraw lines
			drawToNeighbors(currentNode);
		}
		else
		{
			//modify text fields to display node info
			nameField.setText(linkedNode.getName());//getData().get(0));
			typeField.setText(Integer.toString(linkedNode.getType()));//getData().get(1)); //TODO: Write type enum and list of associated strings
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
		if (newNodeButton.getText().equals("Add a New Node"))
		{
			makingNew = true;
			newNodeButton.setText("Cancel New Node Creation");
		}
		else
		{
			makingNew = false;
			newNodeButton.setText("Add a New Node");
		}
	}

	/**
	 * modify a node's location on the map. alternately, cancel node location modification
	 */
	@FXML
	void modNodeLocation(ActionEvent event)
	{
		if (!modifyingLocation)
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

    /**
     * update a node's X coordinate, both visually and in the node's properties
     */
	@FXML
    void updateNodeX(ActionEvent event)
    {
        try
        {
            currentButton.setLayoutX(Double.parseDouble(xField.getText()));
            currentNode.setX(Double.parseDouble(xField.getText()));
            database.updateNode(currentNode);

            //redraw lines for any node that has currentNode as a neighbor
            //store nodes that need to be redrawn in a list as a workaround
            //for concurrentmodificationexception
            ArrayList<Node> toRedraw = new ArrayList<Node>();
            for(Node n: lineGroups.keySet()){
                if(n.getNeighbors().contains(currentNode))
                    toRedraw.add(n);
            }

            for(Node n : toRedraw)
                drawToNeighbors(n);
            drawToNeighbors(currentNode);
        }
        catch (NumberFormatException e)
        {
            //TODO: need more exception handling?
            System.out.println("Not a double");
        }
    }

    /**
     * update a node's Y coordinate, both visually and in the node's properties
     */
	@FXML
    void updateNodeY(ActionEvent event) {
        try
        {
            if(currentButton != null && currentNode != null)
            {
                currentButton.setLayoutY(Double.parseDouble(yField.getText()));
                currentNode.setY(Double.parseDouble(yField.getText()));
                database.updateNode(currentNode); //TODO: meld multiple database update calls into one, that function is expensive!

                //redraw lines for any node that has currentNode as a neighbor
                //store nodes that need to be redrawn in a list as a workaround
                //for concurrentmodificationexception
                ArrayList<Node> toRedraw = new ArrayList<Node>();
                for(Node n: lineGroups.keySet()){
                    if(n.getNeighbors().contains(currentNode))
                        toRedraw.add(n);
                }

                for(Node n: toRedraw)
                    drawToNeighbors(n);
                drawToNeighbors(currentNode);
            }
        } catch (NumberFormatException e){
            //TODO: need more exception handling?
            System.out.println("Not a double");
        }
    }

	/**
	 * update a node's Name string
	 */
	@FXML
	void updateNodeData(ActionEvent event)
	{
		try
		{
			if (currentButton != null && currentNode != null)
			{
				currentNode.setName(nameField.getText());
				database.updateNode(currentNode);
			}
		} catch (NumberFormatException e)
		{
			//TODO: need more exception handling?
			System.out.println("Not a double");
		}
	}

	/**
	 * Create lines from a node (source) to all of the node's neighbors.
	 *
	 * This will need to be called many times to properly and fully update drawn lines
	 * because this only updates lines coming from a Node.
	 */
	@FXML
	void drawToNeighbors(Node source)
	{
		// Remove all existing lines coming from source Node
		// Check if source has existing lines
		if (lineGroups.containsKey(source))
		{
			ArrayList<Group> oldGroups = lineGroups.get(source);
			for (Group g : oldGroups)
				// Removes all lines from oldLines from the UI
				((AnchorPane) g.getParent()).getChildren().remove(g);
			lineGroups.remove(source);
		}

		//keep track of lines created for this node
		ArrayList<Group> groups = new ArrayList<Group>();
		Collection<Node> neighbors = source.getNeighbors();

		//for each neighbor associated with source Node, draw a line.
		for (Node neighbor : neighbors)
		{
			Line line = new Line();
			line.setStartX(source.getX());
			line.setStartY(source.getY());
			line.setEndX(neighbor.getX());
			line.setEndY(neighbor.getY());

			//some shape creation magic for making arrow lines

			//get difference in xy position
			double diffY = neighbor.getY() - source.getY();
			double diffX = neighbor.getX() - source.getX();

			//we will use pathShift to place the line's arrow somewhere in the middle of the line.
			//this number should be <1. for example, a value of 0.5 should put the arrow in the middle of the line.
			double pathShift = 0.8;

			//make a new triangle
			Polygon arrowTriangle = new Polygon();
			arrowTriangle.getPoints().addAll(0.0, 0.0,
					-10.0, 10.0,
					10.0, 10.0);

			//get angle the angle of the line we'll be making in degrees
			double slopeAngle = Math.toDegrees(Math.atan2(diffY, diffX));
			//reotate the triangle we made
			//simple correction rotation angle
			double correction = 90;
			arrowTriangle.getTransforms().add(new Rotate(slopeAngle + correction, 0, 0));
			//position the triangle onto the line
			arrowTriangle.setLayoutX(source.getX() + pathShift * diffX);
			arrowTriangle.setLayoutY(source.getY() + pathShift * diffY);

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

	/**
	 * Set controller to addingNeighbor state
	 */
	@FXML
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

	/**
	 * Set controller to removingNeighbor state
	 */
	@FXML
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

	/**
	 *  Go back to startup screen
	 */
	@FXML
	void goBack()
	{
		Main.loadFXML("/fxml/Startup.fxml");
	}

    /**
     * Add the current node to the deleteNodesList to be deleted from the database
     */
	@FXML
    void deleteNode(ActionEvent event) {
        if(currentNode != null)
        {
            //if this node had any neighbors remove lines
            if (lineGroups.containsKey(currentNode))
            {
                for (Group g : lineGroups.get(currentNode))
                    ((AnchorPane) g.getParent()).getChildren().remove(g); //Removes all lines going from the deleted node
                lineGroups.remove(currentNode); //Remove current Node from lineGroups if it is inside lineGroups
            }

            //Delete arrows pointing to the deleted node
            for (Node n : currentNode.getNeighbors()){
            	//Ugh, say goodbye to the beautiful cascade delete on edges in the database...
				n.delNeighbor(currentNode);
				drawToNeighbors(n);
			}

			database.deleteNodeByUUID(currentNode.getID());
        	((AnchorPane)currentButton.getParent()).getChildren().remove(currentButton);
        	//hide details view
        	hideNodeDetails();
        }
    }
}
