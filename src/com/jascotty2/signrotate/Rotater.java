package com.jascotty2.signrotate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

public class Rotater {

	ArrayList<Location> signs = new ArrayList<Location>();
	SignRotatePlugin plugin = null;
	BukkitTask delaySaveTask = null;
	private final SignSaver delaySave = new SignSaver();
	public AddSignListener signAdder = new AddSignListener();
	protected final SignRotater rotater = new SignRotater();

	public Rotater(SignRotatePlugin signPlugin) {
		plugin = signPlugin;
	}

	public void start() {
		rotater.start(plugin.config.rotateWait);
	}

	public void stop() {
		rotater.cancel();
	}

	public void load() {
		if (plugin.config.signDBFile.exists()) {
//				List<String[]> signdb = CSV.loadFile(plugin.config.signDBFile);
//				for (String[] s : signdb) {
//					if (s.length == 4 && plugin.getServer().getWorld(s[0]) != null) {
//						signs.add(new Location(plugin.getServer().getWorld(s[0]),
//								CheckInput.GetDouble(s[1], 0),
//								CheckInput.GetDouble(s[2], 0),
//								CheckInput.GetDouble(s[3], 0)));
//					}
//				}

			DataInputStream in = null;
			try {
				in = new DataInputStream(new BufferedInputStream(new FileInputStream(plugin.config.signDBFile)));
				while (true) {
					UUID test = new UUID(in.readLong(), in.readLong());
					World w = plugin.getServer().getWorld(test);
					if (w != null) {
						signs.add(new Location(w, in.readInt(), in.readInt(), in.readInt()));
					} else {
						// skip to next
						in.readInt();
						in.readInt();
						in.readInt();
					}
				}
			} catch (EOFException ex) {
			} catch (Exception ex) {
				plugin.getLogger().log(Level.SEVERE, "Failed to load sign database", ex);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException ex) {
					}
				}
			}

		}

	}

	public void save() {
		if (delaySaveTask == null) {
			delaySaveTask = plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, delaySave, 10 * 20);
		}
	}

	public boolean flushSave() {
		if (delaySaveTask != null) {
			plugin.getServer().getScheduler().cancelTask(delaySaveTask.getTaskId());
			delaySaveTask = null;
		}
//			LinkedList<String> file = new LinkedList<String>();
//			for (Location l : signs) {
//				file.add(l.getWorld().getName() + ","
//						+ l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ());
//			}
//			return CSV.saveFile(plugin.config.signDBFile, file);

		DataOutputStream out = null;
		try {
			out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(plugin.config.signDBFile)));
			for (Location l : signs) {
				if (l != null) {
					UUID wid = l.getWorld().getUID();
					out.writeLong(wid.getMostSignificantBits());
					out.writeLong(wid.getLeastSignificantBits());
					out.writeInt(l.getBlockX());
					out.writeInt(l.getBlockY());
					out.writeInt(l.getBlockZ());
				}
			}
			out.flush();
		} catch (Exception ex) {
			plugin.getLogger().log(Level.SEVERE, "Failed to save  sign database", ex);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ex) {
				}
			}
		}

		return false;
	}

	public long getWait() {
		return plugin.config.rotateWait;
	}

	public void setWait(long wait) {
		plugin.config.rotateWait = wait < SRConfig.MIN_WAIT ? SRConfig.MIN_WAIT : wait;
	}

	protected class SignSaver implements Runnable {

		@Override
		public void run() {
			delaySaveTask = null;
			flushSave();
		}
	}

	public class SignRotater implements Runnable {

		int taskID = -1;

		public void start(long wait) {
			// 20 ticks per second
			taskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, (wait * 20) / 1000, (wait * 20) / 1000);
		}

		public void cancel() {
			if (taskID != -1) {
				plugin.getServer().getScheduler().cancelTask(taskID);
				taskID = -1;
			}
		}

		@Override
		public void run() {
			for (Location l : signs.toArray(new Location[0])) {
				final BlockState bs = l.getBlock().getState();
				if (bs instanceof Sign) {
					if (plugin.config.isClockwise) {
						if (l.getBlock().getData() == 0xF) {
							l.getBlock().setData((byte) 0);
						} else {
							l.getBlock().setData((byte) (l.getBlock().getData() + 1));
						}
					} else {
						if (l.getBlock().getData() == 0x0) {
							l.getBlock().setData((byte) 0xF);
						} else {
							l.getBlock().setData((byte) (l.getBlock().getData() - 1));
						}
					}
				} else if (bs instanceof Skull) {
					Skull s = (Skull) bs;
					BlockFace rot = s.getRotation();
					int i = Math.max(cwRot.indexOf(rot), 0) + (plugin.config.isClockwise ? 1 : -1);
					if (i < 0) {
						i += cwRot.size();
					} else if (i >= cwRot.size()) {
						i = 0;
					}
					s.setRotation(cwRot.get(i));
					s.update();
				} else {
					signs.remove(l);
					save();
				}
			}
		}
	}
	List<BlockFace> cwRot = Arrays.asList(
			BlockFace.NORTH, BlockFace.NORTH_NORTH_EAST, BlockFace.NORTH_EAST, BlockFace.EAST_NORTH_EAST,
			BlockFace.EAST, BlockFace.EAST_SOUTH_EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_SOUTH_EAST,
			BlockFace.SOUTH, BlockFace.SOUTH_SOUTH_WEST, BlockFace.SOUTH_WEST, BlockFace.WEST_SOUTH_WEST,
			BlockFace.WEST, BlockFace.WEST_NORTH_WEST, BlockFace.NORTH_WEST, BlockFace.NORTH_NORTH_WEST);

	public class AddSignListener implements Listener {

		ArrayList<Player> waiting = new ArrayList<Player>();

		/**
		 *
		 * @param player
		 * @return true if is in the wait queue
		 */
		public boolean toggleWait(Player player) {
			if (waiting.contains(player)) {
				waiting.remove(player);
				return false;
			} else {
				waiting.add(player);
				return true;
			}
		}

		@EventHandler(priority = EventPriority.LOWEST)
		public void onPlayerInteract(PlayerInteractEvent event) {
			if (waiting.contains(event.getPlayer())
					&& (event.getAction() == Action.LEFT_CLICK_BLOCK
					|| event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				//waiting.remove(event.getPlayer());
				if (event.getClickedBlock().getType() == Material.SIGN_POST
						|| event.getClickedBlock().getType() == Material.SKULL) {
					if (signs.contains(event.getClickedBlock().getLocation())) {
						signs.remove(event.getClickedBlock().getLocation());
						event.getPlayer().sendMessage(SignRotatePlugin.name + "Block removed from rotater");
					} else {
						signs.add(event.getClickedBlock().getLocation());
						event.getPlayer().sendMessage(SignRotatePlugin.name + "Block added to rotater");
					}
					save();
				} else {
					event.getPlayer().sendMessage(SignRotatePlugin.name + ChatColor.RED + "This is not a supported block");
				}
				event.setCancelled(true);
			}
		}
	}
} // end class Rotater

