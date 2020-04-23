package com.genomeRing;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.genomeRing.presenter.Presenter;
import com.genomeRing.view.genomeRingWindow.GenomeRingWindow;

public class GenomeRing extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        GenomeRingWindow view = new GenomeRingWindow(primaryStage);
        Presenter presenter = new Presenter(view,primaryStage);
        Scene scene = new Scene(presenter.getWindow().getRoot(),1600,800);

        primaryStage.setTitle("GenomeRing");
        primaryStage.setScene(scene);
        primaryStage.show();
    }



}
