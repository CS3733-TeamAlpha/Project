package ui.controller;

import data.Node;
import data.Provider;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import ui.Paths;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AdminDirectoryController extends BaseController
{
	public Button backButton;
	public ToggleButton providerSelectButton;
	public ToggleButton locationSelectButton;
	public ListView mainListView;
	public TextField firstNameField;
	public TextField lastNameField;
	public TextField titleField;
	public ListView providerUsedLocationsList;
	public Button providerAddLocationButton;
	public Button providerRemoveLocationButton;
	public ListView providerUnusedLocationsList;
	public Button deleteProviderButton;
	public Button saveProviderButton;
	public StackPane providerEditorPane;

	private Provider selectedProvider = null;

	@Override
	public void initialize()
	{
		List<Provider> providers = database.getProviders();
		providers = providers.stream().sorted(Comparator.comparing(Provider::getLastName)).collect(Collectors.toList());

		List<Node> allLocations = database.getAllServices();

		providerEditorPane.setVisible(false);

		providerUsedLocationsList.setCellFactory(param -> new ServiceCell());
		providerUnusedLocationsList.setCellFactory(param -> new ServiceCell());

		providerUnusedLocationsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			providerUsedLocationsList.getSelectionModel().clearSelection();
			providerAddLocationButton.setDisable(false);
			providerRemoveLocationButton.setDisable(true);
		});

		providerUsedLocationsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			providerUnusedLocationsList.getSelectionModel().clearSelection();
			providerAddLocationButton.setDisable(true);
			providerRemoveLocationButton.setDisable(false);
		});

		mainListView.setCellFactory(param -> new ProviderCell());
		mainListView.setItems(FXCollections.observableArrayList(providers));

		mainListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			providerEditorPane.setVisible(true);
			Provider prov = (Provider)newValue;
			firstNameField.setText(prov.getFirstName());
			lastNameField.setText(prov.getLastName());
			titleField.setText(prov.getTitle());

			List<Node> assignedLocations = allLocations.stream()
					.filter(location -> prov.getLocationIds().contains(location.getID()))
					.collect(Collectors.toList());

			providerUsedLocationsList.setItems(FXCollections.observableList(assignedLocations));

			List<Node> unassignedLocations = allLocations.stream()
					.filter(location -> !prov.getLocationIds().contains(location.getID()))
					.collect(Collectors.toList());

			providerUnusedLocationsList.setItems(FXCollections.observableList(unassignedLocations));
		});

		saveProviderButton.setOnAction(event ->
		{
			selectedProvider.setAll(firstNameField.getText(), lastNameField.getText(), titleField.getText());
		});

		providerAddLocationButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{

			}
		});

		providerSelectButton.setOnAction(event -> locationSelectButton.setSelected(!providerSelectButton.isSelected()));
		locationSelectButton.setOnAction(event -> providerSelectButton.setSelected(!locationSelectButton.isSelected()));
	}

	public void backButton()
	{
		loadFXML(Paths.ADMIN_PAGE_FXML);
	}

	class ProviderCell extends ListCell<Provider>
	{
		@Override
		protected void updateItem(Provider item, boolean empty)
		{
			super.updateItem(item, empty);
			if(item != null)
			{
				Label providerLabel = new Label(item.getLastName() + ", " + item.getFirstName());
				setGraphic(providerLabel);
			}
		}
	}

	class ServiceCell extends ListCell<Node>
	{
		@Override
		protected void updateItem(Node item, boolean empty)
		{
			super.updateItem(item, empty);
			if(item != null)
			{
				Label providerLabel = new Label(item.getName());
				setGraphic(providerLabel);
			}
		}
	}
}
