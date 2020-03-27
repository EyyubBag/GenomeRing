package com.genomeRing.view.dialogWindow;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class DialogWindowController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TextField blockSizeInput;

    @FXML
    private CheckBox subBlockCheck;

    @FXML
    void initialize() {
        assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'DialogWindow.fxml'.";
        assert blockSizeInput != null : "fx:id=\"blockSizeInput\" was not injected: check your FXML file 'DialogWindow.fxml'.";
        assert subBlockCheck != null : "fx:id=\"subBlockCheck\" was not injected: check your FXML file 'DialogWindow.fxml'.";

    }

    public ResourceBundle getResources() {
        return resources;
    }

    public URL getLocation() {
        return location;
    }

    public AnchorPane getAnchorPane() {
        return anchorPane;
    }

    public TextField getBlockSizeInput() {
        return blockSizeInput;
    }

    public CheckBox getSubBlockCheck() {
        return subBlockCheck;
    }
}
