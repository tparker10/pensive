package net.stash.pensive;

import gov.usgs.swarm.data.DataSourceType;
import gov.usgs.swarm.data.SeismicDataSource;
import gov.usgs.util.ConfigFile;
import gov.usgs.util.Log;
import gov.usgs.util.Util;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Tom Parker
 * 
 */
public class Plotter implements Runnable {
    private static final Logger LOGGER = Log.getLogger("gov.usgs");

    public static final String DEFAULT_TYPE = "wws";
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 16022;
    public static final int DEFAULT_TIMEOUT_S = 15;
    public static final String DEFAULT_PATH_ROOT = "html";
    
    private final SeismicDataSource dataSource;
    private BlockingQueue<PlotJob> plotJobs;
    private String pathRoot;
    public final String name;

    public Plotter(String name, BlockingQueue<PlotJob> plotJobs, ConfigFile config) {
        this.plotJobs = plotJobs;
        
        this.name = name;
        String type = Util.stringToString(config.getString("type"), DEFAULT_TYPE);
        String host = Util.stringToString(config.getString("host"), DEFAULT_HOST);
        int port = Util.stringToInt(config.getString("port"), DEFAULT_PORT);
        int timeout = Util.stringToInt(config.getString("timeout"), DEFAULT_TIMEOUT_S);
        int compress = 1;
        
        pathRoot = Util.stringToString(config.getString("pathRoot"), DEFAULT_PATH_ROOT);
        
        String dsString = name + ";" + type + ":" + host + ":" + port + ":" + timeout + ":" + compress; 
        
        dataSource = DataSourceType.parseConfig(dsString);
        dataSource.setUseCache(false);
        dataSource.setName(config.getString("waveSource"));
    }

    /**
     * 
     */
    @Override
    public void run() {
        while (true) {
            PlotJob pj = null;
            try {
                pj = plotJobs.take();
            } catch (InterruptedException noAction) {
            	continue;
            }
            
            LOGGER.log(Level.FINE, "Ploting " + pj.subnet.subnetName + " from " + name);
            
        }
    }
}
