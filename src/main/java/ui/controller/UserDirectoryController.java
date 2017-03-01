package ui.controller;

import data.Node;
import data.Provider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import ui.Paths;
import ui.Watchdog;

import java.util.ArrayList;
import java.util.List;

public class UserDirectoryController extends BaseController
{
	public TableColumn<Provider, String> firstNameColumn;
	public TableColumn<Provider, String> lastNameColumn;
	public TableColumn<Provider, String> titleColumn;
	public TableColumn<Provider, String> locationsColumn;
	public TableView tableView;
	public TextField searchBar;

	private ObservableList<Provider> observableList;
	private FilteredList<Provider> filteredData;
	private ArrayList<Provider> fullList;

	@FXML
	private Button getDirButton;

	private ContextMenu providerLocationsContextMenu = new ContextMenu();

	public UserDirectoryController()
	{
		fullList = database.getProviders();
		observableList = FXCollections.observableArrayList(fullList);
	}

	@Override
	public void initialize()
	{
		if (watchdog == null)
		{
			watchdog = new Watchdog(Duration.seconds(uiTimeout), () -> loadFXML(Paths.STARTUP_FXML));
			watchdog.registerScene(stage.getScene(), Event.ANY);
		}
		watchdog.notIdle();

		filteredData = new FilteredList<>(observableList, p -> true);
		SortedList<Provider> sortedProviders = new SortedList<Provider>(filteredData);
		sortedProviders.comparatorProperty().bind(tableView.comparatorProperty());

		firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
		lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
		titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
		locationsColumn.setCellValueFactory(new PropertyValueFactory<>("StringLocations"));

		tableView.setItems(sortedProviders);
		tableView.getSortOrder().add(lastNameColumn);

		searchBar.textProperty().addListener((observable, oldValue, newValue) ->
		{
			String query = newValue.toLowerCase().trim();
			if(query.isEmpty())
			{
				filteredData.setPredicate(p -> true);
			}
			else
			{
				filteredData.setPredicate(provider -> ((provider.getFirstName().toLowerCase() + " " + provider.getLastName().toLowerCase()).contains(query))
														|| provider.getStringLocations().toLowerCase().contains(query));
			}
		});
	}

	public void backButton()
	{
		loadFXML(Paths.STARTUP_FXML);
	}

	public void getProviderDirections()
	{
		if(tableView.getSelectionModel().getSelectedItem() != null)
		{
			Provider selected = (Provider)tableView.getSelectionModel().getSelectedItem();
			List<Node> locations = selected.getLocations();
			if(locations.size() == 1)
			{
				setSearchedFor(locations.get(0));
				loadFXML(Paths.MAP_FXML);
			} else if (locations.size() > 1)
			{
				providerLocationsContextMenu.getItems().clear();
				for(Node loc: locations)
				{
					MenuItem litem = new MenuItem(loc.getName());
					providerLocationsContextMenu.getItems().add(litem);
					litem.setOnAction(e ->
					{
						setSearchedFor(loc);
						loadFXML(Paths.MAP_FXML);
					});
				}
				providerLocationsContextMenu.show(getDirButton, Side.BOTTOM, 0, 0);
			}
		}
	}
}
