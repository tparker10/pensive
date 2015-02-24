package net.stash.pensive;

public class PlotJob implements Comparable<PlotJob> {
    public final long plotEnd;
    public final Subnet subnet;

    public PlotJob (long plotEnd, Subnet subnet) {
        this.plotEnd = plotEnd;
        this.subnet = subnet;
    }
    
    public PlotJob (Subnet subnet) {
        this.subnet = subnet;
        this.plotEnd = generatePlotEnd();
    }

    private long generatePlotEnd() {
        long startTime = System.currentTimeMillis();
        startTime -= subnet.embargo * 60 * 1000;

        startTime -= startTime % (10 * 60 * 1000);
        
        return startTime;
    }

    public long getPlotEnd() {
        return plotEnd;
    }
    
    @Override
    public int compareTo(PlotJob o) {
        return (int) (plotEnd - o.plotEnd);
    }
}
