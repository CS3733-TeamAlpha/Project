package ui.controller;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import misc.LoginState;
import org.mindrot.jbcrypt.BCrypt;
import ui.Paths;
import ui.Watchdog;

public class LoginController extends BaseController
{
	public Label resultText;
	public ProgressIndicator progressIndicator;
	public Button cancelButton;
	public Button loginButton;
	@FXML
	private TextField usernameField;

	@FXML
	private PasswordField passwordField;

	public LoginController()
	{
	}

	public void initialize()
	{
		Platform.runLater(() -> usernameField.requestFocus());
		watchdog = new Watchdog(Duration.seconds(uiTimeout), ()->loadFXML(Paths.STARTUP_FXML));
		watchdog.unregisterScene(stage.getScene(), Event.ANY);
		usernameField.textProperty().addListener((observable, oldValue, newValue) ->
				loginButton.setDisable(newValue.isEmpty()));

		LoginState.logout();
	}

	public void showStartup()
	{
		loadFXML(Paths.STARTUP_FXML);
	}

	public void login()
	{
		usernameField.setDisable(true);
		passwordField.setDisable(true);
		loginButton.setDisable(true);
		cancelButton.setDisable(true);

		boolean wrongUser = false;
		String storedHash = "";
		// Keeps null pointer from showing up if incorrect username
		if(database.getHashedPassword(usernameField.getText()) != null)
			storedHash = database.getHashedPassword(usernameField.getText());
		else
			wrongUser = true;

		if (!storedHash.isEmpty() && BCrypt.checkpw(passwordField.getText(), storedHash))
		{
			progressIndicator.setVisible(true);
			resultText.setText("Logging in...");
			resultText.setTextFill(Color.BLACK);
			resultText.setVisible(true);

			LoginState.login(usernameField.getText());

			Platform.runLater(() -> loadFXML(Paths.ADMIN_PAGE_FXML));
		}
		else
		{
			if(wrongUser) // Tell them username is wrong only if user isn't recognized
				resultText.setText("Unrecognized Username");
			else // Tell them password is wrong if user is correct
				resultText.setText("Incorrect Password");

			resultText.setTextFill(Color.RED);
			resultText.setVisible(true);

			usernameField.setDisable(false);
			passwordField.setDisable(false);
			loginButton.setDisable(false);
			cancelButton.setDisable(false);
		}
	}
}
