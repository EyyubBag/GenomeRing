package com.genomeRing.model.structure;

import java.util.Arrays;

public class ScalingInfo {

	protected double[] sizes;
	protected double[] starts;
	protected int lastGoodStart;
	
	protected long modifyCount=0;
	
	public ScalingInfo(int numberOfElements) {
		setNumberOfElements(numberOfElements);
	}
	
	public void setNumberOfElements(int numberOfElements) {
		sizes = new double[numberOfElements];
		starts = new double[numberOfElements];
		if (numberOfElements>0)
			starts[0] = 0;
		setSizesForAll(1);
	}
	
	public void setSizes(double[] sizes) {
		if (sizes.length==this.sizes.length) {
			System.arraycopy(sizes, 0, this.sizes, 0, sizes.length);
			lastGoodStart=0;
			++modifyCount;
		} else {
			throw new RuntimeException("Setting sizes for the wrong number of elements");
		}
	}
	
	public void setSizesForAll(double size) {
		Arrays.fill(sizes, size);
		lastGoodStart=0;
		++modifyCount;
	}
	
	public void setSize(int position, double size) {
		if (sizes[position]!=size) {
			sizes[position] = size;
			lastGoodStart = Math.min(lastGoodStart,position);
			++modifyCount;
		}	
	}
	
	public double getSize(int position) {
		if (position>=starts.length)
			return 0;
		if (position<0)
			return 0;
		return sizes[position];
	}
	
	public double getStart(int position) {
		if (position>=starts.length)
			return 0;
		if (position>lastGoodStart) {
			for (int i=lastGoodStart+1; i<=position; ++i) {
				starts[i] = starts[i-1] + getSize(i-1);
			}
			lastGoodStart = position;
		}
		if (position>=starts.length)
			return 0;
		if (position<0)
			return 0;
		return starts[position];
	}
	
	public double getEnd(int position) {
		return getStart(position)+getSize(position);
	}
	
	public int size() {
		return sizes.length;
	}
	
	public int indexAtPosition(int position) {
		// make sure all starts are computed
		getStart(size()-1);
		// starts are sorted by definition
		int target = Arrays.binarySearch(starts, position);
		if (target<0)
			target = target*-1 -1; 
		target--;
		if (target>size()-1)
			target = size()-1;
		return target;
	}
	
	public long getModificationCount() {
		return modifyCount;
	}
	
}
