package ui;

import javafx.event.ActionEvent;

public class AdminPageController
{
	public void initialize()
	{

	}

	public void editDirectory(ActionEvent actionEvent)
	{
		Main.loadFXML(Paths.DIRECTORY_FXML);
	}

	public void editMap(ActionEvent actionEvent)
	{
		Main.loadFXML(Paths.MAP_EDITOR_FXML);
	}
}
