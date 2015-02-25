package net.stash.pensive;

import gov.usgs.plot.Plot;
import gov.usgs.plot.data.SliceWave;
import gov.usgs.plot.render.FrameRenderer;
import gov.usgs.util.ConfigFile;
import gov.usgs.util.Log;
import gov.usgs.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Subnet {
    private static final Logger LOGGER = Log.getLogger("net.stash.pensive");

    public static final int DURATION_S = 10 * 60;
    
    public static final int WIDTH = 576;
    public static final int HEIGHT = 756;
    public static final int LABEL_HEIGHT = 35;
    public static final int LABEL_WIDTH = 30;
    public static final int EMBARGO = 0;

    private List<Channel> channels;
    public final String networkName;
    public final String subnetName;
    public final int plotWidth;
    public final int plotHeight;
    public final int labelHeight;
    public final int embargo;

    private long plotEnd;

    public Subnet(String networkName, String subnetName, ConfigFile config) {
        this.subnetName = subnetName;
        this.networkName = networkName;
        plotWidth = Util.stringToInt(config.getString("width"), WIDTH);
        plotHeight = Util.stringToInt(config.getString("height"), HEIGHT);
        embargo = Util.stringToInt(config.getString("embargo"), EMBARGO);

        labelHeight = LABEL_HEIGHT;

        channels = createChannels(config);
    }

    private List<Channel> createChannels(ConfigFile config) {

        channels = new ArrayList<Channel>();
        for (String channel : config.getList("channel")) {
            ConfigFile c = config.getSubConfig(channel, true);
            Channel chan = new Channel(channel, c);
            channels.add(chan);
        }

        return channels;
    }

    public void writePlots(SliceWave wave, double startTime) {

        for (Channel c : channels) {
            c.setWave(wave);
        }
        writePlot();
        writeThumbnail();
    }

    private Plot writePlot() {
        Plot plot = new Plot(plotWidth, plotHeight);

        int channelHeight = (plotHeight - (labelHeight * 2)) / channels.size();

        int i = 0;
        for (Channel c : channels) {
            // renderer adds a 1 pixel border, making images larger than
            // requested
            int top = (i * channelHeight) + labelHeight - 1 + i;

            boolean decorateX = i == channels.size() ? true : false;

//            FrameRenderer fr = c.getChannelRenderer(channelHeight, plotWidth, decorateX);

            // skip 1 pixel border
//            fr.setLocation(0, top, plotWidth, plotHeight);
//            plot.addRenderer(fr);
        }

        return plot;
    }

    private Plot writeThumbnail() {
        Plot plot = new Plot(plotWidth, plotHeight);

        int channelHeight = (plotHeight - (labelHeight * 2)) / channels.size();

        int i = 0;
        for (Channel c : channels) {
            // renderer adds a 1 pixel border, making images larger than
            // requested
            int top = (i * channelHeight) + labelHeight - 1 + i;

            boolean decorateX = i == channels.size() ? true : false;

//            FrameRenderer fr = c.getChannelRenderer(channelHeight, plotWidth, decorateX);

            // skip 1 pixel border
//            fr.setLocation(0, top, plotWidth, plotHeight);
//            plot.addRenderer(fr);
        }

        return plot;
    }

    public long getPlotEnd() {
        return plotEnd;
    }
    
    public void setPlotEnd(long l) {
        plotEnd = l;
    }
}