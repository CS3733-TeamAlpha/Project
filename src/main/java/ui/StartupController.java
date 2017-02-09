package ui;

import javafx.event.ActionEvent;
import data.DatabaseController;

public class StartupController
{

	public StartupController(){}

	public void initialize()
	{
	}

	public void showMap()
	{
		DatabaseController.initializeAll();
		Main.loadFXML("/fxml/Map.fxml");
	}

	public void showDirectory()
	{
		DatabaseController.initializeAll();
		Main.loadFXML("/fxml/Directory.fxml");
	}

	public void showLogin()
	{
		//TODO: load up login instead of going straight to the editor tool
		DatabaseController.initializeAll();
		Main.loadFXML("/fxml/MapEditorTool.fxml");

	}

	public void toggleHighContrast(ActionEvent actionEvent)
	{
		Accessibility.toggleHighContrast();
		Main.toggleHighContrast();
	}

	public void resetData(ActionEvent e){
		DatabaseController.resetData();
	}
}
