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
		Main.loadFXML("/fxml/Map.fxml");
		DatabaseController.initializeAll();
	}

	public void showDirectory()
	{
		Main.loadFXML("/fxml/Directory.fxml");
		DatabaseController.initializeAll();
	}

	public void showLogin()
	{
		//TODO: load up login instead of going straight to the editor tool
		Main.loadFXML("/fxml/MapEditorTool.fxml");
		DatabaseController.initializeAll();

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
