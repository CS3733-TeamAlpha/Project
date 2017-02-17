package ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
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

public class MapController extends BaseController
{
	//public ImageView floorImage;
	private Graph graph;
	private boolean roomInfoShown;



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
	private AnchorPane imgAnchor;
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
		ArrayList<Node> nodes = database.getAllNodes();
		kiosk = database.getNodeByUUID("00000000-0000-0000-0000-000000000000"); //kiosk gets the default node
		for (Node n:nodes)
		{
			if(n.getType() != 0) //If n isn't a hallway node... TODO: Create node type enumeration
			{
				Button b = new Button("+");
				b.setLayoutX(n.getX());
				b.setLayoutY(n.getY());
				imgAnchor.getChildren().add(1, b);
				b.setOnAction(new EventHandler<ActionEvent>() //not a typo btw
				{
					@Override
					public void handle(ActionEvent event)
					{
						showRoomInfo(n);
					}
				});
			}
		}
		graph = new ConcreteGraph();

		if(Accessibility.isHighContrast())
		{
			floorImage.setImage(new Image(Accessibility.HIGH_CONTRAST_MAP_PATH));
		}

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
					line.setStartX(path.get(i).getX()+15); //plus 15 to center on button
					line.setStartY(path.get(i).getY()+15);
					line.setEndX(path.get(i+1).getX()+15);
					line.setEndY(path.get(i+1).getY()+15);
					line.setStrokeWidth(10);
					line.setStroke(Color.BLUE);
					imgAnchor.getChildren().add(1,line);
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
	 * TODO: Make the min floor change depending on current building (iteration 3)
	 */
	void goDownFloor(ActionEvent event) {
		if(FLOORID > 1){
			//remove all buttons and lines on the current floor
			//purgeButtonsAndLines();
			//FLOORID--;
			//loadNodesFromDatabase();
			//currentFloorLabel.setText(Integer.toString(FLOORID));
			//setFloorImage(FLOORID);
		}
	}

	@FXML
	/**
	 * Change the current floor to increment down by 1.
	 * Prevent going up if floor is already 7.
	 * TODO: Make the max floor change depending on current building (iteration 3)
	 * TODO: Make them actually work
	 */
	void goUpFloor(ActionEvent event) {
		if(FLOORID < 7){
			//remove all buttons and lines on the current floor
			//purgeButtonsAndLines();
			//FLOORID++;
			//loadNodesFromDatabase();
			//currentFloorLabel.setText(Integer.toString(FLOORID));
			//setFloorImage(FLOORID);
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

}
