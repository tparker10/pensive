package net.stash.pensive;

import gov.usgs.util.ConfigFile;
import gov.usgs.util.Log;
import gov.usgs.util.Pool;
import gov.usgs.util.Util;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Tom Parker
 * 
 */
public class PlotScheduler implements Runnable {
    private static final Logger LOGGER = Log.getLogger("net.stash.pensive");

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 16022;
    public static final int DEFAULT_NUMTHREADS = 5;

    private final Pool<Plotter> plotter;
    private final BlockingQueue<PlotJob> plotJobs;
    private final List<Subnet> subnets;
    private final int numThreads;

    /**
     * 
     * @param host
     * @param port
     * @param numThreads
     */
    public PlotScheduler(ConfigFile config) {
        
        numThreads = Util.stringToInt(config.getString("numThreads"), DEFAULT_NUMTHREADS);
        subnets = new LinkedList<Subnet>();
        plotJobs = new LinkedBlockingQueue<PlotJob>();

        plotter = new Pool<Plotter>();
        for (int i = 0; i < numThreads; i++) {
            Plotter p = new Plotter(plotJobs, config);
            plotter.checkin(p);
            Thread t = new Thread(p);
            t.start(); 
        }
    }

    
    /**
     * 
     * @param plotSettings
     */
    public void add(Subnet subnet) {
        subnets.add(subnet);
    }
    

    /**
     * @throws InterruptedException
     * 
     */
    public void schedulePlots() {
        for (Subnet subnet : subnets) {
            try {
                LOGGER.log(Level.FINE, "Scheduling " + subnet.subnetName);
                plotJobs.put(new PlotJob(subnet));
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, "Interrupted. Unable to schedule " + subnet.subnetName);
            }
        }
    }

    @Override
    public void run() {
        LOGGER.log(Level.FINE, "scheduling plots");
        schedulePlots();
    }
}