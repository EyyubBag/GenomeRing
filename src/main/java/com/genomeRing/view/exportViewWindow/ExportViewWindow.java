package com.genomeRing.view.exportViewWindow;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ExportViewWindow extends BorderPane {

    private Parent root;
    private ExportViewWindowController controller;

    public ExportViewWindow() throws Exception {

        final FXMLLoader fxmlLoader = new FXMLLoader();
        final URL url = getClass().getResource("ExportViewWindow.fxml");

        if (url == null)
            throw new Exception("URL not found");
        try (final InputStream ins = url.openStream()) {
            fxmlLoader.load(ins);
        }

        this.root = fxmlLoader.getRoot();
        this.controller = fxmlLoader.getController();

    }






    public Parent getRoot() {
        return root;
    }

    public ExportViewWindowController getController() {
        return controller;
    }
}
