package ui;

import data.DatabaseController;
import data.Provider;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import pathfinding.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class DirectoryController
{

	//link buttons to node objects
	private HashMap<ProviderBox, Provider> boxProviderLinks = new HashMap<ProviderBox, Provider>();

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

	public DirectoryController(){}

	public void initialize()
	{
		if(Accessibility.isHighContrast())
		{
			//TODO: fix this with the appropriate image
			floorImage.setImage(new Image(Accessibility.HIGH_CONTRAST_MAP_PATH));
		}
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

	public void loadProvider(Provider p)
	{
		//TODO: initialize new custom bos here

		//on button click display the node's details
		???.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				showNodeDetails(nodeB);
			}
		});

		boxProviderLinks.put(???, p);
		//add button to scene
		//TODO: set editingFloor to the correct panel name
		editingFloor.getChildren().add(1, ???);
	}

	public void createNewProvider()
	{
		//TODO:
	}

	public void pushChangesToDatabase(){
		for(ProviderBox pb: boxProviderLinks.keySet()){
			Provider thisProvider = boxProviderLinks.get(pb);

			thisProvider.setfName(pb.getFName());
			thisProvider.setlName(pb.getLName());
			thisProvider.setTitle(pb.getTitle());
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

			for(Node loc: pb.getLocations())
			{
				pbIDs.add(loc.getID());
			}
			for(Node n: thisProvider.getLocations())
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
