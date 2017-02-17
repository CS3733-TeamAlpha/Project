package ui;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;


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

	public void changePassword(ActionEvent actionEvent)
	{

	}

	public void factoryReset(ActionEvent actionEvent)
	{
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Warning");
		alert.setHeaderText("Warning: Factory Reset");
		alert.setContentText("All directory and map data will be reset to factory settings. This operation cannot be undone.");

		ButtonType ok = new ButtonType("OK");
		ButtonType cancel = new ButtonType("Cancel");

		alert.getButtonTypes().setAll(ok, cancel);

		Optional<ButtonType> result = alert.showAndWait();
		if(result.get() == ok)
		{
			database.resetDatabase();
			database.reloadCache();

			Alert cleared = new Alert(Alert.AlertType.INFORMATION);
			cleared.setTitle("Data Reset");
			cleared.setHeaderText("Data Reset Successfully");
			cleared.show();
		}
		else
		{
			//Nothing to do here, user canceled
		}
	}

	public void logout(ActionEvent actionEvent)
	{
		loadFXML(Paths.STARTUP_FXML);
	}
}
