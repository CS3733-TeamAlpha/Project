package ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import misc.Account;
import org.mindrot.jbcrypt.BCrypt;
import ui.Paths;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class ManageAccountsController extends BaseController
{
	public Button backButton;
	public VBox listBox;

	@Override
	public void initialize()
	{
		reloadList();
	}

	private void reloadList()
	{
		listBox.getChildren().clear();
		ArrayList<String> allAccounts = database.getAllAccounts();

		for (int i = 0; i < allAccounts.size(); i++)
		{
			String account = allAccounts.get(i);
			AnchorPane anchorPane = generateListItem(new Account(account));
			listBox.getChildren().add(anchorPane);
			if(i % 2 == 1)
			{
				anchorPane.getStyleClass().add("fake-list-alternate");
			}
			if(i == allAccounts.size()-1)
			{
				anchorPane.getStyleClass().add("fake-list-cell-last");
			}
		}
	}

	public void backButton(ActionEvent actionEvent)
	{
		loadFXML(Paths.ADMIN_PAGE_FXML);
	}

	private AnchorPane generateListItem(Account account)
	{
		String accountName = account.getUserName();


		AnchorPane root = null;
		try
		{
			root = FXMLLoader.load(getClass().getResource(Paths.ACCOUNT_BOX_FXML));
		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}

		Label accountLabel = (Label) root.lookup("#accountLabel");
		accountLabel.setText(accountName);

		Button deleteButton = (Button) root.lookup("#deleteButton");
		if(account.isAdmin())
		{
			deleteButton.setVisible(false);
		}
		else
		{
			deleteButton.setOnAction(event ->
					{
						Alert deleteWarning = new Alert(Alert.AlertType.WARNING);
						deleteWarning.setTitle("Warning: Account Deleting");
						deleteWarning.setHeaderText("Warning: Deleting account '" + account.getUserName() + "'");
						deleteWarning.setContentText("This operation cannot be undone");

						deleteWarning.getButtonTypes().addAll(ButtonType.CANCEL);
						Optional<ButtonType> result = deleteWarning.showAndWait();
						if (result.isPresent())
						{
							if(result.get() == ButtonType.OK)
							{
								database.deleteAccount(accountName);

								Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
								confirmation.setTitle("Account Deleted");
								confirmation.setHeaderText("Account deleted successfully");
								confirmation.show();
								reloadList();
							}
						}
					});
		}

		Button changePasswordButton = (Button) root.lookup("#changePasswordButton");
		changePasswordButton.setOnAction(event ->
		{
			Alert alert = new Alert(Alert.AlertType.NONE);
			alert.setTitle("Change Password");
			alert.setHeaderText("Change Password");

			ButtonType submit = new ButtonType("Save");

			alert.getButtonTypes().setAll(submit, ButtonType.CANCEL);

			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(20, 30, 20, 30));

			PasswordField newPasswordField = new PasswordField();
			newPasswordField.setPromptText("New Password");

			PasswordField retypePasswordField = new PasswordField();
			retypePasswordField.setPromptText("Confirm Password");

			Label badPasswordLabel = new Label("Incorrect password");
			badPasswordLabel.setTextFill(Color.RED);
			badPasswordLabel.setVisible(false);

			grid.add(new Label("New Password:"), 0, 0);
			grid.add(newPasswordField, 1, 0);

			grid.add(new Label("Confirm Password: "), 0, 1);
			grid.add(retypePasswordField, 1, 1);

			grid.add(badPasswordLabel, 1, 2);

			alert.getDialogPane().setContent(grid);

			Button okButton = (Button) alert.getDialogPane().lookupButton(submit);
			okButton.setDisable(true);

			newPasswordField.textProperty().addListener((observable, oldValue, newValue) ->
					okButton.setDisable(newValue.isEmpty()));

			okButton.addEventFilter(ActionEvent.ACTION, ae ->
			{
				okButton.setDisable(true);
				if(newPasswordField.getText().equals(retypePasswordField.getText()))
				{
					String hashed = BCrypt.hashpw(newPasswordField.getText(), BCrypt.gensalt());
					database.storeHashedPassword(accountName, hashed);

					Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
					confirmation.setTitle(accountName + " Updated");
					confirmation.setHeaderText("Password Successfully Updated");
					confirmation.getButtonTypes().setAll(ButtonType.OK);
					confirmation.show();
				}
				else
				{
					ae.consume();
					badPasswordLabel.setVisible(true);
					badPasswordLabel.setText("New password fields do not match");
					okButton.setDisable(false);
				}
			});

			alert.showAndWait();
		});

		root.getStyleClass().add("fake-list-cell");
		return root;
	}

	public void addAccount(ActionEvent actionEvent)
	{
		Alert alert = new Alert(Alert.AlertType.NONE);
		alert.setTitle("Add account");
		alert.setHeaderText("Add New Account");

		ButtonType submit = new ButtonType("Save");

		alert.getButtonTypes().setAll(submit, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 30, 20, 30));

		TextField userNameField = new TextField();
		userNameField.setPromptText("Username");

		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Password");

		Label badPasswordLabel = new Label("Incorrect password");
		badPasswordLabel.setTextFill(Color.RED);
		badPasswordLabel.setVisible(false);

		grid.add(new Label("Username:"), 0, 0);
		grid.add(userNameField, 1, 0);

		grid.add(new Label("Password: "), 0, 1);
		grid.add(passwordField, 1, 1);

		grid.add(badPasswordLabel, 1, 2);

		alert.getDialogPane().setContent(grid);

		Button okButton = (Button) alert.getDialogPane().lookupButton(submit);
		okButton.setDisable(true);

		passwordField.textProperty().addListener((observable, oldValue, newValue) ->
				okButton.setDisable(newValue.isEmpty()));

		okButton.addEventFilter(ActionEvent.ACTION, ae ->
		{
			okButton.setDisable(true);
			if(!userNameField.getText().trim().isEmpty() && !userNameField.getText().contains(" "))
			{
				if(!passwordField.getText().isEmpty() && !passwordField.getText().contains(" "))
				{
					if(database.getHashedPassword(userNameField.getText()) == null)
					{
						String hashed = BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt());
						database.storeHashedPassword(userNameField.getText(), hashed);

						Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
						confirmation.setTitle(userNameField.getText() + " Created");
						confirmation.setHeaderText("User '" + userNameField.getText() + "' Successfully Created");
						confirmation.getButtonTypes().setAll(ButtonType.OK);
						confirmation.show();

						reloadList();
					}
					else
					{
						ae.consume();
						badPasswordLabel.setVisible(true);
						badPasswordLabel.setText("Account exists already");
						okButton.setDisable(false);
					}
				}
				else
				{
					ae.consume();
					badPasswordLabel.setVisible(true);
					badPasswordLabel.setText("Bad Password Format");
					okButton.setDisable(false);
				}
			}
			else
			{
				ae.consume();
				badPasswordLabel.setVisible(true);
				badPasswordLabel.setText("Bad username value");
				okButton.setDisable(false);
			}
		});

		alert.showAndWait();
	}
}
