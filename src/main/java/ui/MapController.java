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

public class MapController
{
	public ImageView floorImage;
	private Graph graph;
	private boolean roomInfoShown;

	private Node selected;
	boolean findingDirections = false;

	@FXML
	private SplitPane roomviewSplit;
	@FXML
	private ScrollPane scroller;
	@FXML
	private TextField searchField;
	@FXML
	private Button searchButton;
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
		for (Node n:nodes)
		{
			Button b = new Button("+");
			b.setLayoutX(n.getX());
			b.setLayoutY(n.getY());
			imgAnchor.getChildren().add(1,b);
			b.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent event)
				{
					showRoomInfo(n);
				}
			});
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
			ArrayList<Node> path = (ArrayList<Node>) graph.findPath(selected, n);
			if(path == null){
				System.out.println("No path found");
			}else
			{
				for (int i = 0; i < path.size()-1; i++)
				{
					Line line = new Line();
					System.out.println("Line from "+path.get(i).getX()+", "+path.get(i).getY()+" to "+path.get(i+1).getX()+", "+path.get(i + 1).getY());
					System.out.println(path.get(i+1).getID());
					line.setStartX(path.get(i).getX());
					line.setStartY(path.get(i).getY());
					line.setEndX(path.get(i+1).getX());
					line.setEndY(path.get(i+1).getY());
					line.setStrokeWidth(5);
					line.setStroke(Color.BLUE);
					imgAnchor.getChildren().add(1,line);
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
		roomName.setText("" + n.getID());
	}

	public void hideRoomInfo()
	{
		roomviewSplit.getItems().remove(roomInfo);
		roomInfoShown = false;
		selected = null;
	}

	public void showDirectory()
	{
		Main.loadFXML("/fxml/Directory.fxml");
	}

	public void showStartup()
	{
		Main.loadFXML("/fxml/Startup.fxml");
	}

	public void findDirectionsTo(){
		findingDirections = true;
	}

}
