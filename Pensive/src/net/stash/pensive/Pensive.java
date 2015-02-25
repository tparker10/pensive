package net.stash.pensive;

import gov.usgs.util.ConfigFile;
import gov.usgs.util.Log;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 * An application to produce a continous collection of subnet spectrograms.
 * 
 * @author Tom Parker
 * 
 */

public class Pensive {

    private static final Logger LOGGER = Log.getLogger("net.stash.pensive");

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private ConfigFile configFile;
    private Map<String, PlotScheduler> plotScheduler;

    /**
     * Class constructor
     * 
     * @param configFile
     */
    public Pensive(ConfigFile configFile) {

        this.configFile = configFile;
        long now = System.currentTimeMillis();
        configFile.put("applicationLaunch", "" + now);
        LOGGER.log(Level.INFO, "Launching Pensive at " + (new Date(now)));
        Logger.getLogger("global").setLevel(Level.OFF);

        createPlotSchedulers();
        assignSubnets();
    }

    /**
	 * 
	 */
    private void createPlotSchedulers() {
        plotScheduler = new HashMap<String, PlotScheduler>();

        for (String server : configFile.getList("waveSource")) {
            ConfigFile c = configFile.getSubConfig(server, true);
            LOGGER.log(Level.INFO, "Creating plot scheduler for " + server);
            plotScheduler.put(server, new PlotScheduler(c));
        }
    }

    /**
     * 
     */
    private void assignSubnets() {
        List<String> networks = configFile.getList("network");
        if (networks == null)
            throw new RuntimeException("No network directives found.");
        
        for (String network : networks) {
            ConfigFile netConfig = configFile.getSubConfig(network, true);
            List<String> subnets = netConfig.getList("subnet");
            if (subnets == null) {
                LOGGER.log(Level.WARNING, "No subnet directives for network " + network + " found. Skipping.");
                return;
            }
            
            for (String subnet : subnets) {
                ConfigFile subnetConfig = netConfig.getSubConfig(subnet, true);

                String dataSource = subnetConfig.getString("dataSource");
                PlotScheduler scheduler = plotScheduler.get(dataSource);
                LOGGER.log(Level.INFO, "Assigning subnet " + subnet + " to " + dataSource);
                scheduler.add(new Subnet(network, subnet, subnetConfig));
            }
        }
    }

    /**
     * 
     */
    private void schedulePlots() {
        for (PlotScheduler ps : plotScheduler.values()) {
            scheduler.scheduleAtFixedRate(ps, 0, Subnet.DURATION_S, TimeUnit.SECONDS);
        }
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {

        // create the package-level logger so inheritance works
        Log.getLogger("gov.usgs.pensive");
        LogManager.getLogManager().getLogger("net.stash.pensive").setLevel(Level.FINEST);

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
    
	/**
	 * Is this worth the trouble? Why not just always call the replaceAll()?
	 * 
	 * @param path
	 *            filesystem path to convert
	 * @return The path suitable for use in a URL
	 */
	public static String pathToPath(String path) {
		if (File.separator.equals("/"))
			return path;
		else
			return path.replaceAll("/", Matcher.quoteReplacement(File.separator));
	}

}