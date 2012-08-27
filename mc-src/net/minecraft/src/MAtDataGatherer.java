package net.minecraft.src;

import java.util.ArrayList;
import java.util.List;

import eu.ha3.matmos.engine.MAtmosData;

/*
            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
                    Version 2, December 2004 

 Copyright (C) 2004 Sam Hocevar <sam@hocevar.net> 

 Everyone is permitted to copy and distribute verbatim or modified 
 copies of this license document, and changing it is allowed as long 
 as the name is changed. 

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 

  0. You just DO WHAT THE FUCK YOU WANT TO. 
*/

public class MAtDataGatherer
{
	final static String INSTANTS = "Instants";
	final static String DELTAS = "Deltas";
	final static String LARGESCAN = "LargeScan";
	final static String SMALLSCAN = "SmallScan";
	final static String LARGESCAN_THOUSAND = "LargeScanPerMil";
	final static String SMALLSCAN_THOUSAND = "SmallScanPerMil";
	final static String SPECIAL_LARGE = "SpecialLarge";
	final static String SPECIAL_SMALL = "SpecialSmall";
	final static String CONTACTSCAN = "ContactScan";
	final static String CONFIGVARS = "ConfigVars";
	
	final static int COUNT_WORLD_BLOCKS = 4096;
	final static int COUNT_INSTANTS = 256;
	final static int COUNT_CONFIGVARS = 256;
	
	final static int MAX_LARGESCAN_PASS = 10;
	
	private MAtMod mod;
	
	private MAtScanVolumetricModel largeScanner;
	private MAtScanVolumetricModel smallScanner;
	
	private MAtScanCoordsPipeline largePipeline;
	private MAtScanCoordsPipeline smallPipeline;
	
	private MAtProcessorModel relaxedProcessor;
	private MAtProcessorModel frequentProcessor;
	private MAtProcessorModel contactProcessor;
	private MAtProcessorModel configVarsProcessor;
	
	private List<MAtProcessorModel> additionalRelaxedProcessors;
	private List<MAtProcessorModel> additionalFrequentProcessors;
	
	private MAtmosData data;
	
	private int cyclicTick;
	
	private long lastLargeScanX;
	private long lastLargeScanY;
	private long lastLargeScanZ;
	private int lastLargeScanPassed;
	
	MAtDataGatherer(MAtMod mAtmosHaddon)
	{
		this.mod = mAtmosHaddon;
		
	}
	
	void resetRegulators()
	{
		this.lastLargeScanPassed = MAX_LARGESCAN_PASS;
		this.cyclicTick = 0;
	}
	
	void load()
	{
		resetRegulators();
		
		this.largeScanner = new MAtScanVolumetricModel(this.mod);
		this.smallScanner = new MAtScanVolumetricModel(this.mod);
		
		this.additionalRelaxedProcessors = new ArrayList<MAtProcessorModel>();
		this.additionalFrequentProcessors = new ArrayList<MAtProcessorModel>();
		
		this.data = new MAtmosData();
		prepareSheets();
		
		this.largePipeline = new MAtPipelineIDAccumulator(this.mod, this.data, LARGESCAN, LARGESCAN_THOUSAND, 1000);
		this.smallPipeline = new MAtPipelineIDAccumulator(this.mod, this.data, SMALLSCAN, SMALLSCAN_THOUSAND, 1000);
		
		this.largeScanner.setPipeline(this.largePipeline);
		this.smallScanner.setPipeline(this.smallPipeline);
		
		this.relaxedProcessor = new MAtProcessorRelaxed(this.mod, this.data, INSTANTS, DELTAS);
		this.frequentProcessor = new MAtProcessorFrequent(this.mod, this.data, INSTANTS, DELTAS);
		this.contactProcessor = new MAtProcessorContact(this.mod, this.data, CONTACTSCAN, null);
		this.configVarsProcessor = new MAtProcessorConfig(this.mod, this.data, CONFIGVARS, null);
		
	}
	
	public MAtmosData getData()
	{
		return this.data;
		
	}
	
	void tickRoutine()
	{
		if (this.cyclicTick % 64 == 0)
		{
			EntityPlayer player = this.mod.manager().getMinecraft().thePlayer;
			long x = (long) Math.floor(player.posX);
			long y = (long) Math.floor(player.posY);
			long z = (long) Math.floor(player.posZ);
			
			if (this.cyclicTick == 0)
			{
				if (this.lastLargeScanPassed >= MAX_LARGESCAN_PASS
					|| Math.abs(x - this.lastLargeScanX) > 16 || Math.abs(y - this.lastLargeScanY) > 8
					|| Math.abs(z - this.lastLargeScanZ) > 16)
				{
					this.lastLargeScanX = x;
					this.lastLargeScanY = y;
					this.lastLargeScanZ = z;
					this.lastLargeScanPassed = 0;
					this.largeScanner.startScan(x, y, z, 64, 32, 64, 8192, null);
					
				}
				else
				{
					this.lastLargeScanPassed++;
					
				}
				
			}
			this.smallScanner.startScan(x, y, z, 16, 8, 16, 2048, null);
			this.relaxedProcessor.process();
			
			for (MAtProcessorModel processor : this.additionalRelaxedProcessors)
			{
				processor.process();
			}
			
			this.data.flagUpdate();
			
		}
		//if (this.cyclicTick % 1 == 0) // XXX
		if (true)
		{
			this.contactProcessor.process();
			this.frequentProcessor.process();
			
			for (MAtProcessorModel processor : this.additionalFrequentProcessors)
			{
				processor.process();
			}
			
			this.data.flagUpdate();
			
		}
		
		if (this.cyclicTick % 2048 == 0)
		{
			this.configVarsProcessor.process();
			
		}
		
		this.largeScanner.routine();
		this.smallScanner.routine();
		
		//this.cyclicTick = (this.cyclicTick + 1) % 256;
		this.cyclicTick = this.cyclicTick + 1;
		
	}
	
	void prepareSheets()
	{
		createSheet(LARGESCAN, COUNT_WORLD_BLOCKS);
		createSheet(LARGESCAN_THOUSAND, COUNT_WORLD_BLOCKS);
		
		createSheet(SMALLSCAN, COUNT_WORLD_BLOCKS);
		createSheet(SMALLSCAN_THOUSAND, COUNT_WORLD_BLOCKS);
		
		createSheet(CONTACTSCAN, COUNT_WORLD_BLOCKS);
		
		createSheet(INSTANTS, COUNT_INSTANTS);
		createSheet(DELTAS, COUNT_INSTANTS);
		
		createSheet(SPECIAL_LARGE, 2);
		createSheet(SPECIAL_SMALL, 1);
		
		createSheet(CONFIGVARS, COUNT_CONFIGVARS);
		
	}
	
	void createSheet(String name, int count)
	{
		ArrayList<Integer> array = new ArrayList<Integer>();
		this.data.sheets.put(name, array);
		for (int i = 0; i < count; i++)
		{
			array.add(0);
			
		}
		
	}
	
}
