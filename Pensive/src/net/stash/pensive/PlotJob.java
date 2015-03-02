package net.stash.pensive;

import gov.usgs.util.Log;

import java.util.logging.Logger;

import net.stash.pensive.plot.SubnetPlotter;

/**
 * A single plot job
 * 
 * @author Tom Parker
 *
 */
public class PlotJob implements Comparable<PlotJob> {

	/** my logger */
	private static final Logger LOGGER = Log.getLogger("gov.usgs");

	/** Time of last sample plotted */
	public final long plotEnd;

	/** my subnet */
	public final SubnetPlotter subnet;

	/**
	 * Class constructor
	 * 
	 * @param plotEnd
	 *            Time of last sample to me plotted
	 * @param subnet
	 *            My subnet
	 */
	public PlotJob(long plotEnd, SubnetPlotter subnet) {
		this.plotEnd = plotEnd;
		this.subnet = subnet;
	}

	/**
	 * Class constructor which uses the most recent time slice as the time of
	 * the last sample to be plotted.
	 * 
	 * @param subnet
	 *            my subnet
	 */
	public PlotJob(SubnetPlotter subnet) {
		this.subnet = subnet;
		this.plotEnd = findPlotEnd();
	}

	/**
	 * Calculate the time of the last sample in the most recent time slice.
	 * 
	 * @return Time of the last sample in the most recent time slice.
	 */
	private long findPlotEnd() {
		long startTime = System.currentTimeMillis();
		startTime -= subnet.embargoS * 60 * 1000;

		startTime -= startTime % (SubnetPlotter.DURATION_S * 1000);

		return startTime;
	}

	/**
	 * Order plots by increasing last sample time
	 */
	@Override
	public int compareTo(PlotJob o) {
		return (int) (plotEnd - o.plotEnd);
	}
}
