package ui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import data.*;
import pathfinding.*;

import java.io.IOException;
import java.util.ArrayList;

public class MapController
{
	private Graph graph;
	private boolean roomInfoShown;
	private Stage stage;

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
			imgAnchor.getChildren().add(b);
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

	public void showRoomInfo(Node n)
	{
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
	}

	public void showDirectory()
	{
		Main.loadFXML("/fxml/Directory.fxml");
	}

	public void showStartup()
	{
		Main.loadFXML("/fxml/Startup.fxml");
	}

}
