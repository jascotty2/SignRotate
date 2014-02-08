package com.jascotty2.signrotate;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class SRConfig {

    protected File signDBFile;
    public final static int MIN_WAIT = 2;
    public int rotateWait = 14;
    public boolean isClockwise = false;
	protected final Plugin plugin;

    public SRConfig(Plugin pl) {
		this.plugin = pl;
		
    }
	
    public final void load() {
		signDBFile = new File(plugin.getDataFolder(), "signs.dat");
		
		plugin.reloadConfig();
		FileConfiguration conf = plugin.getConfig();
		
		Object rotateDelay = conf.get("rotateDelay");
		rotateWait = conf.getInt("rotateTick", rotateDelay instanceof Number ? (int)(((Number)rotateDelay).longValue() * (20/1000.)) : rotateWait);
		isClockwise = conf.getBoolean("rotateCW", isClockwise);
	}

    public final void save() throws IOException {
		FileConfiguration conf = plugin.getConfig();
		
		if(conf.get("rotateDelay") != null) {
			conf.set("rotateDelay", null);
		}
		
		conf.set("rotateDelay", rotateWait);
		conf.set("rotateCW", isClockwise);
		
		conf.save(new File(plugin.getDataFolder(), "config.yml"));
    }
} // end class SRConfig

