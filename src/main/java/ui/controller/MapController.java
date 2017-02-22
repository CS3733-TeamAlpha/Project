package ui.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import pathfinding.ConcreteGraph;
import pathfinding.Graph;
import pathfinding.Node;
import ui.Accessibility;
import ui.Paths;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MapController extends BaseController
{
	public static final int PATH_LINE_OFFSET = 0;
	public Label textDirectionsLabel;
	//public ImageView floorImage;
	private Graph graph;
	private boolean roomInfoShown;
	private HashMap<Button, Node> nodeButtons = new HashMap<Button, Node>();

	private Node selected;
	private Node kiosk;
	boolean findingDirections = false;
	boolean hasNextStep = false;
	boolean resetSteps = false;
	int targetFloor = -1;
	String targetBuilding = "";

	private ArrayList<Line> currentPath = new ArrayList<Line>();

	public class MapZoomHandler implements EventHandler<ScrollEvent> {

		public MapZoomHandler()
		{
		}

		@Override
		public void handle(ScrollEvent scrollEvent) {
			final double scale = calculateScale(scrollEvent);
			editingFloor.setScaleX(scale);
			editingFloor.setScaleY(scale);
			zoomWrapper.setMinWidth(editingFloor.getWidth() * scale);
			zoomWrapper.setMinHeight(editingFloor.getHeight() * scale);
			zoomWrapper.setMaxWidth(editingFloor.getWidth() * scale);
			zoomWrapper.setMaxHeight(editingFloor.getHeight() * scale);

			editingFloor.setLayoutX((zoomWrapper.getWidth() - editingFloor.getWidth()) / 2);
			editingFloor.setLayoutY((zoomWrapper.getHeight() - editingFloor.getHeight()) / 2);
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

	@FXML
	private SplitPane roomviewSplit;
	@FXML
	private ScrollPane scroller;
	@FXML
	private VBox roomInfo;
	@FXML
	private Label roomName;
	@FXML
	private Label roomDescription;
	@FXML
	private AnchorPane editingFloor;
	@FXML
	private AnchorPane zoomWrapper;
	@FXML
	private Button upFloor;
	@FXML
	private Button downFloor;
	@FXML
	private Label currentFloorLabel;
	@FXML
	private ImageView floorImage;
	@FXML
	private Button previousStep;
	@FXML
	private Button nextStep;

	ArrayList<Label> loadedLabels = new ArrayList<>();
	ChoiceBox buildingChoice = null;

	public MapController()
	{
		super();
	}

	public void initialize()
	{
		hideRoomInfo();
		//ArrayList<Node> nodes = database.getAllNodes();
		kiosk = database.getSelectedKiosk();
		graph = new ConcreteGraph();
		loadNodesFromDatabase(); //Get nodes in from database
		setFloorImage(BUILDINGID, FLOORID); //Set image of floor

		//style up/down buttons
		upFloor.setId("upbuttonTriangle");
		downFloor.setId("downbuttonTriangle");
		Node searched = getSearchedFor();
		if(searched!=null){
			System.out.println(searched.getName());
			jumpFloor(searched.getFloor());
			selected = searched;
			showRoomInfo(searched);
			focusView(searched);
			setSearchedFor(null);
		}

		//set up the choicebox for changing buildings
		ArrayList<String> buildings = database.getBuildings();
		for(String s: buildings)
			System.out.println(s);
		buildingChoice = new ChoiceBox();
		buildingChoice.setItems(FXCollections.observableArrayList(buildings.toArray()));
		((Pane)currentFloorLabel.getParent()).getChildren().add(buildingChoice);
		buildingChoice.setLayoutX(49);
		buildingChoice.setLayoutY(106);
		buildingChoice.setOnAction(event ->
				{
					changeBuilding((String)buildingChoice.getValue());
				}
		);
//		buildingChoice.setValue("faulkner_main");

		nextStep.setDisable(true);
		previousStep.setDisable(true);

		//add event filter to let scrolling do zoom instead
		scroller.addEventFilter(ScrollEvent.ANY, new MapZoomHandler());

	}


	public void showRoomInfo(Node n)
	{
		if(selected != n)
		{
			selected = n;
			findingDirections = false;
		}
		if(findingDirections)
		{
			Node focusNode = null;
			if(!hasNextStep)
			{
				BUILDINGID = kiosk.getBuilding();
				jumpFloor(kiosk.getFloor());
				focusNode = kiosk;
			}
			ArrayList<Node> path = graph.findPath(kiosk,selected);

			ArrayList<String> textDirections = graph.textDirect(kiosk, selected, 0.1);
			StringBuilder build = new StringBuilder();
			for(String sentence : textDirections)
			{
				build.append(sentence);
				build.append("\n");
			}
			textDirectionsLabel.setText(build.toString());

			if(path == null){
				System.out.println("No path found");
			}else
			{
				if(currentPath.size() != 0){
					for(Line l: currentPath){
						((AnchorPane) l.getParent()).getChildren().remove(l);
					}
					currentPath.clear();
				}
				for (int i = 0; i < path.size()-1; i++)
				{
					if(path.get(i).getFloor() == FLOORID && path.get(i).getBuilding().equals(BUILDINGID)
							&& path.get(i+1).getFloor() == FLOORID && path.get(i+1).getBuilding().equals(BUILDINGID))
					{
						if(focusNode == null)
						{
							focusNode = path.get(i);
						}
						Line line = new Line();
						//System.out.println("Line from "+path.get(i).getX()+", "+path.get(i).getY()+" to "+path.get(i+1).getX()+", "+path.get(i + 1).getY());
						//System.out.println(path.get(i+1).getID());
						line.setStartX(path.get(i).getX()+ PATH_LINE_OFFSET); //plus 15 to center on button
						line.setStartY(path.get(i).getY()+PATH_LINE_OFFSET);
						line.setEndX(path.get(i+1).getX()+PATH_LINE_OFFSET);
						line.setEndY(path.get(i+1).getY()+PATH_LINE_OFFSET);
						line.setStrokeWidth(10);
						// Change color for line if it is high contrast
						if (Accessibility.isHighContrast()) {
							line.setStroke(Color.WHITE);
						} else {
							line.setStroke(Color.BLUE);
						}
						editingFloor.getChildren().add(1,line);
						currentPath.add(line);
					}
				}

				if(!hasNextStep)
				{
					if(!n.getBuilding().equals(kiosk.getBuilding()) ||
							n.getFloor() != kiosk.getFloor())
					{
						hasNextStep = true;
						nextStep.setDisable(false);
					}
				}
				focusView(focusNode);
				focusNode = null;
			}
			//findingDirections = false;
		}
		if (!roomInfoShown)
		{
			roomviewSplit.getItems().add(1, roomInfo);
			roomviewSplit.setDividerPositions(.75);
			roomInfoShown = true;
		}
		roomName.setText(n.getName());
		//roomDescription.setText(n.getData().get(1)); //TODO: implement a proper node description field
	}

	public void hideRoomInfo()
	{
		roomviewSplit.getItems().remove(roomInfo);
		roomInfoShown = false;

		selected = null;

		findingDirections = false;
		hasNextStep = false;
	}

	/**
	 * focus the view of the map to center on the node
	 * @param n The node to focus view on
	 */
	private void focusView(Node n){

		if(zoomWrapper.getWidth() > scroller.getWidth())
		{
			double focusX = n.getX()*currentZoom+editingFloor.getLayoutX();
			double focusY = n.getY()*currentZoom+editingFloor.getLayoutY();

			if(zoomWrapper.getWidth()-focusX < scroller.getWidth()/2)
			{
				scroller.hvalueProperty().setValue(1);
			}
			else if(focusX < scroller.getWidth()/2)
			{
				scroller.hvalueProperty().setValue(0);
			}
			else
			{
				scroller.hvalueProperty().setValue(focusX/(zoomWrapper.getWidth()-scroller.getWidth()/2));
			}

			if(focusY < scroller.getHeight()/2)
			{
				scroller.vvalueProperty().setValue(0);
			}
			else if(zoomWrapper.getHeight()-focusY < scroller.getHeight()/2)
			{
				scroller.vvalueProperty().setValue(1);
			}
			else
			{
				scroller.vvalueProperty().setValue(focusY/(zoomWrapper.getHeight()-scroller.getHeight()/2));
			}
		}
	}

	public void showStartup()
	{
		loadFXML(Paths.STARTUP_FXML);
	}

	public void findDirectionsTo(){
		if(hasNextStep)
		{
			hasNextStep = false;
		}
		jumpFloor(kiosk.getFloor());
		findingDirections = true;
		showRoomInfo(selected);
	}


	/**
	 * jump directly to the target floor
	 */
	void jumpFloor(int floor)
	{
		purgeButtons();
		FLOORID = floor;
		loadNodesFromDatabase();
		currentFloorLabel.setText(Integer.toString(FLOORID));
		setFloorImage(BUILDINGID, FLOORID);
	}

	@FXML
	/**
	 * Change the current floor to increment up by 1.
	 * Prevent going down if floor is already 1.
	 * TODO: Stole this from map editor, may want to fix
	 */
	void goDownFloor(ActionEvent event) {
		if(currentPath.size() != 0){
			for(Line l: currentPath){
				((AnchorPane) l.getParent()).getChildren().remove(l);
			}
			currentPath.clear();
		}
		if(FLOORID > 1){
			//remove all buttons and lines on the current floor
			purgeButtons();
			FLOORID--;
			loadNodesFromDatabase();
			currentFloorLabel.setText(Integer.toString(FLOORID));
			setFloorImage(BUILDINGID, FLOORID);
		}
		if(selected != null)
		{
			showRoomInfo(selected);
		}

		if(hasNextStep)
		{
			resetSteps = true;
		}
	}

	@FXML
	/**
	 * Change the current floor to increment down by 1.
	 * Prevent going up if floor is already 7.
	 * TODO: Stole this from map editor, may want to fix
	 */
	void goUpFloor (ActionEvent event){
		int topFloor;

		if(currentPath.size() != 0){
			for(Line l: currentPath){
				((AnchorPane) l.getParent()).getChildren().remove(l);
			}
			currentPath.clear();
		}

		if(BUILDINGID.equals("00000000-0000-0000-0000-111111111111")) { //If we are in belkin house
			topFloor = 4;
		} else if(BUILDINGID.equals("00000000-0000-0000-0000-000000000000")) { //If we are in Faulkner House
			topFloor = 7;
		} else { //Assume we are outside at this point
			topFloor = 1;
		}
		if(FLOORID < topFloor){
			//remove all buttons and lines on the current floor
			purgeButtons();
			FLOORID++;
			loadNodesFromDatabase();
			currentFloorLabel.setText(Integer.toString(FLOORID));
			setFloorImage(BUILDINGID, FLOORID);
		}
		if(selected != null)
		{
			showRoomInfo(selected);
		}
		if(hasNextStep)
		{
			resetSteps = true;
		}
	}

	/**
	 * Go to next floor in path
	 * Prevent moving if already on last floor
	 * @param event
	 */
	@FXML
	public void goNextStep(ActionEvent event) {
		if(resetSteps)
		{
			resetSteps();
		}
		else if(hasNextStep)
		{
			previousStep.setDisable(false);
			if(currentPath.size() != 0){
				for(Line l: currentPath){
					((AnchorPane) l.getParent()).getChildren().remove(l);
				}
				currentPath.clear();
			}
			if(!selected.getBuilding().equals(kiosk.getBuilding()))
			{
				if(FLOORID != 1)
				{
					jumpFloor(1);
				}
				else
				{
					if(BUILDINGID.equals("00000000-0000-0000-0000-222222222222"))//if we are in outsidefloor
					{
						purgeButtons();
						BUILDINGID = selected.getBuilding();
						loadNodesFromDatabase();
						currentFloorLabel.setText(Integer.toString(FLOORID));
						setFloorImage(BUILDINGID, FLOORID);
//						if(BUILDINGID.equals("00000000-0000-0000-0000-000000000000"))
//						{
//							buildingChoice.setValue("faulkner_main");
//						} else
//						{
//							buildingChoice.setValue("belkin_house");
//						}
					}
					else //otherwise go outside
					{
						purgeButtons();
						BUILDINGID = "00000000-0000-0000-0000-222222222222";
						loadNodesFromDatabase();
						currentFloorLabel.setText(Integer.toString(FLOORID));
						setFloorImage(BUILDINGID, FLOORID);
//						buildingChoice.setValue("outdoors");
					}
				}

				findingDirections = true;
				showRoomInfo(selected);
			}
			else
			{
				//jump to correct floor
				jumpFloor(selected.getFloor());
				findingDirections = true;
				showRoomInfo(selected);
			}
		}
		if(BUILDINGID.equals(selected.getBuilding()) && FLOORID == selected.getFloor())
		{
			nextStep.setDisable(true);
		}
	}

	/**
	 * Reset to kiosk location
	 */
	private void resetSteps()
	{
		//idiot proof, reset to kiosk step
		resetSteps = false;
		previousStep.setDisable(true);
		nextStep.setDisable(false);
		if(currentPath.size() != 0){
			for(Line l: currentPath){
				((AnchorPane) l.getParent()).getChildren().remove(l);
			}
			currentPath.clear();
		}
		BUILDINGID = kiosk.getBuilding();
		FLOORID = kiosk.getFloor();
		purgeButtons();
		loadNodesFromDatabase();
		currentFloorLabel.setText(Integer.toString(FLOORID));
		setFloorImage(BUILDINGID, FLOORID);
		findingDirections = true;
		showRoomInfo(selected);
//		if(BUILDINGID.equals("00000000-0000-0000-0000-000000000000"))
//		{
//			buildingChoice.setValue("faulkner_main");
//		} else
//		{
//			buildingChoice.setValue("belkin_house");
//		}
	}

	/**
	 * Go to previous floor in path
	 * Prevent moving if already on first floor
	 * @param event
	 */
	@FXML
	public void goPreviousStep(ActionEvent event) {
		if(resetSteps)
		{
			resetSteps();
		}
		else if(hasNextStep)
		{
			nextStep.setDisable(false);
			if(currentPath.size() != 0){
				for(Line l: currentPath){
					((AnchorPane) l.getParent()).getChildren().remove(l);
				}
				currentPath.clear();
			}
			if(!selected.getBuilding().equals(kiosk.getBuilding())) // If selected is in a different building
			{
				if(FLOORID == 1 && (BUILDINGID == kiosk.getBuilding())) // If we are in kiosk building
				{
					jumpFloor(kiosk.getFloor());
				}
				else if(BUILDINGID.equals("00000000-0000-0000-0000-222222222222")) // If we are outside
				{
					purgeButtons();
					BUILDINGID = kiosk.getBuilding();
					loadNodesFromDatabase();
					currentFloorLabel.setText(Integer.toString(FLOORID));
					setFloorImage(BUILDINGID, FLOORID);
//					if(BUILDINGID.equals("00000000-0000-0000-0000-000000000000"))
//					{
//						buildingChoice.setValue("faulkner_main");
//					} else
//					{
//						buildingChoice.setValue("belkin_house");
//					}
				} else if(FLOORID == 1) { // Check to see if selected is on bottom floor
					purgeButtons();
					BUILDINGID = "00000000-0000-0000-0000-222222222222";
					loadNodesFromDatabase();
					currentFloorLabel.setText(Integer.toString(FLOORID));
					setFloorImage(BUILDINGID, FLOORID);
//					buildingChoice.setValue("outdoors");
				} else { // Must be on upper floor in selected's building
					jumpFloor(1); // Go to first floor of selected's building
				}

				findingDirections = true;
				showRoomInfo(selected);
			}
			else
			{
				//jump to kiosk floor
				jumpFloor(kiosk.getFloor());
				findingDirections = true;
				showRoomInfo(selected);
			}
		}
		if(BUILDINGID.equals(kiosk.getBuilding()) && FLOORID == kiosk.getFloor())
		{
			previousStep.setDisable(true);
		}
	}

	/**
	 * Clear line showing path, deselect all nodes
	 * @param event
	 */
	@FXML
	void clearPath(ActionEvent event) {
		hasNextStep = false;
		findingDirections = false;
		if(currentPath.size() != 0){
			for(Line l: currentPath){
				((AnchorPane) l.getParent()).getChildren().remove(l);
			}
			currentPath.clear();
		}
		nextStep.setDisable(true);
		previousStep.setDisable(true);
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
		purgeButtons();

		if(currentPath.size() != 0){
			for(Line l: currentPath){
				((AnchorPane) l.getParent()).getChildren().remove(l);
			}
			currentPath.clear();
		}
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
		if(selected != null)
		{
			showRoomInfo(selected);
		}
		if(hasNextStep)
		{
			resetSteps = true;
		}

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
			//TODO: possibly reimplement highcontrast
			//if(Accessibility.isHighContrast())
			//{
			//	floorImage.setImage(Paths.contrastFloorImages[floor-1].getFXImage());
			//}
			//else
			//{
			floorImage.setImage(Paths.regularFloorImages[floor-1].getFXImage());
			//}
		}
		else if(buildingid.equals("00000000-0000-0000-0000-111111111111"))
		{
			floorImage.setImage(Paths.belkinFloorImages[floor-1].getFXImage());
		}
		else if (buildingid.equals("00000000-0000-0000-0000-222222222222"))
		{
			//TODO: fix path of outdoor image
			floorImage.setImage(Paths.outdoorImageProxy.getFXImage());
		}

		editingFloor.setMinWidth(floorImage.getFitWidth());
		editingFloor.setMinHeight(floorImage.getFitHeight());
		editingFloor.setMaxWidth(floorImage.getFitWidth());
		editingFloor.setMaxHeight(floorImage.getFitHeight());

		final double scale = 1;
		currentZoom = scale;
		editingFloor.setScaleX(scale);
		editingFloor.setScaleY(scale);

		zoomWrapper.setMinWidth(floorImage.getFitWidth());
		zoomWrapper.setMinHeight(floorImage.getFitHeight());
		zoomWrapper.setMaxWidth(floorImage.getFitWidth());
		zoomWrapper.setMaxHeight(floorImage.getFitHeight());

		editingFloor.setLayoutX(0);
		editingFloor.setLayoutY(0);

	}

	// TODO: Stole this from map editor, may want to fix


	/**
	 * Create a button on the scene and associate it with a node
	 *
	 * @param n the node to load into the scene
	 * TODO: Stole this from map editor, may want to fix
	 */
	private void loadNode(Node n, ArrayList<LabelThingy> thingies)
	{
		if(n.getType() != 0)
		{
			//new button
			Button nodeB = new Button();

			//experimental style changes to make the button a circle
			nodeB.setId("node-button-unselected");

			//add node to nodeButtons
			nodeButtons.put(nodeB, n);

			//set button XY coordinates
			nodeB.setLayoutX(n.getX() - XOFFSET);
			nodeB.setLayoutY(n.getY() - YOFFSET);

			setButtonImage(nodeB, n.getType());
			if (n.getType() == 1)
			{
				boolean changed = false;
				for(LabelThingy thingy : thingies)
				{
					if(thingy.x == n.getX() && thingy.y == n.getY())
					{
						changed = true;
						if(!thingy.text.isEmpty())
						{
							thingy.text += ", ";
						}
						thingy.text += n.getName().trim();
					}
				}

				if(!changed)
				{
					LabelThingy temp = new LabelThingy();
					temp.x = (int)n.getX();
					temp.y = (int)n.getY();
					temp.displayX = (int)nodeB.getLayoutX();
					temp.displayY = (int)nodeB.getLayoutY();
					temp.text = n.getName().trim();
					thingies.add(temp);
				}
			}

			nodeB.hoverProperty().addListener(l->{
				ToolTipManager.sharedInstance().setInitialDelay(0);
				ToolTipManager.sharedInstance().setDismissDelay(100000);
				String[] splittedName = n.getName().split(";");
				//ToDO: switch the below index to 1 when the new data is implemented
				String descNames = splittedName[0];
				Tooltip t = new Tooltip(descNames);
				nodeB.setCursor(Cursor.HAND);
				Tooltip.install(nodeB, t);
			});
				nodeB.setOnAction(event -> showRoomInfo(n));
			//add button to scene
			editingFloor.getChildren().add(1, nodeB);
		}
	}

	private void addLabels(LabelThingy thingy)
	{
		Label roomLabel = new Label(thingy.text);
		roomLabel.setLayoutX(thingy.x-10);
		roomLabel.setLayoutY(thingy.y-35);
		roomLabel.setId("roomLabel");
		editingFloor.getChildren().add(1, roomLabel);
		loadedLabels.add(roomLabel);
	}

	/**
	 * Load all nodes from the databasecontroller's list of nodes onto our scene
	 * TODO: Stole this from map editor, may want to fix
	 */
	public void loadNodesFromDatabase()
	{
		ArrayList<LabelThingy> points = new ArrayList<>();

		ArrayList<Node> nodesInBuildingFloor = database.getNodesInBuildingFloor(BUILDINGID, FLOORID);
		for (Node n : nodesInBuildingFloor)
			loadNode(n, points);

		for(LabelThingy thingy : points)
		{
			addLabels(thingy);
		}
	}

	/**
	 * Hide all node buttons for the current floor, in preperation for changing floors.
	 */
	private void purgeButtons()
	{
		for(Button b: nodeButtons.keySet())
		{
			Node linkedNode = nodeButtons.get(b);

			//remove this button from the UI
			((AnchorPane) b.getParent()).getChildren().remove(b);
		}
		//clear all entries in nodeButtonLinks
		nodeButtons.clear();

		for(Label l : loadedLabels)
		{
			((AnchorPane) l.getParent()).getChildren().remove(l);
		}
		loadedLabels.clear();
	}


	class LabelThingy
	{
		int displayX, displayY;
		int x, y;
		String text = "";
	}
}
