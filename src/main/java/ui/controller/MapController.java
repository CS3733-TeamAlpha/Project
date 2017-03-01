package ui.controller;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import data.Node;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.util.Duration;
import pathfinding.AStarGraph;
import pathfinding.Graph;
import pathfinding.TextualDirections;
import ui.Paths;
import ui.Watchdog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class MapController extends BaseController
{
	public static final int PATH_LINE_OFFSET = 0;
	public Label textDirectionsLabel;
	static Graph graph;
	public Button returnToKioskButton;
	public Button backButton;
	private HashMap<Button, Node> nodeButtons = new HashMap<>();

	private Node selected;
	private Node kiosk;
	boolean findingDirections = false;
	boolean hasNextStep = false;
	boolean resetSteps = false;
	boolean stepping = false;
	boolean dontDoSelection = false;

	SequentialTransition magicalSequence = new SequentialTransition();
	Circle magicalCircle = new Circle();

	private Pane currentTooltip = null;
	static boolean usingStairs = false; //ugly static hack

	private ArrayList<Line> currentPath = new ArrayList<>();

	public class MapZoomHandler implements EventHandler<ScrollEvent>
	{

		public MapZoomHandler()
		{
		}

		@Override
		public void handle(ScrollEvent scrollEvent) {
			double preZoom = currentZoom;
			calculateScale(scrollEvent);
			rescale();
			if(preZoom != currentZoom)
			{
				scroller.setHvalue(scroller.getHvalue() + (currentZoom - preZoom)/2);
				scroller.setVvalue(scroller.getVvalue() + (currentZoom - preZoom)/2);
			}
			scrollEvent.consume();
		}

		private void calculateScale(ScrollEvent scrollEvent)
		{
			double scale = currentZoom + scrollEvent.getDeltaY() / 5000;
			currentZoom = scale;
		}
	}

	/**
	 * rescale the floor/image and adjust its position to fit.
	 * Currently doesn't keep any sort of image centering so view shifts strangely
	 */
	void rescale(){

		double hRatio = scroller.getHeight()/floorImage.getImage().getHeight();
		double wRatio = scroller.getWidth()/floorImage.getImage().getWidth();

		//determine minzoom dynamically so we can zoom out to see whole map
		double minZoom = Math.min(hRatio, wRatio);

		if (currentZoom <= minZoom)
			currentZoom = minZoom;
		else if (currentZoom >= MAXZOOM)
			currentZoom = MAXZOOM;

		editingFloor.setScaleX(currentZoom);
		editingFloor.setScaleY(currentZoom);

		//set sizing to zoomwrapper. maybe excessive set statements, but sometimes its fucky
		zoomWrapper.setPrefHeight(editingFloor.getHeight()*currentZoom);
		zoomWrapper.setPrefWidth(editingFloor.getWidth()*currentZoom);

		zoomWrapper.setMinWidth(editingFloor.getWidth()*currentZoom);
		zoomWrapper.setMinHeight(editingFloor.getHeight()*currentZoom);
		zoomWrapper.setMaxWidth(editingFloor.getWidth()*currentZoom);
		zoomWrapper.setMaxHeight(editingFloor.getHeight()*currentZoom);

		//if width is larger than scroll area...
		if(zoomWrapper.getWidth() > scroller.getWidth())
			editingFloor.setLayoutX((zoomWrapper.getWidth() - floorImage.getImage().getWidth()) / 2);
		//otherwise center the image
		else
			editingFloor.setLayoutX(((scroller.getWidth()-zoomWrapper.getWidth()/currentZoom)/2));
		editingFloor.setLayoutY((zoomWrapper.getHeight() - floorImage.getImage().getHeight()) / 2);
	}

	@FXML
	private ScrollPane faulknerScroller;

	@FXML
	private AnchorPane faulknerZoomWrapper;

	@FXML
	private AnchorPane faulknerEditingFloor;

	@FXML
	private ImageView faulknerFloorImage;

	@FXML
	private ScrollPane outdoorsScroller;

	@FXML
	private AnchorPane outdoorsZoomWrapper;

	@FXML
	private AnchorPane outdoorsEditingFloor;

	@FXML
	private ImageView outdoorsFloorImage;

	@FXML
	private ScrollPane belkinScroller;

	@FXML
	private AnchorPane belkinZoomWrapper;

	@FXML
	private AnchorPane belkinEditingFloor;

	@FXML
	private ImageView belkinFloorImage;

	@FXML
	private Button upFloor;

	@FXML
	private Button downFloor;

	@FXML
	private Label currentFloorLabel;

	@FXML
	private VBox roomInfo;

	@FXML
	private Label roomName;

	@FXML
	private Label servicesLabel;

	@FXML
	private Button previousStep;

	@FXML
	private Button nextStep;

	@FXML
	private TabPane buildingTabs;

	@FXML
	private Tab faulknerTab;

	@FXML
	private Tab belkinTab;

	@FXML
	private Tab outdoorsTab;

	@FXML
	private CheckBox stairsCheckbox;

	ScrollPane scroller = faulknerScroller;
	ImageView floorImage = faulknerFloorImage;
	AnchorPane zoomWrapper = faulknerZoomWrapper;
	AnchorPane editingFloor = faulknerEditingFloor;

	ArrayList<Label> loadedLabels = new ArrayList<>();

	//these variables are set just to help deal with the building UUIDs
	final String FAULKNER_UUID = "00000000-0000-0000-0000-000000000000";
	final String BELKIN_UUID = "00000000-0000-0000-0000-111111111111";
	final String OUTSIDE_UUID = "00000000-0000-0000-0000-222222222222";

	public MapController()
	{
		super();
	}

	public void initialize()
	{
		watchdog = new Watchdog(Duration.seconds(uiTimeout), ()->
		{
			loadFXML(Paths.STARTUP_FXML);
			clearPath(null);
			setSearchedFor(null);
		});
		watchdog.registerScene(stage.getScene(), Event.ANY);
		initializeTabs();

		System.out.println("MapController.initialize()");
		hideRoomInfo();
		kiosk = database.getSelectedKiosk();
		if (graph == null)
			graph = new AStarGraph();
		loadNodesFromDatabase(); //Get nodes in from database
		setFloorImage(BUILDINGID, FLOORID); //Set image of floor

		//style up/down buttons
		upFloor.setId("upbuttonTriangle");
		downFloor.setId("downbuttonTriangle");

		nextStep.setDisable(true);
		previousStep.setDisable(true);

		DropShadow ds = new DropShadow();
		ds.setColor(Color.BLACK);
		backButton.setEffect(ds);

		ds = new DropShadow();
		ds.setSpread(0.75);
		ds.setRadius(15);
		ds.setColor(Color.color(1, 1, 1));
		currentFloorLabel.setEffect(ds);

		Node searched = getSearchedFor();

		//make a fade transition on all editing floor images, so that we can hide the initial
		//jarring jump to the kiosk/searchednode
		FadeTransition ftf = new FadeTransition(Duration.millis(1000), faulknerEditingFloor);
		ftf.setFromValue(0);
		ftf.setToValue(1);
		FadeTransition ftb = new FadeTransition(Duration.millis(1000), belkinEditingFloor);
		ftb.setFromValue(0);
		ftb.setToValue(1);
		FadeTransition fto = new FadeTransition(Duration.millis(1000), outdoorsEditingFloor);
		fto.setFromValue(0);
		fto.setToValue(1);
		ftf.play();
		ftb.play();
		fto.play();

		if(searched != null)
		{
			new Thread(() ->
			{
				try
				{
					Thread.sleep(100);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				Platform.runLater(() -> initialSearchFocusView(searched));
			}).start();

		}
		else
		{
			new Thread(() ->
			{
				try
				{
					Thread.sleep(100);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				Platform.runLater(() -> initialFocusView(kiosk));
			}).start();
		}

		stairsCheckbox.setSelected(usingStairs);
	}

	/**
	 * Function used for workaround for bug that makes focusView in initialize completely fuck up
	 * the map's scaling and scrollbars
	 * @param n Node to focus on, in this case the kiosk
	 */
	private void initialFocusView(Node n)
	{
		BUILDINGID = n.getBuilding();
		changeBuilding(BUILDINGID);
		jumpFloor(n.getFloor());
		focusView(n);

		//make the kiosk's location more obvious
		ImageView yahImage = new ImageView(Paths.yahImageProxy.getFXImage());
		yahImage.setX(n.getX() - yahImage.getImage().getWidth()/2);
		yahImage.setY(n.getY() - yahImage.getImage().getHeight());
		editingFloor.getChildren().add(yahImage);

		//animate a fade after a delay
		SequentialTransition sequence = new SequentialTransition();
		Timeline timeline = new Timeline();
		KeyValue solid = new KeyValue(yahImage.opacityProperty(), 1);
		KeyFrame kf1 = new KeyFrame(Duration.millis(1500), solid);
		timeline.getKeyFrames().add(kf1);
		KeyValue invis = new KeyValue(yahImage.opacityProperty(), 0);
		KeyFrame kf2 = new KeyFrame(Duration.millis(2500), invis);
		timeline.getKeyFrames().add(kf2);
		sequence.getChildren().add(timeline);
		sequence.play();
		yahImage.toFront();

		selected = n;
	}

	@FXML
	void gotoKiosk(ActionEvent event)
	{
		clearPath(null);
		initialFocusView(kiosk);
		showRoomInfo(kiosk, false);
	}

	/**
	 * Function used for workaround for bug that makes focusView in initialize completely fuck up
	 * the map's scaling and scrollbars
	 * @param n Node to focus on, in this case the searched node
	 */
	private void initialSearchFocusView(Node n)
	{
		//go to kiosk to, beginning of path
		BUILDINGID = kiosk.getBuilding();
		changeBuilding(BUILDINGID);
		jumpFloor(kiosk.getFloor());
		focusView(kiosk);

		selected = n;
		findingDirections = true;
		showRoomInfo(n, false);
		setSearchedFor(null);

		//begin magical journey
		magicalJourney();
	}

	/**
	 * Set up handlers for changing tabs and set default panes for interaction.
	 * Default to faulkner
	 */
	private void initializeTabs()
	{
		faulknerFloorImage.setImage(Paths.regularFloorImages[0].getFXImage());
		belkinFloorImage.setImage(Paths.belkinFloorImages[0].getFXImage());
		outdoorsFloorImage.setImage(Paths.outdoorImageProxy.getFXImage());

		scroller = faulknerScroller;
		floorImage = faulknerFloorImage;
		zoomWrapper = faulknerZoomWrapper;
		editingFloor = faulknerEditingFloor;
		faulknerTab.setOnSelectionChanged(event ->
		{
			if(faulknerTab.isSelected() && !dontDoSelection)
				changeBuilding(FAULKNER_UUID);
		});
		belkinTab.setOnSelectionChanged(event ->
		{
			if(belkinTab.isSelected()  && !dontDoSelection)
				changeBuilding(BELKIN_UUID);
		});
		outdoorsTab.setOnSelectionChanged(event ->
		{
			if(outdoorsTab.isSelected() && !dontDoSelection)
				changeBuilding(OUTSIDE_UUID);
		});

		//add event filter to let scrolling do zoom instead
		faulknerScroller.addEventFilter(ScrollEvent.ANY, new MapZoomHandler());
		belkinScroller.addEventFilter(ScrollEvent.ANY, new MapZoomHandler());
		outdoorsScroller.addEventFilter(ScrollEvent.ANY, new MapZoomHandler());
	}

	public void showRoomInfo(Node n)
	{
		showRoomInfo(n, true);
	}

	public void showRoomInfo(Node n, boolean focus)
	{
		Node focusNode = null;
		if(selected != n) //no selection, so set node that we're showing room info for as selected
		{
			selected = n;
			findingDirections = false;
			focusNode = n;
		}
		if(findingDirections)
		{
			ArrayList<Node> path = trimPathToBuildingFloor(graph.findPath(kiosk, selected, usingStairs));

			if(path == null)
			{
				focusNode = kiosk;
				textDirectionsLabel.setText("No path found");
			}
			else
			{
				ArrayList<String> textDirections =
					TextualDirections.getDirections(path, graph.findPath(kiosk, selected, usingStairs));
				StringBuilder build = new StringBuilder();
				if(textDirections != null)
				{
					for (String sentence : textDirections)
					{
						build.append(sentence);
						build.append("\n");
					}
				}
				textDirectionsLabel.setText(build.toString());

				if (currentPath.size() != 0)
				{
					for(Line l: currentPath)
						((AnchorPane) l.getParent()).getChildren().remove(l);
					currentPath.clear();
				}
				//flip path to be ordered
				for(int i = 0; i < path.size() / 2; i++)
				{
					Node temp = path.get(i);
					path.set(i, path.get(path.size()-1-i));
					path.set(path.size()-1-i, temp);
				}
				for (int i = 0; i < path.size()-1; i++)
				{
					if(path.get(i).getFloor() == FLOORID && path.get(i).getBuilding().equals(BUILDINGID)
							&& path.get(i+1).getFloor() == FLOORID && path.get(i+1).getBuilding().equals(BUILDINGID))
					{
						if(focusNode == null)
							focusNode = path.get(i);
						Line line = new Line();
						line.setStrokeLineCap(StrokeLineCap.ROUND);
						line.setStartX(path.get(i).getX()+ PATH_LINE_OFFSET); //plus 15 to center on button
						line.setStartY(path.get(i).getY()+PATH_LINE_OFFSET);
						line.setEndX(path.get(i+1).getX()+PATH_LINE_OFFSET);
						line.setEndY(path.get(i+1).getY()+PATH_LINE_OFFSET);
						line.setStrokeWidth(10);
						// Change color for line if it is high contrast

						line.setStroke(Color.BLUE);
						editingFloor.getChildren().add(1,line);
						currentPath.add(line);
					}
				}

				if (!hasNextStep)
				{
					//if target is in different building/floor this path has a next step
					if(!n.getBuilding().equals(kiosk.getBuilding()) ||
							n.getFloor() != kiosk.getFloor())
					{
						hasNextStep = true;
						nextStep.setDisable(false);
					}
				}
			}
		}

		if(focusNode != null && focus)
			focusView(focusNode);
		roomName.setText(n.getName());
		String toAdd = "";
		ArrayList<String> services = n.getServices();
		for (int i = 0; i < services.size(); i++)
		{
			String s = services.get(i).trim();
			toAdd += s;
			if(i < services.size()-1)
				toAdd += ", ";
		}
		servicesLabel.setText(toAdd);
	}

	/**
	 * Remove nodes in path that are not in the current building/floor
	 * @param path The list of nodes representing the path to the destination
	 * @return nodes in the path that are in the current buildingfloor
	 */
	private ArrayList<Node> trimPathToBuildingFloor(ArrayList<Node> path)
	{
		ArrayList<Node> trimPath = new ArrayList<Node>();
		//checks if the node has no path associated with it
		if(path == null)
		{
			//put the desired popup below
			System.out.println("Straight empty son");
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Error");
			alert.setHeaderText("Error: This Node has no connections to it");
			alert.setContentText("Please contact your admin about setting up a connection to this location");

			ButtonType ok = new ButtonType("OK");

			alert.getButtonTypes().setAll(ok);

			Optional<ButtonType> result = alert.showAndWait();
			findingDirections = false;
			clearPath(null);
			return null;
		}
		else
		{
			//if node does have path
			for(Node n: path)
				if (n.getBuilding().equals(BUILDINGID) && n.getFloor() == FLOORID)
					trimPath.add(n);
		}
		return trimPath;
	}

	public void hideRoomInfo()
	{
		selected = null;
		findingDirections = false;
		hasNextStep = false;
	}

	/**
	 * focus the view of the map to center on the node
	 * @param n The node to focus view on
	 */
	private void focusView(Node n)
	{
		double nX = n.getX()*currentZoom;
		double nY = n.getY()*currentZoom;

		//check that width or height is actually larger than scroll area
		if(zoomWrapper.getWidth() > scroller.getWidth() ||
				zoomWrapper.getHeight() > scroller.getHeight())
		{
			double newHval, newVval;
			//all the way to the right
			if(zoomWrapper.getWidth()-nX < scroller.getWidth()/2)
				newHval = 1;
			//all the way to the left
			else if(nX < (scroller.getWidth())/2)
				newHval = 0;
			//otherwise math
			else
				newHval = (nX-scroller.getWidth() / 2) / (zoomWrapper.getWidth() - scroller.getWidth());

			//all the way to the top
			if(nY < (scroller.getHeight()) / 2)
				newVval = 0;
			//all the way to the bottom
			else if(zoomWrapper.getHeight()-nY < scroller.getHeight() / 2)
				newVval = 1;
			//math
			else
				newVval = (nY-scroller.getHeight() / 2) / (zoomWrapper.getHeight()-scroller.getHeight());

			if(!stepping)
			{
				Timeline animated = new Timeline();
				KeyValue hKey = new KeyValue(scroller.hvalueProperty(), newHval, Interpolator.EASE_OUT);
				KeyValue vKey = new KeyValue(scroller.vvalueProperty(), newVval, Interpolator.EASE_OUT);
				KeyFrame frame = new KeyFrame(Duration.millis(300), hKey, vKey);
				animated.getKeyFrames().add(frame);
				animated.play();
			}
			else //if building/floor was changed while animation, jump instantly to focus
			{
				scroller.setHvalue(newHval);
				scroller.setVvalue(newVval);
			}
		}
	}

	public void showStartup()
	{
		loadFXML(Paths.STARTUP_FXML);
	}

	public void findDirectionsTo()
	{
		if(selected != null)
		{
			clearPath(null);
			resetSteps = false;
			if (hasNextStep)
				hasNextStep = false;
			BUILDINGID = kiosk.getBuilding();
			changeBuilding(BUILDINGID);
			jumpFloor(kiosk.getFloor());

			findingDirections = true;
			nextStep.setDisable(true);
			previousStep.setDisable(true);
			showRoomInfo(selected);

			magicalJourney();
		}
	}


	/**
	 * Jump directly to the target floor.
	 * This functino also purges and reload buttons for the appropriate floor
	 */
	void jumpFloor(int floor)
	{
		purgeButtons();
		FLOORID = floor;
		loadNodesFromDatabase();
		currentFloorLabel.setText(Integer.toString(FLOORID));
		setFloorImage(BUILDINGID, FLOORID);
	}

	@FXML
	/**
	 * Change the current floor to increment up by 1.
	 * Prevent going down if floor is already 1.
	 */
	void goDownFloor(ActionEvent event)
	{
		if(FLOORID > 1)
		{
			//remove all buttons and lines on the current floor
			FLOORID--;
			purgeButtons();
			loadNodesFromDatabase();
			currentFloorLabel.setText(Integer.toString(FLOORID));
			setFloorImage(BUILDINGID, FLOORID);
		}
		if(hasNextStep)
		{
			hasNextStep = false;
			resetSteps = true;
		}
		if(selected != null)
			showRoomInfo(selected);
	}

	@FXML
	/**
	 * Change the current floor to increment down by 1.
	 * Prevent going up if floor is already 7.
	 */
	void goUpFloor (ActionEvent event){
		int topFloor;

		if(BUILDINGID.equals(BELKIN_UUID))
			topFloor = 4;
		else if(BUILDINGID.equals(FAULKNER_UUID))
			topFloor = 7;
		 else //Outside
			topFloor = 1;

		if(FLOORID < topFloor){
			//remove all buttons and lines on the current floor
			purgeButtons();
			FLOORID++;
			loadNodesFromDatabase();
			currentFloorLabel.setText(Integer.toString(FLOORID));
			setFloorImage(BUILDINGID, FLOORID);
		}

		//if a path has been searched, indicate the user has manually changed the view
		if(hasNextStep)
		{
			hasNextStep = false;
			resetSteps = true;
		}

		if(selected != null)
			showRoomInfo(selected);
	}

	/**
	 * Go to next step in path
	 * Prevent moving if already on last floor
	 * @param event
	 */
	@FXML
	public void goNextStep(ActionEvent event)
	{
		if(resetSteps) //reset to start from the kiosk
			resetSteps();
		else if(hasNextStep) //if the searched path has next steps
		{
			stepping = true;
			//previousstep button starts disabled, so reenable after going to next step
			previousStep.setDisable(false);
			//clear lines
			if(currentPath.size() != 0)
			{
				for(Line l: currentPath)
					((AnchorPane) l.getParent()).getChildren().remove(l);
				currentPath.clear();
			}

			//if target is not in the same building as kiosk
			if(!selected.getBuilding().equals(kiosk.getBuilding()))
			{
				//if we're not on ground floor and we're in kiosk's building, go to bottom floor
				if(FLOORID != 1 && BUILDINGID.equals(kiosk.getBuilding()))
					jumpFloor(1);

				//if we are already in target building, go to target floor
				else if (BUILDINGID.equals(selected.getBuilding()))
					jumpFloor(selected.getFloor());
				else
				{
					//if we are in outside building, go into target building
					if(BUILDINGID.equals(OUTSIDE_UUID))
					{
						BUILDINGID = selected.getBuilding();
						changeBuilding(BUILDINGID);
					}
					//otherwise go outside
					else
					{
						BUILDINGID = OUTSIDE_UUID;
						changeBuilding(BUILDINGID);
					}
				}

				findingDirections = true;
				showRoomInfo(selected);
			}
			//if selected is in same building as kiosk
			else
			{
				//jump to correct floor
				jumpFloor(selected.getFloor());
				findingDirections = true;
				showRoomInfo(selected);
			}
			stepping = false;
		}

		//if we're at the target building and floor, disable next step
		if(BUILDINGID.equals(selected.getBuilding()) && FLOORID == selected.getFloor())
			nextStep.setDisable(true);

		magicalJourney();
	}

	/**
	 * take the visitor on a magical journey
	 */
	private void magicalJourney()
	{
		boolean nextDisabled = nextStep.isDisabled();
		boolean prevDisabled = previousStep.isDisabled();

		//manually set layoutX and scaling to workaround weird sizing bug when changing tabs
		currentZoom = 1;
		rescale();
		if(!BUILDINGID.equals(BELKIN_UUID))
		{
			editingFloor.setLayoutX(0);
			editingFloor.setLayoutY(0);
		}

		//disable next/prev step while animating
		nextStep.setDisable(true);
		previousStep.setDisable(true);
		if (currentPath.size() != 0)
		{
			magicalCircle.setRadius(14f);
			magicalCircle.setFill(Color.RED);
			editingFloor.getChildren().add(magicalCircle);
			magicalCircle.toFront();
			magicalCircle.setCenterX(currentPath.get(0).getStartX());
			magicalCircle.setCenterY(currentPath.get(0).getStartY());
			magicalCircle.setOpacity(1);
			double newH = 0;
			double newV = 0;
			double newX = currentPath.get(0).getStartX();
			double newY = currentPath.get(0).getStartY();

			newH = getNewH(newX);
			newV = getNewV(newY);
			//jump to correct initial position
			scroller.setVvalue(newV);
			scroller.setHvalue(newH);
		}
		magicalSequence = new SequentialTransition();
		//for each line calculate the vvalue the scroll bar should be at to "center" it in view
		for (Line l : currentPath)
		{
			double duration = 350;

			Timeline timeline = new Timeline();
			double newH = 0;
			double newV = 0;
			double newX = l.getEndX();
			double newY = l.getEndY();

			newH = getNewH(newX);
			newV = getNewV(newY);

			//kinda unnecessary math.
			double dif = Math.sqrt(
					Math.pow(l.getStartX()-l.getEndX(), 2) +
							Math.pow(l.getStartY()-l.getEndY(), 2));
			duration = 6.5 * dif;

			//keyframe stuff
			KeyValue kv = new KeyValue(scroller.vvalueProperty(), newV);
			KeyValue kh = new KeyValue(scroller.hvalueProperty(), newH);
			KeyValue cx = new KeyValue(magicalCircle.centerXProperty(), l.getEndX());
			KeyValue cy = new KeyValue(magicalCircle.centerYProperty(), l.getEndY());
			KeyFrame kf = new KeyFrame(Duration.millis(duration), kv, kh, cx, cy);
			KeyFrame wd = new KeyFrame(Duration.millis(0), e->{watchdog.notIdle();});
			timeline.getKeyFrames().add(kf);
			timeline.getKeyFrames().add(wd);
			magicalSequence.getChildren().add(timeline);
		}
		//make circle fade out as the last animation item, fix bug caused by stupid javafx shit
		KeyValue cc = new KeyValue(magicalCircle.opacityProperty(), 0);
		KeyFrame cckf = new KeyFrame(Duration.millis(300), cc);
		Timeline lastTimeline = new Timeline();
		lastTimeline.getKeyFrames().add(cckf);
		magicalSequence.getChildren().add(lastTimeline);

		magicalSequence.play();
		magicalSequence.setOnFinished(event ->
		{
			nextStep.setDisable(nextDisabled);
			previousStep.setDisable(prevDisabled);
			editingFloor.getChildren().remove(magicalCircle);
		});
	}

	double getNewH(double newX)
	{
		double newH = 0;
		if (editingFloor.getWidth() - newX < scroller.getWidth() / 2)
			newH = 1;
		//all the way to the left
		else if (newX < scroller.getWidth() / 2)
			newH = 0;
		//math
		else
			newH = (newX - scroller.getWidth() / 2) / (editingFloor.getWidth() - scroller.getWidth());
		return newH;
	}

	double getNewV(double newY)
	{
		double newV = 0;
		//all the way to at the bottom
		if (editingFloor.getHeight() - newY < scroller.getHeight() / 2)
			newV = 1;
		//all the way at the top
		else if (newY < scroller.getHeight() / 2)
			newV = 0;
		//math
		else
			newV = (newY - scroller.getHeight() / 2) / (editingFloor.getHeight() - scroller.getHeight());
		return newV;
	}

	/**
	 * work in progress
	 */
	private void fadeinWrapper()
	{
		FadeTransition fit = new FadeTransition(Duration.millis(2000), editingFloor);
		fit.setFromValue(0.0);
		fit.setToValue(1.0);
		fit.play();
	}

	/**
	 * Reset to kiosk location
	 */
	private void resetSteps()
	{
		//reset to the start of the steps if user manually changed view while showing path

		//disable step buttons
		previousStep.setDisable(true);
		nextStep.setDisable(false);

		//reset all stuff so we go to the kiosk view
		BUILDINGID = kiosk.getBuilding();
		changeBuilding(BUILDINGID);
		FLOORID = kiosk.getFloor();
		jumpFloor(FLOORID);
		findingDirections = true;
		showRoomInfo(selected);

		resetSteps = false;
	}

	/**
	 * Go to previous floor in path
	 * Prevent moving if already on first floor
	 * @param event
	 */
	@FXML
	public void goPreviousStep(ActionEvent event)
	{
		if(resetSteps) //reset to start from the kiosk
			resetSteps();
		else if(hasNextStep) //if the searched path has next steps
		{
			stepping = true;
			//next step button starts disabled, so reenable after going to prev step
			nextStep.setDisable(false);
			//clear lines
			if(currentPath.size() != 0)
			{
				for(Line l: currentPath)
					((AnchorPane) l.getParent()).getChildren().remove(l);
				currentPath.clear();
			}

			//if target is not in the same building as kiosk
			if(!selected.getBuilding().equals(kiosk.getBuilding()))
			{
				//if we're on ground floor and we're in kiosk's building, go to kiosk floor
				if(FLOORID == 1 && BUILDINGID.equals(kiosk.getBuilding()))
					jumpFloor(kiosk.getFloor());

				//if we are already in target building, go to target floor
				else if (BUILDINGID.equals(kiosk.getBuilding()))
					jumpFloor(kiosk.getFloor());
				else
				{
					//if we are in outside building, go into kiosk building
					if(BUILDINGID.equals(OUTSIDE_UUID))
					{
						BUILDINGID = kiosk.getBuilding();
						changeBuilding(BUILDINGID);
					}
					//otherwise go outside
					else
					{
						BUILDINGID = OUTSIDE_UUID;
						changeBuilding(BUILDINGID);
					}
				}

				findingDirections = true;
				showRoomInfo(selected);
			}
			//if selected is in same building as kiosk
			else
			{
				//jump to correct floor
				jumpFloor(kiosk.getFloor());
				findingDirections = true;
				showRoomInfo(selected);
			}
			stepping = false;
		}

		//if we're all the way back at the kiosk, disable previous step
		if(BUILDINGID.equals(kiosk.getBuilding()) && FLOORID == kiosk.getFloor())
			previousStep.setDisable(true);
		magicalJourney();
	}

	/**
	 * Clear line showing path, deselect all nodes
	 * @param event
	 */
	@FXML
	void clearPath(ActionEvent event)
	{
		hasNextStep = false;
		findingDirections = false;
		if(currentPath.size() != 0)
		{
			for(Line l: currentPath)
				((AnchorPane) l.getParent()).getChildren().remove(l);
			currentPath.clear();
		}
		nextStep.setDisable(true);
		previousStep.setDisable(true);
		textDirectionsLabel.setText("");

		editingFloor.getChildren().remove(magicalCircle);
		magicalSequence.stop();
		magicalSequence.getChildren().clear();
	}

	/**
	 * Change the building that is currently being edited.
	 * This will change the tab that is currently selected
	 * @param building String name of the buildng to view
	 */
	private void changeBuilding(String building)
	{
		//change selected building ID
		BUILDINGID = building;
		//remove all buttons and lines on the current floor

		purgeButtons();

		//default to floor 1 when changing buildings
		FLOORID = 1;

		//dontdoselection flag is set so that changing the tab selection doesn't incur infinite loop
		dontDoSelection = true;

		//change tab and set scroller/floorimage/zoomwrapper/editingfloor to the correct selections
		if (BUILDINGID.equals(FAULKNER_UUID))//faulkner, max 7 floor
		{
			scroller = faulknerScroller;
			floorImage = faulknerFloorImage;
			zoomWrapper = faulknerZoomWrapper;
			editingFloor = faulknerEditingFloor;
			if (buildingTabs.getSelectionModel().getSelectedIndex() != 0)
				buildingTabs.getSelectionModel().select(0);
			MAXFLOOR = 7;
		}
		else if (BUILDINGID.equals(BELKIN_UUID))//belkin, max 4 floor
		{
			scroller = belkinScroller;
			floorImage = belkinFloorImage;
			zoomWrapper = belkinZoomWrapper;
			editingFloor = belkinEditingFloor;
			if (buildingTabs.getSelectionModel().getSelectedIndex() != 2)
				buildingTabs.getSelectionModel().select(2);
			MAXFLOOR = 4;
		}
		else
		{
			scroller = outdoorsScroller;
			floorImage = outdoorsFloorImage;
			zoomWrapper = outdoorsZoomWrapper;
			editingFloor = outdoorsEditingFloor;
			if(buildingTabs.getSelectionModel().getSelectedIndex() != 1)
				buildingTabs.getSelectionModel().select(1);
			MAXFLOOR = 1;
		}

		currentZoom = 1;
		rescale();

		editingFloor.setLayoutX(0);
		editingFloor.setLayoutY(0);
		dontDoSelection = false;

		jumpFloor(FLOORID);

		//if we have a next step and changebuilding wasn't called within the next/prev step functions
		if (hasNextStep && !stepping)
		{
			hasNextStep = false;
			resetSteps = true;
		}

		if (selected != null)
			showRoomInfo(selected);
	}


	/**
	 * Set the floorImage imageview object to display the image of
	 * a specific floor.
	 * Currently only works based on floor, not building
	 * @param floor The floor to display
	 */
	private void setFloorImage(String buildingid, int floor)
	{
		//faulkner
		if(buildingid.equals(FAULKNER_UUID))
			floorImage.setImage(Paths.regularFloorImages[floor-1].getFXImage());
		//belkin
		else if(buildingid.equals(BELKIN_UUID))
			floorImage.setImage(Paths.belkinFloorImages[floor-1].getFXImage());
		//outside
		else if (buildingid.equals(OUTSIDE_UUID));
			//floorImage.setImage(Paths.outdoorImageProxy.getFXImage());
	}

	/**
	 * Create a button on the scene and associate it with a node
	 * @param n the node to load into the scene
	 */
	private void loadNode(Node n, ArrayList<LabelThingy> thingies)
	{
		if(n.getType() != 0)
		{
			//new button
			Button nodeB = new Button();

			if(n.getType() != 0)
				nodeB.setCursor(Cursor.HAND);

			//experimental style changes to make the button a circle
			nodeB.setId("node-button-unselected");

			//add node to nodeButtons
			nodeButtons.put(nodeB, n);

			//set button XY coordinates
			nodeB.setLayoutX(n.getX() - XOFFSET);
			nodeB.setLayoutY(n.getY() - YOFFSET);

			setButtonImage(nodeB, n.getType());
			if (n.getType() == 1)
			{
				boolean changed = false;
				for(LabelThingy thingy : thingies)
				{
					if(thingy.x == n.getX() && thingy.y == n.getY())
					{
						changed = true;
						if(!thingy.text.isEmpty())
							thingy.text += ", ";
						thingy.text += n.getName().trim();
					}
				}

				if(!changed)
				{
					LabelThingy temp = new LabelThingy();
					temp.x = (int)n.getX();
					temp.y = (int)n.getY();
					temp.displayX = (int)nodeB.getLayoutX();
					temp.displayY = (int)nodeB.getLayoutY();
					temp.text = n.getName().trim();
					thingies.add(temp);
				}
			}

			nodeB.hoverProperty().addListener((observable, oldValue, newValue) ->
			{
				if(currentTooltip != null && !newValue)
					editingFloor.getChildren().remove(currentTooltip);

				if(newValue)
				{
					StringBuilder tooltipText = new StringBuilder();
					for (int i = 0; i < n.getProviders().size(); i++)
					{
						tooltipText.append(n.getProviders().get(i).getFirstName());
						tooltipText.append(", ");
						tooltipText.append(n.getProviders().get(i).getLastName());
						if (i < n.getProviders().size() - 1)
							tooltipText.append("\n");
					}
					if(!tooltipText.toString().isEmpty())
					{
						StackPane p = new StackPane();
						p.setAlignment(Pos.CENTER);
						p.setLayoutX(nodeB.getLayoutX() + 25);
						p.setLayoutY(nodeB.getLayoutY() + 25);
						p.getStyleClass().add("bh-tooltip");

						Label theText = new Label(tooltipText.toString());
						theText.getStyleClass().add("bh-tooltip-text");
						p.getChildren().add(theText);
						editingFloor.getChildren().add(p);
						currentTooltip = p;
					}
				}
			});

			nodeB.setOnAction(event -> showRoomInfo(n));
			//add button to scene
			editingFloor.getChildren().add(1, nodeB);
		}
	}

	private void addLabels(LabelThingy thingy)
	{
		InnerShadow innerShadow = new InnerShadow();
		innerShadow.setColor(Color.BLACK);

		DropShadow ds = new DropShadow();
		ds.setSpread(0.75);
		ds.setRadius(15);
		ds.setColor(Color.color(1, 1, 1));
		ds.setInput(innerShadow);

		Label roomLabel = new Label(thingy.text);
		roomLabel.setEffect(ds);
		roomLabel.setFont(new Font("GlacialIndifference", 14));

		FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
		int roundedXOffset = (int)Math.round(fontLoader.computeStringWidth(roomLabel.getText(), roomLabel.getFont()) / 2.0);

		if (thingy.text.length()%2 == 0 || thingy.text.equals("Radiology"))
		{
			roomLabel.setLayoutX(thingy.x - roundedXOffset);
			roomLabel.setLayoutY(thingy.y-35);
		}
		else
		{
			roomLabel.setLayoutX(thingy.x - roundedXOffset);
			roomLabel.setLayoutY(thingy.y+15);
		}
		roomLabel.setId("roomLabel");
		editingFloor.getChildren().add(1, roomLabel);
		loadedLabels.add(roomLabel);
	}

	/**
	 * Load all nodes from the databasecontroller's list of nodes onto our scene
	 */
	public void loadNodesFromDatabase()
	{
		ArrayList<LabelThingy> points = new ArrayList<>();

		ArrayList<Node> nodesInBuildingFloor = database.getNodesInBuildingFloor(BUILDINGID, FLOORID);
		for (Node n : nodesInBuildingFloor)
			loadNode(n, points);

		for(LabelThingy thingy : points)
			addLabels(thingy);
	}

	/**
	 * Hide all node buttons for the current floor, in preperation for changing floors.
	 */
	private void purgeButtons()
	{
		for(Button b: nodeButtons.keySet()) //remove this button from the UI
			((AnchorPane) b.getParent()).getChildren().remove(b);

		//clear all entries in nodeButtonLinks
		nodeButtons.clear();

		if(currentPath.size() != 0)
		{
			for(Line l: currentPath)
				((AnchorPane) l.getParent()).getChildren().remove(l);
			currentPath.clear();
		}

		//remove dot and stop animation if purgebuttons is called
		//don't try to refactor this poorly by just using clearpath. probably will mess something up
		if(editingFloor.getChildren().contains(magicalCircle))
			editingFloor.getChildren().remove(magicalCircle);
		magicalSequence.stop();
		magicalSequence.getChildren().clear();

		for(Label l : loadedLabels)
			((AnchorPane) l.getParent()).getChildren().remove(l);
		loadedLabels.clear();
	}

	/**
	 * Changes the status of whether stairs are being used, then re-plots the path.
	 */
	@FXML
	private void changeUseStairs()
	{
		usingStairs = stairsCheckbox.isSelected();
	}

	class LabelThingy
	{
		int displayX, displayY;
		int x, y;
		String text = "";
	}
}
