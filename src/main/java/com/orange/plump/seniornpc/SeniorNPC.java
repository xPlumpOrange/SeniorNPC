package com.orange.plump.seniornpc;

import com.orange.plump.seniornpc.commands.SeniorNPCCommand;
import com.orange.plump.seniornpc.events.JoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SeniorNPC extends JavaPlugin {

	private NPCManager npcManager;
	private ServerVersion version;

	public void onLoad() {
		String raw = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
		//System.out.println(raw);
		if (raw.startsWith("v1_8")) {
			version = ServerVersion.EIGHT;
		} else if (raw.startsWith("v1_9")) {
			version = ServerVersion.NINE;
		} else if (raw.startsWith("v1_10")) {
			version = ServerVersion.TEN;
		} else if (raw.startsWith("v1_11")) {
			version = ServerVersion.ELEVEN;
		} else if (raw.startsWith("v1_12")) {
			version = ServerVersion.TWELVE;
		} else if (raw.startsWith("v1_13")) {
			version = ServerVersion.THIRTEEN;
		} else if (raw.startsWith("v1_14")) {
			version = ServerVersion.FOURTEEN;
		} else if (raw.startsWith("v1_15")) {
			version = ServerVersion.FIFTEEN;
		} else if (raw.startsWith("v1_16")) {
			version = ServerVersion.SIXTEEN;
		}
	}

	public void onEnable() {
		log("Starting plugin...");
		npcManager = new NPCManager(this);
		getCommand("seniornpc").setExecutor(new SeniorNPCCommand(this));
		registerEvents();
		log("Started plugin.");
	}

	public void onDisable() {
		log("Stopping plugin...");
		if (npcManager != null) npcManager.despawnNPCS();
		log("Stopped plugin.");
	}

	private void registerEvents() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(new JoinEvent(this), this);
	}

	public NPCManager getNpcManager() {
		return npcManager;
	}

	public ServerVersion getVersion() {
		return this.version;
	}


	public static void log(Object o) {
		System.out.println("[SeniorNPC] " + o.toString());
	}
}
