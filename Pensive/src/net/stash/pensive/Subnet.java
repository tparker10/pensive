package net.stash.pensive;

import gov.usgs.plot.Plot;
import gov.usgs.plot.data.SliceWave;
import gov.usgs.plot.render.BasicFrameRenderer;
import gov.usgs.plot.render.FrameRenderer;
import gov.usgs.plot.render.TextRenderer;
import gov.usgs.plot.render.wave.SliceWaveRenderer;
import gov.usgs.plot.render.wave.SpectrogramRenderer;
import gov.usgs.swarm.data.SeismicDataSource;
import gov.usgs.util.ConfigFile;
import gov.usgs.util.Log;
import gov.usgs.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A single subnet.
 * 
 * @author Tom Parker
 *
 */
public class Subnet {
	
	/** my logger */
    private static final Logger LOGGER = Log.getLogger("gov.usgs");

    public static final int DEFAULT_WIDTH = 576;
    public static final int DEFAULT_HEIGHT = 756;
    public static final int DEFAULT_EMBARGO = 0;
    
    public static final int LABEL_HEIGHT = 35;
    public static final int LABEL_WIDTH = 30;

    /** The duration of a single plot */
    public static final int DURATION_S = 10 * 60;
    
    /** Channels on this plot */
    private List<Channel> channels;
    
    /** Network this subnet belongs to */
    public final String networkName;
    
    /** my name */
    public final String subnetName;

    /** Delay image production by this amount */
    public final int embargoS;

    /** Width of image */
    public final int plotWidth;
    
    /** Height of image */
    public final int plotHeight;
    
    /** Height of the plot label */
    public final int labelHeight;
    
    /** Width of the plot label */
    public final int labelWidth;

    public Subnet(String networkName, String subnetName, ConfigFile config) {
        this.subnetName = subnetName;
        this.networkName = networkName;
        plotWidth = Util.stringToInt(config.getString("width"), DEFAULT_WIDTH);
        plotHeight = Util.stringToInt(config.getString("height"), DEFAULT_HEIGHT);
        embargoS = Util.stringToInt(config.getString("embargo"), DEFAULT_EMBARGO);

        labelHeight = LABEL_HEIGHT;
        labelWidth = LABEL_WIDTH;

        channels = createChannels(config);
    }

    private List<Channel> createChannels(ConfigFile config) {

        channels = new ArrayList<Channel>();
        for (String channel : config.getList("channel")) {
            ConfigFile c = config.getSubConfig(channel, true);
            Channel chan = new Channel(channel, plotHeight/config.getList("channel").size(), plotWidth, c);
            channels.add(chan);
        }

        return channels;
    }

//    public void writePlots(SliceWave wave, double startTime) {
//
//        for (Channel c : channels) {
//            c.setWave(wave);
//        }
//        writePlot();
//        writeThumbnail();
//    }
//
//    private Plot writePlot() {
//        Plot plot = new Plot(plotWidth, plotHeight);
//
//        int channelHeight = (plotHeight - (labelHeight * 2)) / channels.size();
//
//        int i = 0;
//        for (Channel c : channels) {
//            // renderer adds a 1 pixel border, making images larger than
//            // requested
//            int top = (i * channelHeight) + labelHeight - 1 + i;
//
//            boolean decorateX = i == channels.size() ? true : false;
//
////            FrameRenderer fr = c.getChannelRenderer(channelHeight, plotWidth, decorateX);
//
//            // skip 1 pixel border
////            fr.setLocation(0, top, plotWidth, plotHeight);
////            plot.addRenderer(fr);
//        }
//
//        return plot;
//    }
//
//    private Plot writeThumbnail() {
//        Plot plot = new Plot(plotWidth, plotHeight);
//
//        int channelHeight = (plotHeight - (labelHeight * 2)) / channels.size();
//
//        int i = 0;
//        for (Channel c : channels) {
//            // renderer adds a 1 pixel border, making images larger than
//            // requested
//            int top = (i * channelHeight) + labelHeight - 1 + i;
//
//            boolean decorateX = i == channels.size() ? true : false;
//
////            FrameRenderer fr = c.getChannelRenderer(channelHeight, plotWidth, decorateX);
//
//            // skip 1 pixel border
////            fr.setLocation(0, top, plotWidth, plotHeight);
////            plot.addRenderer(fr);
//        }
//
//        return plot;
//    }

    /**
     * 
     * @param plotEnd
     * @param dataSource
     * @return
     */
	public void plot(long plotEnd, SeismicDataSource dataSource) {
		Plot plot = new Plot(plotWidth, plotHeight);

		int channelHeight = (plotHeight - (labelHeight * 2)) / channels.size();
		
		int idx = 0;
		for (Channel channel : channels) {
			int top = (idx* channelHeight) + labelHeight - 1 + idx;
			
			
			BasicFrameRenderer chanPlot = channel.plot(plotEnd, dataSource, false);
			chanPlot.setLocation(labelWidth, top, plotWidth - (labelWidth + 2), channelHeight);
			
			plot.addRenderer(chanPlot);
			idx++;
		}

		//		new File(fileName).getParentFile().mkdirs();
//		try {
//			plot.writePNG(fileName);
//		} catch (PlotException e) {
//			fatalError(e.getLocalizedMessage());
//		}
		
	}
}