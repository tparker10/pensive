package net.stash.pensive;

import gov.usgs.util.ConfigFile;
import gov.usgs.util.Log;
import gov.usgs.util.Util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

/**
 * An application to produce a continuous collection of subnet spectrograms.
 * 
 * @author Tom Parker
 * 
 *         I waive copyright and related rights in the this work worldwide
 *         through the CC0 1.0 Universal public domain dedication.
 *         https://creativecommons.org/publicdomain/zero/1.0/legalcode
 */
public class Pensive {


	public static final boolean DEFAULT_WRITE_HTML = true;
	public static final String DEFAULT_CONFIG_FILENAME = "pensive.config";

	/** my logger */
	private static final Logger LOGGER = Log.getLogger("gov.usgs");

	/** my configuration file */
	private ConfigFile configFile;

	/** My SPA */
	private Page page;

	/** One plot scheduler per wave server */
	private Map<String, PlotScheduler> plotScheduler;

	// JSAP related stuff.
	public static final String JSAP_PROGRAM_NAME = "java -jar net.stash.pensive.Pensive";
	public static final String JSAP_EXPLANATION_PREFACE = "I am the Pensive server";

	private static final String DEFAULT_JSAP_EXPLANATION = "\n";

	private static final Parameter[] DEFAULT_JSAP_PARAMETERS = new Parameter[] {
			new Switch("create-config", 'c', "create-config",
					"Create an example config file in the curent working directory."),
			new Switch("verbose", 'v', "verbose", "Verbose logging."),
			new UnflaggedOption("configFilename", JSAP.STRING_PARSER, DEFAULT_CONFIG_FILENAME, JSAP.NOT_REQUIRED,
					JSAP.NOT_GREEDY, "The config file name.") };

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

	public static JSAPResult getArguments(String[] args) {
		JSAPResult config = null;
		try {
			SimpleJSAP jsap = new SimpleJSAP(JSAP_PROGRAM_NAME, JSAP_EXPLANATION_PREFACE + DEFAULT_JSAP_EXPLANATION,
					DEFAULT_JSAP_PARAMETERS);


			config = jsap.parse(args);
			if (jsap.messagePrinted()) {
				// The following error message is useful for catching the case
				// when args are missing, but help isn't printed.
				if (!config.getBoolean("help"))
					System.err.println("Try using the --help flag.");

				System.exit(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		return config;
	}

	public static void createConfig() throws IOException {

		InputStream is = Pensive.class.getResourceAsStream("pensive.config");
		FileOutputStream os = new FileOutputStream(DEFAULT_CONFIG_FILENAME);
		try {
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
		}
	}

	/**
	 * Where it all begins
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		JSAPResult config = getArguments(args);

		// create the package-level logger so inheritance works
		Log.getLogger("gov.usgs");

		Logger logger = LogManager.getLogManager().getLogger("gov.usgs");
		if (config.getBoolean("verbose"))
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.FINE);

		Log.attachFileLogger(LOGGER, "pensiveLog", 100000, 10, true);
		LOGGER.setLevel(Level.INFO);

		if (config.getBoolean("create-config")) {
			try {
				LOGGER.warning("Creating example config " + DEFAULT_CONFIG_FILENAME);
				Pensive.createConfig();
			} catch (IOException e) {
				LOGGER.warning("Cannot write example config. " + e.getLocalizedMessage());
			}
			System.exit(0);
		}
		
		String fn = config.getString("configFilename");
		ConfigFile cf = new ConfigFile(fn);
		if (!cf.wasSuccessfullyRead()) {
			LOGGER.warning("Can't parse config file " + fn + ". Try using the --help flag.");
			System.exit(1);
		}

		LOGGER.finest("starting Pensive");

		Pensive pensive = new Pensive(cf);
		pensive.schedulePlots();
	}
}
