package com.genomeRing.model.supergenome;

import java.util.*;

public class SuperGenome
{
	private Map<String,int[]> fromSuperGenome;
	private Map<String,int[]> toSuperGenome;
	private List<XmfaBlock> blocks;
	private List<XmfaBlock> refBlocks;
	
	private int perfectColCount;
	private Map<String, Integer> delCountMap;
	private Map<String, Integer> insCountMap;

	private String[] refGenome;
	
	public SuperGenome(List<XmfaBlock> blocks, String[] refGenome)
	{
		fromSuperGenome = new HashMap<String, int[]>();
		toSuperGenome = new HashMap<String, int[]>();
		this.blocks=blocks;
		this.refGenome=refGenome;
		refBlocks = new LinkedList<XmfaBlock>();
		
		perfectColCount = 0;
		delCountMap     = new HashMap<String, Integer>();
		insCountMap     = new HashMap<String, Integer>();
		
		createPositionMap();
		
//		int[] indices = fromSuperGenome.get("2");
//		
//		int[] search = {298572,481476,485138,890453,1087397,1324009,1155582,1339813,2459766,3102778,1104232,2751783,330125,642131,807613,20910,2815502,3267975,527964,537330,22719,365373,379804,1079902,1313204,1575600,2096406};
//		
//		for(int i = 0; i < search.length; i++) {
//			System.out.println(indices[search[i]]);
//		}
	}
	
	private void createPositionMap()
	{
		Map<String, Integer> lengthMap = new HashMap<String, Integer>();
		
		//get number and length of genomes and superGenome length
		int superGenomeLength = 0;
		for(XmfaBlock b:blocks)
		{
			for(XmfaSequence s:b.getSeqs().values())
			{
				if(!lengthMap.containsKey(s.getSourceId()))
					lengthMap.put(s.getSourceId(), s.getEnd());
				else
					lengthMap.put(s.getSourceId(), Math.max(lengthMap.get(s.getSourceId()), s.getEnd()));
			}
			
			superGenomeLength += b.getBlockLength();	
		}
		
		//select and sort reference blocks
//		System.out.println("\tSelect and sort blocks...");
		for(XmfaBlock b:blocks)
				refBlocks.add(b);
		
		//old sorting
//		
//		BlockComparator refComp = new BlockComparator(refGenome);
//		
//		Collections.sort(refBlocks, refComp);
		
		//new sorting
		refBlocks = sortBlocks(refBlocks, refGenome);
		
		//initialize Mapping
		
		for(String s:lengthMap.keySet())
		{
			toSuperGenome.put(s, new int[lengthMap.get(s)+1]); //make array one base longer (1based)
			fromSuperGenome.put(s, new int[superGenomeLength+1]); //make array one base longer (1based)
		}

		
		//initialize statistics
		for(String s:lengthMap.keySet())
		{
			insCountMap.put(s, 0);
			delCountMap.put(s, 0);
		}
		
		//create Mapping blockwise in correct order
		int superGenomePos = 1;
		int tmpStrandFactor;
		
		char tmpChar;
		char tmpColChar;
		boolean perfectCol;
		Set<String> hasGapSet = new HashSet<String>();
		
//		System.out.println("\tProcessing SuperGenome positions...");
		for(XmfaBlock b : refBlocks)
		{
			int length = b.getBlockLength();
			Map<String,XmfaSequence> seqs = b.getSeqs();
			
			//start indices for processing
			Map<String, Integer> indices = new HashMap<String, Integer>();
			for(XmfaSequence s : seqs.values())
			{
				if(s.getStrand()=='+')
					indices.put(s.getSourceId(), s.getStart());
				else
					indices.put(s.getSourceId(), s.getEnd());
			}
			
			//direction of incrementation
			Map<String, Integer> nextValue = new HashMap<String, Integer>();
			for(XmfaSequence s : seqs.values())
			{
				if(s.getStrand()=='+')
					nextValue.put(s.getSourceId(), 1);
				else
					nextValue.put(s.getSourceId(), -1);
			}
			
			//do mapping
//			System.out.println("\nNew Block");
			for(int posToProcess = 0; posToProcess<length; posToProcess++,superGenomePos++)
			{
//				if(superGenomePos%1000 == 0)
//					System.out.print("\r\t\t"+superGenomePos/1000+" kb processed   ");
				
				//statistics
				tmpColChar = '$';
				perfectCol = true;
				hasGapSet.clear();
				
				//for genomes not contained in the block...
				for(String id : toSuperGenome.keySet())
					if(!seqs.keySet().contains(id))
					{
						perfectCol = false;
						hasGapSet.add(id);
					}
				
				//for all sequences in block
				for(String s:seqs.keySet())
				{
					tmpChar = seqs.get(s).getSeq().charAt(posToProcess);
					
					//statistics
					if(tmpChar=='-')
					{
						perfectCol = false;
						hasGapSet.add(s);
					}
					else
						if(tmpColChar=='$')
							tmpColChar=tmpChar;
						else
							if(tmpColChar!=tmpChar)
								perfectCol = false;
					
					//mapping
					if(tmpChar!='-')
					{
						if(seqs.get(s).getStrand()=='+')
							tmpStrandFactor=1;
						else
							tmpStrandFactor=-1;
						
						fromSuperGenome.get(s)[superGenomePos]=indices.get(s) * tmpStrandFactor;
						toSuperGenome.get(s)[indices.get(s)]=superGenomePos * tmpStrandFactor;
						
						indices.put(s, indices.get(s)+nextValue.get(s));
					}
				}
				
				//statistics
				if(perfectCol)
					perfectColCount++;
				
				//insertion
				if(toSuperGenome.keySet().size() - hasGapSet.size() == 1)
				{
					for(String id:toSuperGenome.keySet())
						if(!hasGapSet.contains(id))
							insCountMap.put(id, insCountMap.get(id)+1);
				}
				else //deletion
				{
					for(String id:hasGapSet)
						delCountMap.put(id, delCountMap.get(id)+1);
				}
						
			}
			
		}
//		System.out.println("done");
	}

	
	public List<XmfaBlock> getRefBlocks() {
		return refBlocks;
	}

	public void setRefBlocks(List<XmfaBlock> refBlocks) {
		this.refBlocks = refBlocks;
	}

	
	/**
	 * Returns for a given genome (genomeID) and position in the model.supergenome (superGenomePos)
	 * the mapped position in genome.
	 * A negative integer indicates a mapping to the reverse strand.
	 * A Value of Zero indiates that there exists no mapping for this position.
	 * @param genomeID - the ID of the genome
	 * @param superGenomePos - position in the genome
	 * @return position in genome 
	 */
	public int getPosInGenome(String genomeID, int superGenomePos)
	{
		return fromSuperGenome.get(genomeID)[superGenomePos];
	}
	
	public int getNextMappingPosInGenome(String genomeID, int superGenomePos)
	{
		int l = fromSuperGenome.get(genomeID).length;
		for(int i=superGenomePos ,j = superGenomePos; i>0 && j<l; i--,j++) //TODO könnte auch ein or || sein
		{
			if(i>0)
				if(fromSuperGenome.get(genomeID)[i]!=0)
					return fromSuperGenome.get(genomeID)[i];
			if(j<l)
				if(fromSuperGenome.get(genomeID)[j]!=0)
					return fromSuperGenome.get(genomeID)[j];
		}
		return 0;
	}
	
	/**
	 * Returns for a given genome (genomeID) and position (genomePos) in that genome
	 * the mapped position in the model.supergenome.
	 * A negative integer indicates a mapping to the reverse strand.
	 * A Value of Zero indicates that there exists no mapping for this position.
	 * @param genomeID - the ID of the genome
	 * @param genomePos - position in the genome
	 * @return position in the model.supergenome
	 */
	public int getPosInSuperGenome(String genomeID, int genomePos)
	{
		return toSuperGenome.get(genomeID)[genomePos];
	}
	
	public int getNumUngappedColumnsInRegion(int start, int end, List<String> genomeIdList)
	{
		//start < end?
		if(end < start)
		{
			int oldStart = start;
			start = end;
			end = oldStart;
		}
		
		//count the gapped cols, because it's easier
		int gappedCols = 0;
		
		//for all positions in the region...
		for(int i=start; i<=end; i++)
		{
			//for all genomes...
			for(String id : genomeIdList)
			{
				//is there a gap?
				if(fromSuperGenome.get(id)[i]==0)
				{
					gappedCols++;
					break;
				}
			}
		}
		
		//region length - number of gapped cols
		return end-start+1 -gappedCols;
	}
	
	
	public int superGenomifyXmfaStart(XmfaBlock block)
	{
		XmfaSequence tmpseq;
		
		int res = Integer.MAX_VALUE;
		int tmp;
		
		for(String id: block.getSeqs().keySet())
		{
			tmpseq = block.getSeq(id);
			
			if(tmpseq.getStrand()=='-')
				tmp = tmpseq.getEnd();
			else
				tmp = tmpseq.getStart();
			
			if(tmp==0)
				continue;
			
			res = Math.min(res, Math.abs(toSuperGenome.get(id)[tmp]));
		}
		
		return(res);
	}
	
	/**
	 * Returns an int array for each genome that gives for each position (index) of the
	 * SuperGenome the corresponding position in the genome.
	 * The indexing is 1based (i.e. index 0 is not used).
	 * A negative value means a mapping to the reverse strand.
	 * A value of 0 means no mapping for this SuperGenome position.
	 * @param genomeIDs
	 * @return int[genome_index][position]
	 */
	public int[][] getSuper2GenomesAsArrayMap(String[] genomeIDs)
	{
		int[][] res = new int[genomeIDs.length][];
		
		for(int i=0; i<genomeIDs.length; i++)
			res[i]=fromSuperGenome.get(genomeIDs[i]);
		
		return res;
	}
	
	public String superGenomifyFASTA(String genomeID, String genomeFasta)
	{
		int[] mapping = fromSuperGenome.get(genomeID);
		
		char[] superSeq = new char[mapping.length-1];
		
		int pos;
		char c;
		for(int i=1; i<mapping.length; i++)
		{
			if(mapping[i]==0)
			{
				superSeq[i-1]='-';
				continue;
			}
			
			pos = Math.abs(mapping[i])-1; //genomeFasta 0based; mapping 1based
			c = genomeFasta.charAt(pos);
			
			if(Math.signum(mapping[i])<0)
				c = getComplement(c);
			
			superSeq[i-1] = c;
		}
		
		return new String(superSeq);
	}
	
	public String superGenomeConsensus(Map<String, String> genomeMap)
	{	
		char[] superSeq = new char[getAlignmentLength()];
		
		int pos;
		char c = 7353; //Mirror, mirror on the wall ...
		int Acount, Tcount, Ccount, Gcount;
		int maxCount;
		for(int i=1; i<=superSeq.length; i++)
		{
			Acount=Tcount=Ccount=Gcount = 0;
			for(String id : genomeMap.keySet())
			{
				pos = Math.abs(fromSuperGenome.get(id)[i])-1;
				
				if(pos==-1) //if SuperG maps to 0, which means 'gap'
					continue;
					
				c = genomeMap.get(id).charAt(pos);
				if(fromSuperGenome.get(id)[i]<0)
					c = getComplement(c);
				
				switch(c)
				{
				case 'A': Acount++; break;
				case 'a': Acount++; break;
				case 'T': Tcount++; break;
				case 't': Tcount++; break;
				case 'G': Gcount++; break;
				case 'g': Gcount++; break;
				case 'C': Ccount++; break;
				case 'c': Ccount++; break;
				}
			}
			
			maxCount=0;
			if(Acount > maxCount)
			{
				c = 'A';
				maxCount=Acount;
			}
			if(Tcount > maxCount)
			{
				c = 'T';
				maxCount=Tcount;
			}
			if(Ccount > maxCount)
			{
				c = 'C';
				maxCount=Ccount;
			}
			if(Gcount > maxCount)
			{
				c = 'G';
				maxCount=Gcount;
			}
			if(maxCount==0)
				throw new Error("SuperGenome column "+i+" only contains gaps!");
			
			superSeq[i-1]=c;
		}
		
		return new String(superSeq);
	}
	
	private char getComplement(char base)
	{
		char complChar;
		
		switch(base)
		{
		case 'A': complChar = 'T'; break;
		case 'a': complChar = 't'; break;
		case 'T': complChar = 'A'; break;
		case 't': complChar = 'a'; break;
		case 'G': complChar = 'C'; break;
		case 'g': complChar = 'c'; break;
		case 'C': complChar = 'G'; break;
		case 'c': complChar = 'g'; break;
		case 'N': complChar = 'N'; break;
		case 'n': complChar = 'n'; break;
		case '-': complChar = '-'; break;
		default: complChar='N'; System.err.println("Cannot create reverse complement!");
		}
		
		return complChar;
	}
	
	
	private char toggleStrand(char strand)
	{
		char res;
		
		switch(strand)
		{
		case '+': res = '-'; break;
		case '-': res = '+'; break;
		case '.': res = '.'; break;
		default:  res = '.'; System.err.println("Warning: Invalid strand identifier in toggleStrand function: "+strand);
		}
		
		return res;
	}
	
	private class BlockComparator implements Comparator<XmfaBlock>
	{
		private String[] myRefGenome;
		
		public BlockComparator(String[] refGenome)
		{
			super();
			this.myRefGenome = refGenome;
		}

		@Override
		public int compare(XmfaBlock b1, XmfaBlock b2)
		{
			int b1Start = 0;
			int b2Start = 0;
			for(int i=0;i<myRefGenome.length;i++)
			{
				if(b1.getSeqs().containsKey(myRefGenome[i]) && b2.getSeqs().containsKey(myRefGenome[i]))
				{
					b1Start = b1.getSeqs().get(myRefGenome[i]).getStart();
					b2Start = b2.getSeqs().get(myRefGenome[i]).getStart();
					break;
				}
			}
			
			return b1Start-b2Start;
		}
		
	}
	
	private static List<XmfaBlock> sortBlocks(List<XmfaBlock> blocks, String[] refGenomes)
	{
		List<XmfaBlock> in  = new LinkedList<XmfaBlock>(blocks);
		List<XmfaBlock> out = new LinkedList<XmfaBlock>();
		
		List<XmfaBlock> toBeRemoved = new LinkedList<XmfaBlock>();
		int tmpInsertIndex;
		boolean insertAtLastPos;
		XmfaBlock tmpBlock;
		
		for(String id : refGenomes)
		{
			toBeRemoved.clear();
			
			//insert blocks in out out list
			for(XmfaBlock block : in)
			{
				//consider only blocks in current genome
				if(!block.getSeqs().containsKey(id))
					continue;
				
				toBeRemoved.add(block);
				
				//first block
				if(out.size()==0)
				{
					out.add(block);
					continue;
				}

				
				//check all blocks already in out list
				tmpInsertIndex=-1; //stays at -1 if first block of the genome -> becomes 0 because insertAtLastPos=T
				insertAtLastPos = true;
				for(int i=0; i<out.size(); i++)
				{
					tmpBlock = out.get(i);
					
					//consider only blocks in current genome
					if(!tmpBlock.getSeqs().containsKey(id))
						continue;
					
					//update position
					tmpInsertIndex = i;
					
					//insertion point found?
					if(block.getSeq(id).getStart() < tmpBlock.getSeq(id).getStart())
					{
						insertAtLastPos=false;
						break;
					}
				}
				
				//Insert at last position? (Or first position if first block of the genome)
				if(insertAtLastPos)
					tmpInsertIndex++;
				
				//insert
				out.add(tmpInsertIndex, block);
				toBeRemoved.add(block);				
			}
			
			//remove inserted blocks from in list
			in.removeAll(toBeRemoved);
		}
		
		return out;
	}

	public int getPerfectColCount() {
		return perfectColCount;
	}

	public Map<String, Integer> getDelCountMap() {
		return delCountMap;
	}

	public Map<String, Integer> getInsCountMap() {
		return insCountMap;
	}
	
	public int getAlignmentLength()
	{
		int res = 0;
		for(String id:fromSuperGenome.keySet())
		{
			res = fromSuperGenome.get(id).length-1; //because array is one position longer
			break;
		}
		return res;
	}
}
