package ui;

import javafx.event.ActionEvent;

public class StartupController extends AbstractController
{

	public StartupController()
	{
	}

	public void initialize()
	{
	}

	public void showMap()
	{
		Main.loadFXML("/fxml/Map.fxml");
	}

	public void showDirectory()
	{
		Main.loadFXML("/fxml/Directory.fxml");
	}

	public void showLogin()
	{
		//TODO: load up login instead of going straight to the editor tool
		Main.loadFXML("/fxml/MapEditorTool.fxml");

	}

	public void toggleHighContrast(ActionEvent actionEvent)
	{
		Accessibility.toggleHighContrast();
		Main.toggleHighContrast();
	}

	public void resetData(ActionEvent e)
	{
		//TODO: Implement database reset functionality
		//DatabaseController.resetData();
	}
}
