package net.minecraft.src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import eu.ha3.easy.EdgeModel;
import eu.ha3.easy.EdgeTrigger;
import eu.ha3.mc.haddon.PrivateAccessException;
import eu.ha3.mc.haddon.SupportsChatEvents;
import eu.ha3.mc.haddon.SupportsFrameEvents;
import eu.ha3.mc.haddon.SupportsIncomingMessages;
import eu.ha3.mc.haddon.SupportsTickEvents;

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

public class DebuggingHa3Haddon extends HaddonImpl
	implements SupportsTickEvents, SupportsFrameEvents, SupportsChatEvents, SupportsIncomingMessages
{
	private EdgeTrigger button;
	private boolean toggle;
	private RenderSpawnPoints renderRelay;
	private RenderAim renderAim;
	private Map<String, Object> pool;
	
	public static List<DebuggingVisibleSounds> sounds = new ArrayList<DebuggingVisibleSounds>();
	public static Object sounds_LOCK = new Object();
	
	@Override
	public void onLoad()
	{
		this.pool = new LinkedHashMap<String, Object>();
		
		this.button = new EdgeTrigger(new EdgeModel() {
			@Override
			public void onTrueEdge()
			{
				in();
			}
			
			@Override
			public void onFalseEdge()
			{
				out();
			}
		});
		
		this.renderRelay = new RenderSpawnPoints(manager().getMinecraft());
		this.renderAim = new RenderAim(manager().getMinecraft());
		
		manager().hookTickEvents(true);
		manager().hookFrameEvents(true);
		manager().hookChatEvents(true);
		manager().addRenderable(this.renderRelay.getRenderEntityClass(), this.renderRelay.getRenderHook());
		manager().addRenderable(this.renderAim.getRenderEntityClass(), this.renderAim.getRenderHook());
		
		try
		{
			Packet.packetIdToClassMap.addKey(62, DebuggingVisibleSounds.class);
			
			// packetClassToIdMap
			HashMap map = (HashMap) util().getPrivateValueLiteral(Packet.class, null, "a", 1);
			map.put(DebuggingVisibleSounds.class, 62);
			
		}
		catch (PrivateAccessException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		//manager().enlistIncomingMessages(null);
		
		/*try
		{
			Class<EntityRenderer> entityRendererClass = EntityRenderer.class;
			Method meth = entityRendererClass.getMethod("orientCamera", float.class);
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		}*/
	}
	
	protected void in()
	{
		if (util().getCurrentScreen() instanceof GuiChat)
		{
			GuiTextField textField;
			try
			{
				textField = (GuiTextField) util().getPrivateValue(GuiChat.class, util().getCurrentScreen(), 7);
				if (textField != null)
				{
					textField.setText("/rs_channelc g <&cHurricaaane&r> " + textField.getText());
					
				}
			}
			catch (PrivateAccessException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				SoundPool soundPoolMusic =
					(SoundPool) util().getPrivateValueLiteral(
						net.minecraft.src.SoundManager.class, manager().getMinecraft().sndManager, "d", 3);
				
				System.out.println("private " + soundPoolMusic.toString());
				
				@SuppressWarnings("unchecked")
				Map<String, ArrayList> nameToSoundPoolEntriesMapping =
					(Map<String, ArrayList>) util().getPrivateValueLiteral(
						net.minecraft.src.SoundPool.class, soundPoolMusic, "d", 1);
				
				for (Entry<String, ArrayList> entry : nameToSoundPoolEntriesMapping.entrySet())
				{
					//String cuteNameWithDots = entry.getKey();
					ArrayList variousSounds = entry.getValue();
					
					for (int i = 0; i < variousSounds.size(); i++)
					{
						SoundPoolEntry sound = (SoundPoolEntry) variousSounds.get(i);
						
						System.out.println(sound.soundName
							+ " " + sound.soundUrl.toString() + " " + sound.getClass().toString());
						
					}
					
				}
			}
			catch (PrivateAccessException e)
			{
				
			}
			
			//this.toggle = !this.toggle;
		}
		
	}
	
	protected void out()
	{
		if (util().getCurrentScreen() instanceof GuiChat)
			return;
		
		Set set = WorldClient.getEntityList(manager().getMinecraft().theWorld);
		Map<String, Integer> types = new HashMap<String, Integer>();
		for (Object o : set)
		{
			Entity entity = (Entity) o;
			String classname = entity.getClass().toString();
			if (!types.containsKey(classname))
			{
				types.put(classname, 1);
			}
			else
			{
				types.put(classname, 1 + types.get(classname));
			}
			
		}
		for (Entry<String, Integer> o : types.entrySet())
		{
			System.out.println(o.getKey() + " " + o.getValue());
			
		}
		System.out.println("--");
		
	}
	
	@Override
	public void onTick()
	{
		Minecraft mc = manager().getMinecraft();
		EntityPlayer ply = mc.thePlayer;
		//int light =
		//	manager().getMinecraft().theWorld.getBlockLightValue((int) ply.posX, (int) ply.posY, (int) ply.posZ);
		//System.out.println(light);
		
		int xx = (int) Math.floor(ply.posX);
		int yy = (int) Math.floor(ply.posY);
		int zz = (int) Math.floor(ply.posZ);
		
		//mc.theWorld.setBlockAndMetadataWithNotify(133, 0, xx, yy - 2, zz);
		//sendPlaceBlock(ModLoader.getMinecraftInstance(), world, itemstack, i, j, k - 1, byte3, byte4);
		
		//boolean flag =
		//	mc.playerController.onPlayerRightClick(
		//		mc.thePlayer, mc.theWorld, mc.thePlayer.getItemInUse(), xx, yy - 2, zz, 1,
		//		Vec3.createVectorHelper(xx, yy, zz));
		
		if (this.pool.get("fus") == null)
		{
			this.pool.put("fus", new EdgeTrigger(new EdgeModel() {
				@Override
				public void onTrueEdge()
				{
					//manager().getMinecraft().thePlayer.sendChatMessage("fus ro dah");
					
					NetClientHandler var1 = manager().getMinecraft().thePlayer.sendQueue;
					var1.addToSendQueue(new Packet19EntityAction(manager().getMinecraft().thePlayer, 3));
				}
				
				@Override
				public void onFalseEdge()
				{
				}
			}));
		}
		
		((EdgeTrigger) this.pool.get("fus")).signalState(util().areKeysDown(Keyboard.KEY_ADD));
		
		this.renderRelay.ensureExists();
		this.renderAim.ensureExists();
		this.button.signalState(util().areKeysDown(29, 42, 49));
		
		if (true)
			return;
		
		if (util().getClientTick() % 20 != 0)
			return;
		
		double x = ply.posX;
		double y = ply.posY;
		double z = ply.posZ;
		double wadd = 16;
		double hadd = 16;
		
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(x - wadd, y - hadd, z - wadd, x + wadd, y + hadd, z + wadd);
		List ll = manager().getMinecraft().theWorld.getEntitiesWithinAABB(Entity.class, aabb);
		for (Object o : ll)
		{
			Entity ee = (Entity) o;
			System.out.println(ee.getClass().toString() + " " + ee.posX + "," + ee.posY + "," + ee.posZ);
			
		}
		System.out.println(ll.size());
		
	}
	
	@Override
	public void onFrame(float semi)
	{
		//System.out.println(manager().getMinecraft().thePlayer.attackTime);
		
		if (!this.toggle)
			return;
		
		try
		{
			//thirdpersondistancetemp
			float value = 3f;
			util().setPrivateValue(
				net.minecraft.src.EntityRenderer.class, manager().getMinecraft().entityRenderer, 14, value);
			util().setPrivateValue(
				net.minecraft.src.EntityRenderer.class, manager().getMinecraft().entityRenderer, 13, value);
		}
		catch (PrivateAccessException e)
		{
			e.printStackTrace();
		}
		
		/*int sc = 1400;
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_DST_COLOR);
		
		float var = 0.5f;
		int tar = (int) (var * 255);
		
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawing(5);
		tessellator.setColorRGBA(tar, tar, tar, 255);
		tessellator.addVertex(0, 0, 0.0D);
		tessellator.addVertex(0, sc, 0.0D);
		tessellator.addVertex(sc, sc, 0.0D);
		tessellator.addVertex(sc, 0, 0.0D);
		tessellator.addVertex(0, 0, 0.0D);
		tessellator.draw();
		
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);*/
		
		boolean inWorld = false;
		if (inWorld)
		{
			showGPS(1523, 736, 0x00FF00);
			showGPS(602, 275, 0xFFFF00);
			showGPS(-102, 187, 0xFF0000);
			showGPS(-508, -55, 0xFFFFFF);
		}
		else
		{
			//showGPS(1497 / 8, 672 / 8, 0x0000FF); // inverse nether portal for sandstone village
			showGPS(-484 / 8, -720 / 8, 0x0000FF); // inverse nether portal for special village
		}
		
		//showGPS(34, -509, 0x00FF00);
	}
	
	private void showGPS(int xDest, int zDest, int color)
	{
		EntityPlayer ply = manager().getMinecraft().thePlayer;
		double myX = ply.posX;
		double myZ = ply.posZ;
		
		double toX = xDest - myX;
		double toZ = zDest - myZ;
		double distance = Math.sqrt(toX * toX + toZ * toZ);
		
		double ang = Math.atan2(toZ, toX) / Math.PI * 180;
		
		double diffang = Math.floor(ang - 90 - ply.rotationYaw);
		
		double modu = diffang - Math.floor(diffang / 360) * 360;
		if (modu > 180)
		{
			modu = modu - 360;
		}
		
		util().prepareDrawString();
		util().drawString(
			(int) distance + "", (float) modu / 150f + 0.5f, 0.05f, 0, 0, '5', color >> 16 & 0xFF, color >> 8 & 0xFF,
			color & 0xFF, color >> 24 & 0xFF, true);
		
	}
	
	private class RenderSpawnPoints extends Ha3RenderRelay
	{
		public RenderSpawnPoints(Minecraft mc)
		{
			super(mc);
		}
		
		@Override
		public void doRender(Entity entity, double dx, double dy, double dz, float f, float semi)
		{
			World w = manager().getMinecraft().theWorld;
			Random r = new Random();
			
			List<DebuggingVisibleSounds> soundsCopy;
			synchronized (DebuggingHa3Haddon.sounds_LOCK)
			{
				soundsCopy = new ArrayList<DebuggingVisibleSounds>(sounds);
			}
			
			for (DebuggingVisibleSounds sound : soundsCopy)
			{
				boolean isFootsteps = false;
				if (sound.getVolume() == 0.15f /*&& sound.getPitch() == 63*/)
				{
					isFootsteps = true;
				}
				for (int i = 0; i < (isFootsteps ? 20 : 2); i++)
				{
					w.spawnParticle(
						"fireworksSpark", sound.getEffectX(), sound.getEffectY(), sound.getEffectZ(),
						r.nextGaussian() * 0.05D, 0.05, r.nextGaussian() * 0.05D);
				}
			}
			
			sounds.clear();
			
			/*boolean renderEnabled = false;
			
			if (!renderEnabled)
				return;
			
			EntityPlayer ply = manager().getMinecraft().thePlayer;
			World world = manager().getMinecraft().theWorld;
			
			int x = (int) Math.floor(ply.posX);
			int y = (int) Math.floor(ply.posY);
			int z = (int) Math.floor(ply.posZ);
			
			final int rad = 16;
			final int hei = 16;
			
			int threshold = 11;
			
			beginTrace();
			for (int i = x - rad; i <= x + rad; i++)
			{
				for (int j = y - hei; j <= y + hei; j++)
					if (j > 0 && j < 253)
					{
						for (int k = z - rad; k <= z + rad; k++)
						{
							if (world.isBlockOpaqueCube(i, j - 1, k)
								&& !world.isBlockOpaqueCube(i, j, k) && !world.isBlockOpaqueCube(i, j + 1, k)
								&& world.getBlockId(i, j, k) == 0)
							{
								int acura = 4;
								int lv = world.getSavedLightValue(EnumSkyBlock.Block, i, j, k) + acura; // 4 = moonlight
								
								if (lv <= threshold)
								{
									float lvs = (1 - ((float) threshold - lv) / ((float) threshold - acura)) * 0.4f;
									
									trace(dx, dy, dz, i + lvs, j, k + lvs, i + 1 - lvs, j, k + 1 - lvs);
									trace(dx, dy, dz, i + 1 - lvs, j, k + lvs, i + lvs, j, k + 1 - lvs);
								}
								
							}
							
						}
					}
			}
			finishTrace();*/
		}
		
		private void beginTrace()
		{
			RenderHelper.disableStandardItemLighting();
			
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ZERO);
		}
		
		private void finishTrace()
		{
			GL11.glDisable(GL11.GL_BLEND);
			
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			
			RenderHelper.enableStandardItemLighting();
		}
		
		private void trace(
			double dx, double dy, double dz, double xa, double ya, double za, double xb, double yb, double zb)
		{
			GL11.glLineWidth(2f);
			
			GL11.glColor3f(1f, 0f, 0f);
			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawing(GL11.GL_LINE_STRIP);
			
			tessellator.setTranslation(-dx, -dy, -dz);
			tessellator.addVertex(xa, ya, za);
			tessellator.addVertex(xb, yb, zb);
			
			tessellator.draw();
			tessellator.setTranslation(0, 0, 0);
		}
		
		@SuppressWarnings("rawtypes")
		@Override
		public Class getRenderEntityClass()
		{
			return MyRenderEntity.class;
		}
		
		@Override
		public Entity newRenderEntity()
		{
			return new MyRenderEntity();
		}
		
		private class MyRenderEntity extends HRenderEntity
		{
		}
		
	}
	
	private class RenderAim extends Ha3RenderRelay
	{
		public RenderAim(Minecraft mc)
		{
			super(mc);
		}
		
		@Override
		public void doRender(Entity entity, double dx, double dy, double dz, float f, float semi)
		{
			if (true)
				return;
			
			EntityPlayer ply = manager().getMinecraft().thePlayer;
			
			if (ply.inventory.getCurrentItem().itemID != 261 || ply.getItemInUseDuration() <= 5)
				return;
			
			World world = manager().getMinecraft().theWorld;
			
			double ply_x = ply.posX;
			double ply_y = ply.posY + ply.getEyeHeight();
			double ply_z = ply.posZ;
			
			int distance = 512;
			
			Vec3 look = ply.getLookVec();
			double d_x = ply_x + look.xCoord * distance;
			double d_y = ply_y + look.yCoord * distance;
			double d_z = ply_z + look.zCoord * distance;
			
			beginTrace();
			trace(dx, dy, dz, ply_x, ply_y, ply_z, d_x, d_y, d_z);
			finishTrace();
		}
		
		private void beginTrace()
		{
			RenderHelper.disableStandardItemLighting();
			
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			/*GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_ALPHA_TEST);*/
			
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ZERO);
		}
		
		private void finishTrace()
		{
			GL11.glDisable(GL11.GL_BLEND);
			
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			/*GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_DEPTH_TEST);*/
			
			RenderHelper.enableStandardItemLighting();
		}
		
		private void trace(
			double dx, double dy, double dz, double xa, double ya, double za, double xb, double yb, double zb)
		{
			GL11.glLineWidth(2f);
			
			GL11.glColor3f(1f, 0f, 0f);
			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawing(GL11.GL_LINE_STRIP);
			
			tessellator.setTranslation(-dx, -dy, -dz);
			tessellator.addVertex(xa, ya, za);
			tessellator.addVertex(xb, yb, zb);
			
			tessellator.draw();
			tessellator.setTranslation(0, 0, 0);
		}
		
		@SuppressWarnings("rawtypes")
		@Override
		public Class getRenderEntityClass()
		{
			return MyRenderEntity.class;
		}
		
		@Override
		public Entity newRenderEntity()
		{
			return new MyRenderEntity();
		}
		
		private class MyRenderEntity extends HRenderEntity
		{
		}
		
	}
	
	public static void main(String[] args)
	{
		try
		{
			Field f = Minecraft.class.getDeclaredField("minecraftDir");
			Field.setAccessible(new Field[] { f }, true);
			f.set(null, new File("."));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		String[] altArgs = new String[2];
		altArgs[0] = "";
		altArgs[1] = "";
		boolean useAltArgs = false;
		
		try
		{
			File f = new File("E:\\Dropbox\\Minecraft\\user\\mainline\\.minecraft\\mcsession.txt");
			
			if (args.length > 0 && args[0].equals("whitefire"))
			{
				f = new File("E:\\MinecraftSymlink\\mainline\\whitefire\\.minecraft\\mcsession.txt");
			}
			
			if (f.exists())
			{
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String[] split = reader.readLine().split(" ");
				reader.close();
				if (split.length >= 2)
				{
					altArgs[0] = split[0];
					altArgs[1] = split[1];
					useAltArgs = true;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		if (!useAltArgs)
		{
			Minecraft.main(args);
		}
		else
		{
			Minecraft.main(altArgs);
		}
		
	}
	
	@Override
	public void onChat(String contents)
	{
		System.err.println("(C) " + contents);
		
		if (contents.contains("startpooling"))
		{
			this.pool.put("pooling", true);
			this.pool.put("poolingtoken", new Random().nextInt());
			this.pool.put("poolingtime", System.currentTimeMillis());
			manager().getMinecraft().thePlayer.sendChatMessage(this.pool.get("poolingtoken").toString());
		}
		if (this.pool.get("pooling") != null && (Boolean) this.pool.get("pooling") == true)
		{
			String trigger = this.pool.get("poolingtoken").toString();
			if (contents.contains(trigger))
			{
				final long msSpent = System.currentTimeMillis() - (Long) this.pool.get("poolingtime");
				
				new Thread(new Runnable() {
					@Override
					public void run()
					{
						try
						{
							Thread.sleep(5000);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
						DebuggingHa3Haddon.this.pool.put("poolingtoken", new Random().nextInt());
						DebuggingHa3Haddon.this.pool.put("poolingtime", System.currentTimeMillis());
						manager().getMinecraft().thePlayer.addChatMessage("Sending ping... "
							+ DebuggingHa3Haddon.this.pool.get("poolingtoken").toString());
						manager().getMinecraft().thePlayer.sendChatMessage(DebuggingHa3Haddon.this.pool.get(
							"poolingtoken").toString()
							+ " (took " + msSpent + "ms last time)");
						
					}
				}).start();
				
			}
		}
		
	}
	
	@Override
	public void onIncomingMessage(Packet250CustomPayload message)
	{
		System.out.println("(IM) " + message.channel + " :: " + new String(message.data, Charset.forName("UTF-8")));
		
	}
	
}
