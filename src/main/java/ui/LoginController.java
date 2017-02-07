package ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Created by Ari on 2/7/17.
 */
public class LoginController
{
	@FXML
	private TextField usernameField;

	@FXML
	private TextField passwordField;

	public LoginController(){}

	public void initialize()
	{

	}

	public void showStartup()
	{
		Main.loadFXML("/fxml/Startup.fxml");
	}

	public void login()
	{
		//TODO - login code
	}

}
