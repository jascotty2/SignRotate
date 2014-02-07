package com.jascotty2.signrotate;

import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SignRotatePlugin extends JavaPlugin {

	public static final String name = ChatColor.GOLD + "[SignRotate] " + ChatColor.AQUA;
	protected Rotater rot = new Rotater(this);
	protected SRConfig config = new SRConfig(this);

	@Override
	public void onEnable() {

		getServer().getPluginManager().registerEvents(rot.signAdder, this);

		config.load();
		rot.load();
		rot.start();
	}

	@Override
	public void onDisable() {
		if (rot.delaySaveTask != null) {
			rot.flushSave();
		}
		rot.stop();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		String commandName = command.getName().toLowerCase();
		if (commandName.equalsIgnoreCase("signrotate")) {
			boolean update = false;
			if (args.length == 0) {
				if (sender instanceof Player) {
					if (sender.isOp() || sender.hasPermission("signrotate.create")) {
						if (rot.signAdder.toggleWait((Player) sender)) {
							sender.sendMessage(name + "Click a sign");
						} else {
							sender.sendMessage(name + "Click listener removed");
						}
					} else {
						sender.sendMessage(name + ChatColor.RED + "Only an authorized player can do that!");
					}
				} else {
					sender.sendMessage(name + ChatColor.RED + "Only a player can do that!");
				}
			} else if (args[0].equalsIgnoreCase("clockwise") || args[0].equalsIgnoreCase("cw")) {
				if (sender.isOp() || sender.hasPermission("signrotate.admin")) {
					config.isClockwise = true;
					update = true;
					sender.sendMessage(name + "Set Clockwise");
				} else {
					sender.sendMessage(name + "Only an authorized player can do that!");
				}
			} else if (args[0].equalsIgnoreCase("counterclockwise") || args[0].equalsIgnoreCase("ccw")) {
				if (sender.isOp() || sender.hasPermission("signrotate.admin")) {
					config.isClockwise = false;
					update = true;
					sender.sendMessage(name + "Set Counter-Clockwise");
				} else {
					sender.sendMessage(name + "Only an authorized player can do that!");
				}
			} else if (args[0].equalsIgnoreCase("delay")) {
				if (sender.isOp() || sender.hasPermission("signrotate.admin")) {
					if (args.length == 2) {
						double t = -1;
						try {
							t = Double.parseDouble(args[1]);
						} catch (Exception e) {
						}
						if (t > 0) {
							rot.setWait((long) (t * 1000));
							update = true;
							sender.sendMessage(String.format(name + "Rotate delay set to %.1fs", (double) rot.getWait() / 1000));
							rot.start();
						} else {
							sender.sendMessage(name + ChatColor.RED + args[1] + " is not a positive number");
						}
					} else {
						sender.sendMessage(name + ChatColor.RED + "Delay time required");
					}
				} else {
					sender.sendMessage(name + ChatColor.RED + "Only an authorized player can do that!");
				}
			} else {
				return false;
			}
			if (update) {
				try {
					config.save();
				} catch (IOException ex) {
					sender.sendMessage(ChatColor.RED + "Something went wrong.. (check log for details)");
					this.getLogger().log(Level.WARNING, "Failed to save config!", ex);
				}
			}
			return true;
		}
		return false;
	}
} // end class SignRotate

