package ui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import misc.LoginState;
import org.mindrot.jbcrypt.BCrypt;
import ui.Paths;

/**
 * Created by Ari on 2/7/17.
 */
public class LoginController extends BaseController
{
	public Label resultText;
	public ProgressIndicator progressIndicator;
	public Button cancelButton;
	public Button loginButton;
	public Pane thinBar;
	public Pane thickBar;
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

		boolean success;
		boolean wrongUser = false;
		String storedHash = "";
		// Keeps null pointer from showing up if incorrect username
		if(database.getHashedPassword(usernameField.getText()) != null) {
			storedHash = database.getHashedPassword(usernameField.getText());
		} else {
			wrongUser = true;
		}
		success = !storedHash.isEmpty() && BCrypt.checkpw(passwordField.getText(), storedHash);

		if (success)
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
			if(wrongUser) { // Tell them username is wrong only if user isn't recognized
				resultText.setText("Unrecognized Username");
			} else { // Tell them password is wrong if user is correct
				resultText.setText("Incorrect Password");
			}
			resultText.setTextFill(Color.RED);
			resultText.setVisible(true);

			usernameField.setDisable(false);
			passwordField.setDisable(false);
			loginButton.setDisable(false);
			cancelButton.setDisable(false);
		}
	}
}
