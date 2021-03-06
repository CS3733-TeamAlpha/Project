package ui.controller;

import data.Node;
import data.SearchResult;
import data.SearchType;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import pathfinding.EmergencyExitFinder;
import ui.Main;
import ui.Paths;

import java.io.IOException;
import java.util.List;

public class StartupController extends BaseController
{
	public ImageView lockImage;
	private ContextMenu contextMenu;
	private ContextMenu providerLocationContextMenu;
	public TextField searchBox;

	public StartupController()
	{

	}

	public void initialize()
	{
		contextMenu = new ContextMenu();
		contextMenu.setMaxWidth(searchBox.getWidth());

		providerLocationContextMenu = new ContextMenu();
		providerLocationContextMenu.setMaxWidth(searchBox.getWidth());

		searchBox.textProperty().addListener(((observable, oldValue, newValue) ->
		{
			List<SearchResult> results = database.getResultsForSearch(newValue, true);
			contextMenu.getItems().remove(0, contextMenu.getItems().size());
			providerLocationContextMenu.getItems().clear();
			for(SearchResult result : results)
			{
				MenuItem item = new MenuItem(result.displayText);
				contextMenu.getItems().add(item);
				item.setOnAction(event ->
				{
					//if provider, make a new contextmenu of locations that provider is at
					if(result.searchType == SearchType.Provider)
					{
						providerLocationContextMenu.hide();
						List<Node> locations = database.getProviderByID(result.id).getLocations();
						if(locations.size() > 0)
						{
							for(Node loc: locations)
							{
								MenuItem litem = new MenuItem(result.displayText + ": " +loc.getName());
								providerLocationContextMenu.getItems().add(litem);
								litem.setOnAction(e ->
								{
									setSearchedFor(loc);
									loadFXML(Paths.MAP_FXML);
								});
							}
						}
						//if no locations, indicate as such
						else
						{
							MenuItem litem = new MenuItem("No Locations");
							providerLocationContextMenu.getItems().add(litem);
						}
						providerLocationContextMenu.show(searchBox, Side.BOTTOM, 0, 0);
					}
					else if(result.searchType == SearchType.Location)
					{
						setSearchedFor(database.getNodeByUUID(result.id));
						loadFXML(Paths.MAP_FXML);
					}
				});
			}

			if(newValue.length() == 0)
			{
				contextMenu.hide();
				providerLocationContextMenu.hide();
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

	public void showAbout()
	{
		//Can't be done using the normal loadFXML function as this should be launched in a second window, NOT replace
		//the current one. This is also quite nicely "inspired" by a post on stackoverflow (no ctrl-c used!)
		Parent root = null;
		try
		{
			root = FXMLLoader.load(Main.class.getResource("/fxml/About.fxml"));

		} catch (IOException e)
		{
			e.printStackTrace();
		}
		if (root == null)
		{
			return;
		}
		Stage stage = new Stage();
		stage.setTitle("About");
		stage.setScene(new Scene(root, 557, 279));
		stage.show();
	}

	public void emergencyDirections()
	{
		//Get emergency directions to the nearest
		EmergencyExitFinder eExit = new EmergencyExitFinder();
		setSearchedFor(eExit.findExit(database.getSelectedKiosk()));
		MapController.usingStairs = true;
		loadFXML(Paths.MAP_FXML);
	}
}
