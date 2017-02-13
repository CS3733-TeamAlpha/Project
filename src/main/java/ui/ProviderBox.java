package ui;

import data.DatabaseController;
import data.Provider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pathfinding.Node;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Ari on 2/8/17.
 */
public class ProviderBox extends HBox
{

	private Provider provider;
	@FXML
	private TextField firstNameField;
	@FXML
	private TextField lastNameField;
	@FXML
	private  TextField titlesField;
	@FXML
	private ChoiceBox locationSelector;
	@FXML
	private VBox locationsVBox;


	public ProviderBox(){
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ProviderBox.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		ArrayList<Node> nodes = DatabaseController.getAllNodes();
		ArrayList<String> nodeNames = new ArrayList<String>();
		for(Node n: nodes){
			nodeNames.add(n.getData().get(0));
		}
		locationSelector.setItems(FXCollections.observableArrayList(nodeNames.toArray()));
	}

	//public ProviderBox(Provider p){
	//	this();
	//	provider = p;
	//	refreshBox();
	//}

	@FXML
	protected void addLocation(){
		String s = locationSelector.getValue().toString();  //might be broken
		Node toAdd = null;
		for(Node n: DatabaseController.getAllNodes()){
			if(n.getData().get(0).equals(s)){
				toAdd = n;
			}
		}
		provider.addLocation(toAdd);
		refreshBox();
	}

	public String getFirstName(){
		return firstNameField.getText();
	}

	public String getLastName(){
		return lastNameField.getText();
	}

	public String getTitles(){
		return titlesField.getText();
	}

	public void refreshBox(){
		firstNameField.setText(provider.getfName());
		lastNameField.setText(provider.getlName());
		titlesField.setText(provider.getTitle());
		for(Node n:provider.getLocations()){
			HBox box = new HBox();
			//Label label = new Label(n.getData().get(0));
			Button button = new Button("X");
			//box.getChildren().addAll(label,button);
			locationsVBox.getChildren().add(0,box);
		}
	}
}
