package ui.controller;

import data.Node;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
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
import javafx.util.Duration;
import pathfinding.AStarGraph;
import pathfinding.Graph;
import pathfinding.TextualDirections;
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
	private String wrapToolTip = "";

	private Node selected;
	private Node kiosk;
	boolean findingDirections = false;
	boolean hasNextStep = false;
	boolean resetSteps = false;
	boolean jumping = false;
	int targetFloor = -1;
	String targetBuilding = "";

	private ArrayList<Line> currentPath = new ArrayList<Line>();

	public class MapZoomHandler implements EventHandler<ScrollEvent> {

		public MapZoomHandler()
		{
		}

		@Override
		public void handle(ScrollEvent scrollEvent) {
			//change scale based on scroll event and set scale/width/heights accordingly
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
	ChoiceBox buildingChoice = new ChoiceBox();

	//these variables are set just to help deal with the building UUIDs
	String faulkner = "00000000-0000-0000-0000-000000000000";
	String belkin = "00000000-0000-0000-0000-111111111111";
	String outside = "00000000-0000-0000-0000-222222222222";

	public MapController()
	{
		super();
	}

	public void initialize()
	{
		hideRoomInfo();
		//ArrayList<Node> nodes = database.getAllNodes();
		kiosk = database.getSelectedKiosk();
		graph = new AStarGraph();
		loadNodesFromDatabase(); //Get nodes in from database
		setFloorImage(BUILDINGID, FLOORID); //Set image of floor

		//style up/down buttons
		upFloor.setId("upbuttonTriangle");
		downFloor.setId("downbuttonTriangle");
		Node searched = getSearchedFor();
		if(searched!=null){
			System.out.println(searched.getName());
			BUILDINGID = searched.getBuilding();
			jumpFloor(searched.getFloor());
			selected = searched;
			findingDirections = true;
			showRoomInfo(searched);
			focusView(searched);
			setSearchedFor(null);
		}

		//set up the choicebox for changing buildings
		ArrayList<String> buildings = database.getBuildings();
		for(String s: buildings)
			System.out.println(s);
		buildingChoice.setItems(FXCollections.observableArrayList(buildings.toArray()));
		((Pane)currentFloorLabel.getParent()).getChildren().add(buildingChoice);
		buildingChoice.setLayoutX(49);
		buildingChoice.setLayoutY(106);
		buildingChoice.setOnAction(event -> changeBuilding((String)buildingChoice.getValue()));

		// sets the buildingChoice to display the correct initial floor
		// this is pretty inelegant but whatever
		if (BUILDINGID.equals(faulkner)) {
			buildingChoice.getSelectionModel().select(0);
		}
		else if (BUILDINGID.equals(belkin)) {
			buildingChoice.getSelectionModel().select(1);
		}
		else {
			buildingChoice.getSelectionModel().select(2);
		}

		nextStep.setDisable(true);
		previousStep.setDisable(true);

		//add event filter to let scrolling do zoom instead
		scroller.addEventFilter(ScrollEvent.ANY, new MapZoomHandler());

	}


	public void showRoomInfo(Node n)
	{
		Node focusNode = null;
		if(selected != n) //no selection, so set node that we're showing room info for as selected
		{
			selected = n;
			findingDirections = false;
			focusNode = n;
		}
		if(findingDirections)
		{
			ArrayList<Node> path = graph.findPath(kiosk,selected);

			ArrayList<String> textDirections = TextualDirections.getDirections(graph.findPath(kiosk, selected), 0.1);
			StringBuilder build = new StringBuilder();
			if(textDirections != null)
			{
				for (String sentence : textDirections)
				{
					build.append(sentence);
					build.append("\n");
				}
			}
			textDirectionsLabel.setText(build.toString());

			if(path == null){
				System.out.println("No path found");
				focusNode = kiosk;
				textDirectionsLabel.setText("No path found");
			}else
			{
				if(currentPath.size() != 0){
					for(Line l: currentPath){
						((AnchorPane) l.getParent()).getChildren().remove(l);
					}
					currentPath.clear();
				}
				//flip path to be ordered
				for(int i=0; i<path.size()/2; i++)
				{
					Node temp = path.get(i);
					path.set(i, path.get(path.size()-1-i));
					path.set(path.size()-1-i, temp);

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
						line.setStartX(path.get(i).getX()+ PATH_LINE_OFFSET); //plus 15 to center on button
						line.setStartY(path.get(i).getY()+PATH_LINE_OFFSET);
						line.setEndX(path.get(i+1).getX()+PATH_LINE_OFFSET);
						line.setEndY(path.get(i+1).getY()+PATH_LINE_OFFSET);
						line.setStrokeWidth(10);
//						// Change color for line if it is high contrast
//						if (Accessibility.isHighContrast()) {
//							line.setStroke(Color.WHITE);
//						} else {
						line.setStroke(Color.BLUE);
//						}
						editingFloor.getChildren().add(1,line);
						currentPath.add(line);
					}
				}

				if(!hasNextStep)
				{
					//if target is in different building/floor this path has a next step
					if(!n.getBuilding().equals(kiosk.getBuilding()) ||
							n.getFloor() != kiosk.getFloor())
					{
						hasNextStep = true;
						nextStep.setDisable(false);
					}
				}
			}
			//findingDirections = false;
		}
		if (!roomInfoShown)
		{
			roomviewSplit.getItems().add(1, roomInfo);
			roomviewSplit.setDividerPositions(.75);
			roomInfoShown = true;
		}

		if(focusNode != null)
		{
			focusView(focusNode);
			focusNode = null;
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
		jumping = false;
	}

	/**
	 * focus the view of the map to center on the node
	 * @param n The node to focus view on
	 */
	private void focusView(Node n){

		if(zoomWrapper.getWidth() > scroller.getWidth() || zoomWrapper.getHeight() > scroller.getHeight())
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
				scroller.hvalueProperty().setValue((focusX-scroller.getWidth()/2)/
						(zoomWrapper.getWidth()-scroller.getWidth()));
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
				scroller.vvalueProperty().setValue((focusY-scroller.getHeight()/2)/
						(zoomWrapper.getHeight()-scroller.getHeight()));
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
		BUILDINGID = kiosk.getBuilding();
		jumpFloor(kiosk.getFloor());
		findingDirections = true;
		previousStep.setDisable(true);
		jumping = true;
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
		if(hasNextStep)
		{
			hasNextStep = false;
			resetSteps = true;
		}
		jumping = false;
		if(selected != null)
		{
			showRoomInfo(selected);
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
		if(hasNextStep)
		{
			hasNextStep = false;
			resetSteps = true;
		}
		jumping = false;
		if(selected != null)
		{
			showRoomInfo(selected);
		}
	}

	/**
	 * Go to next step in path
	 * Prevent moving if already on last floor
	 * @param event
	 */
	@FXML
	public void goNextStep(ActionEvent event) {
		jumping = true;
		if(resetSteps)
		{
			//reset to start from the kiosk
			resetSteps();
		}
		else if(hasNextStep)
		{
			//previousstep button starts disabled, so reenable after going to next step
			previousStep.setDisable(false);
			//clear lines
			if(currentPath.size() != 0){
				for(Line l: currentPath){
					((AnchorPane) l.getParent()).getChildren().remove(l);
				}
				currentPath.clear();
			}
			if(!selected.getBuilding().equals(kiosk.getBuilding()))
			{
				//if we're not on ground floor and we're in kiosk's building, go to bottom floor
				if(FLOORID != 1 && BUILDINGID.equals(kiosk.getBuilding()))
				{
					jumpFloor(1);
				}
				//if we are already in target building, go to target floor
				else if (BUILDINGID.equals(selected.getBuilding()))
				{
					jumpFloor(selected.getFloor());
				}
				else
				{
					//if we are in outside building, go into target building
					if(BUILDINGID.equals("00000000-0000-0000-0000-222222222222"))
					{
						purgeButtons();
						BUILDINGID = selected.getBuilding();
						loadNodesFromDatabase();
						currentFloorLabel.setText(Integer.toString(FLOORID));
						setFloorImage(BUILDINGID, FLOORID);
					}
					else //otherwise go outside
					{
						purgeButtons();
						BUILDINGID = "00000000-0000-0000-0000-222222222222";
						loadNodesFromDatabase();
						currentFloorLabel.setText(Integer.toString(FLOORID));
						setFloorImage(BUILDINGID, FLOORID);
					}
				}

				findingDirections = true;
				showRoomInfo(selected);
			}
			//if selected is in same building as kiosk
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
		fadeinWrapper();
	}

	/**
	 * work in progress
	 */
	private void fadeinWrapper()
	{
		FadeTransition fit = new FadeTransition(Duration.millis(2000), zoomWrapper);
		fit.setFromValue(0.0);
		fit.setToValue(1.0);
		fit.play();
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
		//reset all stuff so we go to the kiosk view
		BUILDINGID = kiosk.getBuilding();
		FLOORID = kiosk.getFloor();
		purgeButtons();
		loadNodesFromDatabase();
		currentFloorLabel.setText(Integer.toString(FLOORID));
		setFloorImage(BUILDINGID, FLOORID);
		findingDirections = true;
		showRoomInfo(selected);
	}

	/**
	 * Go to previous floor in path
	 * Prevent moving if already on first floor
	 * @param event
	 */
	@FXML
	public void goPreviousStep(ActionEvent event) {
		jumping = true;
		if(resetSteps)
		{
			//reset to kiosk view, first step
			resetSteps();
		}
		else if(hasNextStep)
		{
			//reenable nextstep button if we went to a previous step
			nextStep.setDisable(false);
			//clear lines
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
				// If we are outside the previous step is going into the kiosk building
				else if(BUILDINGID.equals("00000000-0000-0000-0000-222222222222"))
				{
					purgeButtons();
					BUILDINGID = kiosk.getBuilding();
					loadNodesFromDatabase();
					currentFloorLabel.setText(Integer.toString(FLOORID));
					setFloorImage(BUILDINGID, FLOORID);
				} else if(FLOORID == 1) {
					//if we are on the first floor of belkin/faulkner, the previus step is to go outside
					purgeButtons();
					BUILDINGID = "00000000-0000-0000-0000-222222222222";
					loadNodesFromDatabase();
					currentFloorLabel.setText(Integer.toString(FLOORID));
					setFloorImage(BUILDINGID, FLOORID);
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
		textDirectionsLabel.setText("");

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
		if(hasNextStep)
		{
			hasNextStep = false;
			resetSteps = true;
		}
		jumping = false;
		if(selected != null)
		{
			showRoomInfo(selected);
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
				if(n.getProviders().size() > 0) {
					for (int i = 0; i < n.getProviders().size(); i++) {
						wrapToolTip.concat(n.getProviders().get(i).getFirstName());
						wrapToolTip.concat(", ");
						wrapToolTip.concat(n.getProviders().get(i).getLastName());
					}
					Tooltip t = new Tooltip(wrapToolTip);
					Tooltip.install(nodeB, t);
				} else {
					Tooltip t = new Tooltip("There are no providers");
					Tooltip.install(nodeB, t);
				}
				nodeB.setCursor(Cursor.HAND);
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
