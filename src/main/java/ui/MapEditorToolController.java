package ui;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import data.*;
import pathfinding.*;

public class MapEditorToolController
{


    public MapEditorToolController(){}

    private boolean makingNew = false;

    @FXML
    private AnchorPane editingFloor;

    @FXML
    private ImageView floorImage;

    @FXML
    private Button newNodeButton;

    @FXML
    void addNodeHere(MouseEvent e) {
        if(e.isStillSincePress() && makingNew)
        {
            Button bz = new Button("+");
            bz.setLayoutX(e.getX());
            bz.setLayoutY(e.getY());
            editingFloor.getChildren().add(1, bz);
            makingNew = false;
            newNodeButton.setText("Add Node");
        }
    }


    public void createNewNode()
    {
        if(newNodeButton.getText().equals("Add Node"))
        {
            makingNew = true;
            newNodeButton.setText("Click here to cancel new node creation");
        } else {
            makingNew = false;
            newNodeButton.setText("Add Node");
        }
    }


}
