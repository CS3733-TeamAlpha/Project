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
	private static Stage stage;

	@Override
	public void start(Stage primaryStage) throws Exception{
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/Map.fxml"));
		stage = primaryStage;
		primaryStage.setTitle("Faulkner Hospital Map");
		primaryStage.setScene(new Scene(root, 1280, 720));
		primaryStage.show();
	}


	public static void main(String[] args) {
		launch(args);
	}

	public static void loadFXML(String path){
		Parent root = null;
		try
		{
			root = FXMLLoader.load(Main.class.getResource(path));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		stage.setScene(new Scene(root, 1280, 720));
	}

}
