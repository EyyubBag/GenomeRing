package com.genomeRing.model.structure;

import javafx.beans.property.SimpleDoubleProperty;

public class RingDimensions implements SuperGenomeListener {

	public static final int OUTER_JUMP_LANES_PER_GENOME = 4;
	public static final int INNER_JUMP_LANES_PER_GENOME = 2;
	public static final int MIN_RING_DISTANCE = 20;
	
	protected double radius_inner;
	protected double radius_outer;
	protected double radius_middle_exchange;
	protected double radius_first_jumplevel;
	protected SimpleDoubleProperty ring_distance = new SimpleDoubleProperty();
//	protected double ring_distance;
	protected double radius_inner_jump;
	protected SimpleDoubleProperty block_gap_degrees = new SimpleDoubleProperty();
	//protected double block_gap_degrees;
	protected SimpleDoubleProperty genomeWidth = new SimpleDoubleProperty();
//	protected double genomeWidth;
	protected double degreesAvailable;
	protected double radius_innercircle_inner;
	protected double radius_outercircle_outer;
	protected double radius_extreme;
	protected int numberOfBlocks, numberOfGenomes, numberOfBases;
	protected double degreesPerBase;
	protected int maximalOuterSkip;
	protected int maximal_used_lane;
	
	protected boolean[][] used_outer_angles; 
	protected boolean[][] used_inter_angles;  
	protected int outer_jump_lanes;
	protected int inter_jump_lanes;
	
	protected int inner_space_per_genome = 100;
	
	public RingDimensions(double genomeWidth, int radius, int ring_distance, double interGenomeGapDegrees, SuperGenome superGenome) {

		this.setGenomeWidth(genomeWidth);
		this.setRingDistance(ring_distance);
		this.setBLockGap(interGenomeGapDegrees);
		this.radius_inner = radius;
		
		superGenome.addListener(this);
		
		superGenomeChanged(new SuperGenomeEvent(superGenome, 0));		
	}
	

	public void superGenomeChanged(SuperGenomeEvent evt) {

		SuperGenome superGenome = evt.getSource();
		this.numberOfGenomes = superGenome.getNumberOfGenomes();
		this.numberOfBlocks = superGenome.getNumberOfBlocks();
		this.numberOfBases = superGenome.getNumberOfBases();		
		this.maximalOuterSkip = superGenome.getMaximalOuterSkip(this);
		this.radius_inner = superGenome.getNumberOfGenomes() * this.inner_space_per_genome;
		
		outer_jump_lanes = OUTER_JUMP_LANES_PER_GENOME*superGenome.getNumberOfGenomes();
		inter_jump_lanes =  INNER_JUMP_LANES_PER_GENOME*superGenome.getNumberOfGenomes();
		used_outer_angles = new boolean[outer_jump_lanes][];
		used_inter_angles = new boolean[inter_jump_lanes][];
		
		setup();
	}
	
	public void changeRingDistance(double d) {
		this.setRingDistance(Math.max(1,d));
		setup();
	}
	
	public void changeBlockGap(double newGap) {
		this.setBLockGap( Math.min(360/numberOfBlocks, Math.max(0.1,newGap))); ;
		setup();
	}

	public void changeGenomeWidth(double genomeWidth) {
		this.setGenomeWidth(Math.max(1,genomeWidth));
		setup();
	}
	
	public void changeRadiusInner(double change) {
		this.radius_inner += change;
		setup();
	}
	
	private boolean useJumpLanes = true;
	
	public void toggleUseJumpLanes() {
		this.useJumpLanes = this.useJumpLanes ? false : true;
		setup();
	}

	public void setup() {
		
		double ring_width = numberOfGenomes * getGenomeWidth();
		
		// INNER RING
		this.radius_innercircle_inner = (radius_inner - ring_width);
		this.radius_inner_jump = (radius_innercircle_inner * 0.8);
		
		// MIDDLE EXCHANGE
		this.radius_middle_exchange = (radius_inner+getRingDistance())+MIN_RING_DISTANCE;
		
		// OUTER RING
		this.radius_outer = radius_middle_exchange + ((useJumpLanes ? inter_jump_lanes : 1) * (getGenomeWidth()+getRingDistance())) + getGenomeWidth()*.5 + MIN_RING_DISTANCE;
		this.radius_outercircle_outer = (radius_outer + ring_width);
		
		this.radius_first_jumplevel = radius_outercircle_outer + 2*getRingDistance() + MIN_RING_DISTANCE;
		radius_extreme = radius_first_jumplevel + (outer_jump_lanes*(getGenomeWidth()+getRingDistance())) + getGenomeWidth()*.5;

		degreesAvailable = 360-(numberOfBlocks*getBlockGap());
		// if there are many blocks, we could end up with a negative number of free degrees
		while (degreesAvailable<1 && getBlockGap()>0) { // make interBlock gap smaller
			setBLockGap(Math.max(0, getBlockGap()-1));
			degreesAvailable = 360-(numberOfBlocks*getBlockGap());
		}
				
		degreesPerBase = degreesAvailable / (double)numberOfBases;
		
		for (int i=0; i!=outer_jump_lanes; ++i)
			used_outer_angles[i] = new boolean[360];		

		for (int i=0; i!=inter_jump_lanes; ++i)
			used_inter_angles[i] = new boolean[360];		

		maximal_used_lane = -1;
	}
	
	public double getRadiusBackward(int genome) {
		return radius_inner - getGenomeWidth()*(genome+.5);
	}

	public double getRadiusForward(int genome) {
		return radius_outer + getGenomeWidth()*(genome+.5);
	}

//	public double getRadiusInterchangeJump(int genome) {
//		return radius_middle_exchange + genomeWidth*(genome+.5);
//	}
	
	public double getRadiusInterchangeJump(int genome, int jumpStartAngle, int jumpEndAngle) {
		int lane = getFreeLane(jumpStartAngle, jumpEndAngle, inter_jump_lanes, used_inter_angles);
		
		int ll=lane+1;
		int direction = ll%2==0?1:-1;
		ll = inter_jump_lanes+direction*ll;
		ll = (int) Math.ceil(ll/2.0) - 1;
		lane=ll;
		
		double radius = radius_middle_exchange + (lane*(getGenomeWidth()+getRingDistance())) + getGenomeWidth()*.5;
		return radius;
	}
	
	public double getRadiusInnerJump(int genome) {
		return radius_inner_jump - (getGenomeWidth()*(genome+.5));
	}
	
//	public double getRadiusOuterJump(int genome, int jumpDistance) {
//		// level == 0 is same block
//		// level == 1 is neighbors, no outer jump needed
//		// level == 2 is first outer jump level
//		int level = Math.min(jumpDistance-2, MAX_OUTER_JUMPLEVEL); 
//		
//		double circle_width = numberOfGenomes * genomeWidth;
//		// jumps start at radius_first_jumplevel
//		double radius = radius_first_jumplevel 
//		// then we add the "level", i.e. circle_radius+ring_distance*level
//		              + level*(ring_distance+circle_width) 
//		// then we add the genome-specific shift, centering on the genome's width
//		              +  genomeWidth*(genome+.5);
//		
//		return radius;
//	}
//	
	public int getFreeLane(int jumpStartAngle, int jumpEndAngle, int lanes, boolean[][] used_langes) {
		int direction = (jumpStartAngle>jumpEndAngle)?-1:1;
		
		// also penalize very close proximity by introducing a 2 degree distance on each side
		jumpStartAngle-=2*direction;
		jumpEndAngle+=2*direction;
		
		// shift into positive range
		while (jumpStartAngle<0 || jumpEndAngle<0) { 
			jumpStartAngle+=360;
			jumpEndAngle+=360;
		}
		
		// find the first level with a free lane, remember the max overlap per lane
		int[] overlap_angles = new int[lanes];
		int lane_found = -1;
		
		for (int level=0; level!=lanes; ++level) {
			boolean[] alreadycovered = used_langes[level];
			for (int angle=jumpStartAngle; angle!=jumpEndAngle; angle+=direction) {
				if (angle<0)
					angle+=360;
				if (alreadycovered[angle%360])
					++overlap_angles[level];				
			}
			if (overlap_angles[level]==0) { // perfect fit
				lane_found = level;
				break;
			}						
		}
				 
		if (lane_found==-1) { // find the best suboptimal lane
//			System.out.println("Placing jump in genome "+genome+" from "+jumpStartAngle+" to "+jumpEndAngle+": No optimal solution");
			int curmin= Integer.MAX_VALUE;
			for (int lane=0; lane!=lanes; ++lane)
				if (overlap_angles[lane]<curmin) {
					curmin = overlap_angles[lane];
					lane_found = lane;
				}
		}
		
		// now block the region we found and return the resulting angle
		boolean[] lane_used = used_langes[lane_found]; 
		for (int angle=jumpStartAngle; angle!=jumpEndAngle; angle+=direction) {
			if (angle<0)
				angle+=360;
			lane_used[angle%360]=true;
		}
		
		return lane_found;
	}
	
	public double getRadiusOuterJump(int genome, int jumpStartAngle, int jumpEndAngle) {

		int lane = getFreeLane(jumpStartAngle, jumpEndAngle, outer_jump_lanes, used_outer_angles);
		
		maximal_used_lane = Math.max(lane, maximal_used_lane);
		
		double radius = radius_first_jumplevel + (lane*(getGenomeWidth()+getRingDistance())) + getGenomeWidth()*.5;

//		System.out.println("Placing jump in genome "+genome+" from "+jumpStartAngle+" to "+jumpEndAngle+
//				" on lane "+level_found+" with overlap "+overlap_angles[level_found]+" on radius "+radius);

		return radius;
	}

	public double getStartDegree(CoveredBlock b) {
		return getStartDegree(b.getBlock());
	}
	
	public double getStartDegree(Block b) {
		double result = b.getStartPercentage()*degreesAvailable;
		result += b.getIndex()*getBlockGap();
		return 90-result;
	}
	
	public double getEndDegree(CoveredBlock b) {
		return getEndDegree(b.getBlock());
	}
	
	public double getEndDegree(Block b) {
		double result = b.getEndPercentage()*degreesAvailable;
		result += b.getIndex()*getBlockGap();
		return 90-result;
	}

	
	public double getInnerRingRadiusInner() {
		return this.radius_innercircle_inner; 
	}
	
	public double getInnerRingRadiusOuter() {
		return this.radius_inner;
	}
	
	public double getExtremeOuterRadius() {
		return radius_extreme;
	}
	

	public double getOuterRingRadiusInner() {
		return this.radius_outer; 
	}
	
	public double getOuterRingRadiusOuter() {
		return this.radius_outercircle_outer;
	}

	public double getGenomeWidth() {
		return genomeWidth.get();
	}

	public SimpleDoubleProperty genomeWidthProperty() {
		return genomeWidth;
	}

	public void setGenomeWidth(double genomeWidth) {
		this.genomeWidth.set(genomeWidth);
	}

	public int getNumberOfBlocks() {
		return numberOfBlocks;
	}

	public double getBlockGap() {
		return block_gap_degrees.get();
	}

	public SimpleDoubleProperty blockGapProperty() {
		return block_gap_degrees;
	}

	public void setBLockGap(double block_gap_degrees) {


		this.block_gap_degrees.set(block_gap_degrees);
	}


	public double getRingDistance() {
		return ring_distance.get();
	}

	public SimpleDoubleProperty ringDistanceProperty() {
		return ring_distance;
	}

	public void setRingDistance(double ring_distance) {
		this.ring_distance.set(ring_distance);
	}



	public double getDegreePerBase() {
		return degreesPerBase;
	}


	public int getNumberOfGenomes() {
		return this.numberOfGenomes;
	}


	public double getLegendRadius() {
		double radius = radius_first_jumplevel + (((useJumpLanes ? maximal_used_lane : 0) + 3)*(getGenomeWidth()+getRingDistance()));
		return radius;
	}


}
