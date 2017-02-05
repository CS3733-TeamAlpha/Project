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
import javafx.stage.Stage;
import data.*;
import pathfinding.*;

public class MapController
{
	private DataFile datafile;
	private NodeRepository database;
	private Graph graph;

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


	public MapController(){}

	public void initialize(){
		roomviewSplit.setDividerPositions(1);
	}

	public void showRoomInfo(MouseEvent e){
		roomviewSplit.setDividerPositions(.75);
		System.out.println("Clicked");
	}


}
