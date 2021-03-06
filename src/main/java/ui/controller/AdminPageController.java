package ui.controller;

import data.Node;
import data.NodeTypes;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import misc.LoginState;
import org.mindrot.jbcrypt.BCrypt;
import pathfinding.AStarGraph;
import pathfinding.BreadthFirstGraph;
import ui.Paths;
import ui.Watchdog;

import java.util.ArrayList;


public class AdminPageController extends BaseController
{
	@FXML
	ChoiceBox algorithmSelector;

	@FXML
	ChoiceBox kioskNodeSelector;

	@FXML
	Spinner<Integer> timeoutSpinner;

	public Button changePasswordButton;

	public void initialize()
	{
		//UI watchdog
		watchdog = new Watchdog(Duration.seconds(uiTimeout), ()->{
			loadFXML(Paths.STARTUP_FXML);
			LoginState.logout();
		});
		watchdog.registerScene(stage.getScene(), Event.ANY);
		if(LoginState.isAdminLoggedIn())
			changePasswordButton.setText("Manage Accounts");

		//Path algorithm selector
		algorithmSelector.getItems().add("A*");
		algorithmSelector.getItems().add("Breadth First");
		if (MapController.graph == null || MapController.graph.getClass().equals(AStarGraph.class))
			algorithmSelector.getSelectionModel().selectFirst();
		else if (MapController.graph.getClass().equals(BreadthFirstGraph.class))
			algorithmSelector.getSelectionModel().select(1);
		algorithmSelector.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) ->
		{
			switch ((int)newValue)
			{
				case 0:
					MapController.graph = new AStarGraph();
					break;
				case 1:
					MapController.graph = new BreadthFirstGraph();
					break;
			}
		});

		//Kiosk selector
		ArrayList<Node> kiosks = new ArrayList<>();
		database.getAllNodes().forEach((node) ->
		{
			if (node.getType() == NodeTypes.KIOSK.val() || node.getType() == NodeTypes.KIOSK_SELECTED.val())
			{
				kiosks.add(node);
				kioskNodeSelector.getItems().add(node.getName());
			}
		});
		Node kiosk = database.getSelectedKiosk();
		if (kiosk != null)
			kioskNodeSelector.getSelectionModel().select(kiosks.indexOf(database.getSelectedKiosk()));
		else
		{	//if no kiosk is currently set as selected, select the first entry in the choicebox and set
			//it as the selected kiosk... only if there ARE any kiosks
			if (!kiosks.isEmpty())
			{
				kioskNodeSelector.getSelectionModel().select(kiosks.get(0));
				database.setSelectedKiosk(kiosks.get(0));
			}
		}
		kioskNodeSelector.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldSelection, newSelection) ->
		{
			System.out.println("Switching from kiosk " + kiosks.get(oldSelection.intValue()).getName() + " to " + kiosks.get(newSelection.intValue()).getName());
			database.setSelectedKiosk(kiosks.get(newSelection.intValue()));
			kioskNodeSelector.getSelectionModel().select(newSelection.intValue());
		});

		//Timeout spinner
		SpinnerValueFactory valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 3600, uiTimeout, 5);
		valueFactory.valueProperty().addListener((observableValue, integer, t1) ->
		{
			uiTimeout = timeoutSpinner.getValue();
			//Make timeout apply to the current UI
			watchdog.unregisterScene(stage.getScene(), Event.ANY);
			watchdog.disconnect();
			watchdog = new Watchdog(Duration.seconds(uiTimeout), ()->loadFXML(Paths.STARTUP_FXML));
			watchdog.registerScene(stage.getScene(), Event.ANY);
		});
		timeoutSpinner.setValueFactory(valueFactory);
	}

	public void editDirectory()
	{
		loadFXML(Paths.DIRECTORY_EDITOR_FXML);
	}

	public void editMap()
	{
		loadFXML(Paths.MAP_EDITOR_FXML);
	}

	public void changePassword()
	{
		if(LoginState.isAdminLoggedIn())
			loadFXML(Paths.MANAGE_ACCOUNTS_FXML);
		else
		{
			Alert alert = new Alert(Alert.AlertType.NONE);
			alert.setTitle("Change Password");
			alert.setHeaderText("Change Password");

			ButtonType submit = new ButtonType("Save");

			alert.getButtonTypes().setAll(submit, ButtonType.CANCEL);

			GridPane grid = new GridPane();
			grid.setPrefWidth(350);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(20, 0, 10, 30));

			PasswordField oldPasswordField = new PasswordField();
			oldPasswordField.setPromptText("Current Password");
			Platform.runLater(oldPasswordField::requestFocus);

			PasswordField newPasswordField = new PasswordField();
			newPasswordField.setPromptText("New Password");

			PasswordField retypePasswordField = new PasswordField();
			retypePasswordField.setPromptText("Confirm Password");

			Label badPasswordLabel = new Label("Incorrect password");
			badPasswordLabel.setWrapText(true);
			badPasswordLabel.setTextFill(Color.RED);
			badPasswordLabel.setVisible(false);

			grid.add(new Label("Current Password:"), 0, 0);
			grid.add(oldPasswordField, 1, 0);

			grid.add(new Label("New Password:"), 0, 1);
			grid.add(newPasswordField, 1, 1);

			grid.add(new Label("Confirm Password: "), 0, 2);
			grid.add(retypePasswordField, 1, 2);

			grid.add(badPasswordLabel, 1, 3);

			alert.getDialogPane().setContent(grid);

			Button okButton = (Button) alert.getDialogPane().lookupButton(submit);
			okButton.setDisable(true);

			newPasswordField.textProperty().addListener((observable, oldValue, newValue) ->
					okButton.setDisable(newValue.isEmpty()));

			okButton.addEventFilter(ActionEvent.ACTION, ae ->
			{
				okButton.setDisable(true);
				String storedHash = database.getHashedPassword(LoginState.getLoggedInAccount());
				boolean success = !storedHash.isEmpty() && BCrypt.checkpw(oldPasswordField.getText(), storedHash);
				if(success)
				{
					if(newPasswordField.getText().equals(retypePasswordField.getText()))
					{
						String hashed = BCrypt.hashpw(newPasswordField.getText(), BCrypt.gensalt());
						database.storeHashedPassword(LoginState.getLoggedInAccount(), hashed);

						Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
						confirmation.setTitle("Password Updated");
						confirmation.setHeaderText("Password Successfully Updated");
						confirmation.getButtonTypes().setAll(ButtonType.OK);
						confirmation.show();
					}
					else
					{
						ae.consume();
						badPasswordLabel.setVisible(true);
						badPasswordLabel.setText("Password must match");
						okButton.setDisable(false);
					}
				}
				else
				{
					ae.consume();
					badPasswordLabel.setVisible(true);
					badPasswordLabel.setText("Incorrect current password");
					okButton.setDisable(false);
				}
			});

			alert.showAndWait();
		}
	}

	public void factoryReset()
	{
		loadFXML(Paths.MANAGE_DATA_FXML);
	}

	public void logout()
	{
		loadFXML(Paths.STARTUP_FXML);
	}
}
