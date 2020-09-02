package com.orange.plump.seniornpc.events;

import com.orange.plump.seniornpc.SeniorNPC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinEvent implements Listener {

	private final SeniorNPC plugin;

	public JoinEvent(SeniorNPC plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		plugin.getNpcManager().showNPCS(event.getPlayer());
	}
}
