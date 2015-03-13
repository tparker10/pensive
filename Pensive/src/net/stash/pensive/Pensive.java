package net.stash.pensive;

import gov.usgs.util.ConfigFile;
import gov.usgs.util.Log;
import gov.usgs.util.Util;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import net.stash.pensive.page.Page;
import net.stash.pensive.plot.SubnetPlotter;

/**
 * An application to produce a continous collection of subnet spectrograms.
 * 
 * @author Tom Parker
 * 
 */
public class Pensive {

    public static final boolean DEFAULT_WRITE_HTML = true;
    /** my logger */
    private static final Logger LOGGER = Log.getLogger("gov.usgs");

    /** my configuration file */
    private ConfigFile configFile;

    private Page page;

    /** One plot scheduler per wave server */
	private Map<String, PlotScheduler> plotScheduler;

	/**
	 * Class constructor
	 * 
	 * @param configFile
	 *            my config file
	 */
	public Pensive(ConfigFile configFile) {

        this.configFile = configFile;
        long now = System.currentTimeMillis();
        configFile.put("applicationLaunch", "" + now);
        LOGGER.log(Level.INFO, "Launching Pensive at " + (new Date(now)));
        Logger.getLogger("global").setLevel(Level.OFF);

        page = new Page(configFile);

        createPlotSchedulers();
        assignSubnets();
        pruneSchedulers();

        boolean writeHtml = Util.stringToBoolean(configFile.getString("writeHtml"), DEFAULT_WRITE_HTML);
        if (writeHtml)
            page.writeHTML();
    }

    /**
     * Create one PlotScheduler per wave server, each running in its own thread.
     */
    private void createPlotSchedulers() {
        plotScheduler = new HashMap<String, PlotScheduler>();

        for (String server : configFile.getList("waveSource")) {
            ConfigFile c = configFile.getSubConfig(server, true);
            LOGGER.log(Level.INFO, "Creating plot scheduler for " + server);
            plotScheduler.put(server, new PlotScheduler(server, c));
        }
    }
    

    /**
     * Assign subnets to it wave server
     */
    private void assignSubnets() {
        List<String> networks = configFile.getList("network");
        if (networks == null)
            throw new RuntimeException("No network directives found.");

        Iterator<String> networkIt = networks.iterator();
        while (networkIt.hasNext()) {
            String network = networkIt.next();
            ConfigFile netConfig = configFile.getSubConfig(network, true);
            List<String> subnets = netConfig.getList("subnet");
            if (subnets == null) {
                LOGGER.log(Level.WARNING, "No subnet directives for network " + network + " found. Skipping.");
                networkIt.remove();
                continue;
            }
            
            Iterator<String> subnetIt = subnets.iterator();
            while (subnetIt.hasNext()) {
                String subnet = subnetIt.next();
                ConfigFile subnetConfig = netConfig.getSubConfig(subnet, true);
                if (subnetConfig.getList("channel") == null) {
                    LOGGER.log(Level.WARNING, "No channel directives for subnet " + subnet + " found. Skipping.");
                    subnetIt.remove();
                    continue;
                } else {
                    page.addSubnet(network, subnet);
                }

                String dataSource = subnetConfig.getString("dataSource");
                PlotScheduler scheduler = plotScheduler.get(dataSource);
                LOGGER.log(Level.INFO, "Assigning subnet " + subnet + " to " + dataSource);
                scheduler.add(new SubnetPlotter(network, subnet, subnetConfig));
            }
            netConfig.putList("subnet", subnets);
        }
        configFile.putList("network", networks);

        Iterator<String> schedulerIt = plotScheduler.keySet().iterator();
        while (schedulerIt.hasNext()) {
            String server = schedulerIt.next();
            PlotScheduler ps = plotScheduler.get(server);
            if (ps.subnetCount() < 1) {
                LOGGER.log(Level.WARNING, "No subnets feeding from " + ps.name + ". I'll prune it.");
                schedulerIt.remove();
            }
        }
    }

    /**
     * Prune PlotSchedulers that have no subnets assigned to them.
     */
    private void pruneSchedulers() {
        Iterator<String> schedulerIt = plotScheduler.keySet().iterator();
        while (schedulerIt.hasNext()) {
            String server = schedulerIt.next();
            PlotScheduler ps = plotScheduler.get(server);
            if (ps.subnetCount() < 1) {
                LOGGER.log(Level.WARNING, "No subnets feeding from " + ps.name + ". I'll prune it.");
                schedulerIt.remove();
            }
        }

    }

    /**
     * Schedule a recurring call to produce plot jobs.
     */
    private void schedulePlots() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        for (PlotScheduler ps : plotScheduler.values()) {
            
        	// schedule first plot immediately
            new Thread(ps).start();

            // satrt automated plots at the top of the next period
            int delay = SubnetPlotter.DURATION_S; 
            delay -= (System.currentTimeMillis() / 1000) % SubnetPlotter.DURATION_S;
            LOGGER.fine("delay: " + delay);
            scheduler.scheduleAtFixedRate(ps, delay, SubnetPlotter.DURATION_S, TimeUnit.SECONDS);
        }
    }

    /**
     * Where it all begins
     * 
     * @param args
     */
    public static void main(String[] args) {

        // create the package-level logger so inheritance works
        Log.getLogger("gov.usgs");
        LogManager.getLogManager().getLogger("gov.usgs").setLevel(Level.FINEST);

        Log.attachFileLogger(LOGGER, "pensiveLog", 100000, 10, true);
        LOGGER.setLevel(Level.INFO);
        LOGGER.finest("starting Pensive");

        ConfigFile cf = new ConfigFile("pensive.config");
        if (!cf.wasSuccessfullyRead())
            throw new RuntimeException("Error reading config file " + cf);

        if (cf.getList("debug") != null) {
            for (String name : cf.getList("debug")) {
                Logger l = Log.getLogger(name);
                l.setLevel(Level.ALL);
                LOGGER.fine("debugging " + name);
            }
        }

        Pensive pensive = new Pensive(cf);
        pensive.schedulePlots();
    }
}
