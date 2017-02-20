package ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import pathfinding.ConcreteGraph;
import pathfinding.Graph;
import pathfinding.Node;

import java.awt.*;
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
	boolean pathingUp = false;
	boolean pathingDown = false;
	int targetFloor = -1;

	private ArrayList<Line> currentPath = new ArrayList<Line>();

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
	private Button upFloor;
	@FXML
	private Button downFloor;
	@FXML
	private Label currentFloorLabel;
	@FXML
	private ImageView floorImage;

	ArrayList<Label> loadedLabels = new ArrayList<>();

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
		setFloorImage(FLOORID); //Set image of floor

		//style up/down buttons
		upFloor.setId("upbuttonTriangle");
		downFloor.setId("downbuttonTriangle");
		Node searched = getSearchedFor();
		if(searched!=null){
			System.out.println(searched.getName());
			jumpFloor(searched.getFloor());

			//TODO - FIX SCROLLING
			double width = scroller.getContent().getBoundsInLocal().getWidth();
			double height = scroller.getContent().getBoundsInLocal().getHeight();
			System.out.println(searched.getX()/(width-scroller.getWidth()));
			System.out.println(searched.getY()/(height-scroller.getHeight()));
			scroller.setHvalue(searched.getX()/(width-scroller.getWidth()));
			scroller.setVvalue(1-searched.getY()/(height-scroller.getHeight()));


			selected = searched;
			showRoomInfo(searched);
			setSearchedFor(null);
		}
	}

	public void showRoomInfo(Node n)
	{
		if(findingDirections)
		{
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

					if(path.get(i).getFloor() == FLOORID)
					{
						Line line = new Line();
						System.out.println("Line from "+path.get(i).getX()+", "+path.get(i).getY()+" to "+path.get(i+1).getX()+", "+path.get(i + 1).getY());
						System.out.println(path.get(i+1).getID());
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

				if(!pathingUp && !pathingDown)
				{
					if (n.getFloor() > kiosk.getFloor())
					{
						targetFloor = n.getFloor();
						pathingUp = true;
					} else if (n.getFloor() < kiosk.getFloor())
					{
						targetFloor = n.getFloor();
						pathingDown = true;
					}
				} else
				{
					pathingUp = false;
					pathingDown = false;
				}
			}
			findingDirections = false;
		}
		selected = n;
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
	}


	public void showStartup()
	{
		loadFXML(Paths.STARTUP_FXML);
	}

	public void findDirectionsTo(){
		jumpFloor(kiosk.getFloor());
		findingDirections = true;
		showRoomInfo(selected);
	}


	void goToNode(Node n){

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
		setFloorImage(FLOORID);
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
		if(pathingUp || pathingDown){
			jumpFloor(targetFloor);
			findingDirections = true;
			showRoomInfo(selected);
		}
		else if(FLOORID > 1){
			//remove all buttons and lines on the current floor
			purgeButtons();
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
	 * TODO: Stole this from map editor, may want to fix
	 */
	void goUpFloor (ActionEvent event){
		if(currentPath.size() != 0){
			for(Line l: currentPath){
				((AnchorPane) l.getParent()).getChildren().remove(l);
			}
			currentPath.clear();
		}
		if(pathingUp || pathingDown){
			jumpFloor(targetFloor);
			findingDirections = true;
			showRoomInfo(selected);
		}
		else if(FLOORID < 7){
			//remove all buttons and lines on the current floor
			purgeButtons();
			FLOORID++;
			loadNodesFromDatabase();
			currentFloorLabel.setText(Integer.toString(FLOORID));
			setFloorImage(FLOORID);
		}
	}

	/**
	 * Set the floorImage imageview object to display the image of
	 * a specific floor.
	 * Currently only works based on floor, not building
	 * @param floor The floor to display
	 */
	private void setFloorImage(int floor)
	{
		if(Accessibility.isHighContrast())
		{
			floorImage.setImage(Paths.contrastFloorImages[floor-1].getFXImage());
		}
		else
		{
			floorImage.setImage(Paths.regularFloorImages[floor-1].getFXImage());
		}
	}

	// TODO: Stole this from map editor, may want to fix
	private void setButtonImage(Button b, int type)
	{
		if(type == 1)
		{
			ImageView buttonImage = new ImageView(Paths.doctorImageProxy.getFXImage());
			buttonImage.setScaleX(0.15);
			buttonImage.setScaleY(0.15);
			b.setGraphic(buttonImage);
		}
		else if(type == 2)
		{
			ImageView buttonImage = new ImageView(Paths.elevatorImageProxy.getFXImage());
			buttonImage.setScaleX(0.15);
			buttonImage.setScaleY(0.15);
			b.setGraphic(buttonImage);
		}
		else if(type == 3)
		{
			ImageView buttonImage = new ImageView(Paths.restroomImageProxy.getFXImage());
			buttonImage.setScaleX(0.15);
			buttonImage.setScaleY(0.15);
			b.setGraphic(buttonImage);
		}
		else if(type == 4 || type == 5)
		{
			ImageView buttonImage = new ImageView(Paths.kioskImageProxy.getFXImage());
			buttonImage.setScaleX(0.15);
			buttonImage.setScaleY(0.15);
			b.setGraphic(buttonImage);
		}
		else if(type == 0)
		{
		}
	}

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
