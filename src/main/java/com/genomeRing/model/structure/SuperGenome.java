package com.genomeRing.model.structure;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import com.genomeRing.model.structure.tasks.SaveSuperGenomeService;
import com.genomeRing.model.supergenome.GenomeRingBlocker;
import com.genomeRing.model.supergenome.XmfaBlock;
import com.genomeRing.model.supergenome.XmfaParser;
import com.genomeRing.view.GenomeColors;

import java.io.*;
import java.util.*;

//import mayday.core.EventFirer; added this to the sourcecode
//import mayday.core.Preferences;
//import mayday.core.pluma.PluginInfo;
//import mayday.core.settings.SettingDialog;
//import mayday.core.settings.generic.HierarchicalSetting;
//import mayday.core.settings.typed.BooleanSetting;
//import mayday.core.settings.typed.IntSetting;
//import mayday.core.tasks.AbstractTask;

public class SuperGenome {
	
	protected final static String LAST_PATH = "LAST_SUPERGENOME_PATH";

	
	protected int total_length;  // sum of all block lengths
	protected ObservableList<Block> blocks;
	protected ArrayList<Genome> genomes;
	protected ScalingInfo scalingInfo;
	protected GenomeColors initialColors;
	protected ArrayList<Block> initialBlockOrder;

	//replaces the GenomeSettings
	private int minBlockLength = 10000;
	private boolean subBlocks = false;
	
	protected boolean silent=false; //fire no events
	
	protected EventFirer<SuperGenomeEvent, SuperGenomeListener> firer = new EventFirer<SuperGenomeEvent, SuperGenomeListener>() {
		@Override
		protected void dispatchEvent(SuperGenomeEvent event,
				SuperGenomeListener listener) {
			listener.superGenomeChanged(event);
		}
	}; 
	
	public SuperGenome() {
		init();
	}
	
	public void init() {
		this.blocks = FXCollections.observableArrayList(new ArrayList<Block>());
		this.genomes = new ArrayList<Genome>();
		this.initialColors = new GenomeColors();
		total_length = 0;
		scalingInfo = null;


	}
	
	public void storeInitialOrder() {
		initialBlockOrder = new ArrayList<Block>(getBlocks());
	}
	
	protected void save(final String fileName) throws IOException {

		SaveSuperGenomeService saveService = new SaveSuperGenomeService(this, fileName);

		saveService.restart();
	}
	

	private static String[] genomeIds = null;

	//changed to public from private so we can use it in the
	public void determineGenomeNames(String fileName) {
		Set<String> genomeIdentifier = new HashSet<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
			String line = null;
			while((line = br.readLine()) != null) {
				//the identifier is the first string without " " that follows until the first occurrence of ":" 
				if(line.startsWith(">")) {
					String[] split = line.split(":");
					if(split.length > 1) {
						String id = split[0];
						if(id.length() > 1) {
							//remove ">" and whitespaces
							id = id.substring(1).trim();
							genomeIdentifier.add(id);
						}
					}
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		genomeIds = genomeIdentifier.toArray(new String[0]);
		Arrays.sort(genomeIds);
	}

	
	public void addBlock(Block b) {
		b.index = blocks.size();
		b.setOffset(total_length);
		blocks.add(b);
		total_length += b.getLength();
		scalingInfo = null;
	}
	
	public int getNumberOfBases() {
		return total_length;
	}
	
	public int getLastBase() {
		return blocks.get(blocks.size()-1).getEnd();
	}
	
	public void setBlocks(List<Block> newOrder) {
		blocks.clear();
		blocks.addAll(newOrder);
		blockOrderChanged();
	}
	
	public void blockOrderChanged() {
		int currentStart=0;
		for (int i=0; i!=blocks.size(); ++i) {
			Block b = blocks.get(i);
			b.index = i;
			b.start = -1; // now we lose the original SG coordinates
			b.setOffset(currentStart);
			currentStart+=b.length;
		}
		scalingInfo = null;
		fireChange(SuperGenomeEvent.BLOCKS_CHANGED);
	}
	
	public void genomeNamesChanged() {
		fireChange(SuperGenomeEvent.GENOMES_CHANGED);
	}

	public void setMinBlockLength(int minBlockLength) {
		this.minBlockLength = minBlockLength;
	}

	public void setSubBlocks(boolean subBlocks) {
		this.subBlocks = subBlocks;
	}

	public ObservableList<Block> getBlocks() {
		return blocks;
	}
	
	public List<Block> getInitialBlockOrder() {
		if (initialBlockOrder==null) {
			if (blocks.size()==0)
				return Collections.emptyList();
			storeInitialOrder();
		}		
		return Collections.unmodifiableList(initialBlockOrder);
	}
	
	public List<Block> getBlocksInternal() {
		return blocks;
	}
	
	public void addGenome(Genome g) {
		genomes.add(g);
		// set color of that genome
		if (g.getColor()==null) {
			int index = genomes.size()-1;

			Color c = initialColors.getColor(index);
			g.setColor(c);			
		}
	}
	
	public int getNumberOfGenomes() {
		return genomes.size();
	}
	
	public int getIndex(Genome genome) {
		return genomes.indexOf(genome); // not very efficient but we only have about 15 genomes tops
	}

	public List<Genome> getGenomes() {
		// update colors if genome was added
		return Collections.unmodifiableList(genomes);
	}

	public int getNumberOfBlocks() {
		return blocks.size();
	}

	//added for access in LoadSuperGenomeService
	public GenomeColors getInitialColors() {
		return initialColors;
	}

	public int getMinBlockLength() {
		return minBlockLength;
	}

	public boolean isSubBlocks() {
		return subBlocks;
	}
	/* == Coordinate system mapping ==
	 * There are four types of coordinate systems
	 * - Angular coordinates (0-360°), translated from sgOffsets using RingDimensions
	 * - sgOffsets: base offset in the model.supergenome as stored here (i.e. in the range [0,total_length[)
	 * - sgPosition: "real" position in the superGenome, according for gaps between blocks, i.e. including Block.start info
	 * - gPosition: real base position in a given Genome g, according to CoveredBlock.start info
	 */
	


	// ============0
	
	
	public int getMaximalOuterSkip(RingDimensions ringdim) {
		int mos=0;
		for (Genome g : genomes) {
			mos = Math.max(mos, g.getMaximalOuterSkip(ringdim));
		}
		return mos;
	}
	
	protected void checkScalingInfo(){
		if (scalingInfo==null) {	
			this.scalingInfo = new ScalingInfo(getNumberOfBlocks());
			for (int i=0; i!=getNumberOfBlocks(); ++i) {
				scalingInfo.setSize(i, blocks.get(i).getLength());
			}
		}
	}
	
	public void addListener(SuperGenomeListener sgl) {
		firer.addListener(sgl);
	}
	
	public void removeListener(SuperGenomeListener sgl) {
		firer.removeListener(sgl);
	}

	//change this from protected to public so we can use it in the Service Class
	public void fireChange(int change) {
		switch(change) {
		case SuperGenomeEvent.SILENT_MODE:
		case SuperGenomeEvent.NO_SILENT_MODE:
			firer.fireEvent(new SuperGenomeEvent(this, change));
			break;
		default:
			if (!silent) {
				firer.fireEvent(new SuperGenomeEvent(this, change));
			}
		}
	}
	
	public void setSilent(boolean silent) {
		this.silent = silent;
		
		if(this.silent) {
			fireChange(SuperGenomeEvent.SILENT_MODE);
		} else {
			fireChange(SuperGenomeEvent.NO_SILENT_MODE);
		}
	}


	
	public static void ringMode(PipedOutputStream out, String inputFilePath, int blockLength, boolean createSubBlocks) throws Exception
	{
//		if(ids.length!=Config.getInt("numberOfDatasets"))
//			throw new Error("numberOfDatasets does not match length of idList!");
		
		//output
		//String outDir = Config.getString("outputDirectory");
		
		//read alignment
		System.out.println("Reading alignment blocks...");
		List<XmfaBlock> alignmentBlocks = XmfaParser.parseXmfa(inputFilePath);
		
		//SuperGenome
		System.out.println("Building SuperGenome...");
		com.genomeRing.model.supergenome.SuperGenome superG = new com.genomeRing.model.supergenome.SuperGenome(alignmentBlocks, genomeIds);
		
		GenomeRingBlocker grb = new GenomeRingBlocker(superG.getRefBlocks(), genomeIds, blockLength, createSubBlocks);
		
		//BlockMap
		//BufferedWriter bw = new BufferedWriter(new FileWriter(outDir+"blocks.out"));
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
		
		List<int[]> blockPositions = new LinkedList<int[]>();
		List<int[]> tmpPos;
		int tmpStart;
		
//		int lengthCount = 0;
		
		for(XmfaBlock b:grb.newBlockList)
		{
			tmpStart = superG.superGenomifyXmfaStart(b);
			
			tmpPos = b.getSubBlockPositions();
			for(int[] posPair:tmpPos)
			{
				posPair[0] = posPair[0]+tmpStart;
				posPair[1] = posPair[1]+tmpStart;
				
//				lengthCount += posPair[1] - posPair[0] + 1;
			}
				
			blockPositions.addAll(tmpPos);
		}
		
		boolean first = true;
		for(int[] posPair:blockPositions)
		{
			if(first)
				first=false;
			else
				bw.append(",");
				
			bw.append(posPair[0]+"-"+posPair[1]);
		}
		bw.newLine();
		
		int nameCounter = 0;
		for(String id : genomeIds)
		{
			bw.append(genomeIds[nameCounter++]+"\t");
			first = true;
			for(Integer i:grb.getGenomeBlockLists().get(id))
			{
				if(first)
					first=false;
				else
					bw.append(",");
				
				int start = Math.abs(superG.getNextMappingPosInGenome(id, blockPositions.get(Math.abs(i)-1)[0]));
				int stopInGenome = blockPositions.get(Math.abs(i)-1)[1];
				
				int stop;
				/*
				 * FIXME is there a better solution for that case?!
				 * prevent from having stop = 0 when there is no mappable position in the genome from the model.supergenome
				 * set stop in this case to the last mappable position
				 */
				while((stop = Math.abs(superG.getNextMappingPosInGenome(id, stopInGenome))) == 0) {
					stopInGenome--;
				}
				
				bw.append(Integer.toString(i)+":"+  start  +"-"+  stop);
			}
			bw.newLine();
		}
		
		bw.close();
	}
	
	public static int i(boolean b)
	{
		if(b)
			return(1);
		return(0);
	}
	
	/**
	 * Setting for loading xmfa files
	 * @author jaeger
	 *
	 */
/*	private class SuperGenomeSetting extends HierarchicalSetting {

		BooleanSetting createSubBlocks;
		IntSetting blockLength;
		
		public SuperGenomeSetting() {
			super("Super Genome Setting");
			
			blockLength = new IntSetting("Minimal Block Length", null, 10000);
			createSubBlocks = new BooleanSetting("Create Sub-Blocks", null, false);
			
			addSetting(blockLength);
			addSetting(createSubBlocks);
		}
		
		@Override
		public SuperGenomeSetting clone() {
			SuperGenomeSetting sgs = new SuperGenomeSetting();
			sgs.fromPrefNode(this.toPrefNode());
			return sgs;
		}
	}*/

	
	public String toString() {
		if(blocks.size() == 0)
			return "";
		
		String superGenomeString = blocks.get(0).toOutputString();
		
		for(int i = 1; i < blocks.size(); i++) {
			Block b = blocks.get(i);
			superGenomeString += "," + b.toOutputString();
		}
		
		return superGenomeString;
	}
}
