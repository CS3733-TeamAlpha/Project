package ui.controller;

import data.DatabaseSaver;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import ui.Paths;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class ManageDataController extends BaseController
{
	public VBox listBox;
	public Button saveDatabaseButton;
	public Button factoryResetButton;
	public ProgressIndicator progressIndicator;
	public Button backButton;

	@Override
	public void initialize()
	{
		reloadList();
	}

	private void reloadList()
	{
		listBox.getChildren().clear();
		File dir = new File("Save_Files");
		if(!dir.exists())
		{
			dir.mkdir();
		}
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++)
		{
			File f = files[i];
			AnchorPane anchorPane = generateSaveBox(f.getName());
			listBox.getChildren().add(anchorPane);
			if(i % 2 == 1)
				anchorPane.getStyleClass().add("fake-list-alternate");
			if(i == files.length - 1)
				anchorPane.getStyleClass().add("fake-list-cell-last");
		}
	}

	public void backButton(ActionEvent actionEvent)
	{
		loadFXML(Paths.ADMIN_PAGE_FXML);
	}

	public void saveDatabase(ActionEvent actionEvent)
	{
		setAllDisabled(true);

		new Thread(() ->
		{
			database.disconnect();
			DatabaseSaver.saveDatabase("FHAlpha");
			database.connect();

			Platform.runLater(() ->
			{
				setAllDisabled(false);
				reloadList();
			});
		}).start();
	}

	private void setAllDisabled(boolean value)
	{
		saveDatabaseButton.setDisable(value);
		factoryResetButton.setDisable(value);
		listBox.setDisable(value);
		progressIndicator.setVisible(value);
		backButton.setDisable(value);
	}

	private AnchorPane generateSaveBox(String saveName)
	{
		final AnchorPane root;
		try
		{
			root = FXMLLoader.load(getClass().getResource(Paths.SAVE_BOX_FXML));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}

		Label saveLabel = (Label)root.lookup("#saveLabel");
		saveLabel.setText(saveName.substring(0, saveName.length()-4));

		Button deleteButton = (Button)root.lookup("#deleteButton");
		deleteButton.setOnAction(event ->
		{
			File f = new File("Save_Files/" + saveName);
			if(f.exists())
			{
				f.delete();
			}
			listBox.getChildren().remove(root);
		});

		Button loadButton = (Button)root.lookup("#loadButton");
		loadButton.setOnAction(event ->
		{
			setAllDisabled(true);

			new Thread(() ->
			{
				File f = new File("Save_Files/" + saveName);
				if(f.exists())
				{
					database.loadSaveState(f.getPath());
				}
				else
				{
					System.err.println("COULDN'T FIND SAVE FILE: Save_Files/" + saveName + ".wong");
				}

				Platform.runLater(() ->
				{
					setAllDisabled(false);
					Alert success = new Alert(Alert.AlertType.INFORMATION);
					success.setHeaderText("Save state loaded successfully");
					success.setTitle("Save State Loaded");
					success.show();
				});
			}).start();

		});

		return root;
	}

	public void factoryReset(ActionEvent actionEvent)
	{
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Warning");
		alert.setHeaderText("Warning: Factory Reset");
		alert.setContentText("All directory and map data will be reset to factory settings. This operation cannot be undone.");

		ButtonType ok = new ButtonType("OK");
		ButtonType cancel = new ButtonType("Cancel");
		alert.getButtonTypes().setAll(ok, cancel);

		Optional<ButtonType> result = alert.showAndWait();
		if(result.isPresent() && result.get() == ok)
		{
			//Reset the database in the background. Can't use runLater as this is a much more complex task (or it will be soon)
			Task resetTask = new Task<Void>()
			{
				@Override
				protected Void call()
				{
					database.resetDatabase();
					return null;
				}
			};
			Thread thread = new Thread(resetTask);
			thread.start();

			Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
			ProgressBar progressBar = new ProgressBar();

			GridPane grid = new GridPane();
			grid.setPrefWidth(350);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(20, 0, 10, 30));
			grid.add(progressBar, 0, 0);

			progressAlert.getDialogPane().setContent(grid);
			progressBar.setProgress(0);
			progressBar.setPrefWidth(300);
			progressAlert.setTitle("Reset Progress");
			progressAlert.setHeaderText("Reset Progress");
			Platform.runLater(progressAlert::show);

			progressAlert.initStyle(StageStyle.UNDECORATED);
			progressAlert.getButtonTypes().remove(ButtonType.OK);

			Task updateTask = new Task<Void>()
			{
				@Override
				public Void call() throws InterruptedException //idc
				{
					while (database.getResetProgress() != 1.0) //Yes, this freezes the main window. Yes, this is what we want.
						progressBar.setProgress(database.getResetProgress());
					progressBar.setProgress(1);
					//this kills the thread
					progressAlert.getButtonTypes().add(ButtonType.FINISH);

					return null;
				}
			};
			thread = new Thread(updateTask);
			thread.start();

			updateTask.setOnSucceeded(e -> {
				progressAlert.close();});
		}
	}
}
