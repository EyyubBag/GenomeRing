/**
 * 
 */
package com.genomeRing.model.structure;

import javafx.beans.property.SimpleStringProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * Implements Serializable and overrides equals() so we can use Drag and Drop
 */
public class Block implements Serializable {

	private transient final SuperGenome superGenome;
	
	protected int length;
	protected int start=-1; // start position of the block in the SuperGenome's coordinate system
	protected int offset=-1; // offset of the block in the SuperGenome
	protected int index=-1; // the index of this block in SuperGenome.blocks
	protected SimpleStringProperty name = new SimpleStringProperty();
	protected String initalName;
	
	protected int sortingIndex = -1;

	
	public Block(SuperGenome superGenome, String name, int length) {
		this.superGenome = superGenome;
		this.length = length;
		this.setName(name);
		this.initalName = name;
	}
	
	public Block(SuperGenome superGenome, String name, int start, int end) {
		this.superGenome = superGenome;
		this.start=start;
		this.length=end-start+1;
		this.setName(name);
		this.initalName = name;
	}
	
	public void setOffset(int offset) {
		this.offset=offset;
		if (start==-1)
			start = offset;
	}
	
	public int getLength() {
		return length;
	}
	public int getStart() {
		return start;
	}
	public int getEnd() {
		return start+length-1;
	}
	public int getIndex() {
		return index;
	}	
	
	public int getOffset() {
		return offset;
	}
	
	public String toString() {
		return "["+index+"] ("+start+"-"+(start+length-1)+")";
	}
	
	public double getStartPercentage() {
		return (double)getOffset()/(double)this.superGenome.getNumberOfBases();
	}
	
	public double getEndPercentage() {
		return (double)(getOffset()+getLength())/(double)this.superGenome.getNumberOfBases();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Block block = (Block) o;
		return length == block.length &&
				start == block.start &&
				offset == block.offset &&
				index == block.index &&
				sortingIndex == block.sortingIndex &&
				name.equals(block.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(length, start, offset, index, name, sortingIndex);
	}

	public String getName() {
		return name.get();
	}

	public SimpleStringProperty nameProperty() {
		return name;
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public void setInitialName(String initialName) {
		this.initalName = initalName;
	}

	public String toOutputString() {
		return getStart() + "-" + getEnd();
	}

	public String getInitialName() {
		return initalName;
	}
}