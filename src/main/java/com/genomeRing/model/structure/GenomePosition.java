package com.genomeRing.model.structure;

import java.util.List;

public class GenomePosition {
	
	private final Genome g;
	private final CoveredBlock gBlock;
	private Boolean forward; // to override the CoveredBlock's info
	private final int gPosition;
	private int gbi=-1;
	
	
	public GenomePosition(Genome _g, CoveredBlock _cb, int _gPosition) {
		g = _g;
		gBlock = _cb;
		gPosition = _gPosition;
		forward = null;
	}
	
	public GenomePosition() {
		g = null; gBlock = null; forward=null; gPosition=-1;
	}
	
	public GenomePosition(Genome _g, int gPosition, Boolean forward) {
		for (CoveredBlock cb : _g.getBlocks()) {
			if (cb.getStart()<=gPosition && cb.getEnd()>=gPosition) {
				g = _g;
				gBlock = cb;
				this.gPosition = gPosition;
				this.forward = forward;
				return;
			} 
		}
		g = null;
		gBlock = null;
		this.gPosition=-1;
		this.forward=null;
	}
	
	public GenomePosition(Genome _g, Block _sgBlock, Boolean _fwd, Integer _gPosition) {
		if (_g!=null) {
			// find corresponding coveredblock
			CoveredBlock cb = null;
			for (CoveredBlock ccb : _g.getBlocks()) {
				if (ccb.getBlock()==_sgBlock) {
					cb = ccb;
					break;
				}
			}

			if (cb!=null) { // valid position
				g=_g; 
				gBlock=cb;
				forward=_fwd;
				gPosition=_gPosition;
				return;
			}  
		}
		g = null; gBlock = null; forward=null; gPosition=-1;		
	} 
	
	public String toString() {
		return g.getName()+" base "+gPosition()+" = ["+(gBlockIndex()+1)+"]+"+blockOffset()
				+"\n"+asSuperGenomePosition().toString()
				+"\n\nBlock length: \n"+gBlock().getLength()+" in "+g.getName()+"\n"
				+sgBlock().length+" in SuperGenome";
	}

	public boolean isValid() {
		return g!=null;
	}
	
	public boolean forward() {
		if (forward==null)
			return gBlock.isForward();
		return forward;
	}

	public int sgPosition() {
		return sgBlock().getStart()+blockOffset();
	}

	public int gPosition() {
		return gPosition;
	}
	
	public int blockOffset() {
		return gPosition - gBlock.getStart();
	}
	
	public CoveredBlock gBlock() {
		return gBlock;
	}
	
	public Block sgBlock() {
		return gBlock.getBlock();
	}
	
	public int gBlockIndex() {
		if (g==null)
			return -1;
		if (gbi==-1) {
			List<CoveredBlock> cbs = g.getBlocks();
			for (int i=0; i!=cbs.size(); ++i)
				if (cbs.get(i)==gBlock)
					gbi = i;
		}
		return gbi;			
	}

	public SuperGenomePosition asSuperGenomePosition() {
		return SuperGenomePosition.from_gPosition(this);
	}
	
	public double asAngular(RingDimensions rd) {
		return asAngular(g, rd, gBlock, gPosition);
	}
	
	public static double asAngular(Genome g, RingDimensions rd, CoveredBlock gBlock, int gPosition) {
		Block sgBlock = gBlock.getBlock();
		int gBlockOffset = gPosition-gBlock.getStart();
		int sgBlockOffset = SuperGenomePosition.translateOffset(gBlockOffset, gBlock, sgBlock);
		return SuperGenomePosition.asAngular(rd, sgBlock, sgBlockOffset);
	}

	public Genome genome() {
		return g;
	}

	public void setForward(Boolean forward2) {
		this.forward = forward2;		
	}
	

}
