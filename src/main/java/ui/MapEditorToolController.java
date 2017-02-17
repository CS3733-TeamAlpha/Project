package ui;

import data.Database;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import pathfinding.ConcreteNode;
import pathfinding.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static java.awt.SystemColor.window;

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


	//define widths for circles/lines that the canvas will draw
	double CIRCLEWIDTH = 13.0;
	double LINEWIDTH = 2.5;

    //currently selected node and button
    private Node currentNode = null;
    private Button currentButton = null;

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


	private String BUILDINGID = "00000000-0000-0000-0000-000000000000";
	
	private int currentFloor = 3; //TODO: make current floor relevant to new/modifying nodes
	//TODO: should nodes be able to be moved to different floor? probably not

	private ProxyImage removeNodeImageProxy = new ProxyImage(Paths.REMOVENODE);
	private ProxyImage chainImageProxy = new ProxyImage(Paths.CHAINNODES);
	private ProxyImage addNeighborImageProxy = new ProxyImage(Paths.ADDNODE);
	private ProxyImage removeNeighborImageProxy = new ProxyImage(Paths.REMOVENEIGHBOR);
	private ProxyImage doctorImageProxy = new ProxyImage(Paths.DOCTORICON);
	private ProxyImage restroomImageProxy = new ProxyImage(Paths.RESTROOMICON);
	private ProxyImage elevatorImageProxy = new ProxyImage(Paths.ELEVATORICON);

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

	static
	{
		//initialize connection and floor/node/provider lists right away
		//DatabaseController will hold onto these lists
	}

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

		//load all nodes for a specific floor, default to FLOORID
		loadNodesFromDatabase();

		//TODO: Load in image for a specific floor and determine canvas size
		//		based on image size

		//create canvas and graphicscontext
		//these will be used later for drawing lines in realtime and drag&drop visuals
		Group root = new Group();
		canvas = new Canvas(2200, 1300);
		canvas.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				clickFloorImage(e);
			}
		});
		gc = canvas.getGraphicsContext2D();
		editingFloor.getChildren().add(1, canvas);

		//style up/down buttons
		upFloor.setId("upbuttonTriangle");
		downFloor.setId("downbuttonTriangle");

		//set the floorImage imageview to display the correct floor's image.
		//TODO: make this work with multiple buildings
		setFloorImage(FLOORID);

		//set up event handlers for drag and drop images
		setupImageEventHandlers();
	}

	/**
	 * Set the floorImage imageview object to display the image of
	 * a specific floor.
	 * Currently only works based on floor, not building
	 * @param floor The floor to display
	 */
	private void setFloorImage(int floor)
	{
		if(floor == 1){
			floorImage.setImage(f1ImageProxy.getFXImage());
		} else if(floor == 2){
			floorImage.setImage(f2ImageProxy.getFXImage());
		} else if(floor == 3){
			floorImage.setImage(f3ImageProxy.getFXImage());
		} else if(floor == 4){
			floorImage.setImage(f4ImageProxy.getFXImage());
		} else if(floor == 5){
			floorImage.setImage(f5ImageProxy.getFXImage());
		} else if(floor == 6){
			floorImage.setImage(f6ImageProxy.getFXImage());
		} else if(floor == 7){
			floorImage.setImage(f7ImageProxy.getFXImage());
		}
	}

	/**
	 * Set event handlers for all images.
	 * These will handle drag events by setting state and moving the image,
	 * as well as the mouse release event at the end of the drag.
	 */
	private void setupImageEventHandlers()
	{
		//set the imaveview objects to use the correct images
		officeImage.setImage(doctorImageProxy.getFXImage());
		restroomImage.setImage(restroomImageProxy.getFXImage());
		elevatorImage.setImage(elevatorImageProxy.getFXImage());

		//For each image, set OnMouseDragged to change currentstate and
		//modify the images' XY coordinates
		//Any additional functionality that we want while dragging an image would go here

		hallwayImage.setOnMouseDragged(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent e)
			{
				currentState = editorStates.MAKINGNEWHALLWAY;
				hallwayImage.setX(e.getX()-hallwayImage.getFitWidth()/2);
				hallwayImage.setY(e.getY()-hallwayImage.getFitHeight()/2);
			}
		});
		officeImage.setOnMouseDragged(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent e)
			{
				currentState = editorStates.MAKINGNEWOFFICE;
				officeImage.setX(e.getX()-officeImage.getFitWidth()/2);
				officeImage.setY(e.getY()-officeImage.getFitHeight()/2);
			}
		});
		restroomImage.setOnMouseDragged(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent e)
			{
				currentState = editorStates.MAKINGNEWRESTROOM;
				restroomImage.setX(e.getX()-restroomImage.getFitWidth()/2);
				restroomImage.setY(e.getY()-restroomImage.getFitHeight()/2);
			}
		});
		elevatorImage.setOnMouseDragged(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent e)
			{
				currentState = editorStates.MAKINGNEWELEVATOR;
				elevatorImage.setX(e.getX()-elevatorImage.getFitWidth()/2);
				elevatorImage.setY(e.getY()-elevatorImage.getFitHeight()/2);
			}
		});

		//use same eventhandler for all images when mouse is released
		//on release, we reset the image location and then make a new node
		EventHandler releaseHandler = new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent e){
				//get source of mouserelease event so we can change its xy coordinates
				ImageView i = (ImageView)e.getSource();
				//set xy to 0 to return to original position
				i.setX(0);
				i.setY(0);

				//TODO: This is a hack
				// because I can't seem to figure out how to get a drop event
				// to fire while getting the right XY coordinates relative to the floorimage

				//modify our XY values based on the image's current scroll and the
				//dragged image's initial position on the screen

				//TODO: fix magic number: 15, probably about the size of the scrollbars
				Double fixX = e.getX()+palettePane.getLayoutX()+i.getLayoutX()+
						mainScroll.getHvalue()*(floorImage.getFitWidth()-mainScroll.getWidth()+15);
				Double fixY = e.getY()+palettePane.getLayoutY()+i.getLayoutY()+
						mainScroll.getVvalue()*(floorImage.getFitHeight()-mainScroll.getHeight()+15);

				dropNode(fixX, fixY);
			}
		};

		//add the release handler to each image
		hallwayImage.setOnMouseReleased(releaseHandler);
		officeImage.setOnMouseReleased(releaseHandler);
		elevatorImage.setOnMouseReleased(releaseHandler);
		restroomImage.setOnMouseReleased(releaseHandler);
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
		//TODO: convert color options to css for high contrast mode
		Arc radialMenu = new Arc(0, 0, CONTEXTRAD, CONTEXTRAD, 0, 360);
		radialMenu.setType(ArcType.OPEN);
		radialMenu.setStrokeWidth(CONTEXTWIDTH);
		radialMenu.setStroke(Color.GRAY);
		radialMenu.setStrokeType(StrokeType.INSIDE);
		radialMenu.setFill(null);
		radialMenu.setOpacity(0.95);

		//arc wedge will be used to indicate current selection
		//TODO: convert color options to css for high contrast mode
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

		//TODO: probaby replace these with all images later
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

				option1 = new ImageView(addNeighborImageProxy.getFXImage());
				option1.setScaleX(0.15);
				option1.setScaleY(0.15);
				option1.setX(imageOffsetX + CONTEXTRAD - CONTEXTWIDTH / 2);
				option1.setY(imageOffsetY);
				option2 = new ImageView(doctorImageProxy.getFXImage());
				option2.setScaleX(0.15);
				option2.setScaleY(0.15);
				option2.setX(imageOffsetX);
				option2.setY(imageOffsetY + CONTEXTRAD - CONTEXTWIDTH / 2);
				option3 = new ImageView(restroomImageProxy.getFXImage());
				option3.setScaleX(0.15);
				option3.setScaleY(0.15);
				option3.setX(imageOffsetX - CONTEXTRAD + CONTEXTWIDTH / 2);
				option3.setY(imageOffsetY);
				option4 = new ImageView(elevatorImageProxy.getFXImage());
				option4.setScaleX(0.15);
				option4.setScaleY(0.15);
				option4.setX(imageOffsetX);
				option4.setY(imageOffsetY - CONTEXTRAD + CONTEXTWIDTH / 2);
				break;
			case SHOWINGNODEMENU: //contextmenu for nodes
				//TODO: replace all of these with appropriate icons/pictures

				//node specific context menu

				option1 = new ImageView(addNeighborImageProxy.getFXImage());
				option1.setScaleX(0.15);
				option1.setScaleY(0.15);
				option1.setX(imageOffsetX + CONTEXTRAD - CONTEXTWIDTH / 2);
				option1.setY(imageOffsetY);
				option2 = new ImageView(removeNeighborImageProxy.getFXImage());
				option2.setScaleX(0.15);
				option2.setScaleY(0.15);
				option2.setX(imageOffsetX);
				option2.setY(imageOffsetY + CONTEXTRAD - CONTEXTWIDTH / 2);
				option3 = new ImageView(removeNodeImageProxy.getFXImage());
				option3.setScaleX(0.15);
				option3.setScaleY(0.15);
				option3.setX(imageOffsetX - CONTEXTRAD + CONTEXTWIDTH / 2);
				option3.setY(imageOffsetY);
				option4 = new ImageView(chainImageProxy.getFXImage());
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
	private Pane palettePane;

	@FXML
	private ImageView floorImage;

	@FXML
	private ImageView hallwayImage;

	@FXML
	private ImageView officeImage;

	@FXML
	private ImageView restroomImage;

	@FXML
	private ImageView elevatorImage;

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

	@FXML
	private VBox dndContainer;


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

	@FXML
	/**
	 * Floor image clicked, hide node details.
	 */
	void clickFloorImage(MouseEvent e)
	{
		if(e.isSecondaryButtonDown())
		{
			currentState = editorStates.DOINGNOTHING;
		} else
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

					break;
				default:
					hideNodeDetails();
					//set currentState to  indicate we are no longer in special state
					currentState = editorStates.DOINGNOTHING;
					break;
			}
		}
	}

	@FXML
	/**
	 * Change the current floor to increment up by 1.
	 * Prevent going down if floor is already 1.
	 * TODO: Make the min floor change depending on current building (iteration 3)
	 */
	void goDownFloor(ActionEvent event) {
		if(FLOORID > 1){
			//remove all buttons and lines on the current floor
			purgeButtonsAndLines();
			FLOORID--;
			loadNodesFromDatabase();
			currentFloorLabel.setText(Integer.toString(FLOORID));
			setFloorImage(FLOORID);
		}
	}

	@FXML
	/**
	 * Change the current floor to increment down by 1.
	 * Prevent going up if floor is already 7.
	 * TODO: Make the max floor change depending on current building (iteration 3)
	 */
	void goUpFloor(ActionEvent event) {
		if(FLOORID < 7){
			//remove all buttons and lines on the current floor
			purgeButtonsAndLines();
			FLOORID++;
			loadNodesFromDatabase();
			currentFloorLabel.setText(Integer.toString(FLOORID));
			setFloorImage(FLOORID);
		}
	}

	//TODO: update to match refactored database
	/**
	 * Create a new node at given xy coordinates
	 * @param x X coordinate of new node
	 * @param y Y coordinate of new node
	 */
	public void createNewNode(double x, double y, int type)
	{
		Node newNode = new ConcreteNode();
		newNode.setName("New " + type);
		//TODO: Set node type! It's -1 by default!
		newNode.setX(x);
		newNode.setY(y);
		newNode.setFloor(FLOORID);
		newNode.setBuilding(BUILDINGID);
		newNode.setType(type);

		database.insertNode(newNode);

		//TODO: set buttons to be appropriate image depending on node type
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
		}
	}

	private void setButtonImage(Button b, int type)
	{
		if(type == 1)
		{
			ImageView buttonImage = new ImageView(doctorImageProxy.getFXImage());
			buttonImage.setScaleX(0.15);
			buttonImage.setScaleY(0.15);
			b.setGraphic(buttonImage);
		}
		else if(type == 2)
		{
			ImageView buttonImage = new ImageView(elevatorImageProxy.getFXImage());
			buttonImage.setScaleX(0.15);
			buttonImage.setScaleY(0.15);
			b.setGraphic(buttonImage);
		}
		else if(type == 3)
		{
			ImageView buttonImage = new ImageView(restroomImageProxy.getFXImage());
			buttonImage.setScaleX(0.15);
			buttonImage.setScaleY(0.15);
			b.setGraphic(buttonImage);
		}
		else if(type == 0)
		{
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
				//currentState = editorStates.SHOWINGNODEMENU;
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
			if(currentState == editorStates.SHOWINGNODEMENU)
			{
				setupContextMenu(currentNode.getX(), currentNode.getY());
			}
			else
			{
				setupContextMenu(e.getX() - CIRCLEWIDTH / 2, e.getY() - CIRCLEWIDTH / 2);
			}
			editingFloor.getChildren().add(1, CONTEXTMENU);
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
						//TODO: set to proper state for whatever top option is
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
				database.updateNode(currentNode);
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
					System.out.println("stuff");
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
		 * TODO: update?
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

	/**
	 * modify a node's location on the map. alternately, cancel node location modification
	 * TODO: defunct function?
	 */
	@FXML
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
