package ui.controller;

import data.Provider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import ui.Paths;

public class UserDirectoryController extends BaseController
{
	public TableColumn<Provider, String> firstNameColumn;
	public TableColumn<Provider, String> lastNameColumn;
	public TableColumn<Provider, String> titleColumn;
	public TableColumn<Provider, String> locationsColumn;
	public TableView tableView;
	public TextField searchBar;

	private ObservableList<Provider> observableList;

	public UserDirectoryController()
	{
		observableList = FXCollections.observableArrayList(database.getProviders());
	}

	@Override
	public void initialize()
	{
		firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
		lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
		titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
		locationsColumn.setCellValueFactory(new PropertyValueFactory<>("StringLocations"));

		tableView.setItems(observableList);
		tableView.getSortOrder().add(lastNameColumn);
	}

	public void backButton()
	{
		loadFXML(Paths.STARTUP_FXML);
	}
}
