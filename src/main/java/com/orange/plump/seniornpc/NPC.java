package com.orange.plump.seniornpc;

import com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.*;
import java.util.UUID;

public class NPC {

	//EntityPlayer
	private Object npc;
	private Location location;
	private final SeniorNPC plugin;
	//EntityPlayer.class
	private Class<?> entityClass;

	public NPC(SeniorNPC plugin, Location location) {
		this.plugin = plugin;
		this.location = location;
		try {
			entityClass = this.getNMSClass("EntityPlayer");
		} catch (Exception e) {
			e.printStackTrace();
		}
		spawnNPC();
	}

	public Player getNPCPlayer() {
		return (Player) npc;
	}

	public void despawnNPC() {
		if (npc != null) {
			try {
				Field deadField = entityClass.getField("dead");
				deadField.set(npc, true);

				Method getID = getNMSClass("EntityPlayer").getMethod("getId");
				int id = (int) getID.invoke(npc);

				Object array = Array.newInstance(int.class, 1);
				Array.set(array, 0, id);

				Class<?> packetClass = this.getNMSClass("PacketPlayOutEntityDestroy");
				Constructor<?> packetConstructor = packetClass.getConstructor(array.getClass());

				for (Player player : Bukkit.getOnlinePlayers()) {
					Object packet = packetConstructor.newInstance(array);
					Method sendPacket = getNMSClass("PlayerConnection").getMethod("sendPacket", this.getNMSClass("Packet"));
					sendPacket.invoke(this.getConnection(player), packet);
				}
				npc = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void spawnNPC() {
		if (npc == null) {
			try {
				Object craftServer = getCraftClass("CraftServer").cast(Bukkit.getServer());
				Method getServer = getCraftClass("CraftServer").getMethod("getServer");
				Object nmsServer = getNMSClass("MinecraftServer").cast(getServer.invoke(craftServer));

				Object craftWorld = getCraftClass("CraftWorld").cast(Bukkit.getWorld(location.getWorld().getName()));
				Method getHandle = getCraftClass("CraftWorld").getMethod("getHandle");
				Object nmsWorld = getHandle.invoke(craftWorld);

				GameProfile gameProfile = new GameProfile(UUID.fromString("c9c83234-4eba-4c36-866d-d5edd9dfcbc8"), "PlumpOrange");

				Class<?> playerInteractManagerClass = this.getNMSClass("PlayerInteractManager");
				if (plugin.getVersion() == ServerVersion.FOURTEEN || plugin.getVersion() == ServerVersion.FIFTEEN || plugin.getVersion() == ServerVersion.SIXTEEN) {
					Constructor<?> playerInteractManagerConstructor = playerInteractManagerClass.getConstructor(getNMSClass("WorldServer"));

					Object playerInteractManager = playerInteractManagerConstructor.newInstance(nmsWorld);

					Constructor<?> entityClassConstructor = entityClass.getConstructor(getNMSClass("MinecraftServer"), getNMSClass("WorldServer"), GameProfile.class, playerInteractManagerClass);

					npc = entityClassConstructor.newInstance(nmsServer, nmsWorld, gameProfile, playerInteractManager);
					Method setLocation = getNMSClass("Entity").getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
					setLocation.invoke(npc, location.getX(), location.getY(), location.getZ(), 0f, 0f);
				} else {
					Constructor<?> playerInteractManagerConstructor = playerInteractManagerClass.getConstructor(getNMSClass("World"));

					Object playerInteractManager = playerInteractManagerConstructor.newInstance(nmsWorld);

					Constructor<?> entityClassConstructor = entityClass.getConstructor(getNMSClass("MinecraftServer"), getNMSClass("WorldServer"), GameProfile.class, playerInteractManagerClass);

					npc = entityClassConstructor.newInstance(nmsServer, nmsWorld, gameProfile, playerInteractManager);
					Method setLocation = getNMSClass("Entity").getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
					setLocation.invoke(npc, location.getX(), location.getY(), location.getZ(), 0f, 0f);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (Player player : Bukkit.getOnlinePlayers())
				showNPC(player);

		}
	}

	public void showNPC(Player player) {
		try {
			Method sendPacket = getNMSClass("PlayerConnection").getMethod("sendPacket", this.getNMSClass("Packet"));

			Class<?> packetClass = this.getNMSClass("PacketPlayOutPlayerInfo");
			Class<?> enumClass = this.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
			Enum e = Enum.valueOf((Class) enumClass, "ADD_PLAYER");
			Class entityPlayerArrayClass = Array.newInstance(entityClass, 1).getClass();
			Constructor<?> packetConstructor = packetClass.getConstructor(enumClass, entityPlayerArrayClass);
			Object array = Array.newInstance(entityClass, 1);
			Array.set(array, 0, npc);
			Object packet = packetConstructor.newInstance(e, array);
			sendPacket.invoke(this.getConnection(player), packet);

			packetClass = this.getNMSClass("PacketPlayOutNamedEntitySpawn");
			packetConstructor = packetClass.getConstructor(getNMSClass("EntityHuman"));
			packet = packetConstructor.newInstance(npc);
			sendPacket.invoke(this.getConnection(player), packet);
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	public void animate(Player player) {
		//jump, jump, shift, unshift, shift, hit, unshift
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(3000);
					jump(player);
					Thread.sleep(1000);
					jump(player);
					Thread.sleep(1000);
					crouch(player);
					Thread.sleep(1000);
					uncrouch(player);
					Thread.sleep(1000);
					crouch(player);
					Thread.sleep(1000);
					hit(player);
					Thread.sleep(1000);
					uncrouch(player);
				} catch (Exception e) {
					SeniorNPC.log("Failed to animate npc!");
					e.printStackTrace();
				}
			}
		});
	}

	private void crouch(Player player) {
		try {
			if (plugin.getVersion() == ServerVersion.EIGHT) {
				Class<?> dataWatcherClass = this.getNMSClass("DataWatcher");
				Constructor<?> dataWatcherClassConstructor = dataWatcherClass.getConstructor(getNMSClass("Entity"));
				Object dataWatcher = dataWatcherClassConstructor.newInstance(npc);

				Method setData = getNMSClass("DataWatcher").getMethod("a", int.class, Object.class);
				setData.invoke(dataWatcher, 0, (byte) 0x02);

				Method getID = getNMSClass("Entity").getMethod("getId");
				int id = (int) getID.invoke(npc);

				Class<?> packetClass = this.getNMSClass("PacketPlayOutEntityMetadata");
				Constructor<?> packetConstructor = packetClass.getConstructor(int.class, dataWatcherClass, boolean.class);
				Object packet = packetConstructor.newInstance(id, dataWatcher, true);
				Method sendPacket = getNMSClass("PlayerConnection").getMethod("sendPacket", this.getNMSClass("Packet"));
				sendPacket.invoke(this.getConnection(player), packet);
			} else if (plugin.getVersion() == ServerVersion.FOURTEEN || plugin.getVersion() == ServerVersion.FIFTEEN || plugin.getVersion() == ServerVersion.SIXTEEN) {

				Class<?> enumClass = this.getNMSClass("EntityPose");
				Enum e = null;
				if (plugin.getVersion() == ServerVersion.FOURTEEN)
					e = Enum.valueOf((Class) enumClass, "SNEAKING");
				else
					e = Enum.valueOf((Class) enumClass, "CROUCHING");

				Class<?> dataWatcherClass = this.getNMSClass("DataWatcher");
				Constructor<?> dataWatcherClassConstructor = dataWatcherClass.getConstructor(getNMSClass("Entity"));
				Object dataWatcher = dataWatcherClassConstructor.newInstance(npc);

				Class<?> dataWatcherRegistryClass = this.getNMSClass("DataWatcherRegistry");
				Method aMethod = dataWatcherRegistryClass.getMethod("a", int.class);
				Object aObject = aMethod.invoke(null, 0);
				Object aObject2 = aMethod.invoke(null, 18);

				Class<?> dataWatcherObjectClass = this.getNMSClass("DataWatcherObject");
				Constructor<?> dataWatcherObjectClassConstructor = dataWatcherObjectClass.getConstructor(int.class, getNMSClass("DataWatcherSerializer"));
				Object dataWatcherObject = dataWatcherObjectClassConstructor.newInstance(0, aObject);
				Object dataWatcherObject2 = dataWatcherObjectClassConstructor.newInstance(6, aObject2);

				Method register = getNMSClass("DataWatcher").getMethod("register", getNMSClass("DataWatcherObject"), Object.class);

				register.invoke(dataWatcher, dataWatcherObject, (byte) 0x02);
				register.invoke(dataWatcher, dataWatcherObject2, e);

				Method getID = getNMSClass("Entity").getMethod("getId");
				int id = (int) getID.invoke(npc);

				Class<?> packetClass = this.getNMSClass("PacketPlayOutEntityMetadata");
				Constructor<?> packetConstructor = packetClass.getConstructor(int.class, dataWatcherClass, boolean.class);
				Object packet = packetConstructor.newInstance(id, dataWatcher, true);
				Method sendPacket = getNMSClass("PlayerConnection").getMethod("sendPacket", this.getNMSClass("Packet"));
				sendPacket.invoke(this.getConnection(player), packet);
			} else {
				Class<?> dataWatcherClass = this.getNMSClass("DataWatcher");
				Constructor<?> dataWatcherClassConstructor = dataWatcherClass.getConstructor(getNMSClass("Entity"));
				Object dataWatcher = dataWatcherClassConstructor.newInstance(npc);

				Class<?> dataWatcherRegistryClass = this.getNMSClass("DataWatcherRegistry");
				Method aMethod = dataWatcherRegistryClass.getMethod("a", int.class);
				Object aObject = aMethod.invoke(null, 0);

				Class<?> dataWatcherObjectClass = this.getNMSClass("DataWatcherObject");
				Constructor<?> dataWatcherObjectClassConstructor = dataWatcherObjectClass.getConstructor(int.class, getNMSClass("DataWatcherSerializer"));
				Object dataWatcherObject = dataWatcherObjectClassConstructor.newInstance(0, aObject);

				Method register = getNMSClass("DataWatcher").getMethod("register", getNMSClass("DataWatcherObject"), Object.class);

				register.invoke(dataWatcher, dataWatcherObject, (byte) 0x02);

				Method getID = getNMSClass("Entity").getMethod("getId");
				int id = (int) getID.invoke(npc);

				Class<?> packetClass = this.getNMSClass("PacketPlayOutEntityMetadata");
				Constructor<?> packetConstructor = packetClass.getConstructor(int.class, dataWatcherClass, boolean.class);
				Object packet = packetConstructor.newInstance(id, dataWatcher, true);
				Method sendPacket = getNMSClass("PlayerConnection").getMethod("sendPacket", this.getNMSClass("Packet"));
				sendPacket.invoke(this.getConnection(player), packet);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void uncrouch(Player player) {
		try {
			if (plugin.getVersion() == ServerVersion.EIGHT) {
				Class<?> dataWatcherClass = this.getNMSClass("DataWatcher");
				Constructor<?> dataWatcherClassConstructor = dataWatcherClass.getConstructor(getNMSClass("Entity"));
				Object dataWatcher = dataWatcherClassConstructor.newInstance(npc);

				Method setData = getNMSClass("DataWatcher").getMethod("a", int.class, Object.class);
				setData.invoke(dataWatcher, 0, (byte) 0x0);

				Method getID = getNMSClass("Entity").getMethod("getId");
				int id = (int) getID.invoke(npc);

				Class<?> packetClass = this.getNMSClass("PacketPlayOutEntityMetadata");
				Constructor<?> packetConstructor = packetClass.getConstructor(int.class, dataWatcherClass, boolean.class);
				Object packet = packetConstructor.newInstance(id, dataWatcher, true);
				Method sendPacket = getNMSClass("PlayerConnection").getMethod("sendPacket", this.getNMSClass("Packet"));
				sendPacket.invoke(this.getConnection(player), packet);
			} else if (plugin.getVersion() == ServerVersion.FOURTEEN || plugin.getVersion() == ServerVersion.FIFTEEN || plugin.getVersion() == ServerVersion.SIXTEEN) {

				Class<?> enumClass = this.getNMSClass("EntityPose");
				Enum e = Enum.valueOf((Class) enumClass, "STANDING");

				Class<?> dataWatcherClass = this.getNMSClass("DataWatcher");
				Constructor<?> dataWatcherClassConstructor = dataWatcherClass.getConstructor(getNMSClass("Entity"));
				Object dataWatcher = dataWatcherClassConstructor.newInstance(npc);

				Class<?> dataWatcherRegistryClass = this.getNMSClass("DataWatcherRegistry");
				Method aMethod = dataWatcherRegistryClass.getMethod("a", int.class);
				Object aObject = aMethod.invoke(null, 0);
				Object aObject2 = aMethod.invoke(null, 18);

				Class<?> dataWatcherObjectClass = this.getNMSClass("DataWatcherObject");
				Constructor<?> dataWatcherObjectClassConstructor = dataWatcherObjectClass.getConstructor(int.class, getNMSClass("DataWatcherSerializer"));
				Object dataWatcherObject = dataWatcherObjectClassConstructor.newInstance(0, aObject);
				Object dataWatcherObject2 = dataWatcherObjectClassConstructor.newInstance(6, aObject2);

				Method register = getNMSClass("DataWatcher").getMethod("register", getNMSClass("DataWatcherObject"), Object.class);

				register.invoke(dataWatcher, dataWatcherObject, (byte) 0x0);
				register.invoke(dataWatcher, dataWatcherObject2, e);

				Method getID = getNMSClass("Entity").getMethod("getId");
				int id = (int) getID.invoke(npc);

				Class<?> packetClass = this.getNMSClass("PacketPlayOutEntityMetadata");
				Constructor<?> packetConstructor = packetClass.getConstructor(int.class, dataWatcherClass, boolean.class);
				Object packet = packetConstructor.newInstance(id, dataWatcher, true);
				Method sendPacket = getNMSClass("PlayerConnection").getMethod("sendPacket", this.getNMSClass("Packet"));
				sendPacket.invoke(this.getConnection(player), packet);
			} else {
				Class<?> dataWatcherClass = this.getNMSClass("DataWatcher");
				Constructor<?> dataWatcherClassConstructor = dataWatcherClass.getConstructor(getNMSClass("Entity"));
				Object dataWatcher = dataWatcherClassConstructor.newInstance(npc);

				Class<?> dataWatcherRegistryClass = this.getNMSClass("DataWatcherRegistry");
				Method aMethod = dataWatcherRegistryClass.getMethod("a", int.class);
				Object aObject = aMethod.invoke(null, 0);

				Class<?> dataWatcherObjectClass = this.getNMSClass("DataWatcherObject");
				Constructor<?> dataWatcherObjectClassConstructor = dataWatcherObjectClass.getConstructor(int.class, getNMSClass("DataWatcherSerializer"));
				Object dataWatcherObject = dataWatcherObjectClassConstructor.newInstance(0, aObject);

				Method register = getNMSClass("DataWatcher").getMethod("register", getNMSClass("DataWatcherObject"), Object.class);

				register.invoke(dataWatcher, dataWatcherObject, (byte) 0x0);

				Method getID = getNMSClass("Entity").getMethod("getId");
				int id = (int) getID.invoke(npc);

				Class<?> packetClass = this.getNMSClass("PacketPlayOutEntityMetadata");
				Constructor<?> packetConstructor = packetClass.getConstructor(int.class, dataWatcherClass, boolean.class);
				Object packet = packetConstructor.newInstance(id, dataWatcher, true);
				Method sendPacket = getNMSClass("PlayerConnection").getMethod("sendPacket", this.getNMSClass("Packet"));
				sendPacket.invoke(this.getConnection(player), packet);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jump(Player player) {
		/*
		Simulated jump with teleports
		 */
		for (int i = 0; i < 10; i++) {
			if (i < 5) {
				setLocation(player, location.getX(), location.getY() + (0.21 * i), location.getZ());
			} else {
				setLocation(player, location.getX(), location.getY() + (0.21 * (5 - (i - 5))), location.getZ());
			}
			try {
				//One tick
				Thread.sleep(1000 / 20);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		resetLocation(player);
	}

	private void hit(Player player) {
		try {
			Class<?> packetClass = this.getNMSClass("PacketPlayOutAnimation");
			Constructor<?> packetConstructor = packetClass.getConstructor(this.getNMSClass("Entity"), int.class);
			Object packet = packetConstructor.newInstance(npc, 0);
			Method sendPacket = getNMSClass("PlayerConnection").getMethod("sendPacket", this.getNMSClass("Packet"));
			sendPacket.invoke(this.getConnection(player), packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void resetLocation(Player player) {
		setLocation(player, location.getX(), location.getY(), location.getZ());
	}

	private void setLocation(Player player, double x, double y, double z) {
		try {

			Method setLocation = getNMSClass("Entity").getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
			setLocation.invoke(npc, x, y, z, 0f, 0f);

			Class<?> packetClass = this.getNMSClass("PacketPlayOutEntityTeleport");
			Constructor<?> packetConstructor = packetClass.getConstructor(this.getNMSClass("Entity"));
			Object packet = packetConstructor.newInstance(npc);
			Method sendPacket = getNMSClass("PlayerConnection").getMethod("sendPacket", this.getNMSClass("Packet"));
			sendPacket.invoke(this.getConnection(player), packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
		String name = "net.minecraft.server." + version + nmsClassString;
		Class<?> nmsClass = Class.forName(name);
		return nmsClass;
	}

	private Class<?> getCraftClass(String craftClassName) throws ClassNotFoundException {
		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
		String name = "org.bukkit.craftbukkit." + version + craftClassName;
		Class<?> nmsClass = Class.forName(name);
		return nmsClass;
	}

	private Object getConnection(Player player) throws SecurityException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InvocationTargetException {
		Method getHandle = player.getClass().getMethod("getHandle");
		Object nmsPlayer = getHandle.invoke(player);
		Field conField = nmsPlayer.getClass().getField("playerConnection");
		Object con = conField.get(nmsPlayer);
		return con;
	}
}
