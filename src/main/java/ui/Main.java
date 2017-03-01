package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.controller.BaseController;

public class Main extends Application
{

	@Override
	public void start(Stage primaryStage) throws Exception
	{
	 	Parent root = FXMLLoader.load(getClass().getResource(Paths.STARTUP_FXML));
		BaseController.setStage(primaryStage);
		primaryStage.setTitle("Faulkner Hospital Map");
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setMaximized(true);
		primaryStage.getScene().getStylesheets().add(Paths.NORMAL_CSS);
		primaryStage.show();
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
