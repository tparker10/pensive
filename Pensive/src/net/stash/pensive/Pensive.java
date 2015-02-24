package net.stash.pensive;

import gov.usgs.util.ConfigFile;
import gov.usgs.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * 
 * @author Tom Parker
 * 
 */

public class Pensive {

    private static final Logger LOGGER = Log.getLogger("gov.usgs.pensive");
    private static final int PLOT_DURRATION_S = 10 * 60;

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
        configFile.put("applicationLaunch", "" + System.currentTimeMillis());
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
            System.out.println("Creating plot scheduler for " + server);
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
                System.out.println("No subnet directives for network " + network + " found. Skipping.");
                return;
            }
            
            for (String subnet : subnets) {
                ConfigFile subnetConfig = netConfig.getSubConfig(subnet, true);

                String dataSource = subnetConfig.getString("dataSource");
                PlotScheduler scheduler = plotScheduler.get(dataSource);
                System.out.println("Assigning subnet " + subnet + " to " + dataSource);
                scheduler.add(new Subnet(subnet, subnetConfig));
            }
        }
    }

    /**
     * 
     */
    private void schedulePlots() {
        for (PlotScheduler ps : plotScheduler.values()) {
            scheduler.scheduleAtFixedRate(ps, 0, PLOT_DURRATION_S, TimeUnit.SECONDS);
        }
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {

        // create the package-level logger so inheritance works
        Log.getLogger("gov.usgs.pensive");
        LogManager.getLogManager().getLogger("gov.usgs.pensive").setLevel(Level.FINEST);

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