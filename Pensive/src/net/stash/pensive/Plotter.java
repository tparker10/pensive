package net.stash.pensive;

import gov.usgs.swarm.data.DataSourceType;
import gov.usgs.swarm.data.SeismicDataSource;
import gov.usgs.util.ConfigFile;

import java.util.concurrent.BlockingQueue;

/**
 * 
 * @author Tom Parker
 * 
 */
public class Plotter implements Runnable {

    private final SeismicDataSource dataSource;
    private BlockingQueue<PlotJob> plotJobs;

    public Plotter(BlockingQueue<PlotJob> plotJobs, ConfigFile config) {
        this.plotJobs = plotJobs;
        dataSource = DataSourceType.parseConfig(config.getString("dataSource"));
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
            System.out.println("ploting " + pj.plotEnd + " from " + dataSource);
        }
    }
}
