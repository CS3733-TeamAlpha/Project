package ui;

import data.DatabaseController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import pathfinding.Node;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Ari on 2/6/17.
 */
public class DirectoryController
{

	@FXML
	private TableColumn nameColumn;

	public DirectoryController(){}

	public void initialize()
	{
		ArrayList<Node> nodes = DatabaseController.getAllNodes();

	}

	public void showMap()
	{
		Main.loadFXML("/fxml/Map.fxml");
	}

	public void showStartup()
	{
		Main.loadFXML("/fxml/Startup.fxml");
	}

}
