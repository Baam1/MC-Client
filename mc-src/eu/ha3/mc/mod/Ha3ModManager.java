package eu.ha3.mc.mod;

import net.minecraft.client.Minecraft;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.Packet250CustomPayload;

public interface Ha3ModManager
{
	public Minecraft getMinecraft();
	
	public void setMod(Ha3Mod modIn);
	
	public void setUsesFrame(boolean enable);
	
	public void setReceivesChat(boolean enable);
	
	public void subscribeToPluginChannel(String channel);
	
	//public void setUsesKeyBindingEvent(boolean enable);
	
	public void addKeyBinding(KeyBinding keyBindingIn, String localization);
	
	public boolean getUsesFrame();
	
	public boolean getReceivesChat();
	
	//public boolean getUsesKeyBindingEvent();
	
	public void communicateFrame(float fspan);
	
	public void communicateKeyBindingEvent(KeyBinding event);
	
	public void communicateChat(String contents);
	
	public void communicatePluginChannelPacket(Packet250CustomPayload packet);
	
	public void communicateManagerReady();
	
	@SuppressWarnings("rawtypes")
	public Object getPrivateValue(Class classToPerformOn,
			Object instanceToPerformOn, int zeroOffsets)
					throws Ha3ModPrivateAccessException;
	
	@SuppressWarnings("rawtypes")
	public void setPrivateValue(Class classToPerformOn,
			Object instanceToPerformOn, int zeroOffsets, Object newValue)
					throws Ha3ModPrivateAccessException;
	
}
