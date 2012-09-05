package eu.ha3.matmos.engine;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

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

public class MAtmosEventTimed extends MAtmosDescriptible
{
	MAtmosMachine machine;
	
	public String event;
	
	public float volMod;
	public float pitchMod;
	
	public float delayMin;
	public float delayMax;
	
	public float delayStart;
	
	public long nextPlayTime;
	
	MAtmosEventTimed(MAtmosMachine machineIn)
	{
		//event = eventIn;
		
		this.event = "";
		
		this.machine = machineIn;
		this.volMod = 1F;
		this.pitchMod = 1F;
		
		this.delayMin = 10F;
		this.delayMax = 10F;
		
		this.delayStart = 0F;
		
	}
	
	void setMachine(MAtmosMachine machineIn)
	{
		this.machine = machineIn;
		
	}
	
	public void routine()
	{
		if (this.machine.knowledge.getTimeMillis() < this.nextPlayTime)
			return;
		
		if (this.machine.knowledge.events.containsKey(this.event))
		{
			this.machine.knowledge.events.get(this.event).playSound(this.volMod, this.pitchMod);
		}
		
		if (this.delayMin == this.delayMax && this.delayMin > 0)
		{
			while (this.nextPlayTime < this.machine.knowledge.getTimeMillis())
			{
				this.nextPlayTime = this.nextPlayTime + (long) (this.delayMin * 1000);
				
			}
			
		}
		else
		{
			this.nextPlayTime =
				this.machine.knowledge.getTimeMillis()
					+ (long) ((this.delayMin + this.machine.knowledge.random.nextFloat()
						* (this.delayMax - this.delayMin)) * 1000);
		}
		
	}
	
	public void restart()
	{
		if (this.delayStart == 0)
		{
			this.nextPlayTime =
				this.machine.knowledge.getTimeMillis()
					+ (long) (this.machine.knowledge.random.nextFloat() * this.delayMax * 1000);
		}
		else
		{
			this.nextPlayTime = this.machine.knowledge.getTimeMillis() + (long) (this.delayStart * 1000);
		}
		
	}
	
	@Override
	public String serialize(XMLEventWriter eventWriter) throws XMLStreamException
	{
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		XMLEvent ret = eventFactory.createDTD("\n");
		XMLEvent tab = eventFactory.createDTD("\t");
		
		eventWriter.add(tab);
		eventWriter.add(eventFactory.createStartElement("", "", "eventtimed"));
		eventWriter.add(ret);
		createNode(eventWriter, "eventname", this.event, 2);
		createNode(eventWriter, "delaymin", "" + this.delayMin, 2);
		createNode(eventWriter, "delaymax", "" + this.delayMax, 2);
		createNode(eventWriter, "delaystart", "" + this.delayStart, 2);
		createNode(eventWriter, "volmod", "" + this.volMod, 2);
		createNode(eventWriter, "pitchmod", "" + this.pitchMod, 2);
		eventWriter.add(tab);
		eventWriter.add(eventFactory.createEndElement("", "", "eventtimed"));
		eventWriter.add(ret);
		
		return "";
	}
	
}
