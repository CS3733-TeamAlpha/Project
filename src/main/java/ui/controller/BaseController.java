package ui.controller;

import data.Database;
import data.NodeTypes;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import data.Node;
import ui.*;

import java.io.IOException;

public abstract class BaseController
{
	protected static Stage stage;
	private boolean currentSceneSupportsHC = true;
	private String[] highContrastBlackList = {Paths.LOGIN_FXML, Paths.DIRECTORY_EDITOR_FXML, Paths.USER_DIRECTORY_FXML};
	protected static Database database;
	protected static int uiTimeout = 60; //default seconds to revert
	protected Watchdog watchdog;
	private static Node searchedFor;

	//default to floor1 of faulkner
	int FLOORID = 1;
	String BUILDINGID = "00000000-0000-0000-0000-000000000000";
	int MAXFLOOR = 7;

	//define widths for circles/lines that the canvas will draw
	double CIRCLEWIDTH = 13.0;
	double LINEWIDTH = 2.5;

	//X and Y offsets, for button placement.
	//TODO: fine tune offsets to make button placement visuals better
	double XOFFSET = CIRCLEWIDTH / 2;
	double YOFFSET = CIRCLEWIDTH / 2;

	double MINZOOM = 0.54;
	double MAXZOOM = 1.4;
	double currentZoom = 1.0;

	static
	{
		database = null;
	}

	public BaseController()
	{
		if (database == null)
			database = new Database("FHAlpha");
	}

	public abstract void initialize();

	public static void setStage(Stage s)
	{
		stage = s;
		stage.getIcons().add(new javafx.scene.image.Image(Main.class.getResourceAsStream(Paths.ICON)));
	}

	protected void loadFXML(String path)
	{
		if (watchdog != null)
		{
			watchdog.unregisterScene(stage.getScene(), Event.ANY);
			watchdog.disconnect();
		}

		Parent root = null;
		try
		{
			root = FXMLLoader.load(Main.class.getResource(path));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		stage.getScene().setRoot(root);

		//Update the high contrast option
		currentSceneSupportsHC = true;
		for (int i = 0; i < highContrastBlackList.length; i++)
			if (highContrastBlackList[i].equals(path))
				currentSceneSupportsHC = false;
	}

	protected void setSearchedFor(Node n)
	{
		searchedFor = n;
	}

	protected Node getSearchedFor()
	{
		return searchedFor;
	}

	/**
	 * Set the correct image to a node button
	 *
	 * @param b    target button to set graphic to
	 * @param type node type
	 */
	protected void setButtonImage(Button b, int type)
	{
		ImageView buttonImage = null;
		if (type == NodeTypes.DOCTOR_OFFICE.val())
			buttonImage = new ImageView(Paths.doctorImageProxy.getFXImage());
		else if (type == NodeTypes.ELEVATOR.val())
			buttonImage = new ImageView(Paths.elevatorImageProxy.getFXImage());
		else if (type == NodeTypes.RESTROOM.val())
			buttonImage = new ImageView(Paths.restroomImageProxy.getFXImage());
		else if (type == NodeTypes.KIOSK.val())
			buttonImage = new ImageView(Paths.kioskImageProxy.getFXImage());
		else if (type == NodeTypes.KIOSK_SELECTED.val())
			buttonImage = new ImageView(Paths.skioskImageProxy.getFXImage());
		else if (type == NodeTypes.STAIRWAY.val())
			buttonImage = new ImageView(Paths.stairwayImageProxy.getFXImage());
		else if (type == NodeTypes.PARKINGLOT.val())
			buttonImage = new ImageView(Paths.parkinglotImageProxy.getFXImage());
		else if (type == 0)
		{
			b.setGraphic(buttonImage);
			return;
		}
		else
			buttonImage = new ImageView(Paths.doorImageProxy.getFXImage());

		buttonImage.setScaleX(0.15);
		buttonImage.setScaleY(0.15);
		b.setGraphic(buttonImage);
	}
}