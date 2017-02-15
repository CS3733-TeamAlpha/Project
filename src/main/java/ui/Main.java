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
	private static boolean currentSceneSupportsHC = true;
	private static String[] highContrastBlackList = {Paths.LOGIN_FXML, Paths.DIRECTORY_FXML};

	@Override
	public void start(Stage primaryStage) throws Exception
	{
	 	Parent root = FXMLLoader.load(getClass().getResource(Paths.STARTUP_FXML));
		stage = primaryStage;
		primaryStage.setTitle("Faulkner Hospital Map");
		Scene scene = new Scene(root, 1280, 720);
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);

		stage.getScene().getStylesheets().add(Accessibility.NORMAL_CSS);

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

		//Update the high contrast option
		currentSceneSupportsHC = true;
		for(int i = 0; i < highContrastBlackList.length; i++)
		{
			if(highContrastBlackList[i].equals(path))
			{
				currentSceneSupportsHC = false;
			}
		}
		updateCSS();
	}

	private static void disableHighContrastCss()
	{
		if(stage.getScene().getStylesheets().contains(Accessibility.HIGH_CONTRAST_CSS))
		{
			stage.getScene().getStylesheets().remove(Accessibility.HIGH_CONTRAST_CSS);
		}
	}

	private static void enableHighContrastCss()
	{
		if(! stage.getScene().getStylesheets().contains(Accessibility.HIGH_CONTRAST_CSS))
		{
			stage.getScene().getStylesheets().add(Accessibility.HIGH_CONTRAST_CSS);
		}
	}

	public static void updateCSS()
	{
		if(currentSceneSupportsHC && Accessibility.isHighContrast())
		{
			enableHighContrastCss();
		}
		else
		{
			disableHighContrastCss();
		}
	}
}
