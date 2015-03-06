package net.stash.pensive;

import gov.usgs.swarm.data.DataSourceType;
import gov.usgs.swarm.data.SeismicDataSource;
import gov.usgs.util.ConfigFile;
import gov.usgs.util.Log;
import gov.usgs.util.Util;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.stash.pensive.plot.SubnetPlotter;

/**
 * Retrieve data and produce plot
 * 
 * @author Tom Parker
 * 
 */
public class WaveSource implements Runnable {

	/** my logger */
	private static final Logger LOGGER = Log.getLogger("gov.usgs");

	public static final String DEFAULT_TYPE = "wws";
	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 16022;
	public static final int DEFAULT_TIMEOUT_S = 15;
	public static final String DEFAULT_PATH_ROOT = "html";

	/** source of wave data */
	private final SeismicDataSource dataSource;

	/** Jobs to be plotted */
	private BlockingQueue<PlotJob> plotJobs;

	/** my name */
	public final String name;

	/**
	 * Class constructor
	 * 
	 * @param name
	 *            My name
	 * @param plotJobs
	 *            Queue containing jobs to plot
	 * @param config
	 *            My config stanza
	 */
	public WaveSource(String name, BlockingQueue<PlotJob> plotJobs, ConfigFile config) {
		this.plotJobs = plotJobs;

		this.name = name;
		String type = Util.stringToString(config.getString("type"), DEFAULT_TYPE);
		String host = Util.stringToString(config.getString("host"), DEFAULT_HOST);
		int port = Util.stringToInt(config.getString("port"), DEFAULT_PORT);
		int timeout = Util.stringToInt(config.getString("timeout"), DEFAULT_TIMEOUT_S);
		int compress = 1;

		String dsString = name + ";" + type + ":" + host + ":" + port + ":" + timeout * 1000 + ":" + compress;
		dataSource = DataSourceType.parseConfig(dsString);
		dataSource.establish();
		dataSource.setUseCache(false);
	}

	/**
	 * Take plot jobs and produce files.
	 */
	@Override
	public void run() {
		while (true) {
			PlotJob pj = null;
			try {
				pj = plotJobs.take();
				SubnetPlotter subnet = pj.subnet;

				LOGGER.log(Level.FINE, "Ploting " + subnet.subnetName + " from " + name);
				subnet.plot(pj.plotEnd, dataSource);
			} catch (InterruptedException noAction) {
				continue;
			}
		}
	}
}