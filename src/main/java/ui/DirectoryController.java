package ui;

import data.Provider;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pathfinding.Node;

import java.util.ArrayList;
import java.util.HashMap;

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
		for (String providerName : database.getProviders())
		{
			//Assemble providers into provider classes. This is a temporary workaround so that database changes can be
			//pushed as soon as possible; the Provider class is slated for removal.
			//TODO: Make DirectoryController and related class no longer use class Provider
			Provider p = new Provider(database.getProviderUUID(providerName),
					providerName.split(",")[1].split(";")[0], 	//fname
					providerName.split(",")[0],						//lname
					providerName.split(";")[1],						//titles. String.split is magical
					database.getProviderLocations(database.getProviderUUID(providerName)));
			loadProvider(p);
		}
	}

	public void addNewProvider(){
		Provider newP = null;
		newP = generateNewProvider();
		loadProvider(newP);
	}

	public Provider generateNewProvider()
	{
		return new Provider(java.util.UUID.randomUUID().toString(), "FName", "LName", "Title", new ArrayList<Node>());
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
		fname.setText(p.getfName());
		fname.setOnAction(event ->
		{
			if(!modifiedProvidersList.contains(p)){
				modifiedProvidersList.add(p);
			}
		});
		TextField lname = new TextField();
		lname.setText(p.getlName());
		lname.setOnAction(event ->
		{
			if(!modifiedProvidersList.contains(p)){
				modifiedProvidersList.add(p);
			}
		});
		TextField title = new TextField();
		title.setText(p.getTitle());
		title.setOnAction(event ->
		{
			if(!modifiedProvidersList.contains(p)){
				modifiedProvidersList.add(p);
			}
		});
		VBox newV = new VBox();
		HBox newLocH = new HBox();
		////////////////////

		ChoiceBox locationSelector = new ChoiceBox();
		ArrayList<Node> nodes = database.getAllNodes();
		ArrayList<String> nodeNames = new ArrayList<String>();
		for(Node n: nodes)
			nodeNames.add(n.getID() + ":" +n.getName()); //this used to be n.getData().get(0) with an int->string id tacked on

		locationSelector.setItems(FXCollections.observableArrayList(nodeNames.toArray()));

		Button addBut = new Button("Add Location");
		addBut.setOnAction(event ->
		{
			String s = locationSelector.getValue().toString();

			int idIndex = s.indexOf(":");
			s = s.substring(0, idIndex);

			if(database.getNodeByUUID(s) != null)
			{
					String nodeID = s; //TODO: Eliminate this

					Node n = database.getNodeByUUID(nodeID);
					p.addLocation(n);
					HBox innerH = new HBox();
					Label locL = new Label();
					locL.setText("ID:" + n.getID() + ": " + n.getName()); //used to be .getData().get(0)
					Button xBut = new Button("X");
					xBut.setOnAction(event1 ->
					{
						((VBox) innerH.getParent()).getChildren().remove(innerH);
						if(!modifiedProvidersList.contains(p)){
							modifiedProvidersList.add(p);
						}
					});
					innerH.getChildren().addAll(locL, xBut);
					newV.getChildren().add(innerH);
			}
		});
		newLocH.getChildren().addAll(locationSelector, addBut);
		newV.getChildren().add(newLocH);
		for(Node n: p.getLocations())
		{
			HBox innerH = new HBox();
			Label locL = new Label();
			locL.setText("ID:"+n.getID()+": "+n.getName());
			Button xBut = new Button("X");
			xBut.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent event)
				{
					p.removeLocation(n);
					((VBox) innerH.getParent()).getChildren().remove(innerH);
					if(!modifiedProvidersList.contains(p)){
						modifiedProvidersList.add(p);
					}
				}
			});
			innerH.getChildren().addAll(locL, xBut);
			newV.getChildren().add(innerH);
		}
		newH.getChildren().addAll(deleteBut, fname, lname, title, newV);
		boxProviderLinks.put(p, newH);
		//add button to scene
		//TODO: set editingFloor to the correct panel name
		scrollContents.getChildren().add(newH);
	}

	public void createNewProvider()
	{
		//TODO:
	}

	/**
	 * Pushes changes to the database. There's a good chance that this function is heavily bugged as a result of the
	 * database changeover; obviously it will be a hotspot for future development. This is due to the entirely new
	 * model that the new database runs under: the new database maintains tight control of its nodes and expects to
	 * recieve updates accordingly. It does not give as easily to the changeset model presented here.
	 */
	public void pushChangesToDatabase(){
		for(Provider thisProvider: modifiedProvidersList){
			HBox hb = boxProviderLinks.get(thisProvider);
			//Provider thisProvider = boxProviderLinks.get(hb);

			TextField tit = (TextField)hb.getChildren().get(1);
			thisProvider.setfName(tit.getText());
			TextField fn = (TextField)hb.getChildren().get(2);
			thisProvider.setlName(fn.getText());
			TextField ln = (TextField)hb.getChildren().get(3);
			thisProvider.setTitle(ln.getText());
			System.out.println(thisProvider.getID()+ thisProvider.getfName()+thisProvider.getlName()+thisProvider.getTitle());

			//Automatically inserts non-existent providers where necessary.
			for (Node node : thisProvider.getLocations())
				database.updateNode(node); //*Might* cause a few interesting bugs. TODO: Investigate?

			ArrayList<String> pbIDs = new ArrayList<String>();
			ArrayList<String> oldIDs = new ArrayList<String>();

			for(Node loc: thisProvider.getLocations())
			{
				pbIDs.add(loc.getID());
			}
			for(Node n : database.getProviderLocations(thisProvider.getID())) //DatabaseController.getProviderByID(thisProvider.getID()).getLocations())
			{
				oldIDs.add(n.getID());

			}
			/* TODO: Can probably be deleted, the updateNode() function should've automagically taken care of it
			for(String i: pbIDs){
				if(!oldIDs.contains(i))
				{
					DatabaseController.insertOffice(thisProvider.getID(), i);
				}
			}
			for(String i: oldIDs){
				if(!pbIDs.contains(i))
				{
					DatabaseController.removeOffice(thisProvider.getID(), i);
				}
			}*/

		}

		for(Provider p: deleteProviderList)
		{
			database.deleteProvider(p.getID());
		}
		/* TODO: Can probably be deleted, the updateNode() function should've automagically taken care of it.
		for(Provider p: newProviderList)
		{
			System.out.println("Pls");
			DatabaseController.insertProvider(p);
			for(Node n: p.getLocations()){
				DatabaseController.insertOffice(p.getID(), n.getID());
			}
		}
		*/
	}


	public void showMap()
	{
		loadFXML(Paths.MAP_FXML);
	}

	public void showStartup()
	{
		loadFXML(Paths.ADMIN_PAGE_FXML);
	}

}
