package ui;

import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;

public class StartupController extends AbstractController
{

	public ImageView eyeImage;
	public ImageView lockImage;
	public ContextMenu contextMenu;
	public TextField searchBox;

	public StartupController(){}

	public void initialize()
	{
		updateLowerImages();
		contextMenu = new ContextMenu();
		contextMenu.setMinWidth(700);

		MenuItem item1 = new MenuItem("Item 1");
		contextMenu.getItems().add(item1);
	}

	public void showMap()
	{
		//contextMenu.show(searchBox, Side.BOTTOM, 0, 0);
		Main.loadFXML(Paths.MAP_FXML);
	}

	public void showDirectory()
	{
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
	}

	private void updateLowerImages()
	{
		/*if(Accessibility.isHighContrast())
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
		}*/
	}
}
