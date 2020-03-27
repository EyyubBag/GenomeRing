package com.genomeRing.model.structure.tasks;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import com.genomeRing.model.structure.SuperGenome;

public class SaveSuperGenomeService extends Service<Void> {
    private SuperGenome superGenome;
    private String fileName;

    public SaveSuperGenomeService(SuperGenome superGenome, String fileName) {
        this.superGenome = superGenome;
        this.fileName = fileName;
    }

    @Override
    protected Task<Void> createTask() {
        SaveSuperGenomeTask task = new SaveSuperGenomeTask(superGenome, fileName);
        //Just to observe exception when running this service
        task.exceptionProperty().addListener(new ChangeListener<Throwable>() {
            @Override
            public void changed(ObservableValue<? extends Throwable> observableValue, Throwable throwable, Throwable t1) {
                t1.printStackTrace();
            }
        });
        return task;
    }
}
