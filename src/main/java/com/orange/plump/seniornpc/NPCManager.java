package com.orange.plump.seniornpc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class NPCManager {

	private FileConfiguration npcConfig;
	private final SeniorNPC plugin;

	private List<NPC> npcs;

	public NPCManager(SeniorNPC plugin) {
		this.plugin = plugin;
		loadConfig();
		npcs = new ArrayList<>();
		loadNPCsInConfig();
	}

	private void loadNPCsInConfig() {
		List<String> raw = npcConfig.getStringList("npcs");
		for (String data : raw) {
			try {
				String world = data.split(":")[0];
				String[] cords = data.split(":")[1].split(",");
				NPC npc = new NPC(plugin, new Location(Bukkit.getWorld(world), Double.parseDouble(cords[0]), Double.parseDouble(cords[1]), Double.parseDouble(cords[2])));
				npcs.add(npc);
			} catch (Exception e) {
				SeniorNPC.log("Failed to npc \"" + data + "\" in config");
			}
		}
	}

	private void loadConfig() {
		try {
			File file = new File(plugin.getDataFolder(), "npcs.yml");
			if (!file.exists()) {
				plugin.saveResource("npcs.yml", false);
			}
			npcConfig = YamlConfiguration.loadConfiguration(file);
			Reader defConfigStream = new InputStreamReader(plugin.getResource("npcs.yml"), "UTF8");
			if (defConfigStream != null) {
				YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
				npcConfig.setDefaults(defConfig);
			}
			npcConfig.options().copyDefaults(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveConfig() {
		try {
			npcConfig.save(new File(plugin.getDataFolder(), "npcs.yml"));
		} catch (Exception e) {
			SeniorNPC.log("Failed to save NPC!");
			e.printStackTrace();
		}
	}

	public void despawnNPCS() {
		for (NPC npc : npcs)
			npc.despawnNPC();
		npcs.clear();
	}

	public NPC createNPC(Location location) {
		NPC npc = new NPC(plugin, location);
		List<String> raw = npcConfig.getStringList("npcs");
		raw.add(location.getWorld().getName() + ":" + location.getX() + "," + location.getY() + "," + location.getZ());
		npcConfig.set("npcs", raw);
		saveConfig();
		npcs.add(npc);
		return npc;
	}

	public void showNPCS(Player player) {
		for (NPC npc : npcs)
			npc.showNPC(player);
	}

}
