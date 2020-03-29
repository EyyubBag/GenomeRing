package com.genomeRing.view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import com.genomeRing.model.structure.Block;
import com.genomeRing.model.structure.RingDimensions;
import com.genomeRing.model.structure.SuperGenome;
import com.genomeRing.view.genomeRingWindow.GenomeRingWindow;

/**
 * Contains the SuperGenome and the Blocklabels.
 */

public class SuperGenomeView extends PathView {

    public SuperGenomeView(SuperGenome superGenome, RingDimensions ringDimensions, GenomeRingWindow window) {
        for (Block b : superGenome.getBlocks()) {
		 // draw inner segment
		    addBlock(b, ringDimensions.getInnerRingRadiusInner(), ringDimensions.getInnerRingRadiusOuter(), ringDimensions);
			// draw outer segment
			addBlock(b, ringDimensions.getOuterRingRadiusInner(), ringDimensions.getOuterRingRadiusOuter(), ringDimensions);
			//draw Label
            addBlockLabel(b,ringDimensions,window);
		}
    }
    private void addBlock(Block b, double radius_inner, double radius_outer, RingDimensions ringdim) {
        double a1 = ringdim.getStartDegree(b);
        double a2 = ringdim.getEndDegree(b);

        //addSegment(radius_inner, a1, a2, false);
        Arc innerArc = new Arc(0,0 , radius_inner,  radius_inner, a1, a2 - a1);
        innerArc.setType(ArcType.OPEN);
        this.setShapeStyle(innerArc);


        Arc outerArc = new Arc(0,0 , radius_outer,  radius_outer, a2, a1 - a2);
        outerArc.setType(ArcType.OPEN);
        this.setShapeStyle(outerArc);

        Line line = new Line(polarToX(radius_inner, a2), polarToY(radius_inner,a2),polarToX(radius_outer, a2), polarToY(radius_outer,a2));
        this.setShapeStyle(line);

        Line line2 = new Line(polarToX(radius_outer, a1), polarToY(radius_outer,a1),polarToX(radius_inner, a1), polarToY(radius_inner,a1));
        this.setShapeStyle(line2);

        this.getChildren().addAll(innerArc,line,outerArc, line2);
    }

    /**
     * Original Code in it.genomering.gui.GenomeRingPanel.java
     * @param b Block
     * @param ringDimensions
     * @param window So we can bind the Checkbox to the visibility of the Text object.
     */
    private void addBlockLabel(Block b, RingDimensions ringDimensions,GenomeRingWindow window){

        Text blocklabel = new Text();
        blocklabel.textProperty().bind(b.nameProperty());
        blocklabel.setFont(new Font(30));


        //Disable the Blocklabels by unselecting the Show Block Labels Checkbox
        blocklabel.visibleProperty().bind(window.getController().getShowBlockLabelsCheck().selectedProperty());
        //Rotate the Blocklables when rotating the View with the Mousewheel
        blocklabel.rotateProperty().bind(window.getGenomeRingView().rotateProperty().multiply(-1));


        //calculate boundaries of the Text
        double width = blocklabel.getLayoutBounds().getWidth();
        double height=blocklabel.getLayoutBounds().getHeight();

        //calculate Distance and Position
        double textdist = Math.max(width, height)/2 + ringDimensions.getRingDistance()/2;

        double a1 = ringDimensions.getStartDegree(b);
        double a2 = ringDimensions.getEndDegree(b);
        double a = (a1+a2)/2;
        double r = ringDimensions.getOuterRingRadiusOuter()+textdist;
        double x = PathView.polarToX(r,a);
        double y = PathView.polarToY(r,a);

        blocklabel.setTranslateX(x);
        blocklabel.setTranslateY(y);


        blocklabel.setX((int)(-width/2));
        blocklabel.setY((int)(height/2));



        this.getChildren().add(blocklabel);

        //When changing the Name of a Block we need to adjust the Position of the Text to the size of the new String.
        blocklabel.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                //calculate boundaries of the Text
                double width = blocklabel.getLayoutBounds().getWidth();
                double height=blocklabel.getLayoutBounds().getHeight();

                //calculate Distance and Position
                double textdist = Math.max(width, height)/2 + ringDimensions.getRingDistance()/2;

                double a1 = ringDimensions.getStartDegree(b);
                double a2 = ringDimensions.getEndDegree(b);
                double a = (a1+a2)/2;
                double r = ringDimensions.getOuterRingRadiusOuter()+textdist;
                double x = PathView.polarToX(r,a);
                double y = PathView.polarToY(r,a);

                blocklabel.setTranslateX(x);
                blocklabel.setTranslateY(y);


                blocklabel.setX((int)-width/2);
                blocklabel.setY((int)height/2);
            }
        });




        }




}
