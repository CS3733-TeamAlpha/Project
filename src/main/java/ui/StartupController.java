package ui;

import javafx.event.ActionEvent;
import data.DatabaseController;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;

public class StartupController extends BaseController
{

	public ImageView eyeImage;
	public ImageView lockImage;

	public StartupController(){}

	public void initialize()
	{
		updateLowerImages();
	}

	public void showMap()
	{
		DatabaseController.initializeAll();
		Main.loadFXML(Paths.MAP_FXML);
	}

	public void showDirectory()
	{
		DatabaseController.initializeAll();
		Main.loadFXML(Paths.DIRECTORY_FXML);
	}

	public void showLogin()
	{
		Main.loadFXML(Paths.LOGIN_FXML);
	}

	public void toggleHighContrast()
	{
		Accessibility.toggleHighContrast();
		updateLowerImages();
	}

	public void resetData(ActionEvent e)
	{
		DatabaseController.resetData();
	}

	private void updateLowerImages()
	{
		if(Accessibility.isHighContrast())
		{
			ColorAdjust white = new ColorAdjust();
			white.setBrightness(1);
			eyeImage.setEffect(white);
			lockImage.setEffect(white);
		}
		else
		{
			ColorAdjust white = new ColorAdjust();
			white.setBrightness(0.3);
			eyeImage.setEffect(white);
			lockImage.setEffect(white);
		}
	}
}
