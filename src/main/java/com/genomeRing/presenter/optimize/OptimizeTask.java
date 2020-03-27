package com.genomeRing.presenter.optimize;

import javafx.concurrent.Task;
import com.genomeRing.model.structure.Block;
import com.genomeRing.model.structure.Genome;
import com.genomeRing.model.structure.RingDimensions;
import com.genomeRing.model.structure.SuperGenome;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Return the optimized List of Blocks depending on the method used.
 * TODO maybe make it return a ObservableList
 */
public class OptimizeTask extends Task<List<Block>> {
    private SuperGenomeOptimizer optimizer;
    private SuperGenome s;
    private RingDimensions ringDim;
    private int method = 0;
    private int variable = 0;

    /**
     *
     * @param method 0 for sort by jumps, 1 for blocks, 2 for angles
     * @param variable
     * @param s SuperGenome
     * @param ringDim
     */
    public OptimizeTask(final int method, final int variable, SuperGenome s, RingDimensions ringDim) {
        this.optimizer = new SuperGenomeOptimizer();
        this.method = method;
        this.variable = variable;
        this.s = s;
        this.ringDim = ringDim;
    }

    @Override
    protected List<Block> call() throws Exception {
        List<Block> original = new ArrayList<Block>(s.getBlocks());
        //first round: get costs from the given confirmation
        optimizer.setOldValue(Double.MAX_VALUE);
        optimizer.setTmpBlocks(null);
        optimizer.calculateCosts(variable, s, ringDim);

        s.setSilent(true);

        double converged = -1;
        int roundCount = 0;
        while (converged != optimizer.getOldValue()) {

            if (this.isCancelled()) {
                s.setBlocks(original);
                s.setSilent(false);
                return null;
            }

            updateMessage("Iteration: round " + (1 + roundCount));
            converged = optimizer.getOldValue();
            //try to optimize according to the current configuration
            switch (method) {
                case SuperGenomeOptimizer.SWITCH_RANDOMLY:
                    optimizer.switchBlocksRandomly(variable, s, ringDim);
                    break;
                case SuperGenomeOptimizer.SWITCH_BLOCKS_EXHAUSTIVELY:
                    optimizer.switchBlocksExhaustively(variable, s, ringDim);
                    break;
                case SuperGenomeOptimizer.SWITCH_FIRST_INSERT_LATER:
                    optimizer.switchFirstInsertLater(variable, s, ringDim);
                    break;
            }

            //try to optimize according to super genome sorting
            boolean better = optimizer.getCost(method, s.getInitialBlockOrder(), s, ringDim);
            if (better)
                optimizer.setTmpBlocks(new ArrayList<Block>(s.getInitialBlockOrder()));

            switch (method) {
                case SuperGenomeOptimizer.SWITCH_RANDOMLY:
                    optimizer.switchBlocksRandomly(variable, s, ringDim);
                    break;
                case SuperGenomeOptimizer.SWITCH_BLOCKS_EXHAUSTIVELY:
                    optimizer.switchBlocksExhaustively(variable, s, ringDim);
                    break;
                case SuperGenomeOptimizer.SWITCH_FIRST_INSERT_LATER:
                    optimizer.switchFirstInsertLater(variable, s, ringDim);
                    break;
            }
            //try to optimize according to all the other genomes
            for (Genome g : new LinkedList<Genome>(s.getGenomes())) {
                //This is also bad because it changes the observableList
                g.resortSuperGenome();

                better = optimizer.calculateCosts(variable, s, ringDim );
                if (better)
                    optimizer.setTmpBlocks(new ArrayList<Block>(s.getBlocks()));

                switch (method) {
                    case SuperGenomeOptimizer.SWITCH_RANDOMLY:
                        optimizer.switchBlocksRandomly(variable, s, ringDim);
                        break;
                    case SuperGenomeOptimizer.SWITCH_BLOCKS_EXHAUSTIVELY:
                        optimizer.switchBlocksExhaustively(variable, s, ringDim);
                        break;
                    case SuperGenomeOptimizer.SWITCH_FIRST_INSERT_LATER:
                        optimizer.switchFirstInsertLater(variable, s, ringDim );
                        break;
                }
            }
            roundCount++;

            if (optimizer.getTmpBlocks() != null) {
               return optimizer.getTmpBlocks();
            }
        }

        updateMessage("Converged after " + (roundCount) + " round(s)!");

        if (optimizer.getTmpBlocks() == null)
            optimizer.setTmpBlocks(original);

        s.setSilent(false);
        return optimizer.getTmpBlocks();
    }

}
