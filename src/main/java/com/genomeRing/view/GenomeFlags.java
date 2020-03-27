package com.genomeRing.view;


import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import com.genomeRing.model.structure.CoveredBlock;
import com.genomeRing.model.structure.Genome;
import com.genomeRing.model.structure.RingDimensions;

@SuppressWarnings("serial")
public class GenomeFlags extends PathView {

	private Path startMarker = new Path();
	private Path endMarker = new Path();

    public GenomeFlags(Genome g, RingDimensions ringdim) {

    	this.getChildren().addAll(startMarker, endMarker);
        this.setShapeStyle(startMarker);
        this.setShapeStyle(endMarker);

        double flag_outer = ringdim.getInnerRingRadiusInner() * .9;
        double flag_inner = flag_outer - 3 * ringdim.getGenomeWidth();

        double shift = ringdim.getNumberOfGenomes() / 2. - g.getIndex();

        double circle_radius = ringdim.getGenomeWidth();



        CoveredBlock genome_first_block = g.getBlocks().get(0);
        CoveredBlock genome_last_block = g.getBlocks().get(g.getBlocks().size() - 1);



        drawStartMarker(genome_first_block,ringdim,g.getIndex());
        drawEndMarker(genome_last_block,ringdim,g.getIndex());

        double start_flag_angle = genome_first_block.isDrawnClockwise()
                ? ringdim.getStartDegree(genome_first_block)
                : ringdim.getEndDegree(genome_first_block);

        double end_flag_angle = genome_last_block.isDrawnClockwise()
                ? ringdim.getEndDegree(genome_last_block)
                : ringdim.getStartDegree(genome_last_block);

        double extent = 2;
        double start_flag_triangle_extent = genome_first_block.isDrawnClockwise() ? -extent : extent;
        double end_flag_triangle_extent = genome_last_block.isDrawnClockwise() ? extent : -extent;


        double startAngle;
        double endAngle;

        double startX, startY, endX, endY;

        double radius = flag_outer - circle_radius;
        if (genome_first_block.isDrawnClockwise()) {

            startAngle = ringdim.getStartDegree(genome_first_block);
            endAngle = ringdim.getEndDegree(genome_first_block);

            startX = polarToX(radius, startAngle);
            startY = polarToY(radius, startAngle);

            endX = polarToX(radius, endAngle);
            endY = polarToY(radius, endAngle);


        } else {
            startAngle = ringdim.getEndDegree(genome_first_block);
            endAngle = ringdim.getEndDegree(genome_first_block);

            startX = polarToX(radius, endAngle);
            startY = polarToY(radius, endAngle);

            endX = polarToX(radius, startAngle);
            endY = polarToY(radius, startAngle);


        }


        // START OF GENOME flag
        // move to start side of the flag pole
     /*   startMarker.getElements().add(new MoveTo(polarToX(flag_outer - circle_radius, start_flag_angle + shift),
                polarToY(flag_outer - circle_radius, start_flag_angle + shift)));


        ArcTo arcTo = new ArcTo(radius, radius, (endAngle - startAngle),endX,endY,false,true );
        startMarker.getElements().add(arcTo);

        startMarker.getElements().add(new LineTo(endX-10,endY-10));
        startMarker.getElements().add(new MoveTo(endX,endY));
		startMarker.getElements().add(new LineTo(endX-10,endY+10));
		startMarker.getElements().add(new MoveTo(endX,endY));
		startMarker.getElements().add(new ClosePath());*/


//		moveTo(	polarToX(flag_outer - circle_radius, start_flag_angle+shift),
//				polarToY(flag_outer - circle_radius, start_flag_angle+shift));
//		// line to top of flag pole
//		lineTo( polarToX(flag_outer, start_flag_angle+shift),
//				polarToY(flag_outer, start_flag_angle+shift));
//		// down and out for triangle
    //    startMarker.getElements().add(new LineTo(polarToX(flag_outer - circle_radius / 2., start_flag_angle + start_flag_triangle_extent + shift),
      //          polarToY(flag_outer - circle_radius / 2., start_flag_angle + start_flag_triangle_extent + shift)));
      //  startMarker.getElements().add(new ClosePath());
//		lineTo( polarToX(flag_outer - circle_radius/2., start_flag_angle+start_flag_triangle_extent+shift),
//				polarToY(flag_outer - circle_radius/2., start_flag_angle+start_flag_triangle_extent+shift));
//		//close triangle
//		closePath();
//		//foot of the flag
//		lineTo(polarToX(flag_inner, start_flag_angle),
//				polarToY(flag_inner, start_flag_angle));
//
//		// END OF GENOME flag
//		// down to center of triangle tip
//        endMarker.getElements().add(new MoveTo(polarToX(flag_inner + circle_radius / 2.0, end_flag_angle + shift),
//                polarToY(flag_inner + circle_radius / 2.0, end_flag_angle + shift)));
////		moveTo( polarToX(flag_inner + circle_radius/2.0, end_flag_angle+shift),
////				polarToY(flag_inner + circle_radius/2.0, end_flag_angle+shift));
////		// down and out to lower triangle edge
//        endMarker.getElements().add(new LineTo(polarToX(flag_inner + circle_radius, end_flag_angle + shift + end_flag_triangle_extent),
//                polarToY(flag_inner + circle_radius, end_flag_angle + shift + end_flag_triangle_extent)));
////		lineTo( polarToX(flag_inner+circle_radius, end_flag_angle+shift+end_flag_triangle_extent),
////				polarToY(flag_inner+circle_radius, end_flag_angle+shift+end_flag_triangle_extent) );
////		// up for triangle back side
////		lineTo( polarToX(flag_inner, end_flag_angle+shift+end_flag_triangle_extent),
////				polarToY(flag_inner, end_flag_angle+shift+end_flag_triangle_extent) );
////		//close triangle
//        endMarker.getElements().add(new ClosePath());
////		closePath();
//
//		//triangle end line
//		lineTo( polarToX(flag_inner, end_flag_angle+shift),
//				polarToY(flag_inner, end_flag_angle+shift));
//
//		lineTo( polarToX(flag_inner + circle_radius, end_flag_angle+shift),
//				polarToY(flag_inner + circle_radius, end_flag_angle+shift));
//
//		//foot of the flag
//		lineTo( polarToX(flag_outer, end_flag_angle),
//				polarToY(flag_outer, end_flag_angle));
    }

    private void drawStartMarker(CoveredBlock b, RingDimensions ringdim, int gidx) {
		double radius = b.isForward()?ringdim.getRadiusForward(gidx):ringdim.getRadiusBackward(gidx);
		double alpha_start = ringdim.getStartDegree(b);
		double alpha_end = ringdim.getEndDegree(b);

		double startX = polarToX(radius, alpha_start);
		double startY = polarToY(radius, alpha_start);

		double endX = polarToX(radius, alpha_end);
		double endY = polarToY(radius, alpha_end);



		if(b.isDrawnClockwise()){
			MoveTo startMove = new MoveTo(startX,startY);
			LineTo lineTo = new LineTo(startX-20,startY-20);
			startMarker.getElements().addAll(startMove,lineTo);
		}else{
			MoveTo startMove = new MoveTo(endX,endY);
			LineTo lineTo = new LineTo(endX-20,endY-20);
			startMarker.getElements().addAll(startMove,lineTo);
		}


    }

    private void drawEndMarker(CoveredBlock b, RingDimensions ringdim,int gidx){
		double radius = b.isForward()?ringdim.getRadiusForward(gidx):ringdim.getRadiusBackward(gidx);
		double alpha_start = ringdim.getStartDegree(b);
		double alpha_end = ringdim.getEndDegree(b);

		double startX = polarToX(radius, alpha_start);
		double startY = polarToY(radius, alpha_start);

		double endX = polarToX(radius, alpha_end);
		double endY = polarToY(radius, alpha_end);

		if(!b.isDrawnClockwise()){
			MoveTo startMove = new MoveTo(startX,startY);
			LineTo lineTo = new LineTo(startX-20,startY-20);
			endMarker.getElements().addAll(startMove,lineTo);
		}else{
			MoveTo startMove = new MoveTo(endX,endY);
			LineTo lineTo = new LineTo(endX-20,endY-20);
			endMarker.getElements().addAll(startMove,lineTo);
		}
	}


}
