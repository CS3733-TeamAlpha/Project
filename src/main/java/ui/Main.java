package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by Ari on 2/4/17.
 */
public class Main extends Application
{
	private static final String HIGH_CONTRAST_CSS = "css/high-contrast.css";

	private static Stage stage;

	@Override
	public void start(Stage primaryStage) throws Exception
	 {
	 	Parent root = FXMLLoader.load(getClass().getResource("/fxml/Startup.fxml"));
		stage = primaryStage;
		primaryStage.setTitle("Faulkner Hospital Map");
		Scene scene = new Scene(root, 1280, 720);
		primaryStage.setScene(scene);
		primaryStage.show();
	}


	public static void main(String[] args) {
		launch(args);
	}

	public static void loadFXML(String path)
	{
		Parent root = null;
		try
		{
			root = FXMLLoader.load(Main.class.getResource(path));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		stage.getScene().setRoot(root);
	}

	public static void toggleHighContrast()
	{
		if(stage.getScene().getStylesheets().contains(HIGH_CONTRAST_CSS))
		{
			stage.getScene().getStylesheets().remove(HIGH_CONTRAST_CSS);
		}
		else
		{
			stage.getScene().getStylesheets().add(HIGH_CONTRAST_CSS);
		}
	}
}
