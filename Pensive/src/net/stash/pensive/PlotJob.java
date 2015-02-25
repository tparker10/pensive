package net.stash.pensive;

import gov.usgs.util.Log;

import java.util.logging.Logger;

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
    public final Subnet subnet;

    
    /**
     * Class constructor
     * 
     * @param Time of last sample to me plotted
     * @param My subnet
     */
    public PlotJob (long plotEnd, Subnet subnet) {
        this.plotEnd = plotEnd;
        this.subnet = subnet;
    }
    
    /**
     * Class constructor which uses the most recent time 
     * slice as the time of the last sample to be plotted.
     * 
     * @param my subnet
     */
    public PlotJob (Subnet subnet) {
        this.subnet = subnet;
        this.plotEnd = generatePlotEnd();
    }

    
    /**
     * Calculate the time of the last sample in the most 
     * recent time slice. 
     * 
     * @return Time of the last sample in the most recent time slice.
     */
    private long generatePlotEnd() {
        long startTime = System.currentTimeMillis();
        startTime -= subnet.embargo * 60 * 1000;

        startTime -= startTime % (Subnet.DURATION_S * 1000);
        
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
