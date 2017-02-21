package ui.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import misc.Account;
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
		String storedHash = database.getHashedPassword(usernameField.getText());
		success = !storedHash.isEmpty() && BCrypt.checkpw(passwordField.getText(), storedHash);

		if (success)
		{
			progressIndicator.setVisible(true);
			resultText.setText("Logging in...");
			resultText.setTextFill(Color.BLACK);
			resultText.setVisible(true);

			LoginState.login(new Account(usernameField.getText()));

			Platform.runLater(() -> loadFXML(Paths.ADMIN_PAGE_FXML));
		}
		else
		{
			resultText.setText("Incorrect login");
			resultText.setTextFill(Color.RED);
			resultText.setVisible(true);

			usernameField.setDisable(false);
			passwordField.setDisable(false);
			loginButton.setDisable(false);
			cancelButton.setDisable(false);
		}
	}

	public void addLogin()
	{
		if(usernameField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty())
		{
			System.err.println("Bad Login");
		}
		else
		{
			String hashed = BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt());
			database.storeHashedPassword(usernameField.getText(), hashed);
			System.out.println("Password stored");
		}
	}
}
