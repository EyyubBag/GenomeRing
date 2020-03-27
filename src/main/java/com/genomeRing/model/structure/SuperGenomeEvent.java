package com.genomeRing.model.structure;

public class SuperGenomeEvent {
	
	public final static int GENOMES_CHANGED = 1;
	public final static int BLOCKS_CHANGED = 2;
	public final static int SILENT_MODE = 3;
	public final static int NO_SILENT_MODE = 4;

	protected int type;
	protected SuperGenome source;
	
	public SuperGenomeEvent(SuperGenome source, int type) {
		this.type = type;
		this.source = source;
	}
	
	public int getChange() {
		return type;
	}
	
	public SuperGenome getSource() {
		return source;
	}
	
}
