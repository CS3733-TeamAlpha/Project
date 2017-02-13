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
		Main.loadFXML(Paths.MAP_FXML);
	}

	public void showDirectory()
	{
		DatabaseController.initializeAll();
		Main.loadFXML(Paths.DIRECTORY_FXML);
	}

	public void showLogin()
	{
		Main.loadFXML(Paths.LOGIN_FXML);

	}

	public void toggleHighContrast()
	{
		Accessibility.toggleHighContrast();
	}

	public void resetData(ActionEvent e){
		DatabaseController.resetData();
	}
}
