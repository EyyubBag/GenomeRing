package com.genomeRing.view;


import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import com.genomeRing.model.structure.CoveredBlock;
import com.genomeRing.model.structure.Genome;
import com.genomeRing.model.structure.RingDimensions;

@SuppressWarnings("serial")
public class GenomeFlags2 extends PathView {

	private Path startMarker = new Path();
	private Path endMarker = new Path();

	public GenomeFlags2(Genome g, RingDimensions ringdim) {

		this.getChildren().addAll(startMarker, endMarker);
		this.setShapeStyle(startMarker);
		this.setShapeStyle(endMarker);

		double flag_outer = ringdim.getInnerRingRadiusInner() * .9;
		double flag_inner = flag_outer - 3 * ringdim.getGenomeWidth();

		double shift = ringdim.getNumberOfGenomes()/2. - g.getIndex();
		
		double circle_radius = ringdim.getGenomeWidth();
		
		CoveredBlock genome_first_block =  g.getBlocks().get(0);
		CoveredBlock genome_last_block =  g.getBlocks().get(g.getBlocks().size()-1);
		
		double start_flag_angle = genome_first_block.isDrawnClockwise() 
									? ringdim.getStartDegree(genome_first_block) 
									: ringdim.getEndDegree(genome_first_block);
									
		double end_flag_angle = genome_last_block.isDrawnClockwise() 
					  			? ringdim.getEndDegree(genome_last_block) 
								: ringdim.getStartDegree(genome_last_block);
		
	    double extent = 2;
		double start_flag_triangle_extent = genome_first_block.isDrawnClockwise() ? -extent : extent;
		double end_flag_triangle_extent = genome_last_block.isDrawnClockwise() ? extent : -extent;
					  			
		// START OF GENOME flag
		// move to start side of the flag pole
		startMarker.getElements().add(new MoveTo(	polarToX(flag_outer - circle_radius, start_flag_angle+shift),
				polarToY(flag_outer - circle_radius, start_flag_angle+shift)));
		// line to top of flag pole
		startMarker.getElements().add(new LineTo( polarToX(flag_outer, start_flag_angle+shift),
				polarToY(flag_outer, start_flag_angle+shift)));
		// down and out for triangle
		startMarker.getElements().add(new LineTo( polarToX(flag_outer - circle_radius/2., start_flag_angle+start_flag_triangle_extent+shift),
				polarToY(flag_outer - circle_radius/2., start_flag_angle+start_flag_triangle_extent+shift)));
		//close triangle
		startMarker.getElements().add(new ClosePath());
		//foot of the flag
		startMarker.getElements().add(new LineTo(polarToX(flag_inner, start_flag_angle),
				polarToY(flag_inner, start_flag_angle)));
			
		// END OF GENOME flag
		// down to center of triangle tip
		endMarker.getElements().add(new MoveTo( polarToX(flag_inner + circle_radius/2.0, end_flag_angle+shift),
				polarToY(flag_inner + circle_radius/2.0, end_flag_angle+shift)));
		// down and out to lower triangle edge
		endMarker.getElements().add(new LineTo( polarToX(flag_inner+circle_radius, end_flag_angle+shift+end_flag_triangle_extent),
				polarToY(flag_inner+circle_radius, end_flag_angle+shift+end_flag_triangle_extent) ));
		// up for triangle back side
		endMarker.getElements().add(new LineTo( polarToX(flag_inner, end_flag_angle+shift+end_flag_triangle_extent),
				polarToY(flag_inner, end_flag_angle+shift+end_flag_triangle_extent)) );
		//close triangle
		endMarker.getElements().add(new ClosePath());
		
		//triangle end line
		endMarker.getElements().add(new LineTo( polarToX(flag_inner, end_flag_angle+shift),
				polarToY(flag_inner, end_flag_angle+shift)));

		endMarker.getElements().add(new LineTo( polarToX(flag_inner + circle_radius, end_flag_angle+shift),
				polarToY(flag_inner + circle_radius, end_flag_angle+shift)));
		
		//foot of the flag
		endMarker.getElements().add(new LineTo( polarToX(flag_outer, end_flag_angle),
				polarToY(flag_outer, end_flag_angle)));
	}
}
