package com.genomeRing.model.structure;

import java.util.EventListener;

public interface GenomeListener extends EventListener {
	
	public void genomeChanged(GenomeEvent evt);

}
