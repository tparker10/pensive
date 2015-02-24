package net.stash.pensive;

import gov.usgs.swarm.data.DataSourceType;
import gov.usgs.swarm.data.SeismicDataSource;
import gov.usgs.util.ConfigFile;
import gov.usgs.util.Util;

import java.util.concurrent.BlockingQueue;

/**
 * 
 * @author Tom Parker
 * 
 */
public class Plotter implements Runnable {
    public static final String DEFAULT_TYPE = "wws";
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 16022;
    public static final int DEFAULT_TIMEOUT_S = 15;
    
    private final SeismicDataSource dataSource;
    private BlockingQueue<PlotJob> plotJobs;

    public Plotter(BlockingQueue<PlotJob> plotJobs, ConfigFile config) {
        this.plotJobs = plotJobs;
        
        String name = "ds1";
        String type = Util.stringToString(config.getString("type"), DEFAULT_TYPE);
        String host = Util.stringToString(config.getString("host"), DEFAULT_HOST);
        int port = Util.stringToInt(config.getString("port"), DEFAULT_PORT);
        int timeout = Util.stringToInt(config.getString("timeout"), DEFAULT_TIMEOUT_S);
        int compress = 1;
        
        String dsString = name + ";" + type + ":" + host + ":" + port + ":" + timeout + ":" + compress; 
        
        dataSource = DataSourceType.parseConfig(dsString);
        dataSource.setUseCache(false);
    }

    @Override
    public void run() {
        while (true) {
            PlotJob pj = null;
            try {
                pj = plotJobs.take();
            } catch (InterruptedException noAction) {
            }
            System.out.println("ploting " + pj.subnet.name + " from " + dataSource);
        }
    }
}
