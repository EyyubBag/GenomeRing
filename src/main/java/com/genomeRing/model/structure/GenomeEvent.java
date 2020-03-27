package com.genomeRing.model.structure;

public class GenomeEvent {
	
	public final static int NAME_CHANGED = 1;
	public final static int COLOR_CHANGED = 2;
	public final static int VISIBILITY_CHANGED = 3;
	public final static int CONNECTED_VIEWMODEL_CHANGED = 4;
	public final static int REMOVED_FROM_SUPERGENOME = 5;

	protected int type;
	protected Genome source;
	
	public GenomeEvent(Genome source, int type) {
		this.type = type;
		this.source = source;
	}
	
	public int getType() {
		return type;
	}
	
	public Genome getSource() {
		return source;
	}
	
}
