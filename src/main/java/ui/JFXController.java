package ui;/**
 * Created by sourec on 2/3/17.
 */

import javafx.application.Application;
import javafx.stage.Stage;
import data.*;
import pathfinding.*;

public class JFXController extends Application
{
	private DataFile datafile;
	private NodeRepository database;
	private Graph graph;

	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void start(Stage primaryStage)
	{

	}
}
