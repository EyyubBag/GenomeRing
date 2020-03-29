package com.genomeRing.view;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import com.genomeRing.model.structure.Genome;
import com.genomeRing.model.structure.GenomeEvent;
import com.genomeRing.model.structure.GenomeListener;
import com.genomeRing.model.structure.RingDimensions;
import com.genomeRing.view.genomeRingWindow.GenomeRingWindow;


/**
 * Contains the Groups for visualizing each Genome.
 * //TODO make a Tooltip appear if this Group is hovered
 * could implement the GenomeListener
 */
public class GenomeView extends Group {


    public GenomeView(Genome genome, RingDimensions ringDimensions, GenomeRingWindow window) {

        Color genomeColor = genome.getColor();

        GenomeSegmentView genomeSegmentView = new GenomeSegmentView(genome, ringDimensions, window);
        genomeSegmentView.setStyle(genomeColor, StrokeLineCap.BUTT, StrokeLineJoin.ROUND,ringDimensions.getGenomeWidth());
        genomeSegmentView.visibleProperty().bind(window.getController().getShowSegmentsCheckbox().selectedProperty());

        GenomePathView genomePathView = new GenomePathView(genome,ringDimensions,window);
        genomePathView.setStroke(GenomeColors.alphaColor(genomeColor, GenomePathView.CONNECTOR_ALPHA));
        genomePathView.setStrokeLineCap(StrokeLineCap.BUTT);
        genomePathView.setStrokeLineJoin(StrokeLineJoin.ROUND);
        genomePathView.setStrokeWidth(ringDimensions.getGenomeWidth());

        //Draw Flag after the paths so we can have the Directions
        GenomeFlagView genomeFlagsView = new GenomeFlagView(genome, ringDimensions);
       genomeFlagsView.setStyle(genomeColor, StrokeLineCap.BUTT, StrokeLineJoin.ROUND,ringDimensions.getGenomeWidth()/4);

        this.getChildren().addAll(genomePathView, genomeSegmentView,genomeFlagsView);

    }
}
