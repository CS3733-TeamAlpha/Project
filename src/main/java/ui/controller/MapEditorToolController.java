package ui.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import data.ConcreteNode;
import data.Node;
import ui.Accessibility;
import ui.Paths;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MapEditorToolController extends BaseController
{
	//Arraylist of all lines drawn from a node to its neighbors
	private HashMap<Node, ArrayList<Group>> lineGroups = new HashMap<Node, ArrayList<Group>>();

	//link buttons to node objects
	private HashMap<Button, Node> nodeButtonLinks = new HashMap<Button, Node>();

	//constants to be used for drawing radial contextmenu
	double CONTEXTWIDTH = 60.0;
	double CONTEXTRAD = 90.0;
	Group CONTEXTMENU = new Group();
	Arc SELECTIONWEDGE = new Arc();




    //currently selected node and button
    private Node currentNode = null;
    private Button currentButton = null;


    //boolean to determine whether or not to automatically connect newly added nodes
	//to the nearest hallway node
	private boolean AUTOCONNECT = false;

	//enums to indicate current state
	private enum editorStates {
		DOINGNOTHING,
		MAKINGNEWHALLWAY,  //making hallway type node
		MAKINGNEWOFFICE,   //making office type node
		MAKINGNEWELEVATOR, //making elevator type node
		MAKINGNEWRESTROOM, //making restroom type node
		CHAINADDING,       //chain add hallways to a given node
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


	//canvas and graphicscontext, to draw onto the scene
	private Canvas canvas;
	private GraphicsContext gc;

	//eventhandler for when the mouse is clicked on a node button
	private EventHandler nodebuttonOnAction = (EventHandler<ActionEvent>) event -> showNodeDetails((Button)event.getSource());

	//drageventhandler for when the mouse is dragged on a node button
	private EventHandler nodebuttonMouseDrag = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle(MouseEvent e)
		{
			if(e.isSecondaryButtonDown())
			{
				if(currentButton != e.getSource() && currentButton != null)
				{
					System.out.println("dont happen");
					currentButton.setId("node-button-unselected");
				}
				currentButton = (Button)e.getSource();
				currentButton.setId("node-button-selected");
				currentNode = nodeButtonLinks.get(currentButton);
				currentState = editorStates.SHOWINGNODEMENU;
				displayContextMenu(e);
			}
			else
			{
				currentState = editorStates.MOVINGNODE;
				Button nodeB = (Button)e.getSource();
				//move the node to match changes in the mouse movement
				//as you drag
				showNodeDetails(nodeB);
				nodeB.setLayoutX(e.getX() - XOFFSET + nodeB.getLayoutX());
				nodeB.setLayoutY(e.getY() - YOFFSET + nodeB.getLayoutY());
				currentNode.setX(nodeB.getLayoutX() + XOFFSET);
				currentNode.setY(nodeB.getLayoutY() + YOFFSET);
				redrawAllNeighbors(currentNode);
			}
		}
	};

	//eventhandler for when the mouse is released after having clicked a node button
	private EventHandler nodebuttonMouseReleased = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle(MouseEvent e)
		{
			releaseMouseFromNode(e);
		}
	};

	public EventHandler nodebuttonMouseMoved = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle(MouseEvent e)
		{
			redrawCanvasBugfix(e);
		}
	};

	public class ZoomHandler implements EventHandler<ScrollEvent> {

		public ZoomHandler()
		{
		}

		@Override
		public void handle(ScrollEvent scrollEvent) {
				final double scale = calculateScale(scrollEvent);
				editingFloor.setScaleX(scale);
				editingFloor.setScaleY(scale);
				zoomWrapper.setMinWidth(editingFloor.getWidth()*scale);
				zoomWrapper.setMinHeight(editingFloor.getHeight()*scale);
				zoomWrapper.setMaxWidth(editingFloor.getWidth()*scale);
				zoomWrapper.setMaxHeight(editingFloor.getHeight()*scale);

				editingFloor.setLayoutX((zoomWrapper.getWidth() - editingFloor.getWidth())/2);
				editingFloor.setLayoutY((zoomWrapper.getHeight() - editingFloor.getHeight())/2);
				scrollEvent.consume();
		}

		private double calculateScale(ScrollEvent scrollEvent) {
			double scale = currentZoom + scrollEvent.getDeltaY() / 5000;

			if (scale <= MINZOOM) {
				scale = MINZOOM;
			} else if (scale >= MAXZOOM) {
				scale = MAXZOOM;
			}
			currentZoom = scale;
			return scale;
		}
	}

	static
	{
		//initialize connection and floor/node/provider lists right away
		//DatabaseController will hold onto these lists
	}

	public MapEditorToolController()
	{
		super();
	}

	@FXML
	public void initialize()
	{

		//load all nodes for a specific floor, default to FLOORID
		loadNodesFromDatabase();

		//create canvas and graphicscontext
		//these will be used later for drawing lines in realtime and drag&drop visuals
		Group root = new Group();
		canvas = new Canvas(2200, 1300);
		canvas.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				clickFloorImage(e);
			}
		});
		canvas.setOnMousePressed(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				if(e.isSecondaryButtonDown() && currentState == editorStates.CHAINADDING)
				{
					currentState = editorStates.DOINGNOTHING;
				}
			}
		});
		gc = canvas.getGraphicsContext2D();
		editingFloor.getChildren().add(1, canvas);

		//style up/down buttons
		upFloor.setId("upbuttonTriangle");
		downFloor.setId("downbuttonTriangle");

		//set the floorImage imageview to display the correct floor's image.
		setFloorImage(BUILDINGID, FLOORID);

		//set up event handlers for drag and drop images
		setupImageEventHandlers();

		//set up the choicebox for changing buildings
		ArrayList<String> buildings = database.getBuildings();
		for(String s: buildings)
			System.out.println(s);
		ChoiceBox buildingChoice = new ChoiceBox();
		buildingChoice.setItems(FXCollections.observableArrayList(buildings.toArray()));
		((Pane)currentFloorLabel.getParent()).getChildren().add(buildingChoice);
		buildingChoice.setLayoutX(49);
		buildingChoice.setLayoutY(106);
		buildingChoice.setOnAction(event ->
				{
					changeBuilding((String)buildingChoice.getValue());
				}
		);

		//add event filter to let scrolling do zoom instead
		mainScroll.addEventFilter(ScrollEvent.ANY, new ZoomHandler());

	}

	/**
	 * Change the building that is currently being edited
	 * @param building String name of the buliding to edit
	 */
	private void changeBuilding(String building)
	{
		//change selected building ID
		BUILDINGID = database.getBuildingUUID(building);
		//remove all buttons and lines on the current floor
		purgeButtonsAndLines();
		//default to floor 1 when changing buildings
		FLOORID = 1;
		if(BUILDINGID.equals("00000000-0000-0000-0000-000000000000"))//faulkner, max 7 floor
		{
			MAXFLOOR = 7;
		} else if(BUILDINGID.equals("00000000-0000-0000-0000-111111111111"))//faulkner, max 4 floor
		{
			MAXFLOOR = 4;
		} else {
			MAXFLOOR = 1;
		}
		loadNodesFromDatabase();
		currentFloorLabel.setText(Integer.toString(FLOORID));
		setFloorImage(BUILDINGID, FLOORID);

		//set floor stuff correctly
		editingFloor.setScaleX(currentZoom);
		editingFloor.setScaleY(currentZoom);
		zoomWrapper.setMinWidth(editingFloor.getWidth()*currentZoom);
		zoomWrapper.setMinHeight(editingFloor.getHeight()*currentZoom);
		zoomWrapper.setMaxWidth(editingFloor.getWidth()*currentZoom);
		zoomWrapper.setMaxHeight(editingFloor.getHeight()*currentZoom);

		editingFloor.setLayoutX((zoomWrapper.getWidth() - editingFloor.getWidth())/2);
		editingFloor.setLayoutY((zoomWrapper.getHeight() - editingFloor.getHeight())/2);
	}

	/**
	 * Set the floorImage imageview object to display the image of
	 * a specific floor.
	 * Currently only works based on floor, not building
	 * @param floor The floor to display
	 */
	private void setFloorImage(String buildingid, int floor)
	{
		//faulkner building
		if(buildingid.equals("00000000-0000-0000-0000-000000000000"))
		{
			floorImage.setImage(Paths.regularFloorImages[floor-1].getFXImage());
		}
		else if(buildingid.equals("00000000-0000-0000-0000-111111111111"))
		{
			floorImage.setImage(Paths.belkinFloorImages[floor-1].getFXImage());
		}
		else if (buildingid.equals("00000000-0000-0000-0000-222222222222"))
		{
			floorImage.setImage(Paths.outdoorImageProxy.getFXImage());
		}
	}

	/**
	 * Set event handlers for all images.
	 * These will handle drag events by setting state and moving the image,
	 * as well as the mouse release event at the end of the drag.
	 */
	private void setupImageEventHandlers()
	{
		//if esc key is pressed, remove contextmenus/canvases and set state to doingnothing
		//this is a failsafe as well as feature
		stage.getScene().setOnKeyPressed(e ->{
			if(e.getCode() == KeyCode.ESCAPE)
			{
				//remove canvas if it exists
				if (editingFloor.getChildren().contains(canvas))
				{
					editingFloor.getChildren().remove(canvas);
				}
				//remove context menu if it exists
				if(editingFloor.getChildren().contains(CONTEXTMENU))
				{
					editingFloor.getChildren().remove(CONTEXTMENU);
				}
				currentState = editorStates.DOINGNOTHING;
				mainScroll.setPannable(true);
			}
		});

	}


	/**
	 * Set up all initial settings for the context menu.
	 * XY position will be initialized based on mouse position
	 * @param x x coord for center of the radial context menu
	 * @param y y coord same as above
	 */
	private void setupContextMenu(double x, double y)
	{
		//clear contents on startup so we can repopulate
		CONTEXTMENU.getChildren().clear();
		//set xy to compare with mouse position later
		CONTEXTMENU.setLayoutX(x);
		CONTEXTMENU.setLayoutY(y);

		//main radial arc, currently colored gray.
		Arc radialMenu = new Arc(0, 0, CONTEXTRAD, CONTEXTRAD, 0, 360);
		radialMenu.setType(ArcType.OPEN);
		radialMenu.setStrokeWidth(CONTEXTWIDTH);
		radialMenu.setStroke(Color.GRAY);
		radialMenu.setStrokeType(StrokeType.INSIDE);
		radialMenu.setFill(null);
		radialMenu.setOpacity(0.95);

		//arc wedge will be used to indicate current selection
		SELECTIONWEDGE = new Arc(0, 0, CONTEXTRAD, CONTEXTRAD, 0, 0);
		SELECTIONWEDGE.setType(ArcType.ROUND);
		SELECTIONWEDGE.setStrokeWidth(CONTEXTWIDTH);
		SELECTIONWEDGE.setStroke(Color.BLUE);
		SELECTIONWEDGE.setStrokeType(StrokeType.INSIDE);
		SELECTIONWEDGE.setFill(null);
		SELECTIONWEDGE.setOpacity(0.9);

		//draw four lines for each section of the menu
		//lots of ugly numbers indicating start/end positions of lines
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

		//4 options, whose contents will differ based on whether we are showing node menu or not
		ImageView option1 = null;
		ImageView option2 = null;
		ImageView option3 = null;
		ImageView option4 = null;

		//offsets from the center of the images
		double imageOffsetY = -100;
		double imageOffsetX = -100;

		switch(currentState)
		{

			case SHOWINGEMPTYMENU: //context menu for non-nodes

				option1 = new ImageView(Paths.hallwayImageProxy.getFXImage());
				option1.setScaleX(0.15);
				option1.setScaleY(0.15);
				option1.setX(imageOffsetX + CONTEXTRAD - CONTEXTWIDTH / 2);
				option1.setY(imageOffsetY);
				option2 = new ImageView(Paths.doctorImageProxy.getFXImage());
				option2.setScaleX(0.15);
				option2.setScaleY(0.15);
				option2.setX(imageOffsetX);
				option2.setY(imageOffsetY + CONTEXTRAD - CONTEXTWIDTH / 2);
				option3 = new ImageView(Paths.restroomImageProxy.getFXImage());
				option3.setScaleX(0.15);
				option3.setScaleY(0.15);
				option3.setX(imageOffsetX - CONTEXTRAD + CONTEXTWIDTH / 2);
				option3.setY(imageOffsetY);
				option4 = new ImageView(Paths.elevatorImageProxy.getFXImage());
				option4.setScaleX(0.15);
				option4.setScaleY(0.15);
				option4.setX(imageOffsetX);
				option4.setY(imageOffsetY - CONTEXTRAD + CONTEXTWIDTH / 2);
				break;
			case SHOWINGNODEMENU: //contextmenu for nodes

				//node specific context menu

				option1 = new ImageView(Paths.addNeighborImageProxy.getFXImage());
				option1.setScaleX(0.15);
				option1.setScaleY(0.15);
				option1.setX(imageOffsetX + CONTEXTRAD - CONTEXTWIDTH / 2);
				option1.setY(imageOffsetY);
				option2 = new ImageView(Paths.removeNeighborImageProxy.getFXImage());
				option2.setScaleX(0.15);
				option2.setScaleY(0.15);
				option2.setX(imageOffsetX);
				option2.setY(imageOffsetY + CONTEXTRAD - CONTEXTWIDTH / 2);
				option3 = new ImageView(Paths.removeNodeImageProxy.getFXImage());
				option3.setScaleX(0.15);
				option3.setScaleY(0.15);
				option3.setX(imageOffsetX - CONTEXTRAD + CONTEXTWIDTH / 2);
				option3.setY(imageOffsetY);
				option4 = new ImageView(Paths.chainImageProxy.getFXImage());
				option4.setScaleX(0.15);
				option4.setScaleY(0.15);
				option4.setX(imageOffsetX);
				option4.setY(imageOffsetY - CONTEXTRAD + CONTEXTWIDTH / 2);
				break;
			default:
				break;
		}


		//add all elements to the CONTEXTMENU group
		CONTEXTMENU.getChildren().add(radialMenu);
		CONTEXTMENU.getChildren().add(SELECTIONWEDGE);
		CONTEXTMENU.getChildren().add(split1);
		CONTEXTMENU.getChildren().add(split2);
		CONTEXTMENU.getChildren().add(split3);
		CONTEXTMENU.getChildren().add(split4);
		CONTEXTMENU.getChildren().add(option1);
		CONTEXTMENU.getChildren().add(option2);
		CONTEXTMENU.getChildren().add(option3);
		CONTEXTMENU.getChildren().add(option4);
	}

	@FXML
	private ScrollPane mainScroll;

	@FXML
	private AnchorPane editingFloor;

	@FXML
	private AnchorPane zoomWrapper;

	@FXML
	private Pane palettePane;

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
	private VBox dndContainer;

	@FXML
	private CheckBox toggleAutoConnect;

	@FXML
	private Button upFloor;

	@FXML
	private Button downFloor;

	@FXML
	private Label currentFloorLabel;

	@FXML
	/**
	 * Function is fired when a drop action is detected, i.e. node is being moved or
	 * new node is being made by drag and drop
	 * @params x The x position of the node
	 * @params y The y position of the node
	 */
	private void dropNode(Double x, Double y) {
		switch(currentState)
		{
			case MAKINGNEWHALLWAY:
				//create a new hallway node
				createNewNode(x, y, 0);
				break;
			case MAKINGNEWOFFICE:
				//create a new office node
				createNewNode(x, y,  1);
				break;
			case MAKINGNEWELEVATOR:
				//create a new elevator node
				createNewNode(x, y, 2);
				break;
			case MAKINGNEWRESTROOM:
				//create a new restroom node
				createNewNode(x, y, 3);
				break;
			default:
				//default case, hide node details
				hideNodeDetails();
				break;
		}

		//set currentState to -1 to indicate we are no longer in special state
		currentState = editorStates.DOINGNOTHING;
	}

	/**
	 * Floor image is rightclicked, only useful for stopping chain adding
	 * @param event
	 */
	@FXML
	void rightclickFloorImage(ContextMenuEvent event) {
		currentState = editorStates.DOINGNOTHING;
	}

	@FXML
	/**
	 * Floor image clicked, hide node details.
	 */
	void clickFloorImage(MouseEvent e)
	{
		switch (currentState)
		{
			case CHAINADDING:
				//create a new hallway node and connect it to the "current" node
				//then update current
				Node oldNode = currentNode;
				createNewNode(e.getX(), e.getY(), 0);
				//currentNode is now the new node
				oldNode.addNeighbor(currentNode);
				currentNode.addNeighbor(oldNode);

				//update currentnode and linked node since both had neighbor added
				database.updateNode(oldNode);
				database.updateNode(currentNode);

				//draw connecting lines
				drawToNeighbors(currentNode);
				drawToNeighbors(oldNode);

				currentButton.toFront();

				break;
			default:
				hideNodeDetails();
				//set currentState to  indicate we are no longer in special state
				currentState = editorStates.DOINGNOTHING;
				break;
		}

	}

	@FXML
	/**
	 * Change the current floor to increment up by 1.
	 * Prevent going down if floor is already 1.
	 */
	void goDownFloor(ActionEvent event) {
		//if the state is adding neighbors and the node is an elevator, add neighbor with lower elevator.
		//WARNING: ELEVATOR NODES MUST BE AT THE SAME XY COORDINATES
		if(currentState == editorStates.ADDINGNEIGHBOR && FLOORID > 1 &&
				currentNode != null && currentNode.getType() == 2)
		{
			//find the lower elevator node and connect to it if it exists
			Node lowerNode = null;
			lowerNode = database.getElevatorNodeByFloorCoordinates(currentNode.getX(), currentNode.getY(), FLOORID-1);
			if(lowerNode != null)
			{
				currentNode.addNeighbor(lowerNode);
				lowerNode.addNeighbor(currentNode);
				//update currentnode and linked node since both had neighbor added
				database.updateNode(lowerNode);
				database.updateNode(currentNode);
				currentState = editorStates.DOINGNOTHING;
				System.out.println("Connected down");
			}
			currentState = editorStates.DOINGNOTHING;
		} else if(currentState == editorStates.REMOVINGNEIGHBOR && FLOORID > 1 &&
				currentNode != null && currentNode.getType() == 2)
		{
			//get the lower elevator node and remove it from neighbor if it exists
			Node lowerNode = null;
			lowerNode = database.getElevatorNodeByFloorCoordinates(currentNode.getX(), currentNode.getY(), FLOORID-1);
			if(lowerNode != null)
			{
				currentNode.delNeighbor(lowerNode);
				//remove neighbor relation from linked node, if valid
				if (lowerNode.getNeighbors().contains(currentNode))
				{
					lowerNode.delNeighbor(currentNode);
					drawToNeighbors(lowerNode);
					//if linked node also had neighbor, update the change
					database.updateNode(lowerNode);
				}
				//update currentNode (not the node that has just been clicked)
				database.updateNode(currentNode);

				//redraw lines
				drawToNeighbors(currentNode);
			}
			currentState = editorStates.DOINGNOTHING;
		}
		else if(FLOORID > 1){
			//remove all buttons and lines on the current floor
			purgeButtonsAndLines();
			FLOORID--;
			loadNodesFromDatabase();
			currentFloorLabel.setText(Integer.toString(FLOORID));
			setFloorImage(BUILDINGID, FLOORID);
		}
	}

	@FXML
	/**
	 * Change the current floor to increment down by 1.
	 * Prevent going up if floor is already 7.
	 */
	void goUpFloor(ActionEvent event)
	{
		//if the state is adding neighbors and the node is an elevator, add neighbor with upper elevator.
		//WARNING: ELEVATOR NODES MUST BE AT THE SAME XY COORDINATES
		if(currentState == editorStates.ADDINGNEIGHBOR && FLOORID < MAXFLOOR &&
				currentNode != null && currentNode.getType() == 2)
		{
			//get the upper elevator node and connect if it exists
			Node upperNode = null;
			upperNode = database.getElevatorNodeByFloorCoordinates(currentNode.getX(), currentNode.getY(), FLOORID+1);
			if(upperNode != null)
			{
				currentNode.addNeighbor(upperNode);
				upperNode.addNeighbor(currentNode);
				//update currentnode and linked node since both had neighbor added
				database.updateNode(upperNode);
				database.updateNode(currentNode);
				System.out.println("Connected up");
			}
			currentState = editorStates.DOINGNOTHING;
		} else if(currentState == editorStates.REMOVINGNEIGHBOR && FLOORID  < MAXFLOOR &&
				currentNode != null && currentNode.getType() == 2)
		{
			//get the upper elevator node and remove it from neighbor if it exists
			Node upperNode = null;
			upperNode = database.getElevatorNodeByFloorCoordinates(currentNode.getX(), currentNode.getY(), FLOORID+1);
			if(upperNode != null)
			{
				currentNode.delNeighbor(upperNode);
				//remove neighbor relation from linked node, if valid
				if (upperNode.getNeighbors().contains(currentNode))
				{
					upperNode.delNeighbor(currentNode);
					drawToNeighbors(upperNode);
					//if linked node also had neighbor, update the change
					database.updateNode(upperNode);
				}
				//update currentNode (not the node that has just been clicked)
				database.updateNode(currentNode);

				//redraw lines
				drawToNeighbors(currentNode);
			}
			currentState = editorStates.DOINGNOTHING;
		}
		else if(FLOORID < MAXFLOOR)
		{
			//remove all buttons and lines on the current floor
			purgeButtonsAndLines();
			FLOORID++;
			loadNodesFromDatabase();
			currentFloorLabel.setText(Integer.toString(FLOORID));
			setFloorImage(BUILDINGID, FLOORID);
		}
	}

	/**
	 * Create a new node at given xy coordinates.
	 * Note on node types: 0 is hallway, 1 is doctorsoffice, 2 is elevator, 3 is resetroom
	 * @param x X coordinate of new node
	 * @param y Y coordinate of new node
	 */
	public void createNewNode(double x, double y, int type)
	{
		ConcreteNode newNode = new ConcreteNode();
		newNode.setName("New " + type);
		newNode.setX(x);
		newNode.setY(y);
		newNode.setFloor(FLOORID);
		newNode.setBuilding(BUILDINGID);
		newNode.setType(type);

		database.insertNode(newNode);

		//make a new button to associate with the node
		Button nodeB = new Button();
		nodeB.setId("node-button-unselected");

		//modify xy position with offset so that button is centered on the node's real location
		nodeB.setLayoutX(x - XOFFSET);
		nodeB.setLayoutY(y - YOFFSET);

		//on button click show node details on the righthand panel
		nodeB.setOnAction(nodebuttonOnAction);
		//move the node or show contextmenu on drag
		nodeB.setOnMouseDragged(nodebuttonMouseDrag);
		//handle when mouse is released after click on button
		nodeB.setOnMouseReleased(nodebuttonMouseReleased);
		//fix bug with moving over node
		nodeB.setOnMouseMoved(nodebuttonMouseMoved);

		currentNode = newNode;
		if(currentButton != null)
		{
			currentButton.setId("node-button-unselected");
		}
		currentButton = nodeB;
		currentButton.setId("node-button-selected");
		setButtonImage(nodeB, type);
		nodeButtonLinks.put(nodeB, newNode);
		//add the new button to the scene
		editingFloor.getChildren().add(1, nodeB);

		//reset current state to doingnothing
		//exception: if we are chainadding, don't set to doingnothing
		if(currentState != editorStates.CHAINADDING)
		{
			currentState = editorStates.DOINGNOTHING;
			//auto connect toggled
			if(AUTOCONNECT)
			{
				//find nearest hallway node and add as neighbor
				Node nearest = database.getNearestHallwayNode(currentNode);
				if(nearest != null)
				{
					nearest.addNeighbor(currentNode);
					currentNode.addNeighbor(nearest);

					//link to nearest neighbor
					database.updateNode(nearest);
					database.updateNode(currentNode);

					//draw connecting lines
					drawToNeighbors(currentNode);
					drawToNeighbors(nearest);

					currentButton.toFront();
				}
			}
		}
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
			if(currentButton != null){ //radial context menu for node options
				mainScroll.setPannable(false);
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
				//update contextmenu based on mouse events
				contextActions(e);
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
		{
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
			if(currentState == editorStates.SHOWINGNODEMENU)
			{
				setupContextMenu(currentNode.getX(), currentNode.getY());
			}
			else
			{
				setupContextMenu(e.getX() - CIRCLEWIDTH / 2, e.getY() - CIRCLEWIDTH / 2);
			}
			editingFloor.getChildren().add(1, CONTEXTMENU);
			CONTEXTMENU.toFront();
		}

		//xy distance from center of contextmenu to mouse
		double xdif = 0;
		double ydif = 0;
		if(currentState == editorStates.SHOWINGNODEMENU)
		{
			xdif = e.getX();
			ydif = e.getY();
		}
		else
		{
			//adjust for layout position if event is being fired by floorimage
			xdif = e.getX() - CONTEXTMENU.getLayoutX();
			ydif = e.getY() - CONTEXTMENU.getLayoutY();
		}

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
	 * Handle whenever the mouse is released after a node was selected.
	 * This function is used to help with the node context menu
	 * @param event mouse event
	 */
	void releaseMouseFromNode(MouseEvent event)
	{
		switch(currentState)
		{
			case SHOWINGNODEMENU:
				//remove context menu
				editingFloor.getChildren().remove(CONTEXTMENU);
				mainScroll.setPannable(true);

				//switch case for contextSelection.
				//set currentState to appropriate state based on selection.
				//if no selection made set to -1
				switch(contextSelection){
					case 0:
						//top option
						System.out.println("top");
						currentState = editorStates.CHAINADDING;
						break;
					case 1:
						//right option
						System.out.println("right");
						currentState = editorStates.ADDINGNEIGHBOR;
						break;
					case 2:
						//bottom option
						System.out.println("bot");
						currentState = editorStates.REMOVINGNEIGHBOR;
						break;
					case 3:
						//left option
						System.out.println("left");
						deleteNode(null);
						currentState = editorStates.DOINGNOTHING;
						break;
					default:
						//no option selected
						currentState = editorStates.DOINGNOTHING;
						break;
				}
				contextSelection = -1;
				break;

			default:
				if(currentNode != null)
				{
					database.updateNode(currentNode);
				}
				currentState = editorStates.DOINGNOTHING;
				break;
		}
	}

	/**
	 * Handle whenever the mouse is released.
	 * This function was made in order to support the radial context menu
	 * @param event mouse event fired when the mouse is released
	 */
	@FXML
	void releaseMouse(MouseEvent event) {

		switch(currentState)
		{
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
						createNewNode(CONTEXTMENU.getLayoutX(), CONTEXTMENU.getLayoutY(), 2);
						break;
					case 1:
						//right option
						//create new hallway node at location
						createNewNode(CONTEXTMENU.getLayoutX(), CONTEXTMENU.getLayoutY(), 0);
						break;
					case 2:
						//bottom option
						//create new office node at location
						createNewNode(CONTEXTMENU.getLayoutX(), CONTEXTMENU.getLayoutY(), 1);
						break;
					case 3:
						//left option
						//create new restroom node at location
						createNewNode(CONTEXTMENU.getLayoutX(), CONTEXTMENU.getLayoutY(), 3);
						break;
					default:
						//no option selected
						currentState = editorStates.DOINGNOTHING;
						break;
				}
				contextSelection = -1;
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
		for (Node n : database.getNodesInBuildingFloor(BUILDINGID, FLOORID))
			loadNode(n);
		for (Node n : database.getNodesInBuildingFloor(BUILDINGID, FLOORID))
			drawToNeighbors(n);
	}

	/**
	 * Hide all node buttons for the current floor, in preperation for changing floors.
	 */
	private void purgeButtonsAndLines()
	{
		for(Button b: nodeButtonLinks.keySet())
		{
			Node linkedNode = nodeButtonLinks.get(b);
			if(lineGroups.containsKey(linkedNode))
			{
				for (Group g : lineGroups.get(linkedNode))
				{
					// Removes all lines from oldLines from the UI
					((AnchorPane) g.getParent()).getChildren().remove(g);
				}
				lineGroups.remove(linkedNode);
			}
			//remove this button from the UI
			((AnchorPane) b.getParent()).getChildren().remove(b);
		}
		//clear all entries in nodeButtonLinks
		nodeButtonLinks.clear();
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
		nodeB.setOnAction(nodebuttonOnAction);
		//move node or display contextmenu on drag
		nodeB.setOnMouseDragged(nodebuttonMouseDrag);
		//handle when mouse released
		nodeB.setOnMouseReleased(nodebuttonMouseReleased);
		//fix bug with moving over node
		nodeB.setOnMouseMoved(nodebuttonMouseMoved);
		setButtonImage(nodeB, n.getType());
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
		switch(currentState){
			case ADDINGNEIGHBOR:
				//add neighbor
				currentNode.addNeighbor(linkedNode);
				linkedNode.addNeighbor(currentNode);
				//update currentnode and linked node since both had neighbor added
				database.updateNode(linkedNode);
				database.updateNode(currentNode);

				//redraw lines
				drawToNeighbors(currentNode);
				drawToNeighbors(linkedNode);
				currentState = editorStates.DOINGNOTHING;
				currentNode = linkedNode;
				break;
			case REMOVINGNEIGHBOR:
				//remove neighbor
				currentNode.delNeighbor(linkedNode);
				//remove neighbor relation from linked node, if valid
				if(linkedNode.getNeighbors().contains(currentNode))
				{
					linkedNode.delNeighbor(currentNode);
					drawToNeighbors(linkedNode);
					//if linked node also had neighbor, update the change
					database.updateNode(linkedNode);
				}
				//update currentNode (not the node that has just been clicked)
				database.updateNode(currentNode);

				//redraw lines
				drawToNeighbors(currentNode);
				currentState = editorStates.DOINGNOTHING;
				break;
			default:
				//modify text fields to display node info
				nameField.setText(linkedNode.getName());
				typeField.setText(Integer.toString(linkedNode.getType()));
				xField.setText(Double.toString(linkedNode.getX()));
				yField.setText(Double.toString(linkedNode.getY()));

				//set current node/button
				currentNode = linkedNode;

				if(currentButton != null)
				{
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
		if(currentButton != null)
		{
			currentButton.setId("node-button-unselected");
		}
		currentButton = null;

		nameField.setText("");
		typeField.setText("");
		xField.setText("");
		yField.setText("");
	}

	/**
	 * toggle new node auto connection setting
	 * @param event
	 */
	@FXML
	void onToggleAutoConnect(ActionEvent event) {
		AUTOCONNECT = toggleAutoConnect.isSelected();
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
                database.updateNode(currentNode);

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
			System.out.println("Not a double");
		}
	}

	/**
	 * update a node's Type and update its corresponding image
	 * if updating a kiosk to a selected kiosk, set the other selected kiosk to a normal kiosk
	 * @param event
	 */
	@FXML
	void updateNodeType(ActionEvent event)
	{
		try
		{
			int newType = Integer.parseInt(typeField.getText());
			if (newType < 20 && newType >= 0)
			{
				if (newType == 5) //changing to selected kiosk
				{
					database.setSelectedKiosk(currentNode);
				}
				if (newType > 5 && newType < 20) //links between buildings
				{
					database.connectEntrances(currentNode, newType);
				} else if(currentNode.getType() > 5) //remove links between buildings if changing type to not be a link
				{
					database.removeEntranceConnection(currentNode, currentNode.getType());
				}
				//update type
				currentNode.setType(newType);
				database.updateNode(currentNode);
				for(Button b: nodeButtonLinks.keySet())
				{
					if(nodeButtonLinks.get(b) == currentNode)
					{
						setButtonImage(b, newType);
						break;
					}
				}
			}
		} catch (NumberFormatException e)
		{
			System.out.println("Not an int");
		}
	}

	/**
	 * Go through all of the neighbors of a given node and redraw lines to the source,
	 * as well as the lines from the source to the neighbors
	 * @param source The source node to redraw all lines to/from
	 */
	private void redrawAllNeighbors(Node source)
	{
		//redraw lines for any node that has currentNode as a neighbor
		//store nodes that need to be redrawn in a list as a workaround
		//for concurrentmodificationexception
		ArrayList<Node> toRedraw = new ArrayList<Node>();
		for(Node n: lineGroups.keySet()){
			boolean has = false;
			if(n.getNeighbors().contains(source)){
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
		drawToNeighbors(source);
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
			if(neighbor.getBuilding().equals(source.getBuilding()) &&
					neighbor.getFloor() == source.getFloor())
			{
				Line line = new Line();
				line.setStrokeWidth(LINEWIDTH);
				if (Accessibility.isHighContrast())
				{
					line.setStroke(Color.WHITE);
				}
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
				arrowTriangle.getPoints().addAll(0.0, 0.0,
						-5.0, 10.0,
						5.0, 10.0);

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
		}
		//store the array of lines for this source node into the hashmap
		//to be used to delete all lines later when redrawing
		lineGroups.put(source, groups);
	}

	/**
	 * bugfix function for when moving mouse over node while adding neighbor
	 * @param e
	 */
	void redrawCanvasBugfix(MouseEvent e)
	{
		//add canvas back if it has previously been removed
		if(!editingFloor.getChildren().contains(canvas)){
			editingFloor.getChildren().add(1, canvas);
		}
		//clear canvas
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

		Button draggedOver = (Button)e.getSource();

		switch(currentState){
			case CHAINADDING:
			case ADDINGNEIGHBOR:
				//draw line
				gc.setStroke(Color.BLACK);
				gc.setLineWidth(LINEWIDTH);
				gc.strokeLine(currentNode.getX(), currentNode.getY(), draggedOver.getLayoutX()+e.getX(),
						draggedOver.getLayoutY()+e.getY());
				break;
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

	/**
	 * Continuously draw and update the line on the canvas so that the user can see
	 * lines as they are adding connections.
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
			case CHAINADDING:
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

	/**
	 * Set controller to addingNeighbor state
	 */
	@FXML
	void addNeighbor(ActionEvent event)
	{
		if (currentNode != null)//Don't do anything unless a node is selected
		{
			currentState = editorStates.ADDINGNEIGHBOR;
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
			currentState = editorStates.REMOVINGNEIGHBOR;
		}
	}

	/**
	 *  Go back to startup screen
	 */
	@FXML
	void goBack()
	{
		loadFXML(Paths.ADMIN_PAGE_FXML);
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
        	nodeButtonLinks.remove(currentButton);
        	//hide details view
        	hideNodeDetails();
        }
    }
}
