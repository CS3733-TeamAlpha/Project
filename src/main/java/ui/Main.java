package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.controller.BaseController;

/**
 * Created by Ari on 2/4/17.
 */
public class Main extends Application
{

	@Override
	public void start(Stage primaryStage) throws Exception
	{
	 	Parent root = FXMLLoader.load(getClass().getResource(Paths.STARTUP_FXML));
		BaseController.setStage(primaryStage);
		primaryStage.setTitle("Faulkner Hospital Map");
		Scene scene = new Scene(root, 1280, 720);
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);

		primaryStage.getScene().getStylesheets().add(Accessibility.NORMAL_CSS);

		primaryStage.show();
	}


	public static void main(String[] args) {
		launch(args);
	}
}
