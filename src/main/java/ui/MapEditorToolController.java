package ui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
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

    //arraylist of nodes to be deleted
    private ArrayList<Node> deleteNodesList = new ArrayList<Node>();

    //currently selected node and button
    private Node currentNode = null;
    private Button currentButton = null;

	//arraylist of nodes loaded by DatabaseController that have been modified
	//we will use this array to check for any updates that need to be made
	// to the database (DatabaseController.modifyXTable)
	private ArrayList<Node> modifiedNodesList = new ArrayList<Node>();

	//constants to be used for drawing radial contextmenu
	double CONTEXTWIDTH = 60.0;
	double CONTEXTRAD = 90.0;
	Group CONTEXTMENU = new Group();
	Arc SELECTIONWEDGE = new Arc();


	//define widths for circles/lines that the canvas will draw
	double CIRCLEWIDTH = 13.0;
	double LINEWIDTH = 2.5;

	//X and Y offsets, for button placement.
	//TODO: fine tune offsets to make button placement visuals better
	private double XOFFSET = CIRCLEWIDTH/2;
	private double YOFFSET = CIRCLEWIDTH/2;


	//enums to indicate current state
	private enum editorStates {
		DOINGNOTHING,
		MAKINGNEWHALLWAY,  //making hallway type node
		MAKINGNEWOFFICE,   //making office type node
		MAKINGNEWELEVATOR, //making elevator type node
		MAKINGNEWRESTROOM, //making restroom type node
		ADDINGNEIGHBOR,    //currently adding neighbor
		REMOVINGNEIGHBOR,  //currently removing neighbor
		MOVINGNODE,        //currently moving node
		SHOWINGEMPTYMENU,  //radial contextmenu, not on node
		SHOWINGNODEMENU    //radial contextmenu for node options
	}
	//store current state
	private editorStates currentState = editorStates.DOINGNOTHING;

	//contextSelection will store which area of the context menu the mouse is hovered over
	//going from 0 to 3 starting from the right, clockwise.
	//We store this independently since we will take different actions based on mouse position
	//depending on whether we are in the context menu for nodes or not
	private int contextSelection = -1; //default to -1, no context menu option selected


	private int FLOORID = 3; //Default floor id for minimal application
	private int currentFloor = 3; //TODO: make current floor relevant to new/modifying nodes
	//TODO: should nodes be able to be moved to different floor? probably not

	//canvas and graphicscontext, to draw onto the scene
	private Canvas canvas;
	private GraphicsContext gc;

	static
	{
		//initialize connection and floor/node/provider lists right away
		//DatabaseController will hold onto these lists
	}

	public MapEditorToolController()
	{
		//TODO: make editortool load all nodes without making everything static
		//currently acheived by pressing a button
		// loadNodesFromDatabase();
	}

	@FXML
	public void initialize()
	{
		if(Accessibility.isHighContrast())
		{
			floorImage.setImage(new Image(Accessibility.HIGH_CONTRAST_MAP_PATH));
		}
		DatabaseController.createConnection();
		DatabaseController.initializeAllFloors();
		DatabaseController.initializeAllNodes();
		DatabaseController.initializeAllProviders();
		loadNodesFromDatabase();

		//TODO: Load in image for a specific floor and determine canvas size
		//		based on image size

		Group root = new Group();
		canvas = new Canvas(2200, 1300);
		canvas.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				clickFloorImage(e);
			}
		});
		gc = canvas.getGraphicsContext2D();
		editingFloor.getChildren().add(1, canvas);

		//TODO: setup node context menu?
		//TODO: node contextmenu options should different depending on type of node?
	}

	/**
	 * Set up all initial settings for the context menu.
	 * XY position will be initialized based on mouse position
	 * @param x x coord for center of the radial context menu
	 * @param y y coord same as above
	 */
	private void setupContextMenu(double x, double y)
	{
		CONTEXTMENU.getChildren().clear();
		//set xy to compare with mouse position later
		CONTEXTMENU.setLayoutX(x);
		CONTEXTMENU.setLayoutY(y);

		Arc radialMenu = new Arc(0, 0, CONTEXTRAD, CONTEXTRAD, 0, 360);
		radialMenu.setType(ArcType.OPEN);
		radialMenu.setStrokeWidth(CONTEXTWIDTH);
		radialMenu.setStroke(Color.GRAY);
		radialMenu.setStrokeType(StrokeType.INSIDE);
		radialMenu.setFill(null);
		radialMenu.setOpacity(0.8);

		//arc wedge will be used to indicate current selection
		SELECTIONWEDGE = new Arc(0, 0, CONTEXTRAD, CONTEXTRAD, 0, 0);
		SELECTIONWEDGE.setType(ArcType.ROUND);
		SELECTIONWEDGE.setStrokeWidth(CONTEXTWIDTH);
		SELECTIONWEDGE.setStroke(Color.BLUE);
		SELECTIONWEDGE.setStrokeType(StrokeType.INSIDE);
		SELECTIONWEDGE.setFill(null);
		SELECTIONWEDGE.setOpacity(0.9);

		//draw four lines for each section of the menu
		Line split1 = new Line((CONTEXTRAD/Math.sqrt(2))-(CONTEXTWIDTH/Math.sqrt(2)),
				(CONTEXTRAD/Math.sqrt(2))-(CONTEXTWIDTH/Math.sqrt(2)),
				(CONTEXTRAD/Math.sqrt(2)), (CONTEXTRAD/Math.sqrt(2)));
		split1.setStrokeWidth(2);
		Line split2 = new Line((CONTEXTRAD/Math.sqrt(2))-(CONTEXTWIDTH/Math.sqrt(2)),
				-(CONTEXTRAD/Math.sqrt(2))+(CONTEXTWIDTH/Math.sqrt(2)),
				(CONTEXTRAD/Math.sqrt(2)), -(CONTEXTRAD/Math.sqrt(2)));
		split2.setStrokeWidth(2);
		Line split3 = new Line(-(CONTEXTRAD/Math.sqrt(2))+(CONTEXTWIDTH/Math.sqrt(2)),
				(CONTEXTRAD/Math.sqrt(2))-(CONTEXTWIDTH/Math.sqrt(2)),
				-(CONTEXTRAD/Math.sqrt(2)), (CONTEXTRAD/Math.sqrt(2)));
		split3.setStrokeWidth(2);
		Line split4 = new Line(-(CONTEXTRAD/Math.sqrt(2))+(CONTEXTWIDTH/Math.sqrt(2)),
				-(CONTEXTRAD/Math.sqrt(2))+(CONTEXTWIDTH/Math.sqrt(2)),
				-(CONTEXTRAD/Math.sqrt(2)), -(CONTEXTRAD/Math.sqrt(2)));
		split4.setStrokeWidth(2);

		//TODO: image for a node instead of circle?
		//Radius of circle is 13px as per css.
		Circle newHallway = new Circle(CONTEXTRAD-CONTEXTWIDTH/2, 0, 13);
		//TODO: decide on new node color?
		newHallway.setFill(Color.GREENYELLOW);

		//TODO: replace with provider image
		//Radius of circle is 13px as per css.
		Circle newOffice = new Circle(0, CONTEXTRAD-CONTEXTWIDTH/2, 13);
		newOffice.setFill(Color.RED);

		//TODO: replace with Elevator image
		//Radius of circle is 13px as per css.
		Circle newElevator = new Circle(0, -CONTEXTRAD+CONTEXTWIDTH/2, 13);
		newElevator.setFill(Color.WHITE);

		//TODO: replace with restroom image
		//Radius of circle is 13px as per css.
		Circle newRestroom = new Circle(-CONTEXTRAD+CONTEXTWIDTH/2, 0, 13);
		newRestroom.setFill(Color.BLUE);

		//TODO: different icons for node context menu

		//add all elements to the CONTEXTMENU group
		CONTEXTMENU.getChildren().add(radialMenu);
		CONTEXTMENU.getChildren().add(SELECTIONWEDGE);
		CONTEXTMENU.getChildren().add(split1);
		CONTEXTMENU.getChildren().add(split2);
		CONTEXTMENU.getChildren().add(split3);
		CONTEXTMENU.getChildren().add(split4);
		//TODO: check whether this is node contextmenu or not to decide what to add
		CONTEXTMENU.getChildren().add(newHallway);
		CONTEXTMENU.getChildren().add(newElevator);
		CONTEXTMENU.getChildren().add(newRestroom);
		CONTEXTMENU.getChildren().add(newOffice);
	}

	@FXML
	private ScrollPane mainScroll;

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
	void clickFloorImage(MouseEvent e)
	{
		if (e.isStillSincePress())
		{
			//TODO: all of these should be occuring on drop
			//TODO: move this shit into a drop function, not click
			switch(currentState)
			{
				case MAKINGNEWHALLWAY:
					//create a new hallway node
					createNewNode(e.getX(), e.getY(), "Hallway");
					break;
				case MAKINGNEWOFFICE:
					//create a new office node
					createNewNode(e.getX(), e.getY(), "Office");
					break;
				case MAKINGNEWELEVATOR:
					//create a new elevator node
					createNewNode(e.getX(), e.getY(), "Elevator");
					break;
				case MAKINGNEWRESTROOM:
					//create a new restroom node
					createNewNode(e.getX(), e.getY(), "Restroom");
					break;
				case MOVINGNODE:
					//modifying the location of an existing node
					if (currentButton != null && currentNode != null) //check that a button/node is actually selected
					{
						//modify button text
						clickModLocation.setText("Modify Location by Click");

						//update button and node location
						currentButton.setLayoutX(e.getX() - XOFFSET);
						currentButton.setLayoutY(e.getY() - YOFFSET);
						currentNode.setX(e.getX());
						currentNode.setY(e.getY());

						//redraw lines for any node that has currentNode as a neighbor

						//store nodes that need to be redrawn in a list as a workaround
						//for concurrentmodificationexception
						ArrayList<Node> toRedraw = new ArrayList<Node>();
						for (Node n : lineGroups.keySet())
						{
							boolean has = false;
							if (n.getNeighbors().contains(currentNode))
							{
								has = true;
							}
							if (has)
							{
								toRedraw.add(n);
							}
						}

						//redraw all lines from other nodes to the modified node
						for (Node n : toRedraw)
						{
							drawToNeighbors(n);
						}

						//redraw all lines from this node
						drawToNeighbors(currentNode);

						//indicate this node has been modified
						if (!modifiedNodesList.contains(currentNode))
						{
							modifiedNodesList.add(currentNode);
						}
					}
					//if for some reason no node/button is selected, hide node details
					else
					{
						//TODO: Can we ever run into this else?
						hideNodeDetails();
					}
					break;
				default:
					//default case, hide node details
					hideNodeDetails();
					break;
			}

			//set currentState to -1 to indicate we are no longer in special state
			currentState = editorStates.DOINGNOTHING;
		}
		else
		{
		}
	}

	//TODO: allow specific node creation for each kind of node
	/**
	 * Create a new node at a mouse location
	 * @param e The mouse event that will be used to determine location
	 */
	public void createNewNode(MouseEvent e){
		createNewNode(e.getX(), e.getY());
	}

	//TODO: THIS IS A PLACEHOLDER FUNCTION IN PLACE UNTIL DATABASE IS UPDATED
	//later all new node creations will have a type associated (which may be an int rather than string)
	//so having this workaround function will become unnecessary
	public void createNewNode(double x, double y){
		createNewNode(x, y, "");
	}

	//TODO: allow specific node creation for each kind of node
	/**
	 * Create a new node at given xy coordinates
	 * @param x X coordinate of new node
	 * @param y Y coordinate of new node
	 */
	public void createNewNode(double x, double y, String type)
	{
		Node newNode = null;

		//TODO: refactor on new node creation
		if (newNodesList.size() == 0)
		{
			//if we haven't made any new nodes yet, call databasecontroller so that
			//we have a baseline nodeID to start with.
			//This nodeID is going the be the greatest int nodeID in the existing nodes
			//IMPORTANT: this doesn't actually add the new node to the database tables
			newNode = DatabaseController.generateNewNode("New "+type, type, x, y, FLOORID);
		}
		else
		{
			//otherwise use our own function to generate a new node.
			newNode = editorGenerateNewNode("New "+type, type, x, y, FLOORID);
		}


		newNodesList.add(newNode);

		//TODO: set buttons to be appropriate image depending on node type
		//make a new button to associate with the node
		Button nodeB = new Button();
		nodeB.setId("node-button-unselected");

		//modify xy position with offset so that button is centered on the node's real location
		nodeB.setLayoutX(x - XOFFSET);
		nodeB.setLayoutY(y - YOFFSET);

		//on button click show node details on the righthand panel
		/*nodeB.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				showNodeDetails(nodeB);
			}
		});*/
		nodeB.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				showNodeDetails(nodeB);
			}
		});
		nodeButtonLinks.put(nodeB, newNode);

		//add the new button to the scene
		editingFloor.getChildren().add(1, nodeB);

		//change new node button text back to original
		newNodeButton.setText("Add a New Node");
	}

	/**
	 * This is called whenever the mouse is dragged around on the map.
	 * We use this function to display and use the radial context menu, if
	 * the mouse event is a rightclick.
	 * @param e mouse event
	 */
	@FXML
	void displayContextMenu(MouseEvent e){

		//is this a right click?
		if(e.isSecondaryButtonDown())
		{
			if(currentButton != null){
				//do nothing if a node is currently selected?
				//possibly keep this doing nothing, since buttons should have their own
				//versino of displayContextMenu occuring
			} else { //radial context menu for adding nodes
				currentState = editorStates.SHOWINGEMPTYMENU;
				mainScroll.setPannable(false);
			}
		}

		switch(currentState){
			case SHOWINGEMPTYMENU:
				//update contextmenu based on mouse events
				contextActions(e);
				break;
			case SHOWINGNODEMENU:
				//TODO
				break;
			default:
				break;
		}
	}

	/**
	 * make the selectionWedge visible and adjust its start angle
	 * to line up with the area where the mouse is being hovered
	 * @param angle the angle of the mouse position (in degrees) relative to
	 *              the center of the context menu
	 */
	private void modifyRadialSelection(double angle){

		//NOTE: angles above the horizontal are negative, from 0 to -180.
		//		angles below are positive, from 0 to 180.
		//		in both cases 0 is in the 3 o'clock position

		if(angle < -45 && angle > -135)
		{
			//selection indicator for top, new provider
			SELECTIONWEDGE.setLength(90);
			SELECTIONWEDGE.setStartAngle(45);
			contextSelection = 0;
		}
		else if(angle > -45 && angle < 45)
		{
			//selection indicator for right, new node
			SELECTIONWEDGE.setLength(90);
			SELECTIONWEDGE.setStartAngle(315);
			contextSelection = 1;
		}
		else if(angle > 45 && angle < 135)
		{
			//selection indicator for bottom, new elevator
			SELECTIONWEDGE.setLength(90);
			SELECTIONWEDGE.setStartAngle(225);
			contextSelection = 2;
		}
		else if(angle > 135 || angle < -135)
		{
			//selection indicator for left, new restroom
			SELECTIONWEDGE.setLength(90);
			SELECTIONWEDGE.setStartAngle(135);
			contextSelection = 3;
		}
		else
		{ //TODO: is this case ever possible?
			//make selectionwedge not visible
			SELECTIONWEDGE.setLength(0);
			contextSelection = -1;
		}
	}

	/**
	 * Update views/objects to display the radial context menu.
	 * This is called whenever the mouse moves while the right click is held down
	 * @param e Mouse event
	 */
	void contextActions(MouseEvent e)
	{

		//add contextmenu if it isn't already contained
		if(!editingFloor.getChildren().contains(CONTEXTMENU))
		{
			setupContextMenu(e.getX()-CIRCLEWIDTH/2, e.getY()-CIRCLEWIDTH/2);
			editingFloor.getChildren().add(1, CONTEXTMENU);
		}

		//xy distance from center of contextmenu to mouse
		double xdif = e.getX()-CONTEXTMENU.getLayoutX();
		double ydif = e.getY()-CONTEXTMENU.getLayoutY();

		//check that the mouse is far enough away from the center
		if(Math.pow((xdif), 2) + Math.pow((ydif), 2) >
				Math.pow(CONTEXTRAD-CONTEXTWIDTH, 2)){
			//update selection wedge location and contextSelection
			modifyRadialSelection(Math.toDegrees(Math.atan2(ydif, xdif)));
		}
		//mouse isn't far enough away from center, i.e. no selection
		else
		{
			SELECTIONWEDGE.setLength(0);
			contextSelection = -1;
		}
	}


	/**
	 * Handle whenever the mouse is released.
	 * This function was made in order to support the radial context menu
	 * @param event mouse event fired when the mouse is released
	 */
	@FXML
	void releaseMouse(MouseEvent event) {

		switch(currentState){
			case SHOWINGEMPTYMENU:
				//remove context menu
				editingFloor.getChildren().remove(CONTEXTMENU);
				mainScroll.setPannable(true);

				//switch case for contextSelection.
				//set currentState to appropriate state based on selection.
				//if no selection made set to -1
				switch(contextSelection){
					case 0:
						//top option
						//create new elevator node at location
						createNewNode(CONTEXTMENU.getLayoutX(), CONTEXTMENU.getLayoutY(), "Elevator");
						break;
					case 1:
						//right option
						//create new hallway node at location
						createNewNode(CONTEXTMENU.getLayoutX(), CONTEXTMENU.getLayoutY(), "Hallway");
						break;
					case 2:
						//bottom option
						//create new office node at location
						createNewNode(CONTEXTMENU.getLayoutX(), CONTEXTMENU.getLayoutY(), "Office");
						break;
					case 3:
						//left option
						//create new restroom node at location
						createNewNode(CONTEXTMENU.getLayoutX(), CONTEXTMENU.getLayoutY(), "Restroom");
						break;
					default:
						//no option selected
						currentState = editorStates.DOINGNOTHING;
						break;
				}
				contextSelection = -1;
				currentState = editorStates.DOINGNOTHING;
				break;
			case SHOWINGNODEMENU:
				//TODO:
				currentState = editorStates.DOINGNOTHING;
				break;
			default:
				currentState = editorStates.DOINGNOTHING;
				break;
		}
	}


	/**
	 * Load all nodes from the databasecontroller's list of nodes onto our scene
	 */
	public void loadNodesFromDatabase()
	{
		for (Node n : DatabaseController.getAllNodes())
		{
			loadNode(n);
		}
		for (Node n : DatabaseController.getAllNodes())
		{
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
		Button nodeB = new Button();

		//experimental style changes to make the button a circle
		nodeB.setId("node-button-unselected");

		//set button XY coordinates
		nodeB.setLayoutX(n.getX() - XOFFSET);
		nodeB.setLayoutY(n.getY() - YOFFSET);

		//on button click display the node's details
		nodeB.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
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
	 *
	 * @param name    node name, placeholder
	 * @param type    node type, placeholder
	 * @param x       node x coordinate
	 * @param y       node y coordinate
	 * @param floorid id of floor this node is on
	 * @return a new Node object
	 */
	private Node editorGenerateNewNode(String name, String type, double x, double y, int floorid)
	{
		//add 1 to greatest node ID value, which should be last item in the list
		int newID = newNodesList.get(newNodesList.size() - 1).getID() + 1;

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
		 * TODO: update?
		 */
		switch(currentState){
			case ADDINGNEIGHBOR:
				//add neighbor
				currentNode.addNeighbor(linkedNode);
				//add currentNode (not the node that has just been clicked) to the modifiedlist
				if (!modifiedNodesList.contains(currentNode))
				{
					modifiedNodesList.add(currentNode);
				}
				//redraw lines
				drawToNeighbors(currentNode);
				currentState = editorStates.DOINGNOTHING;
				break;
			case REMOVINGNEIGHBOR:
				//remove neighbor
				currentNode.removeNeighbor(linkedNode);
				//add currentNode (not the node that has just been clicked) to the modifiedlist
				if (!modifiedNodesList.contains(currentNode))
				{
					modifiedNodesList.add(currentNode);
				}
				//redraw lines
				drawToNeighbors(currentNode);
				currentState = editorStates.DOINGNOTHING;
				break;
			default:
				//modify text fields to display node info
				nameField.setText(linkedNode.getData().get(0));
				typeField.setText(linkedNode.getData().get(1));
				xField.setText(Double.toString(linkedNode.getX()));
				yField.setText(Double.toString(linkedNode.getY()));

				//set current node/button
				currentNode = linkedNode;

				if(currentButton != null)
				{
					//TODO: set style for background-color using hex without copying everything?
					//TODO: Just rying to setstyle for color made button change shape
					currentButton.setId("node-button-unselected");
				}
				currentButton = nodeB;
				nodeB.setId("node-button-selected");
				break;
		}
	}

	/**
	 * Hide node details sidebar and deselect current node/button
	 */
	private void hideNodeDetails()
	{
		currentNode = null;
		//TODO: is null check on currentbutton necessary? can we get to this function with a null button?
		if(currentButton != null)
		{
			//TODO: set style for background-color using hex without copying everything?
			//TODO: Just trying to setstyle for color made button change shape
			currentButton.setId("node-button-unselected");
		}
		currentButton = null;

		nameField.setText("");
		typeField.setText("");
		xField.setText("");
		yField.setText("");
	}

	//TODO: this entire function should be defunct by DnD
	/**
	 * Create a new node on the map. alternatively, cancel new node creation
	 */
	public void addNewNodeClicked()
	{
		if (newNodeButton.getText().equals("Add a New Node"))
		{
			currentState = editorStates.MAKINGNEWHALLWAY;
			newNodeButton.setText("Cancel New Node Creation");
			editingFloor.getChildren().add(canvas);
		}
		else
		{
			currentState = editorStates.DOINGNOTHING;
			newNodeButton.setText("Add a New Node");
		}
	}

	@FXML
	/**
	 * modify a node's location on the map. alternately, cancel node location modification
	 */
	void modNodeLocation(ActionEvent event)
	{
		if (currentState != editorStates.MOVINGNODE)
		{
			clickModLocation.setText("Cancel");
			//set booleans to track which state we are in right now
			currentState = editorStates.MOVINGNODE;
		}
		else
		{
			clickModLocation.setText("Modify Location by Click");
			//set booleans to track which state we are in right now
			currentState = editorStates.DOINGNOTHING;
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

            //redraw lines for any node that has currentNode as a neighbor
            //store nodes that need to be redrawn in a list as a workaround
            //for concurrentmodificationexception
            ArrayList<Node> toRedraw = new ArrayList<Node>();
            for(Node n: lineGroups.keySet()){
                boolean has = false;
                if(n.getNeighbors().contains(currentNode)){
                    has = true;
                }
                if(has){
                    toRedraw.add(n);
                }
            }
            //redraw all lines pointing to this node
            for(Node n: toRedraw){
                drawToNeighbors(n);
            }
            //redraw all lines coming out of this node
            drawToNeighbors(currentNode);
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

                //redraw lines for any node that has currentNode as a neighbor
                //store nodes that need to be redrawn in a list as a workaround
                //for concurrentmodificationexception
                ArrayList<Node> toRedraw = new ArrayList<Node>();
                for(Node n: lineGroups.keySet()){
                    boolean has = false;
                    if(n.getNeighbors().contains(currentNode)){
                        has = true;
                    }
                    if(has){
                        toRedraw.add(n);
                    }
                }
                //redraw all lines pointing to this node
                for(Node n: toRedraw){
                    drawToNeighbors(n);
                }
                //redraw all lines coming out of this node
                drawToNeighbors(currentNode);
            }
        } catch (NumberFormatException e){
            //TODO: need more exception handling?
            System.out.println("Not a double");
        }
    }

    //TODO: update function to work with new database
	@FXML
	/**
	 * update a node's Name string
	 */
	void updateNodeData(ActionEvent event)
	{
		try
		{
			if (currentButton != null && currentNode != null)
			{
				ArrayList<String> data = new ArrayList<String>();
				data.add(nameField.getText());
				data.add(typeField.getText());
				currentNode.setData(data);
				//track that this node has been modified
				if (!modifiedNodesList.contains(currentNode))
				{
					modifiedNodesList.add(currentNode);
				}
			}
		} catch (NumberFormatException e)
		{
			//TODO: need more exception handling?
			System.out.println("Not a double");
		}
	}


	//TODO: make this function work with updated database
	@FXML
	/**
	 * Push newly created nodes into the databasecontroller's node table.
	 *
	 * This function will:
	 *  - insert all newly created nodes into the database
	 *  - update all modified nodes that were loaded in from the database
	 *  - insert all new neighbor relations that have been created
	 *  - delete all neighbor relations that have been deleted
	 */
	void pushChangesToDatabase(ActionEvent event)
	{
		for (Node n : newNodesList)
		{
			//TODO: change types of things to be concretenode instead of node?
			//sloppy to cast like this I assume
			ConcreteNode newNode = (ConcreteNode) n;
			DatabaseController.insertNode(newNode);
		}

		//modifyNodes will update Node tables for all nodes in the list
		DatabaseController.modifyNodes(modifiedNodesList);

		//for each newly created node, insert it's neighbor relations into the table
		for (Node n : newNodesList)
		{
			//Also has sloppy casting here
			ConcreteNode newNode = (ConcreteNode) n;
			for (Node neighborNode : newNode.getNeighbors())
			{
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
		for (Node n : modifiedNodesList)
		{
			//initialize a collection of all neighbors for node N that the database knows about
			Collection<Node> sourceNeighbors = DatabaseController.getNodeByID(n.getID()).getNeighbors();
			//get neighbors from node N as the editortool knows about
			Collection<Node> modNeighbors = n.getNeighbors();

			//arrays of nodes that will be used to store which nodes are common to both sourceNeighbors and modNeighbors
			ArrayList<Node> toDeleteSourceNeighbors = new ArrayList<Node>();
			ArrayList<Node> toDeleteModNeighbors = new ArrayList<Node>();

			//for each neighbor editor knows about
			for (Node modNeighbor : modNeighbors)
			{
				//for each neighbor database knows about
				for (Node sourceNeighbor : sourceNeighbors)
				{
					//if ID is same, they are the same node.
					if (sourceNeighbor.getID() == modNeighbor.getID())
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

        //remove nodes from database
        for(Node n: deleteNodesList){
            //delete any neighbor relations coming from this node
            DatabaseController.removeNeighborsFromID(n.getID());
            DatabaseController.removeNode(n.getID());
        }
    }


	@FXML
	/**
	 * Create lines from a node (source) to all of the node's neighbors.
	 *
	 * This will need to be called many times to properly and fully update drawn lines
	 * because this only updates lines coming from a Node.
	 */
	void drawToNeighbors(Node source)
	{

		// Remove all existing lines coming from source Node
		// Check if source has existing lines
		if (lineGroups.containsKey(source))
		{
			ArrayList<Group> oldGroups = lineGroups.get(source);
			for (Group g : oldGroups)
			{
				// Removes all lines from oldLines from the UI
				((AnchorPane) g.getParent()).getChildren().remove(g);
			}
			lineGroups.remove(source);
		}

		//keep track of lines created for this node
		ArrayList<Group> groups = new ArrayList<Group>();
		Collection<Node> neighbors = source.getNeighbors();

		//for each neighbor associated with source Node, draw a line.
		for (Node neighbor : neighbors)
		{
			Line line = new Line();
			line.setStrokeWidth(LINEWIDTH);
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
			double pathShift = 0.9;

			//make a new triangle
			Polygon arrowTriangle = new Polygon();
			arrowTriangle.getPoints().addAll(new Double[]{
					0.0, 0.0,
					-5.0, 10.0,
					5.0, 10.0
			});

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
			editingFloor.getChildren().add(2, g);
			//add this line into the lines array
			groups.add(g);
		}
		//store the array of lines for this source node into the hashmap
		//to be used to delete all lines later when redrawing
		lineGroups.put(source, groups);
	}

	/**
	 * Continuously draw and update the line on the canvas so that the user can see
	 * lines as they are adding connections.
	 * TODO: possibly need to fix naming of function
	 * @param event mouse event
	 */
	@FXML
	void redrawCanvas(MouseEvent event) {

		//add canvas back if it has previously been removed
		if(!editingFloor.getChildren().contains(canvas)){
			editingFloor.getChildren().add(1, canvas);
		}
		//clear canvas
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

		switch(currentState){
			case ADDINGNEIGHBOR:
				//draw line
				gc.setStroke(Color.BLACK);
				gc.setLineWidth(LINEWIDTH);
				gc.strokeLine(currentNode.getX(), currentNode.getY(), event.getX(), event.getY());
				break;
			case MAKINGNEWHALLWAY:
				//draw circle
				gc.setFill(Color.BLUE);
				//circle drawing placement offset
				gc.fillOval(event.getX()-CIRCLEWIDTH/2, event.getY()-CIRCLEWIDTH/2, CIRCLEWIDTH, CIRCLEWIDTH);
				break;
			//TODO: cases for making different kinds of nodes

			//TODO: do context menus need to do anything here?
			case SHOWINGEMPTYMENU:
				break;
			case SHOWINGNODEMENU:
				break;
			default:
				//remove canvas from scene if it is there but unneeded
				if (editingFloor.getChildren().contains(canvas))
				{
					((AnchorPane) canvas.getParent()).getChildren().remove(canvas);
				}
				break;
		}
	}

	@FXML
	/**
	 * Set controller to addingNeighbor state
	 */
	void addNeighbor(ActionEvent event)
	{
		if (currentNode != null)//Don't do anything unless a node is selected
		{
			currentState = editorStates.ADDINGNEIGHBOR;
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
			currentState = editorStates.REMOVINGNEIGHBOR;
		}
	}

	@FXML
	/**
	 *  Go back to startup screen
	 */
	void goBack()
	{
		DatabaseController.initializeAllNodes();
		Main.loadFXML("/fxml/Startup.fxml");
	}

    @FXML
    /**
     * Add the current node to the deleteNodesList to be deleted from the database
     */
    void deleteNode(ActionEvent event) {
        if(currentNode != null)
        {
            //if this node had any neighbors remove lines
            if (lineGroups.containsKey(currentNode))
            {
                for (Group g : lineGroups.get(currentNode))
                {
                    // Removes all lines going from the deleted node
                    ((AnchorPane) g.getParent()).getChildren().remove(g);
                }
                lineGroups.remove(currentNode); // Remove current Node from lineGroups if it is inside lineGroups
            }

            //redraw lines for any nodes that have the deleted node as a neighbor
            for(Node n: newNodesList)
            {
                boolean has = false;
                if(n.getNeighbors().contains(currentNode)){
                    has = true;
                }
                if(has)
                {
                    n.removeNeighbor(currentNode);
                    drawToNeighbors(n);
                    //indicate that this node has been modified
                    if (!modifiedNodesList.contains(n))
                    {
                        modifiedNodesList.add(n);
                    }
                }
            }
            //TODO: collapse this into a single function, completely duplicate and perhaps redundant code
            for(Node n: DatabaseController.getAllNodes())
            {
                boolean has = false;
                if(n.getNeighbors().contains(currentNode)){
                    has = true;
                }
                if(has)
                {
                    n.removeNeighbor(currentNode);
                    drawToNeighbors(n);
                    //indicate that this node has been modified
                    if (!modifiedNodesList.contains(n))
                    {
                        modifiedNodesList.add(n);
                    }
                }
            }
            //TODO: collapse this into a single function, completely duplicate and perhaps redundant code
            for(Node n: modifiedNodesList)
            {
                boolean has = false;
                if(n.getNeighbors().contains(currentNode)){
                    has = true;
                }
                if(has)
                {
                    n.removeNeighbor(currentNode);
                    drawToNeighbors(n);
                    //indicate that this node has been modified
                    if (!modifiedNodesList.contains(n))
                    {
                        modifiedNodesList.add(n);
                    }
                }
            }

			//add node to deleteNodesList
			deleteNodesList.add(currentNode);
			((AnchorPane)currentButton.getParent()).getChildren().remove(currentButton);
			//hide details view
			hideNodeDetails();
        }
    }

}
