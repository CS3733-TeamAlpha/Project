package ui.controller;

import data.SearchResult;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import ui.Accessibility;
import ui.Paths;

import java.util.ArrayList;
import java.util.List;

public class StartupController extends BaseController
{
//	public ImageView eyeImage;
	public ImageView lockImage;
	private ContextMenu contextMenu;
	public TextField searchBox;

	public StartupController()
	{

	}

	public void initialize()
	{
		contextMenu = new ContextMenu();
		contextMenu.setMaxWidth(searchBox.getWidth());

		searchBox.textProperty().addListener(((observable, oldValue, newValue) ->
		{
			List<SearchResult> results = database.getResultsForSearch(newValue, true);
			contextMenu.getItems().remove(0, contextMenu.getItems().size());
			for(SearchResult result : results)
			{
				MenuItem item = new MenuItem(result.displayText);
				contextMenu.getItems().add(item);
				item.setOnAction(event ->
				{
					setSearchedFor(database.getNodeByUUID(result.id));
					System.out.println(result.id);
					loadFXML(Paths.MAP_FXML);
				});
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
		loadFXML(Paths.USER_DIRECTORY_FXML);
	}

	public void showLogin()
	{
		loadFXML(Paths.LOGIN_FXML);
	}

//	public void toggleHighContrast()
//	{
//		Accessibility.toggleHighContrast(this);
//	}
}
