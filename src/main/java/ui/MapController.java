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

import java.util.ArrayList;
import java.util.HashMap;

public class MapController extends BaseController
{
	public static final int PATH_LINE_OFFSET = 0;
	//public ImageView floorImage;
	private Graph graph;
	private boolean roomInfoShown;
	private boolean pathGoesDown = false;
	private boolean pathGoesUp = false;
	private HashMap<Button, Node> nodeButtons = new HashMap<Button, Node>();

	private Node selected;
	private Node kiosk;
	boolean findingDirections = false;

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


	public MapController()
	{
		super();
	}

	public void initialize()
	{
		hideRoomInfo();
		//ArrayList<Node> nodes = database.getAllNodes();
		kiosk = database.getNodeByUUID("00000000-0000-0000-0000-000000000000"); //kiosk gets the default node
		graph = new ConcreteGraph();
		loadNodesFromDatabase(); //Get nodes in from database
		setFloorImage(FLOORID); //Set image of floor

		//style up/down buttons
		upFloor.setId("upbuttonTriangle");
		downFloor.setId("downbuttonTriangle");
	}

	public void showRoomInfo(Node n)
	{
		if(findingDirections){
			ArrayList<Node> path = graph.findPath(kiosk,selected);
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
		findingDirections = true;
		showRoomInfo(selected);
	}

	@FXML
	/**
	 * Change the current floor to increment up by 1.
	 * Prevent going down if floor is already 1.
	 * TODO: Stole this from map editor, may want to fix
	 */
	void goDownFloor(ActionEvent event) {
		//If the path goes down, make the button jump straight to bottom floor
		if (pathGoesDown) { //TODO make this stuff

		} else {
			if (FLOORID > 1) {
				//remove all buttons and lines on the current floor
				purgeButtons();
				FLOORID--;
				loadNodesFromDatabase();
				currentFloorLabel.setText(Integer.toString(FLOORID));
				setFloorImage(FLOORID);
				//Delete line
				for (Line l: currentPath) {
					editingFloor.getChildren().remove(l);
				}
				currentPath.clear();
			}
		}
	}

	@FXML
	/**
	 * Change the current floor to increment down by 1.
	 * Prevent going up if floor is already 7.
	 * TODO: Stole this from map editor, may want to fix
	 */
	void goUpFloor(ActionEvent event) {
		if (pathGoesUp) { //TODO make this stuff

		} else {
			if (FLOORID < 7) {
				//remove all buttons and lines on the current floor
				purgeButtons();
				FLOORID++;
				loadNodesFromDatabase();
				currentFloorLabel.setText(Integer.toString(FLOORID));
				setFloorImage(FLOORID);
				//Delete line
				for (Line l: currentPath) {
					editingFloor.getChildren().remove(l);
				}
				currentPath.clear();
			}
		}
	}

	/**
	 * Set the floorImage imageview object to display the image of
	 * a specific floor.
	 * Currently only works based on floor, not building
	 * @param floor The floor to display
	 * TODO: Stole this from map editor, may want to fix
	 */
	private void setFloorImage(int floor)
	{
		if(Accessibility.isHighContrast())
		{
			if (floor == 1)
			{
				floorImage.setImage(f1ContrastProxy.getFXImage());
			} else if (floor == 2)
			{
				floorImage.setImage(f2ContrastProxy.getFXImage());
			} else if (floor == 3)
			{
				floorImage.setImage(f3ContrastProxy.getFXImage());
			} else if (floor == 4)
			{
				floorImage.setImage(f4ContrastProxy.getFXImage());
			} else if (floor == 5)
			{
				floorImage.setImage(f5ContrastProxy.getFXImage());
			} else if (floor == 6)
			{
				floorImage.setImage(f6ContrastProxy.getFXImage());
			} else if (floor == 7)
			{
				floorImage.setImage(f7ContrastProxy.getFXImage());
			}
		}
		else
		{
			if (floor == 1)
			{
				floorImage.setImage(f1ImageProxy.getFXImage());
			} else if (floor == 2)
			{
				floorImage.setImage(f2ImageProxy.getFXImage());
			} else if (floor == 3)
			{
				floorImage.setImage(f3ImageProxy.getFXImage());
			} else if (floor == 4)
			{
				floorImage.setImage(f4ImageProxy.getFXImage());
			} else if (floor == 5)
			{
				floorImage.setImage(f5ImageProxy.getFXImage());
			} else if (floor == 6)
			{
				floorImage.setImage(f6ImageProxy.getFXImage());
			} else if (floor == 7)
			{
				floorImage.setImage(f7ImageProxy.getFXImage());
			}
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
	private void loadNode(Node n)
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
			Label roomLabel = new Label(n.getName());
			roomLabel.setLayoutX(nodeB.getLayoutX()-10);
			roomLabel.setLayoutY(nodeB.getLayoutY()-25);
			roomLabel.setId("roomLabel");
			editingFloor.getChildren().add(1, roomLabel);
		}

		nodeB.setOnAction(event -> showRoomInfo(n));
		//add button to scene
		editingFloor.getChildren().add(1, nodeB);
	}

	/**
	 * Load all nodes from the databasecontroller's list of nodes onto our scene
	 * TODO: Stole this from map editor, may want to fix
	 */
	public void loadNodesFromDatabase()
	{
		for (Node n : database.getNodesInBuildingFloor(BUILDINGID, FLOORID))
			loadNode(n);
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
	}

}
