package com.orange.plump.seniornpc.commands;

import com.orange.plump.seniornpc.NPC;
import com.orange.plump.seniornpc.SeniorNPC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SeniorNPCCommand implements CommandExecutor {

	private final SeniorNPC plugin;

	public SeniorNPCCommand(SeniorNPC plugin) {
		this.plugin = plugin;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 1) {
			sender.sendMessage(ChatColor.RED + "Invalid usage! /seniornpc spawn");
			return false;
		}

		if (!args[0].equalsIgnoreCase("spawn")) {
			sender.sendMessage(ChatColor.RED + "Invalid usage! /seniornpc spawn");
			return false;
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can use this command!");
			return false;
		}

		Player player = (Player) sender;
		NPC npc = plugin.getNpcManager().createNPC(player.getLocation());
		player.sendMessage(ChatColor.GREEN + "NPC created!");
		npc.animate(player);
		return false;
	}
}
