package ui;/**
 * Created by sourec on 2/3/17.
 */

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import data.*;
import pathfinding.*;

import java.io.IOException;

public class MapController
{
	private DataFile datafile;
	private NodeRepository database;
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


	public MapController(){}

	public void initialize(){
		hideRoomInfo();
	}

	public void showRoomInfo(MouseEvent e){
		if(!roomInfoShown){
			roomviewSplit.getItems().add(1, roomInfo);
			roomviewSplit.setDividerPositions(.75);
			roomInfoShown = true;
		}
		roomName.setText(""+e.getX()+", "+e.getY());
	}

	public void hideRoomInfo(){
		roomviewSplit.getItems().remove(roomInfo);
		roomInfoShown = false;
	}

	public void showDirectory(){
		Main.loadFXML("/fxml/Directory.fxml");
	}


}
