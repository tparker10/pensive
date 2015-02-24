package net.stash.pensive;

import gov.usgs.util.ConfigFile;
import gov.usgs.util.Pool;
import gov.usgs.util.Util;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 
 * @author Tom Parker
 * 
 */
public class PlotScheduler implements Runnable {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 16022;
    public static final int DEFAULT_NUMTHREADS = 5;

    private Pool<Plotter> plotter;
    public final String host;
    public final int port;
    public final int numThreads;
    private BlockingQueue<PlotJob> plotJobs;
    private final List<Subnet> subnets;

    /**
     * 
     * @param host
     * @param port
     * @param numThreads
     */
    public PlotScheduler(ConfigFile config) {
        this.host = Util.stringToString(config.getString("host"), DEFAULT_HOST);
        this.port = Util.stringToInt(config.getString("port"), DEFAULT_PORT);
        this.numThreads = Util.stringToInt(config.getString("numThreads"), DEFAULT_NUMTHREADS);

        subnets = new LinkedList<Subnet>();
        plotJobs = new LinkedBlockingQueue<PlotJob>();

        plotter = new Pool<Plotter>();
        for (int i = 0; i < numThreads; i++)
            plotter.checkin(new Plotter(plotJobs, config));
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
                plotJobs.put(new PlotJob(subnet));
            } catch (InterruptedException e) {
                System.out.println("Interrupted. Skipping " + subnet);
            }
        }
    }

    @Override
    public void run() {
        schedulePlots();
    }
}