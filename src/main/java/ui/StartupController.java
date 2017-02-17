package ui;

import data.SearchResult;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.util.ArrayList;

public class StartupController extends BaseController
{
	public ImageView eyeImage;
	public ImageView lockImage;
	private ContextMenu contextMenu;
	public TextField searchBox;

	public StartupController()
	{

	}

	public void initialize()
	{
		updateLowerImages();
		contextMenu = new ContextMenu();
		contextMenu.setMaxWidth(searchBox.getWidth());

		searchBox.textProperty().addListener(((observable, oldValue, newValue) ->
		{
			ArrayList<SearchResult> results = database.getResultsForSearch(newValue, true);
			contextMenu.getItems().remove(0, contextMenu.getItems().size());
			for(SearchResult result : results)
			{
				MenuItem item = new MenuItem(result.displayText);
				contextMenu.getItems().add(item);
			}

			if(newValue.length() == 0)
			{
				contextMenu.hide();
			}
			else if(!contextMenu.isShowing())
			{
				contextMenu.show(searchBox, Side.BOTTOM, 0, 0);
				contextMenu.setMinWidth(searchBox.getWidth());
			}
		}));
	}

	public void showMap()
	{
		loadFXML(Paths.MAP_FXML);
	}

	public void ShowDirectory()
	{
		loadFXML(Paths.DIRECTORY2_FXML);
	}

	public void showLogin()
	{
		loadFXML(Paths.LOGIN_FXML);
	}

	public void toggleHighContrast()
	{
		Accessibility.toggleHighContrast(this);
		updateLowerImages();
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
