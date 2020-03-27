package com.genomeRing.view;

import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import com.genomeRing.model.structure.CoveredBlock;
import com.genomeRing.model.structure.Genome;
import com.genomeRing.model.structure.RingDimensions;
import com.genomeRing.view.genomeRingWindow.GenomeRingWindow;

import java.util.List;

public class GenomeSegmentView extends PathView {

    public GenomeSegmentView(Genome genome, RingDimensions ringDimensions, GenomeRingWindow window) {
        this.colorProperty().bind(genome.colorProperty());
        int gidx = genome.getIndex();
        List<CoveredBlock> blocks = genome.getBlocks(); // blocks sorted in order of genome

        for (int i=0; i!=blocks.size(); ++i) {
            CoveredBlock b = blocks.get(i);
            // add block
            addBlock(b, ringDimensions, gidx);
        }
    }

    private void addBlock(CoveredBlock b, RingDimensions ringdim, int gidx) {
        double segment_radius = b.isForward()?ringdim.getRadiusForward(gidx):ringdim.getRadiusBackward(gidx);
        double alpha_start = ringdim.getStartDegree(b);
        double alpha_end = ringdim.getEndDegree(b);

        Arc segmentArc = new Arc(0,0 , segment_radius,  segment_radius, alpha_start, alpha_end - alpha_start);
        segmentArc.setType(ArcType.OPEN);
        this.setShapeStyle(segmentArc);

        this.getChildren().add(segmentArc);
    }
}
