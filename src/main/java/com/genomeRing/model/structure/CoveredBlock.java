package com.genomeRing.model.structure;
 
import java.util.Comparator;


public class CoveredBlock {
	
	protected Block block;
	protected boolean forward;
	protected int start, end; // start and end position of the covered block in the Genome's coordinate system
	
	protected boolean drawn_clockwise = true;
	
	public CoveredBlock(Block b, boolean fwd) {
		this.block = b;
		this.forward = fwd;
	}
	
	public void setLocationInGenome(int start, int end) {
		this.start=start;
		this.end=end;
	}
	
	public Block getBlock() {
		return block;
	}
	
	public boolean isForward() {
		return forward;
	}
	
	public int getIndex() {
		return block.getIndex();
	}

	public String toString() {
		return getStart()+"-"+getEnd()+" fwd="+forward+", on "+block;
	}
	
	public void setDrawnClockwise(boolean cw) {
		this.drawn_clockwise=cw;
	}
	
	public boolean isDrawnClockwise() {
		return drawn_clockwise;
	}
	
	public static class BlockSorter implements Comparator<CoveredBlock> {

		@Override
		public int compare(CoveredBlock o1, CoveredBlock o2) {
			Integer o1i = Integer.valueOf(o1.getBlock().getIndex());
			return o1i.compareTo(o2.getBlock().getIndex());
		}
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public int getLength() {
		return end-start+1;
	}
	
	
}
