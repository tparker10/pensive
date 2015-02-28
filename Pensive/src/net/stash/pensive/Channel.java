package net.stash.pensive;

import gov.usgs.plot.data.SliceWave;
import gov.usgs.plot.data.Wave;
import gov.usgs.plot.render.Renderer;
import gov.usgs.swarm.data.SeismicDataSource;
import gov.usgs.util.ConfigFile;
import gov.usgs.util.Log;
import gov.usgs.util.Util;

import java.awt.Dimension;
import java.util.logging.Logger;

import net.stash.pensive.plot.AbstractChannelPlot;
import net.stash.pensive.plot.ChannelThumbnail;
import net.stash.pensive.plot.FullChannelPlot;

/**
 * A single channel of seismic data on a single subnet plot.
 * 
 * @author Tom Parker
 * 
 */
public class Channel {

    /** my logger */
    private static final Logger LOGGER = Log.getLogger("gov.usgs");

    /** channel name in config file format */
    public final String name;

    private final AbstractChannelPlot plot;
    private final AbstractChannelPlot thumb;

    /**
     * Class constructor
     * 
     * @param my
     *            channel in config file format
     * @param plot
     *            height
     * @param plot
     *            width
     * @param my
     *            config file
     */
    public Channel(String channel, int index, Dimension plotDimension, Dimension thumbDimension, boolean decorateX,
            ConfigFile config) {
        this.name = channel;

        plot = new FullChannelPlot(channel, index, plotDimension, decorateX, config);
        thumb = new ChannelThumbnail(channel, index, thumbDimension, config);

    }

    /**
     * 
     * @param plotEnd
     * @param dataSource
     */
    public void updateWave(long plotEnd, SeismicDataSource dataSource) {
        double t2 = Util.ewToJ2K(plotEnd / 1000);
        double t1 = t2 - Subnet.DURATION_S;
        Wave w = dataSource.getWave(name, t1, t2);
        SliceWave wave = null;
        if (w != null) {
            wave = new SliceWave(w);
            wave.setSlice(t1, t2);
        }
        plot.setWave(wave);
        thumb.setWave(wave);
    }

    public Renderer plot() {
        return plot.plot();
    }

    public Renderer plotThumb() {
        return thumb.plot();
    }

}