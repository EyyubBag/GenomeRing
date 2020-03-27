package com.genomeRing.presenter.optimize;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import com.genomeRing.model.structure.Block;
import com.genomeRing.model.structure.RingDimensions;
import com.genomeRing.model.structure.SuperGenome;

import java.util.List;

public class OptimizeService extends Service<List<Block>> {
    private SuperGenome s;
    private RingDimensions ringDim;
    private int method;
    private int variable;

    public OptimizeService( SuperGenome s, RingDimensions ringDim, int method, int variable) {
        this.s = s;
        this.ringDim = ringDim;
        this.method = method;

        this.variable = variable;
    }

    @Override
    protected Task<List<Block>> createTask() {
        OptimizeTask optimizeTask = new OptimizeTask(method,variable,s,ringDim);
        optimizeTask.exceptionProperty().addListener(new ChangeListener<Throwable>() {
            @Override
            public void changed(ObservableValue<? extends Throwable> observable, Throwable oldValue, Throwable newValue) {
                newValue.printStackTrace();
            }
        });

        return optimizeTask;
    }
}
