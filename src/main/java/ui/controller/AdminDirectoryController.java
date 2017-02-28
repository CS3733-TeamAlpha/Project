package ui.controller;

import com.sun.javafx.scene.control.skin.ListViewSkin;
import data.Node;
import data.Provider;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import ui.Paths;

import java.util.*;
import java.util.stream.Collectors;

public class AdminDirectoryController extends BaseController
{
	public Button backButton;
	public ListView<Provider> mainListView;
	public TextField firstNameField;
	public TextField lastNameField;
	public TextField titleField;
	public ListView<Node> providerUsedLocationsList;
	public Button providerAddLocationButton;
	public Button providerRemoveLocationButton;
	public ListView<Node> providerUnusedLocationsList;
	public Button deleteProviderButton;
	public StackPane providerEditorPane;
	public Button addProviderButton;

	private Provider selectedProvider = null;

	@Override
	public void initialize()
	{
		List<Provider> providers = database.getProviders();
		providers = providers.stream().sorted(Comparator.comparing(Provider::getLastName)).collect(Collectors.toList());

		List<Node> allLocations = database.getAllServices();

		providerUsedLocationsList.setCellFactory(param -> new ServiceCell());
		providerUnusedLocationsList.setCellFactory(param -> new ServiceCell());

		providerUnusedLocationsList.getFocusModel().focusedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			if(newValue != null)
			{
				providerUsedLocationsList.getSelectionModel().clearSelection();
			}
		});
		providerUsedLocationsList.getFocusModel().focusedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			if(newValue != null)
			{
				providerUnusedLocationsList.getSelectionModel().clearSelection();
			}
		});

		providerUnusedLocationsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			if(newValue != null)
			{
				providerAddLocationButton.setDisable(false);
				providerRemoveLocationButton.setDisable(true);
			}
		});

		providerUsedLocationsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			if(newValue != null)
			{
				providerAddLocationButton.setDisable(true);
				providerRemoveLocationButton.setDisable(false);
			}
		});

		mainListView.setCellFactory(param -> new ProviderCell());
		mainListView.setItems(FXCollections.observableArrayList(providers));

		Comparator<Node> alphabeticalNodeComparator = Comparator.comparing(Node::getName);

		mainListView.setSkin(new MyRefreshSkin(mainListView));
		mainListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			if(newValue != null)
			{
				providerEditorPane.setDisable(false);

				providerEditorPane.setVisible(true);

				selectedProvider = newValue;
				firstNameField.setText(newValue.getFirstName());
				lastNameField.setText(newValue.getLastName());
				titleField.setText(newValue.getTitle());

				List<Node> assignedLocations = allLocations.stream()
						.filter(location -> newValue.getLocationIds().contains(location.getID()))
						.collect(Collectors.toList());

				providerUsedLocationsList.setItems(FXCollections.observableList(assignedLocations));

				List<Node> unassignedLocations = allLocations.stream()
						.filter(location -> !newValue.getLocationIds().contains(location.getID()))
						.collect(Collectors.toList());

				providerUnusedLocationsList.setItems(FXCollections.observableList(unassignedLocations));

				providerUsedLocationsList.getItems().sort(alphabeticalNodeComparator);
				providerUnusedLocationsList.getItems().sort(alphabeticalNodeComparator);
			}
		});

		providerAddLocationButton.setOnAction(event ->
		{
			Object possibleLocation = providerUnusedLocationsList.getSelectionModel().getSelectedItem();
			if(possibleLocation != null)
			{
				Node location = (Node)possibleLocation;
				selectedProvider.addLocation(location);
				providerUnusedLocationsList.getItems().remove(location);
				providerUsedLocationsList.getItems().add(location);

				providerUsedLocationsList.getItems().sort(alphabeticalNodeComparator);
				providerUnusedLocationsList.getItems().sort(alphabeticalNodeComparator);
			}
		});

		providerRemoveLocationButton.setOnAction(event ->
		{
			Object possibleLocation = providerUsedLocationsList.getSelectionModel().getSelectedItem();
			if(possibleLocation != null)
			{
				Node location = (Node)possibleLocation;
				selectedProvider.removeLocation(location.getID());
				providerUnusedLocationsList.getItems().add(location);
				providerUsedLocationsList.getItems().remove(location);

				providerUsedLocationsList.getItems().sort(alphabeticalNodeComparator);
				providerUnusedLocationsList.getItems().sort(alphabeticalNodeComparator);
			}
		});

		deleteProviderButton.setOnAction(event ->
		{
			Alert deleteWarning = new Alert(Alert.AlertType.WARNING);
			deleteWarning.setTitle("Warning");
			deleteWarning.setHeaderText("Warning: Deleting Provider");
			deleteWarning.setContentText("Are you sure you want to delete provider '" + selectedProvider.getFirstName() + " " + selectedProvider.getLastName());

			ButtonType delete = new ButtonType("Delete");
			deleteWarning.getButtonTypes().setAll(ButtonType.CANCEL, delete);

			Optional<ButtonType> result = deleteWarning.showAndWait();
			if(result.isPresent())
			{
				if(result.get() == delete)
				{
					database.deleteProvider(selectedProvider);
					mainListView.getItems().remove(selectedProvider);
					if(mainListView.getSelectionModel() != null)
						mainListView.getSelectionModel().clearSelection();
					disableEditorView();
				}
			}
		});


		ChangeListener<Boolean> textFocusListener = (observable, oldValue, newValue) ->
		{
			selectedProvider.setAll(firstNameField.getText(), lastNameField.getText(), titleField.getText());

			((MyRefreshSkin)mainListView.getSkin()).refresh();
		};

		firstNameField.focusedProperty().addListener(textFocusListener);
		lastNameField.focusedProperty().addListener(textFocusListener);
		titleField.focusedProperty().addListener(textFocusListener);

		addProviderButton.setOnAction(event ->
		{
			Provider newProvider = new Provider("FirstName", "LastName", UUID.randomUUID().toString(), "Title");
			mainListView.getItems().add(0, newProvider);
			mainListView.getSelectionModel().select(newProvider);
			database.addProvider(newProvider);
		});
	}

	private void disableEditorView()
	{
		providerEditorPane.setDisable(true);
		firstNameField.clear();
		lastNameField.clear();
		titleField.clear();
		providerUsedLocationsList.getItems().clear();
		providerUnusedLocationsList.getItems().clear();
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
			else
			{
				setGraphic(new Pane());
			}
		}
	}

	class MyRefreshSkin extends ListViewSkin<Provider>
	{
		public MyRefreshSkin(ListView<Provider> listView)
		{
			super(listView);
		}

		public void refresh()
		{
			super.flow.recreateCells();
		}
	}
}
