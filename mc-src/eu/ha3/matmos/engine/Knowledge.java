package eu.ha3.matmos.engine;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamResult;

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

/**
 * Stores a Knowledge.
 */
public class Knowledge
{
	HashMap<String, Dynamic> dynamics;
	HashMap<String, SugarList> lists;
	
	HashMap<String, Condition> conditions;
	HashMap<String, ConditionSet> csets;
	HashMap<String, Machine> machines;
	
	HashMap<String, Event> events;
	
	Data data;
	SoundRelay soundManager;
	RunningClock clock;
	
	private boolean isRunning;
	int dataLastVersion;
	
	Random random;
	
	public Knowledge()
	{
		this.data = new Data();
		this.soundManager = null;
		
		this.dataLastVersion = 0;
		this.isRunning = false;
		
		this.random = new Random(System.currentTimeMillis());
		
		this.clock = new RunningClock();
		
		patchKnowledge();
		
	}
	
	/**
	 * Closes the Knowledge, annihilates all references to libraries of objects
	 * from the current knowledge, and instantiates new ones.<br>
	 * <br>
	 * This does not clear the previously stored libraries, in order to preserve
	 * the integrity of keyrings.<br>
	 * This renews the library by creating a new object, so any referenced
	 * library from another object will keep its integrity.
	 * 
	 */
	public void patchKnowledge()
	{
		turnOff();
		
		this.dynamics = new HashMap<String, Dynamic>();
		this.lists = new HashMap<String, SugarList>();
		
		this.conditions = new HashMap<String, Condition>();
		this.csets = new HashMap<String, ConditionSet>();
		this.machines = new HashMap<String, Machine>();
		
		this.events = new HashMap<String, Event>();
		
	}
	
	public void turnOn()
	{
		if (this.soundManager == null)
			return;
		
		if (this.isRunning)
			return;
		
		reclaimKeyring();
		this.isRunning = true;
		
		// FIXME Why do i have to do that -> look at the UML sheet
		// Machines have to be powered on for their routines to run even if the machines are turned off
		for (Machine machine : this.machines.values())
		{
			machine.powerOn();
			
		}
		
	}
	
	public void turnOff()
	{
		if (!this.isRunning)
			return;
		
		this.isRunning = false;
		
		// FIXME Why do i have to do that -> look at the UML sheet
		// Machines have to be powered on for their routines to run even if the machines are turned off
		for (Machine machine : this.machines.values())
		{
			machine.powerOff();
			
		}
		
	}
	
	public boolean isTurnedOn()
	{
		return this.isRunning;
		
	}
	
	public Set<String> getDynamicsKeySet()
	{
		return this.dynamics.keySet();
		
	}
	
	public Set<String> getListsKeySet()
	{
		return this.lists.keySet();
		
	}
	
	public Set<String> getConditionsKeySet()
	{
		return this.conditions.keySet();
		
	}
	
	public Set<String> getConditionSetsKeySet()
	{
		return this.csets.keySet();
		
	}
	
	public Set<String> getMachinesKeySet()
	{
		return this.machines.keySet();
		
	}
	
	public Set<String> getEventsKeySet()
	{
		return this.events.keySet();
		
	}
	
	/**
	 * Makes sure referenced database uses this knowledge.
	 */
	public void reclaimKeyring()
	{
		turnOff();
		
		for (Dynamic dynamic : this.dynamics.values())
		{
			dynamic.setKnowledge(this);
		}
		
		// Lists don't have to be tied with the knowledge
		
		for (Condition condition : this.conditions.values())
		{
			condition.setKnowledge(this);
		}
		
		for (ConditionSet cset : this.csets.values())
		{
			cset.setKnowledge(this);
		}
		
		for (Machine machine : this.machines.values())
		{
			machine.setKnowledge(this);
		}
		
		for (Event event : this.events.values())
		{
			event.setKnowledge(this);
		}
		
	}
	
	/**
	 * Gets from originalKnowledge a keyring of the database referencing the
	 * original database objects.
	 */
	@SuppressWarnings("unchecked")
	public void retreiveKeyring(Knowledge originalKnowledge)
	{
		if (originalKnowledge.isRunning)
			return;
		
		this.dynamics = (HashMap<String, Dynamic>) originalKnowledge.dynamics.clone();
		this.lists = (HashMap<String, SugarList>) originalKnowledge.lists.clone();
		this.conditions = (HashMap<String, Condition>) originalKnowledge.conditions.clone();
		this.csets = (HashMap<String, ConditionSet>) originalKnowledge.csets.clone();
		this.machines = (HashMap<String, Machine>) originalKnowledge.machines.clone();
		this.events = (HashMap<String, Event>) originalKnowledge.events.clone();
		reclaimKeyring();
		
	}
	
	public void setSoundManager(SoundRelay soundManagerIn)
	{
		this.soundManager = soundManagerIn;
		
	}
	
	public void cacheSounds()
	{
		for (Event event : this.events.values())
		{
			event.cacheSounds();
		}
		
	}
	
	public void setClock(RunningClock clockIn)
	{
		this.clock = clockIn;
	}
	
	public void setData(Data dataIn)
	{
		this.data = dataIn;
		applySheetFlagNeedsTesting();
	}
	
	public long getTimeMillis()
	{
		return this.clock.getMilliseconds();
		
	}
	
	void applySheetFlagNeedsTesting()
	{
		for (Condition condition : this.conditions.values())
		{
			condition.flagNeedsTesting();
		}
		
		for (Dynamic dynamic : this.dynamics.values())
		{
			dynamic.flagNeedsTesting();
		}
		
	}
	
	public Event getEvent(String name)
	{
		return this.events.get(name);
		
	}
	
	public boolean addEvent(String name)
	{
		if (this.events.containsKey(name))
			return false;
		
		this.events.put(name, new Event(this));
		this.events.get(name).nickname = name;
		
		return true;
		
	}
	
	public boolean removeEvent(String name)
	{
		if (!this.events.containsKey(name))
			return false;
		
		this.events.remove(name);
		
		return true;
		
	}
	
	public boolean renameEvent(String name, String newName)
	{
		if (!this.events.containsKey(name))
			return false; // Error?
			
		if (this.events.containsKey(newName))
			return false;
		
		this.events.put(newName, this.events.get(name));
		this.events.remove(name);
		this.events.get(newName).nickname = newName;
		
		for (Machine machine : this.machines.values())
		{
			for (TimedEvent etime : machine.getTimedEvents())
			{
				if (etime.event.equals(name))
				{
					etime.event = newName;
				}
				
			}
			
		}
		
		return true;
		
	}
	
	void applyDynamicFlagNeedsTesting()
	{
		for (Condition condition : this.conditions.values())
		{
			condition.flagNeedsTesting();
			
		}
		
	}
	
	public Dynamic getDynamic(String name)
	{
		return this.dynamics.get(name);
		
	}
	
	public boolean addDynamic(String name)
	{
		if (this.dynamics.containsKey(name))
			return false;
		
		this.dynamics.put(name, new Dynamic(this));
		this.dynamics.get(name).nickname = name;
		
		applyDynamicFlagNeedsTesting();
		
		return true;
		
	}
	
	public boolean removeDynamic(String name)
	{
		if (!this.dynamics.containsKey(name))
			return false;
		
		this.dynamics.remove(name);
		
		applyDynamicFlagNeedsTesting();
		
		return true;
		
	}
	
	public boolean renameDynamic(String name, String newName)
	{
		if (!this.dynamics.containsKey(name))
			return false; // Error?
			
		if (this.dynamics.containsKey(newName))
			return false;
		
		this.dynamics.put(newName, this.dynamics.get(name));
		this.dynamics.remove(name);
		this.dynamics.get(newName).nickname = newName;
		
		for (Condition condition : this.conditions.values())
		{
			condition.replaceDynamicName(name, newName);
		}
		
		return true;
		
	}
	
	void applyListFlagNeedsTesting()
	{
		for (Condition condition : this.conditions.values())
		{
			condition.flagNeedsTesting();
			
		}
		
	}
	
	public SugarList getList(String name)
	{
		return this.lists.get(name);
		
	}
	
	public boolean addList(String name)
	{
		if (this.lists.containsKey(name))
			return false;
		
		this.lists.put(name, new SugarList());
		this.lists.get(name).nickname = name;
		
		applyDynamicFlagNeedsTesting();
		
		return true;
		
	}
	
	public boolean removeList(String name)
	{
		if (!this.lists.containsKey(name))
			return false;
		
		this.lists.remove(name);
		
		applyDynamicFlagNeedsTesting();
		
		return true;
		
	}
	
	public boolean renameList(String name, String newName)
	{
		if (!this.lists.containsKey(name))
			return false; // Error?
			
		if (this.lists.containsKey(newName))
			return false;
		
		this.lists.put(newName, this.lists.get(name));
		this.lists.remove(name);
		this.lists.get(newName).nickname = newName;
		
		for (Condition condition : this.conditions.values())
		{
			condition.replaceListName(name, newName);
		}
		
		return true;
		
	}
	
	void applyDataConditionNeedsTesting()
	{
		for (ConditionSet cset : this.csets.values())
		{
			cset.flagNeedsTesting();
		}
		
	}
	
	public Condition getDataCondition(String name)
	{
		return this.conditions.get(name);
		
	}
	
	public boolean addDataCondition(String name)
	{
		if (this.conditions.containsKey(name))
			return false;
		
		this.conditions.put(name, new Condition(this));
		this.conditions.get(name).nickname = name;
		
		applyDataConditionNeedsTesting();
		
		return true;
		
	}
	
	public boolean renameDataCondition(String name, String newName)
	{
		if (!this.conditions.containsKey(name))
			return false;
		
		if (this.conditions.containsKey(newName))
			return false;
		
		this.conditions.put(newName, this.conditions.get(name));
		this.conditions.remove(name);
		this.conditions.get(newName).nickname = newName;
		
		for (ConditionSet cset : this.csets.values())
		{
			cset.replaceConditionName(name, newName);
		}
		
		applyDataConditionNeedsTesting();
		
		return true;
		
	}
	
	public boolean removeDataCondition(String name)
	{
		if (!this.conditions.containsKey(name))
			return false;
		
		this.conditions.remove(name);
		
		applyDataConditionNeedsTesting();
		
		return true;
		
	}
	
	void applyConditionSetNeedsTesting()
	{
		for (Machine machine : this.machines.values())
		{
			machine.flagNeedsTesting();
		}
		
	}
	
	public ConditionSet getConditionSet(String name)
	{
		return this.csets.get(name);
	}
	
	public boolean addConditionSet(String name)
	{
		if (this.csets.containsKey(name))
			return false;
		
		this.csets.put(name, new ConditionSet(this));
		this.csets.get(name).nickname = name;
		
		applyConditionSetNeedsTesting();
		
		return true;
		
	}
	
	public boolean renameConditionSet(String name, String newName)
	{
		if (!this.csets.containsKey(name))
			return false;
		
		if (this.csets.containsKey(newName))
			return false;
		
		this.csets.put(newName, this.csets.get(name));
		this.csets.remove(name);
		this.csets.get(newName).nickname = newName;
		
		for (Machine machine : this.machines.values())
		{
			machine.replaceSetName(name, newName);
			
		}
		
		applyConditionSetNeedsTesting();
		
		return true;
		
	}
	
	public boolean removeConditionSet(String name)
	{
		if (!this.csets.containsKey(name))
			// MAtmosEngine.logger;
			// Not an exception!
			return false;
		
		this.csets.remove(name);
		
		applyConditionSetNeedsTesting();
		
		return true;
		
	}
	
	void applyMachineNeedsTesting()
	{
		// Do nothing
	}
	
	public Machine getMachine(String name)
	{
		return this.machines.get(name);
	}
	
	public boolean addMachine(String name)
	{
		if (this.machines.containsKey(name))
			return false;
		
		this.machines.put(name, new Machine(this));
		this.machines.get(name).nickname = name;
		
		applyMachineNeedsTesting();
		
		return true;
		
	}
	
	public boolean removeMachine(String name)
	{
		if (!this.machines.containsKey(name))
			return false;
		
		this.machines.remove(name);
		
		applyMachineNeedsTesting();
		
		return true;
		
	}
	
	public boolean renameMachine(String name, String newName)
	{
		if (!this.machines.containsKey(name))
			return false;
		
		if (this.machines.containsKey(newName))
			return false;
		
		this.machines.put(newName, this.machines.get(name));
		this.machines.remove(name);
		this.machines.get(newName).nickname = newName;
		
		// Nothing to do!
		
		applyConditionSetNeedsTesting();
		
		return true;
		
	}
	
	public void routine()
	{
		if (!this.isRunning)
			return;
		
		if (this.dataLastVersion != this.data.updateVersion)
		{
			evaluate();
			this.dataLastVersion = this.data.updateVersion;
			
		}
		
		this.soundManager.routine();
		for (Iterator<Machine> iter = this.machines.values().iterator(); iter.hasNext();)
		{
			iter.next().routine();
			
		}
		
	}
	
	public void soundRoutine()
	{
		if (!this.isRunning)
			return;
		
		this.soundManager.routine();
		for (Iterator<Machine> iter = this.machines.values().iterator(); iter.hasNext();)
		{
			iter.next().routine();
			
		}
		
	}
	
	public void dataRoutine()
	{
		if (!this.isRunning)
			return;
		
		if (this.dataLastVersion != this.data.updateVersion)
		{
			evaluate();
			this.dataLastVersion = this.data.updateVersion;
			
		}
		
	}
	
	void evaluate()
	{
		if (!this.isRunning) // The keyring may not be reclaimed: If running then it must have been reclaimed. Do not perform if not running.
			return;
		
		for (Dynamic dynamic : this.dynamics.values())
		{
			dynamic.evaluate();
			
		}
		// Lists don't have to be tied with the knowledge
		for (Condition condition : this.conditions.values())
		{
			condition.evaluate();
			
		}
		for (ConditionSet cset : this.csets.values())
		{
			cset.evaluate();
			
		}
		for (Machine machine : this.machines.values())
		{
			machine.evaluate();
			
		}
		
	}
	
	public String createXML() throws XMLStreamException
	{
		StreamResult serialized = new StreamResult(new StringWriter());
		
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		
		XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(serialized);
		
		XMLEvent ret = eventFactory.createDTD("\n");
		XMLEvent end = eventFactory.createDTD("\n");
		
		Object[] keysArray;
		
		eventWriter.add(eventFactory.createStartDocument());
		eventWriter.add(ret);
		eventWriter.add(eventFactory.createStartElement("", "", "contents"));
		
		keysArray = this.dynamics.keySet().toArray();
		Arrays.sort(keysArray);
		for (int i = 0; i < keysArray.length; i++)
		{
			String name = keysArray[i].toString();
			
			eventWriter.add(ret);
			eventWriter.add(eventFactory.createStartElement("", "", "dynamic"));
			eventWriter.add(eventFactory.createAttribute("name", name));
			eventWriter.add(ret);
			this.dynamics.get(name).serialize(eventWriter);
			eventWriter.add(eventFactory.createEndElement("", "", "dynamic"));
			
		}
		keysArray = this.lists.keySet().toArray();
		Arrays.sort(keysArray);
		for (int i = 0; i < keysArray.length; i++)
		{
			String name = keysArray[i].toString();
			
			eventWriter.add(ret);
			eventWriter.add(eventFactory.createStartElement("", "", "list"));
			eventWriter.add(eventFactory.createAttribute("name", name));
			eventWriter.add(ret);
			this.lists.get(name).serialize(eventWriter);
			eventWriter.add(eventFactory.createEndElement("", "", "list"));
			
		}
		keysArray = this.conditions.keySet().toArray();
		Arrays.sort(keysArray);
		for (int i = 0; i < keysArray.length; i++)
		{
			String name = keysArray[i].toString();
			
			eventWriter.add(ret);
			eventWriter.add(eventFactory.createStartElement("", "", "condition"));
			eventWriter.add(eventFactory.createAttribute("name", name));
			eventWriter.add(ret);
			this.conditions.get(name).serialize(eventWriter);
			eventWriter.add(eventFactory.createEndElement("", "", "condition"));
			
		}
		keysArray = this.csets.keySet().toArray();
		Arrays.sort(keysArray);
		for (int i = 0; i < keysArray.length; i++)
		{
			String name = keysArray[i].toString();
			
			eventWriter.add(ret);
			eventWriter.add(eventFactory.createStartElement("", "", "set"));
			eventWriter.add(eventFactory.createAttribute("name", name));
			eventWriter.add(ret);
			this.csets.get(name).serialize(eventWriter);
			eventWriter.add(eventFactory.createEndElement("", "", "set"));
			
		}
		keysArray = this.events.keySet().toArray();
		Arrays.sort(keysArray);
		for (int i = 0; i < keysArray.length; i++)
		{
			String name = keysArray[i].toString();
			
			eventWriter.add(ret);
			eventWriter.add(eventFactory.createStartElement("", "", "event"));
			eventWriter.add(eventFactory.createAttribute("name", name));
			eventWriter.add(ret);
			this.events.get(name).serialize(eventWriter);
			eventWriter.add(eventFactory.createEndElement("", "", "event"));
			
		}
		keysArray = this.machines.keySet().toArray();
		Arrays.sort(keysArray);
		for (int i = 0; i < keysArray.length; i++)
		{
			String name = keysArray[i].toString();
			
			eventWriter.add(ret);
			eventWriter.add(eventFactory.createStartElement("", "", "machine"));
			eventWriter.add(eventFactory.createAttribute("name", name));
			eventWriter.add(ret);
			this.machines.get(name).serialize(eventWriter);
			eventWriter.add(eventFactory.createEndElement("", "", "machine"));
			
		}
		
		eventWriter.add(ret);
		eventWriter.add(eventFactory.createEndElement("", "", "contents"));
		
		eventWriter.add(end);
		eventWriter.add(eventFactory.createEndDocument());
		eventWriter.close();
		
		return serialized.getWriter().toString();
		
	}
	/*
	public String diffXML(MAtmosKnowledge base) throws XMLStreamException
	{
		StreamResult serialized = new StreamResult(new StringWriter());
		
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		
		XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(serialized);

		XMLEvent ret = eventFactory.createDTD("\n");
		XMLEvent end = eventFactory.createDTD("\n");
		
		eventWriter.add(eventFactory.createStartDocument());
		eventWriter.add(ret);
		eventWriter.add(eventFactory.createStartElement("", "", "contents"));
		
		
		
		for (Iterator<Entry<String,MAtmosDynamic>> iter = dynamics.entrySet().iterator(); iter.hasNext();)
		{
			Entry<String,MAtmosDynamic> entry = iter.next();
			boolean addMe = false;
			
			if (base.getDynamic( entry.getKey() ) == null)
			{
				addMe = true;
				
			}
			else
			
		}
		
		
		return "";
		
	}
	 */
	
}
