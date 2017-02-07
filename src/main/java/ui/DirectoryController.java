package ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;

import java.io.IOException;

/**
 * Created by Ari on 2/6/17.
 */
public class DirectoryController
{

	@FXML
	private TextField searchField;

	@FXML
	private TableColumn nameColumn;

	public DirectoryController(){}

	public void initialize()
	{
		//TODO - read from database and populate tables
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
