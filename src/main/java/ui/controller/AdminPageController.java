package ui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import misc.LoginState;
import org.mindrot.jbcrypt.BCrypt;
import ui.Paths;

import java.util.Optional;


public class AdminPageController extends BaseController
{
	public Button changePasswordButton;

	public void initialize()
	{
		if(LoginState.isAdminLoggedIn())
		{
			changePasswordButton.setText("Manage Accounts");
		}
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
		if(LoginState.isAdminLoggedIn())
		{
			loadFXML(Paths.MANAGE_ACCOUNTS_FXML);
		}
		else
		{
			Alert alert = new Alert(Alert.AlertType.NONE);
			alert.setTitle("Change Password");
			alert.setHeaderText("Change Password");

			ButtonType submit = new ButtonType("Save");

			alert.getButtonTypes().setAll(submit, ButtonType.CANCEL);

			GridPane grid = new GridPane();
			grid.setPrefWidth(350);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(20, 0, 10, 30));

			PasswordField oldPasswordField = new PasswordField();
			oldPasswordField.setPromptText("Current Password");
			Platform.runLater(oldPasswordField::requestFocus);

			PasswordField newPasswordField = new PasswordField();
			newPasswordField.setPromptText("New Password");

			PasswordField retypePasswordField = new PasswordField();
			retypePasswordField.setPromptText("Confirm Password");

			Label badPasswordLabel = new Label("Incorrect password");
			badPasswordLabel.setWrapText(true);
			badPasswordLabel.setTextFill(Color.RED);
			badPasswordLabel.setVisible(false);

			grid.add(new Label("Current Password:"), 0, 0);
			grid.add(oldPasswordField, 1, 0);

			grid.add(new Label("New Password:"), 0, 1);
			grid.add(newPasswordField, 1, 1);

			grid.add(new Label("Confirm Password: "), 0, 2);
			grid.add(retypePasswordField, 1, 2);

			grid.add(badPasswordLabel, 1, 3);

			alert.getDialogPane().setContent(grid);

			Button okButton = (Button) alert.getDialogPane().lookupButton(submit);
			okButton.setDisable(true);

			newPasswordField.textProperty().addListener((observable, oldValue, newValue) ->
					okButton.setDisable(newValue.isEmpty()));

			okButton.addEventFilter(ActionEvent.ACTION, ae ->
			{
				okButton.setDisable(true);
				String storedHash = database.getHashedPassword(LoginState.getLoggedInAccount());
				boolean success = !storedHash.isEmpty() && BCrypt.checkpw(oldPasswordField.getText(), storedHash);
				if(success)
				{
					if(newPasswordField.getText().equals(retypePasswordField.getText()))
					{
						String hashed = BCrypt.hashpw(newPasswordField.getText(), BCrypt.gensalt());
						database.storeHashedPassword(LoginState.getLoggedInAccount(), hashed);

						Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
						confirmation.setTitle("Password Updated");
						confirmation.setHeaderText("Password Successfully Updated");
						confirmation.getButtonTypes().setAll(ButtonType.OK);
						confirmation.show();
					}
					else
					{
						ae.consume();
						badPasswordLabel.setVisible(true);
						badPasswordLabel.setText("Password must match");
						okButton.setDisable(false);
					}
				}
				else
				{
					ae.consume();
					badPasswordLabel.setVisible(true);
					badPasswordLabel.setText("Incorrect current password");
					okButton.setDisable(false);
				}
			});

			alert.showAndWait();
		}
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
		if(result.isPresent() && result.get() == ok)
		{
			database.resetDatabase();

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
