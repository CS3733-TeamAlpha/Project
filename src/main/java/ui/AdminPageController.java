package ui;

import javafx.event.ActionEvent;

public class AdminPageController extends BaseController
{
	public void initialize()
	{

	}

	public void editDirectory(ActionEvent actionEvent)
	{
		loadFXML(Paths.DIRECTORY_FXML);
	}

	public void editMap(ActionEvent actionEvent)
	{
		loadFXML(Paths.MAP_EDITOR_FXML);
	}
}
