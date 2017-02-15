package ui;

import data.Database;
import data.Provider;
import javafx.collections.FXCollections;
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

public class ProviderBox extends HBox
{
	//Terrible, disgusting hack to grand ProviderBox access to the database BECAUSE JAVA IS STUPID AND DOESN'T ALLOW FOR
	//MULTIPLE INHERITANCE! F*** YOU, JAVA, I'M LEAVING YOU FOR C++ AFTER THIS!
	//Basically, the AbstractController class will inject the right database object into this. Gah!
	static Database database = null;

	private Provider provider; //TODO: EXTERMINATE
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
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(Paths.PROVIDER_BOX_FXML));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		ArrayList<Node> nodes = database.getAllNodes();
		ArrayList<String> nodeNames = new ArrayList<String>();
		for(Node n: nodes){
			nodeNames.add(n.getName());
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
		for(Node n: database.getAllNodes()){
			if(n.getName().equals(s)){
				toAdd = n; //TODO: Jesus christ, this can be sped up.
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
			Label label = new Label(n.getName());
			Button button = new Button("X");
			box.getChildren().addAll(label,button);
			locationsVBox.getChildren().add(0,box);
		}
	}
}
