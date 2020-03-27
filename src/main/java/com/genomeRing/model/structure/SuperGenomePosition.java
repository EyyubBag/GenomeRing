package com.genomeRing.model.structure;

public class SuperGenomePosition {
	
	protected final int sgOffset;
	protected final int sgPosition;
	protected final Block sgBlock;
	
	public SuperGenomePosition(Block b, int sgPosition, int sgOffset) {
		this.sgPosition=sgPosition;
		this.sgBlock = b;
		this.sgOffset = sgOffset;
	}
	
	/** create a coordinate from an angle
	 * @return null if the angle is outside of any model.supergenome block
	 */

	public static SuperGenomePosition from_angle(SuperGenome sg, RingDimensions ringdim, double alpha) {
		return from_angle(sg, ringdim, alpha, 0);
	}
	
	/** create a coordinate even if not within a block, use the next block in the indicated direction, i.e.
	 * if next_in_direction==1 use the next block counter-clockwise of the given angle
	 * if next_in_direction==-1 use the next block clockwise of the given angle
	 * Never returns null
	 */
	public static SuperGenomePosition from_angle(SuperGenome sg, RingDimensions ringdim, double alpha, int next_in_direction) {
		alpha=90-alpha;
		Block closest=null;
		
		for (Block b : sg.getBlocks()) {
			double block_start = ringdim.getStartDegree(b);
			double block_end = ringdim.getEndDegree(b);
			if (alpha>=block_end && alpha<=block_start) {
				double alpha_offset = block_start-alpha;
				double base_offset = alpha_offset/ringdim.getDegreePerBase();
				int sgPosition = (int)(base_offset+b.getStart());
				return map0(b, sgPosition);
			} else if (next_in_direction!=0) {
				if (next_in_direction==1 && block_start>alpha)
					closest=b;
				else if (next_in_direction==-1 && block_end<alpha)
					closest=b;
			}
		}
		
		if (closest!=null) {
			double alpha_offset = ringdim.getStartDegree(closest)-alpha;
			double base_offset = alpha_offset/ringdim.getDegreePerBase();
			int sgPosition = (int)(base_offset+closest.getStart());
			return map0(closest, sgPosition);
		}
		
		return null;
	}
	
	public static SuperGenomePosition from_sgOffset(SuperGenome sg, int sgOffset) {
		sg.checkScalingInfo();
		int i=sg.scalingInfo.indexAtPosition(sgOffset);
		Block b = sg.blocks.get(i);
		int blockOffset = sgOffset - (int)sg.scalingInfo.getStart(i);		
		int sgPosition = b.getStart() + blockOffset;
		return new SuperGenomePosition(b, sgPosition, sgOffset);
	}
	
	public static SuperGenomePosition from_sgPosition(SuperGenome sg, int sgPosition) {
		for (Block b : sg.blocks) {
			if (b.getStart()<=sgPosition && b.getEnd()>=sgPosition) {
				return map0(b, sgPosition);
			}
		}
		return null;
	}
	
	private static SuperGenomePosition map0(Block b, int sgPosition) {
		int blockOffset = sgPosition - b.getStart(); 
		int sgOffset = b.getOffset() + blockOffset;
		return new SuperGenomePosition( b, sgPosition, sgOffset);
	}
	
	public static SuperGenomePosition from_gPosition(GenomePosition gp) {
		Block sgBlock = gp.sgBlock();
		int blockOffset = gp.blockOffset();
		blockOffset = translateOffset(blockOffset, gp.gBlock(), sgBlock);
		return map0(sgBlock, sgBlock.getStart()+blockOffset);
	}
	
	
	
	public int sgOffset() {
		return sgOffset;
	}
	
	public int sgPosition() {
		return sgPosition;
	}
	
	public Block sgBlock() {
		return sgBlock;
	}
	
	public int blockOffset() {
		return sgPosition-sgBlock.getStart();
	}
	
	public double asAngular(RingDimensions rd) {
		return asAngular(rd, sgBlock, blockOffset());
	}
	
	public static double asAngular(RingDimensions rd, Block sgBlock, int blockOffset) {
		double blockStartAngle = rd.getStartDegree(sgBlock);
		double extraBaseAngle = blockOffset*rd.getDegreePerBase();
		return blockStartAngle - extraBaseAngle;
	}
	
	
	public int gPosition(Genome g) {
		// find correct block
		for (CoveredBlock cb : g.getBlocks()) {
			if (cb.getBlock()==sgBlock) {
				return blockOffset()+cb.getStart();
			}
		}
		return -1;
	}
	
	/** translate a block offset from superGenome to Genome */
	public static int translateOffset(int offset, Block sg, CoveredBlock g) {
		if (sg.getLength()==g.getLength())
			return offset;
		double perc = ((double)offset) / ((double)sg.getLength());
		return (int) Math.round( perc*g.getLength() );
	}
	
	/** translate a block offset from Genome to superGenome */
	public static int translateOffset(int offset, CoveredBlock b, Block sg) {
		if (sg.getLength()==b.getLength())
			return offset;
		double perc = ((double)offset) / ((double)b.getLength());
		return (int) Math.round( perc*sg.getLength() );
	}
	
	public GenomePosition asGenomePosition(Genome g) {
		// find correct block
		for (CoveredBlock cb : g.getBlocks()) {
			if (cb.getBlock()==sgBlock) {
				// offset is either absolute (same block length) or in percentage (different block length)
				int offset = translateOffset(blockOffset(), sgBlock, cb);				
				return new GenomePosition(g, sgBlock, cb.isForward(), offset+cb.getStart());
			}
		}
		return new GenomePosition(null,null,null,null);
	}
	
	
	public String toString() {
		return "SuperGenome base "+sgPosition()+" at offset "+sgOffset()+" = ["+(sgBlock.getIndex()+1)+"]+"+blockOffset();
	}
}
