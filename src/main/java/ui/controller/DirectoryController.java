package ui.controller;

import data.Provider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pathfinding.Node;
import ui.Paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class DirectoryController extends BaseController
{

	//link buttons to node objects
	private HashMap<Provider, HBox> boxProviderLinks = new HashMap<Provider, HBox>();

	//arraylist of new Providers
	private ArrayList<Provider> newProviderList = new ArrayList<Provider>();

	//arraylist of Providers to be deleted
	private ArrayList<Provider> deleteProviderList= new ArrayList<Provider>();

	//currently selected node and button
	private Node currentNode = null;
	private Button currentButton = null;

	//list of modified providers whos changes will be pushed to the database
	private ArrayList<Provider> modifiedProvidersList = new ArrayList<Provider>();

	private int FLOORID = 3; //Default floor id for minimal application


	@FXML
	private ScrollPane directoryScroll;

	@FXML
	private VBox scrollContents;

	@FXML
	private Button pushChanges;

	public DirectoryController(){}

	public void initialize()
	{
		loadProvidersFromDatabase();
	}

	/**
	 * Load all nodes from the databasecontroller's list of providers onto our scene
	 */
	public void loadProvidersFromDatabase()
	{
		for (Provider p : database.getProviders())
		{
			loadProvider(p);
		}
	}

	public void addNewProvider()
	{
		Provider newProvider = Provider.newInstance("First Name", "Last Name", UUID.randomUUID().toString(), "Title", new ArrayList<>());
		newProviderList.add(newProvider);
		loadProvider(newProvider);
	}


	public void loadProvider(Provider p)
	{
		//ProviderBox pb = new ProviderBox();
		HBox newH = new HBox();
		Button deleteBut = new Button("X");
		deleteBut.setOnAction(event ->
		{
			deleteProviderList.add(p);
			((VBox) newH.getParent()).getChildren().remove(newH);
		});
		TextField fname = new TextField();
		fname.setText(p.getFirstName());
		fname.textProperty().addListener(event ->
		{
			if(!modifiedProvidersList.contains(p)){
				modifiedProvidersList.add(p);
			}
		});
		TextField lname = new TextField();
		lname.setText(p.getLastName());
		lname.textProperty().addListener(event ->
		{
			if(!modifiedProvidersList.contains(p))
				modifiedProvidersList.add(p);
		});
		TextField title = new TextField();
		title.setText(p.getTitle());
		title.textProperty().addListener(event ->
		{
			if(!modifiedProvidersList.contains(p))
				modifiedProvidersList.add(p);
		});
		VBox newV = new VBox();
		HBox newLocH = new HBox();
		////////////////////

		ChoiceBox locationSelector = new ChoiceBox();
		ArrayList<Node> nodes = database.getAllNodes();
		ArrayList<String> nodeNames = new ArrayList<>();
		for(Node n: nodes)
			nodeNames.add(n.getID() + ":" +n.getName());

		locationSelector.setItems(FXCollections.observableArrayList(nodeNames.toArray()));

		//Location stuff
		Button addBut = new Button("Add Location");
		addBut.setOnAction(event ->
		{
			String s = locationSelector.getValue().toString();

			int idIndex = s.indexOf(":");
			s = s.substring(0, idIndex);

			if(database.getNodeByUUID(s) != null)
			{
				Node n = database.getNodeByUUID(s);
				p.addLocation(n);
				HBox innerH = new HBox();
				Label locL = new Label();
				locL.setText("ID:" + n.getID() + ": " + n.getName()); //used to be .getData().get(0)
				Button xBut = new Button("X");
				xBut.setOnAction(event1 ->
				{
					((VBox) innerH.getParent()).getChildren().remove(innerH);
					if(!modifiedProvidersList.contains(p))
						modifiedProvidersList.add(p);
				});
				innerH.getChildren().addAll(locL, xBut);
				newV.getChildren().add(innerH);
				modifiedProvidersList.add(p);
			}
		});

		newLocH.getChildren().addAll(locationSelector, addBut);
		newV.getChildren().add(newLocH);

		HashMap<String, String> nodeIdNameMap = p.getNodeIdNameMap();
		for(String nodeKey: nodeIdNameMap.keySet())
		{
			HBox innerH = new HBox();
			Label locL = new Label();
			locL.setText("ID:"+nodeKey+": "+nodeIdNameMap.get(nodeKey));
			Button xBut = new Button("X");
			xBut.setOnAction(event ->
			{
				p.removeLocation(nodeKey);
				((VBox) innerH.getParent()).getChildren().remove(innerH);
				if(!modifiedProvidersList.contains(p))
					modifiedProvidersList.add(p);
			});
			innerH.getChildren().addAll(locL, xBut);
			newV.getChildren().add(innerH);
		}
		newH.getChildren().addAll(deleteBut, fname, lname, title, newV);
		boxProviderLinks.put(p, newH);
		scrollContents.getChildren().add(newH);
	}

	public void pushChangesToDatabase()
	{
		//Add any new providers
		for (Provider provider : newProviderList)
		{
			database.addProvider(provider);
		}
		newProviderList.clear();

		//Update any changed providers
		for(Provider thisProvider: modifiedProvidersList){
			HBox hb = boxProviderLinks.get(thisProvider);
			TextField title = (TextField)hb.getChildren().get(3);
			TextField fn = (TextField)hb.getChildren().get(1);
			TextField ln = (TextField)hb.getChildren().get(2);

			thisProvider.setFirstName(fn.getText());
			thisProvider.setLastName(ln.getText());
			thisProvider.setTitle(title.getText());

			//Update the provider...
			database.updateOrAddProvider(thisProvider);
			System.out.println("Updated provider!");
		}
		modifiedProvidersList.clear();

		for(Provider p: deleteProviderList)
			database.deleteProvider(p);
		deleteProviderList.clear();
	}

	public void showMap()
	{
		loadFXML(Paths.MAP_FXML);
	}

	public void showStartup()
	{
		loadFXML(Paths.STARTUP_FXML);
	}

}
