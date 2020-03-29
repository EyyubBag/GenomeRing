package com.genomeRing.view.dialogWindow;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.InputStream;
import java.net.URL;

//TODO align the elements correctly

/**
 * Wraps the Window created by the  "DialogWindow.fxml". Links it with the controller.
 */
public class DialogWindow  {

    private DialogWindowController controller;
    private Parent root;

    public DialogWindow() throws Exception {
        final FXMLLoader fxmlLoader = new FXMLLoader();
        final URL url = getClass().getResource("DialogWindow.fxml");

        if (url == null)
            throw new Exception("URL not found");
        try (final InputStream ins = url.openStream()) {
            fxmlLoader.load(ins);
        }

        this.root = fxmlLoader.getRoot();
        this.controller = fxmlLoader.getController();

    }

    public DialogWindowController getController() {
        return controller;
    }

    public Parent getRoot() {
        return root;
    }
}
