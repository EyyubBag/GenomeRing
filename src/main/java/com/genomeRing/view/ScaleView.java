package com.genomeRing.view;

import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import com.genomeRing.model.structure.RingDimensions;
import com.genomeRing.model.structure.SuperGenome;

/**
 * Determines and draws the scale.
 * This class is written really weird.
 * We use a Path Object to draw the vertical lines of the scale and a single arc to draw
 * the horizontal arc connecting all the vertical lines.
 */
public class ScaleView extends PathView {

    protected String text="";
    protected double textX, textY;
    protected Path legendPath = new Path();

    public ScaleView(SuperGenome sg, RingDimensions ringdim, double angle_span, double min_angle_per_unit) {
        this.setStyle(Color.BLACK, StrokeLineCap.BUTT, StrokeLineJoin.ROUND,ringdim.getGenomeWidth()*.2);
        //this.setStyle(Color.black, new BasicStroke((float) (ringdim.getGenomeWidth()*.2)));
        this.setShapeStyle(legendPath);

        // find good scaling
        double degperunit=ringdim.getDegreePerBase();

        if (degperunit==0)
            return;

        int unit=1;
        while (degperunit<min_angle_per_unit) {
            degperunit*=10;
            unit*=10;
        }

        // how many units in angle_span? --> find perfect angle_span
        int units_displayed = (int)Math.round(angle_span/degperunit);
        angle_span = units_displayed*degperunit;

        int scale = (int)(Math.log(unit)/Math.log(1000));
        String unit_str="";
        int rest=0;
        switch (scale) {
            case 0: unit_str=" bp"; rest=unit; break;
            case 1: unit_str=" Kb"; rest=unit/1000; break;
            case 2: unit_str=" Mb"; rest=unit/1000000; break;
            case 3: unit_str=" Gb"; rest=unit/1000000000; break;
        }
//		scale = (int)Math.round(Math.log(rest)/Math.log(10));
//		switch (scale) {
//		case 0: unit_str=" "+unit_str; break;
//		case 1: unit_str="0 "+unit_str; break;
//		case 2: unit_str="00 "+unit_str; break;
//		case 3: unit_str="00 "+unit_str; break;
//		}
        text = rest*units_displayed + unit_str;

        double alpha_start = 270-(angle_span/2d);
        double alpha_end = 270+(angle_span/2d);
        double inner_radius = ringdim.getLegendRadius();
        double outer_radius = inner_radius+ringdim.getGenomeWidth();

       // addSegment(outer_radius, alpha_start, alpha_end, false);

        legendPath.getElements().add(new MoveTo(polarToX(outer_radius,alpha_start),polarToY(outer_radius,alpha_start)));
        ArcTo arcTo = new ArcTo(outer_radius,outer_radius,alpha_end-alpha_start,polarToX(outer_radius,alpha_end),polarToY(outer_radius,alpha_end),false, false);
        legendPath.getElements().add(arcTo);



        for (double alpha_tick=alpha_start; alpha_tick<=alpha_end; alpha_tick+=degperunit) {
            MoveTo moveTo = new MoveTo(polarToX(outer_radius,alpha_tick), polarToY(outer_radius,alpha_tick));
            LineTo lineTo = new LineTo(polarToX(inner_radius,alpha_tick), polarToY(inner_radius,alpha_tick));
            //moveTo(polarToX(outer_radius,alpha_tick), polarToY(outer_radius,alpha_tick));
            //lineTo(polarToX(inner_radius,alpha_tick), polarToY(inner_radius,alpha_tick));
            legendPath.getElements().addAll(moveTo,lineTo);
        }
        MoveTo moveTo = new MoveTo(polarToX(outer_radius,alpha_end), polarToY(outer_radius,alpha_end));
        LineTo lineTo = new LineTo(polarToX(inner_radius,alpha_end), polarToY(inner_radius,alpha_end));

        legendPath.getElements().addAll(moveTo, lineTo);
        //moveTo(polarToX(outer_radius,alpha_end), polarToY(outer_radius,alpha_end));
        //lineTo(polarToX(inner_radius,alpha_end), polarToY(inner_radius,alpha_end));

        textX = polarToX(outer_radius, alpha_end);
        textY = polarToY(outer_radius, alpha_end);

        Text scaleText = new Text(textX,textY,text);
        scaleText.setFont(new Font(30));
        this.getChildren().addAll(legendPath,scaleText);

    }

}
