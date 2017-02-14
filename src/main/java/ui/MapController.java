package ui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import data.*;
import pathfinding.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class MapController extends BaseController
{
	public ImageView floorImage;
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
	private Button directoryButton;
	@FXML
	private VBox roomInfo;
	@FXML
	private Label roomName;
	@FXML
	private Label roomDescription;
	@FXML
	private AnchorPane imgAnchor;


	public MapController() {}

	public void initialize()
	{
		hideRoomInfo();
		ArrayList<Node> nodes = DatabaseController.getAllNodes();
		kiosk = DatabaseController.getNodeByID(9);
		for (Node n:nodes)
		{
			if(!n.getData().get(1).equals("Hallway"))
			{
				Button b = new Button("+");
				b.setLayoutX(n.getX());
				b.setLayoutY(n.getY());
				imgAnchor.getChildren().add(1, b);
				b.setOnAction(new EventHandler<ActionEvent>()
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
		roomName.setText(n.getData().get(0));
		roomDescription.setText(n.getData().get(1));
	}

	public void hideRoomInfo()
	{
		roomviewSplit.getItems().remove(roomInfo);
		roomInfoShown = false;
		selected = null;
	}

	public void showDirectory()
	{
		loadFXML(Paths.DIRECTORY_FXML);
	}

	public void showStartup()
	{
		loadFXML(Paths.STARTUP_FXML);
	}

	public void findDirectionsTo(){
		findingDirections = true;
		showRoomInfo(selected);
	}

}
