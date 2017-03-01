package ui.controller;

import data.Provider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import sun.plugin.javascript.navig.Anchor;
import ui.Paths;

import java.util.ArrayList;

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

	public UserDirectoryController()
	{
		fullList = database.getProviders();
		observableList = FXCollections.observableArrayList(fullList);
	}

	@Override
	public void initialize()
	{
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

	public void getDirections() { }
}
