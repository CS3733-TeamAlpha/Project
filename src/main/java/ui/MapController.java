package ui;/**
 * Created by sourec on 2/3/17.
 */

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import data.*;
import pathfinding.*;

public class MapController
{
	private DataFile datafile;
	private NodeRepository database;
	private Graph graph;

	private boolean roomInfoShown;

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
	}

	public void hideRoomInfo(){
		roomviewSplit.getItems().remove(roomInfo);
		roomInfoShown = false;
	}


}
