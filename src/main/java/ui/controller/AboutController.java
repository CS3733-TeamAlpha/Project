package ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AboutController
{
	@FXML
	private Button closeButton;

	public AboutController(){}

	public void initialize(){}

	@FXML
	public void close()
	{
		Stage stage = (Stage)closeButton.getScene().getWindow();
		stage.close();
	}
}
