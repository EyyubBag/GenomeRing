package com.genomeRing.model.structure.tasks;

import javafx.concurrent.Task;
import com.genomeRing.model.structure.CoveredBlock;
import com.genomeRing.model.structure.Genome;
import com.genomeRing.model.structure.SuperGenome;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * Saves the SuperGenome into a .blocks file.
 */
public class SaveSuperGenomeTask extends Task<Void> {
    private SuperGenome superGenome;
    private String fileName;

    public SaveSuperGenomeTask(SuperGenome superGenome, String fileName) {
        this.superGenome = superGenome;
        this.fileName = fileName;
    }

    @Override
    protected Void call() throws Exception {
        updateMessage("Writing SuperGenome blocks...\n");

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName)));
        bw.write(superGenome.toString());
        bw.newLine();
        updateMessage("Finished SuperGenome\n");

        for(int i = 0; i < superGenome.getGenomes().size(); i++) {
            Genome g = superGenome.getGenomes().get(i);
            bw.write(g.getName());
            List<CoveredBlock> gBlocks = g.getBlocks();

            if(gBlocks.size() > 0) {
                bw.write("\t");
                for(int j = 0; j < gBlocks.size(); j++) {
                    CoveredBlock cb = gBlocks.get(j);
                    if(!cb.isForward()) {
                        bw.write("-");
                    }

                    bw.write(Integer.toString(cb.getIndex()+1));
                    bw.write(":");
                    bw.write(cb.getStart() + "-" + cb.getEnd());

                    if(j != gBlocks.size()-1) {
                        bw.write(",");
                    }
                }
            }

            bw.newLine();
            updateMessage("Finished Genome " + g.getName() + "\n");
        }

        bw.close();
        updateMessage("Done!\n");
        //TODO maybe change return type,
        return null;
    }

}
