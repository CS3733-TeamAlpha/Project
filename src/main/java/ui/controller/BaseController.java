package ui.controller;

import data.Database;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import data.Node;
import ui.*;

import java.io.IOException;

/**
 * Created by Ari on 2/14/17.
 */
public abstract class BaseController
{
	protected static Stage stage;
	private boolean currentSceneSupportsHC = true;
	private String[] highContrastBlackList = {Paths.LOGIN_FXML, Paths.DIRECTORY_EDITOR_FXML, Paths.USER_DIRECTORY_FXML};
	protected static Database database;

	private static Node searchedFor;

	// Make proxyimages to store floor pictures
	ProxyImage f1ImageProxy = Paths.f1ImageProxy;
	ProxyImage f2ImageProxy = Paths.f2ImageProxy;
	ProxyImage f3ImageProxy = Paths.f3ImageProxy;
	ProxyImage f4ImageProxy = Paths.f4ImageProxy;
	ProxyImage f5ImageProxy = Paths.f5ImageProxy;
	ProxyImage f6ImageProxy = Paths.f6ImageProxy;
	ProxyImage f7ImageProxy = Paths.f7ImageProxy;

	ProxyImage f1ContrastProxy = Paths.f1ContrastProxy;
	ProxyImage f2ContrastProxy = Paths.f2ContrastProxy;
	ProxyImage f3ContrastProxy = Paths.f3ContrastProxy;
	ProxyImage f4ContrastProxy = Paths.f4ContrastProxy;
	ProxyImage f5ContrastProxy = Paths.f5ContrastProxy;
	ProxyImage f6ContrastProxy = Paths.f6ContrastProxy;
	ProxyImage f7ContrastProxy = Paths.f7ContrastProxy;

	ProxyImage outdoorsProxy = Paths.outdoorImageProxy;

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
		{
			database = new Database("FHAlpha");
			ProviderBox.database = database;
		}
	}

	public abstract void initialize();

	public static void setStage(Stage s)
	{
		stage = s;
	}

	protected void loadFXML(String path)
	{
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
		{
			if (highContrastBlackList[i].equals(path))
			{
				currentSceneSupportsHC = false;
			}
		}
		updateCSS();
	}

	public void updateCSS()
	{
		if (currentSceneSupportsHC && Accessibility.isHighContrast())
		{
			enableHighContrastCss();
		} else
		{
			disableHighContrastCss();
		}
	}

	private void disableHighContrastCss()
	{
		if (stage.getScene().getStylesheets().contains(Accessibility.HIGH_CONTRAST_CSS))
		{
			stage.getScene().getStylesheets().remove(Accessibility.HIGH_CONTRAST_CSS);
		}
	}

	private void enableHighContrastCss()
	{
		if (!stage.getScene().getStylesheets().contains(Accessibility.HIGH_CONTRAST_CSS))
		{
			stage.getScene().getStylesheets().add(Accessibility.HIGH_CONTRAST_CSS);
		}
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
		if (type == 1)
		{
			ImageView buttonImage = new ImageView(Paths.doctorImageProxy.getFXImage());
			buttonImage.setScaleX(0.15);
			buttonImage.setScaleY(0.15);
			b.setGraphic(buttonImage);
		} else if (type == 2)
		{
			ImageView buttonImage = new ImageView(Paths.elevatorImageProxy.getFXImage());
			buttonImage.setScaleX(0.15);
			buttonImage.setScaleY(0.15);
			b.setGraphic(buttonImage);
		} else if (type == 3)
		{
			ImageView buttonImage = new ImageView(Paths.restroomImageProxy.getFXImage());
			buttonImage.setScaleX(0.15);
			buttonImage.setScaleY(0.15);
			b.setGraphic(buttonImage);
		} else if (type == 4 || type == 5)
		{
			ImageView buttonImage = new ImageView(Paths.kioskImageProxy.getFXImage());
			buttonImage.setScaleX(0.15);
			buttonImage.setScaleY(0.15);
			b.setGraphic(buttonImage);
		} else if (type == 0)
		{
		}
	}



}