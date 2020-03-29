package com.genomeRing.view.exportViewWindow;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class ExportViewWindowController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private BorderPane exportBorderPane;

    @FXML
    private Button exportSaveButton;

    @FXML
    private Button exportCancelButton;

    @FXML
    private StackPane exportStackPane;

    @FXML
    private ImageView imageView;

    @FXML
    void initialize() {
        assert exportBorderPane != null : "fx:id=\"exportBorderPane\" was not injected: check your FXML file 'ExportViewWindow.fxml'.";
        assert exportSaveButton != null : "fx:id=\"exportSaveButton\" was not injected: check your FXML file 'ExportViewWindow.fxml'.";
        assert exportCancelButton != null : "fx:id=\"exportCancelButton\" was not injected: check your FXML file 'ExportViewWindow.fxml'.";
        assert exportStackPane != null : "fx:id=\"exportStackPane\" was not injected: check your FXML file 'ExportViewWindow.fxml'.";
        assert imageView != null : "fx:id=\"imageView\" was not injected: check your FXML file 'ExportViewWindow.fxml'.";

    }

    public ResourceBundle getResources() {
        return resources;
    }

    public URL getLocation() {
        return location;
    }

    public BorderPane getExportBorderPane() {
        return exportBorderPane;
    }

    public Button getExportSaveButton() {
        return exportSaveButton;
    }

    public Button getExportCancelButton() {
        return exportCancelButton;
    }

    public StackPane getExportStackPane() {
        return exportStackPane;
    }

    public ImageView getImageView() {
        return imageView;
    }
}
