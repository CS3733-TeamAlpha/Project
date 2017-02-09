package ui;

import data.DatabaseController;
import data.Provider;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pathfinding.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class DirectoryController
{

	//link buttons to node objects
	private HashMap<HBox, Provider> boxProviderLinks = new HashMap<HBox, Provider>();

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
		DatabaseController.createConnection();
		DatabaseController.initializeAllFloors();
		DatabaseController.initializeAllNodes();
		DatabaseController.initializeAllProviders();
		loadProvidersFromDatabase();
	}

	/**
	 * Load all nodes from the databasecontroller's list of providers onto our scene
	 */
	public void loadProvidersFromDatabase()
	{
		for (Provider p : DatabaseController.getAllProviders())
		{
			loadProvider(p);
		}
	}

	public void addNewProvider(){
		Provider newP = null;
		if(newProviderList.size() == 0){
			newP = DatabaseController.generateNewProvider("FName", "LName", "Title");
		} else {
			newP = generateNewProvider();
		}
		loadProvider(newP);
	}

	public Provider generateNewProvider()
	{
		int newID = newProviderList.get(newProviderList.size()-1).getID()+1;
		return new Provider(newID, "FName", "LName", "Title");
	}

	public void loadProvider(Provider p)
	{
		//ProviderBox pb = new ProviderBox();
		HBox newH = new HBox();
		Button deleteBut = new Button("X");
		deleteBut.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				deleteProviderList.add(p);
				((VBox) newH.getParent()).getChildren().remove(newH);
			}
		});
		TextField fname = new TextField();
		fname.setText(p.getfName());
		TextField lname = new TextField();
		lname.setText(p.getlName());
		TextField title = new TextField();
		title.setText(p.getTitle());
		VBox newV = new VBox();
		HBox newLocH = new HBox();
		TextField inputID = new TextField();
		Button addBut = new Button("Add Location");
		addBut.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if(DatabaseController.getNodeByID(Integer.parseInt(inputID.getText())) != null)
				{
					Node n = DatabaseController.getNodeByID(Integer.parseInt(inputID.getText()));
					p.addLocation(n);
					HBox innerH = new HBox();
					Label locL = new Label();
					locL.setText("ID:" + n.getID() + ": " + n.getData().get(0));
					Button xBut = new Button("X");
					xBut.setOnAction(new EventHandler<ActionEvent>()
					{
						@Override
						public void handle(ActionEvent event)
						{
							((VBox) innerH.getParent()).getChildren().remove(innerH);
						}
					});
					innerH.getChildren().addAll(locL, xBut);
					newV.getChildren().add(innerH);
				}
			}
		});
		newLocH.getChildren().addAll(inputID, addBut);
		newV.getChildren().add(newLocH);
		for(Node n: p.getLocations())
		{
			HBox innerH = new HBox();
			Label locL = new Label();
			locL.setText("ID:"+n.getID()+": "+n.getData().get(0));
			Button xBut = new Button("X");
			xBut.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent event)
				{
					p.removeLocation(n);
					((VBox) innerH.getParent()).getChildren().remove(innerH);
				}
			});
			innerH.getChildren().addAll(locL, xBut);
			newV.getChildren().add(innerH);
		}
		newH.getChildren().addAll(deleteBut, fname, lname, title, newV);
		boxProviderLinks.put(newH, p);
		//add button to scene
		//TODO: set editingFloor to the correct panel name
		scrollContents.getChildren().add(newH);
	}

	public void createNewProvider()
	{
		//TODO:
	}

	public void pushChangesToDatabase(){
		for(HBox hb: boxProviderLinks.keySet()){
			Provider thisProvider = boxProviderLinks.get(hb);

			TextField tit = (TextField)hb.getChildren().get(1);
			thisProvider.setfName(tit.getText());
			TextField fn = (TextField)hb.getChildren().get(2);
			thisProvider.setlName(fn.getText());
			TextField ln = (TextField)hb.getChildren().get(3);
			thisProvider.setTitle(ln.getText());
			System.out.println(thisProvider.getID()+ thisProvider.getfName()+thisProvider.getlName()+thisProvider.getTitle());
			if(DatabaseController.getProviderByID(thisProvider.getID()) == null)
			{
				DatabaseController.insertProvider(thisProvider);
			}
			else
			{
				DatabaseController.modifyProviderTable(thisProvider);
			}

			ArrayList<Integer> pbIDs = new ArrayList<Integer>();
			ArrayList<Integer> oldIDs = new ArrayList<Integer>();

			for(Node loc: thisProvider.getLocations())
			{
				pbIDs.add(loc.getID());
			}
			DatabaseController.initializeAllProviders();
			for(Node n: DatabaseController.getProviderByID(thisProvider.getID()).getLocations())
			{
				oldIDs.add(n.getID());
			}
			for(int i: pbIDs){
				if(!oldIDs.contains(i))
				{
					DatabaseController.insertOffice(thisProvider.getID(), i);
				}
			}
			for(int i: oldIDs){
				if(!pbIDs.contains(i))
				{
					DatabaseController.removeOffice(thisProvider.getID(), i);
				}
			}

		}

		for(Provider p: deleteProviderList)
		{
			DatabaseController.removeOfficeByProvider(p.getID());
			DatabaseController.removeProvider(p.getID());
		}
		for(Provider p: newProviderList)
		{
			System.out.println("Pls");
			DatabaseController.insertProvider(p);
			for(Node n: p.getLocations()){
				DatabaseController.insertOffice(p.getID(), n.getID());
			}
		}
	}


	public void showMap()
	{
		Main.loadFXML("/fxml/Map.fxml");
	}

	public void showStartup()
	{
		Main.loadFXML("/fxml/Startup.fxml");
	}

}
