package ui;

import data.Database;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by Ari on 2/14/17.
 */
abstract class BaseController
{
	private static Stage stage;
	private boolean currentSceneSupportsHC = true;
	private String[] highContrastBlackList = {Paths.LOGIN_FXML, Paths.DIRECTORY_FXML};
	protected static Database database;

	int FLOORID = 3; //Default floor id for minimal application

	static
	{
		database = null;
	}

	public BaseController()
	{
		if (database == null)
		{
			database = new Database("FHAlpha");
			ProviderBox.database = database;
		}
	}

	public abstract void initialize();

	public static void setStage(Stage s){
		stage = s;
	}
	protected void loadFXML(String path)
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

	protected void updateCSS()
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

	private void disableHighContrastCss()
	{
		if(stage.getScene().getStylesheets().contains(Accessibility.HIGH_CONTRAST_CSS))
		{
			stage.getScene().getStylesheets().remove(Accessibility.HIGH_CONTRAST_CSS);
		}
	}

	private void enableHighContrastCss()
	{
		if(! stage.getScene().getStylesheets().contains(Accessibility.HIGH_CONTRAST_CSS))
		{
			stage.getScene().getStylesheets().add(Accessibility.HIGH_CONTRAST_CSS);
		}
	}

}
