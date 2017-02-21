package ui;

import data.Provider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class UserDirectoryController extends BaseController
{
	public TableColumn<Provider, String> firstNameColumn;
	public TableColumn<Provider, String> lastNameColumn;
	public TableColumn<Provider, String> titleColumn;
	public TableColumn<Provider, String> locationsColumn;

	public UserDirectoryController()
	{

	}

	@Override
	public void initialize()
	{
		firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
		lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
		titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
		locationsColumn.setCellValueFactory(new PropertyValueFactory<>("StringLocations"));
	}
}
