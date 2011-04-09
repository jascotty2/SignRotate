/**
 * Programmer: Jacob Scott
 * Program Name: SRConfig
 * Description:
 * Date: Apr 8, 2011
 */
package com.jascotty2.signrotate;

import java.io.File;
import java.util.logging.Level;
import org.bukkit.util.config.Configuration;

/**
 * @author jacob
 */
public class SRConfig {

    public final static File pluginFolder = new File("plugins", SignRotate.name);
    public final static File configfile = new File(pluginFolder, "config.yml");
    public final static File signDBFile = new File(pluginFolder, "signs.dat");
    public final static long MIN_WAIT = 100;
    public static long saveWait = 30000;
    public long rotateWait = 700;
    public boolean isClockwise = false;
    Configuration config = null;

    public SRConfig(Configuration config) {
        pluginFolder.mkdirs();
        this.config = config;
        if (config != null) {
            try {
                config.load();
                if (config.getProperty("rotateDelay") != null) {
                    rotateWait = config.getInt("rotateDelay", (int) rotateWait);
                }

                if (config.getProperty("rotateCW") != null) {
                    isClockwise = config.getBoolean("rotateCW", isClockwise);
                }
                save();
            } catch (Exception ex) {
                SignRotate.Log(Level.WARNING, "Failed to load config", ex);
            }
        }
    }

    public final void save() {
        if (config != null) {
            config.setProperty("rotateDelay", rotateWait);
            config.setProperty("rotateCW", isClockwise);
            config.save();
        }
    }
} // end class SRConfig

