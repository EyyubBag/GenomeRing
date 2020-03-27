package com.genomeRing.model.structure.tasks;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import com.genomeRing.model.structure.*;

import java.io.*;
import java.util.ArrayList;

/**
 * Reads in either a .xmfa or .blocks file and creates a SuperGenome Object
 */
public class LoadSuperGenomeTask extends Task<Void> {
    private SuperGenome superGenome;
    private final String fileName;

    public LoadSuperGenomeTask(SuperGenome superGenome, String fileName) {
        this.superGenome = superGenome;
        this.fileName = fileName;
        //Outputting the updateMessages to the Console
        this.messageProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                System.out.println(t1);
            }
        });
    }

    @Override
    protected Void call() throws Exception {
        BufferedReader br;
        //file must be in the xmfa format!
        if (!fileName.toLowerCase().endsWith(".blocks")) {
            //automatic number of genomes determination
            updateMessage("Determining number of genomes in the xmfa file ...\n");
            superGenome.determineGenomeNames(fileName);

            PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        SuperGenome.ringMode(out, fileName, superGenome.getMinBlockLength(), superGenome.isSubBlocks());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            updateMessage("Parsing XMFA file...\n");

            br = new BufferedReader(new InputStreamReader(in));
        } else { //or use a blocks file instead
            br = new BufferedReader(new FileReader(new File(fileName)));
        }

        Block[] blocks;

        // Read the blocks
        String blockline = br.readLine();
        if (blockline.trim().equals("")) {
//                    JOptionPane.showMessageDialog(null, "The SuperGenome contains no blocks!" +
//                            "\nUse less strict block length filter.", "Error", JOptionPane.ERROR_MESSAGE);

            br.close();
            updateMessage("Error - Stopped!\n");
            return null;
        }

        superGenome.init();

        updateMessage("Creating SuperGenome blocks...\n");

        String[] blockInfos = blockline.split(",");
        blocks = new Block[blockInfos.length];

        for (int i = 0; i != blockInfos.length; ++i) {
            String blockInfo = blockInfos[i];
            // get block name if included
            int pos = blockInfo.indexOf(":");
            String name = Integer.toString(i + 1);
            if (pos > -1) {
                name = blockInfo.substring(pos + 1);
                blockInfo = blockInfo.substring(0, pos);
            }
            // find out which format was used
            pos = blockInfo.indexOf('-');
            if (pos == -1) { // "length" format
                int length = Integer.parseInt(blockInfo);
                blocks[i] = new Block(superGenome, name, length);
            } else { // "start-end" format
                int start = Integer.parseInt(blockInfo.substring(0, pos));
                int end = Integer.parseInt(blockInfo.substring(pos + 1));
                blocks[i] = new Block(superGenome, name, start, end);
            }
            superGenome.addBlock(blocks[i]);
        }

        // Read the Genomes
        ArrayList<Genome> _genomes = new ArrayList<Genome>();

        String genomeLine;
        while ((genomeLine = br.readLine()) != null) {
            String[] name_and_stuff = genomeLine.split("\t");
            String name = name_and_stuff[0];
            updateMessage("Calculating covered blocks for genome " + name + "\n");
            if (name_and_stuff.length == 1) {
                updateMessage("Genome " + name + " has no blocks!\n");
                continue;
            }
            Genome g = new Genome(superGenome, false, name); // TODO: im moment keine Zirkulären
            String[] parts = name_and_stuff[1].split(",");
            for (int i = 0; i < parts.length; ++i) {
                String partInfo = parts[i];
                String position = null;
                int pos = partInfo.indexOf(':');
                if (pos != -1) { // "[-]block:start-end" format
                    position = partInfo.substring(pos + 1);
                    partInfo = partInfo.substring(0, pos);
                }
                CoveredBlock nextBlock;
                int value = Integer.parseInt(partInfo);
                int bidx = Math.abs(value) - 1;
                boolean bstrand = value > 0;
                nextBlock = new CoveredBlock(blocks[bidx], bstrand);
                if (position != null) { // Block has specific coordinates in Genome's space
                    pos = position.indexOf('-');
                    int start = Integer.parseInt(position.substring(0, pos));
                    int end = Integer.parseInt(position.substring(pos + 1));
                    nextBlock.setLocationInGenome(start, end);
                } else {
                    nextBlock.setLocationInGenome(blocks[bidx].getStart(), blocks[bidx].getEnd());
                }
                g.addCoveredBlock(nextBlock);
            }
            _genomes.add(g);
        }

        // prepare colors
        superGenome.getInitialColors().getColor(_genomes.size() - 1);
        // add genomes
        for (Genome g : _genomes)
            superGenome.addGenome(g);

        br.close();

        superGenome.storeInitialOrder();

        superGenome.fireChange(SuperGenomeEvent.GENOMES_CHANGED);

        updateMessage("Finished!\n");
        return null;
    }

}

