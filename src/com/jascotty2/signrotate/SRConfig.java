package com.jascotty2.signrotate;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class SRConfig {

    protected File signDBFile;
    public final static long MIN_WAIT = 100;
    public static long saveWait = 30000;
    public long rotateWait = 700;
    public boolean isClockwise = false;
	protected final Plugin plugin;

    public SRConfig(Plugin pl) {
		this.plugin = pl;
		
    }
	
    public final void load() {
		signDBFile = new File(plugin.getDataFolder(), "signs.dat");
		
		plugin.reloadConfig();
		FileConfiguration conf = plugin.getConfig();
		
		rotateWait = conf.getLong("rotateDelay", rotateWait);
		isClockwise = conf.getBoolean("rotateCW", isClockwise);
	}

    public final void save() throws IOException {
		FileConfiguration conf = plugin.getConfig();
		
		conf.set("rotateDelay", rotateWait);
		conf.set("rotateCW", isClockwise);
		
		conf.save(new File(plugin.getDataFolder(), "config.yml"));
    }
} // end class SRConfig

