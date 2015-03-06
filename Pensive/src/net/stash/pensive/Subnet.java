package net.stash.pensive;

import gov.usgs.plot.Plot;
import gov.usgs.plot.PlotException;
import gov.usgs.swarm.data.SeismicDataSource;
import gov.usgs.util.ConfigFile;
import gov.usgs.util.Log;
import gov.usgs.util.Time;
import gov.usgs.util.Util;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 * A single subnet.
 * 
 * @author Tom Parker
 * 
 */
public class Subnet {

    /** my logger */
    private static final Logger LOGGER = Log.getLogger("gov.usgs");

    public static final String DEFAULT_PATH_ROOT = "html";

    public static final int DEFAULT_PLOT_WIDTH = 576;
    public static final int DEFAULT_PLOT_HEIGHT = 756;
    public static final int DEFAULT_THUMB_WIDTH = 151;
    public static final int DEFAULT_THUMB_HEIGHT = 198;
    public static final int DEFAULT_EMBARGO = 0;

    public static final int LABEL_HEIGHT = 35;
    public static final int LABEL_WIDTH = 30;

    /** The duration of a single plot */
    public static final int DURATION_S = 10 * 60;
    public static final String FILE_PATH_FORMAT = "yyyy/DDD";
    public static final String FILE_SUFFIX_FORMAT = "_yyyyMMdd-HHmm";

    /** Root of plot directory */
    private final String pathRoot;

    /** Network this subnet belongs to */
    public final String networkName;

    /** my name */
    public final String subnetName;

    /** Delay image production by this amount */
    public final int embargoS;

    /** plot dimension */
    private final Dimension plotDimension;

    /** thumbnail dimension */
    private final Dimension thumbDimension;
    
    /** Channels on this plot */
    private final List<Channel> channels;

    /** height of a single channel plot */
    private int channelHeight;
    
    public Subnet(String networkName, String subnetName, ConfigFile config) {
        this.subnetName = subnetName;
        this.networkName = networkName;

        pathRoot = Util.stringToString(config.getString("pathRoot"), DEFAULT_PATH_ROOT);
        embargoS = Util.stringToInt(config.getString("embargo"), DEFAULT_EMBARGO);

        plotDimension = new Dimension();
        plotDimension.width = Util.stringToInt(config.getString("plotWidth"), DEFAULT_PLOT_WIDTH);
        plotDimension.height = Util.stringToInt(config.getString("plotHeight"), DEFAULT_PLOT_HEIGHT);

        thumbDimension = new Dimension();
        thumbDimension.width = Util.stringToInt(config.getString("thumbWidth"), DEFAULT_THUMB_WIDTH);
        thumbDimension.height = Util.stringToInt(config.getString("thumbHeight"), DEFAULT_THUMB_HEIGHT);

        channels = createChannels(config);
    }

    private List<Channel> createChannels(ConfigFile config) {

        List<Channel> channels = new ArrayList<Channel>();
        List<String> chans = config.getList("channel");
        
        Dimension plotChanDimension = new Dimension();
        plotChanDimension.height = (plotDimension.height - 2*Subnet.LABEL_HEIGHT) / chans.size();
        plotChanDimension.width = plotDimension.width;
        
        Dimension thumbChanDimension = new Dimension();
        thumbChanDimension.height = thumbDimension.height / chans.size();
        thumbChanDimension.width = thumbDimension.width;
        
        int idx = 0;
        for (String channel : chans) {
            boolean decorateX = (idx == chans.size() - 1) ? true : false;
            ConfigFile c = config.getSubConfig(channel, true);
            Channel chan = new Channel(channel, idx, plotChanDimension, thumbChanDimension, decorateX, c);
            channels.add(chan);
            idx++;
        }
        
        return channels;
    }


    /**
     * 
     * @param plotEnd
     * @param dataSource
     * @return
     */
    public void plot(long plotEnd, SeismicDataSource dataSource) {
        Plot plot = new Plot(plotDimension.width, plotDimension.height);
        Plot thumb = new Plot(thumbDimension.width, thumbDimension.height);

        for (Channel channel : channels) {
            channel.updateWave(plotEnd,  dataSource);
            plot.addRenderer(channel.plot());
            thumb.addRenderer(channel.plotThumb());
        }

        String fileBase = generateFileBase(plotEnd);
        new File(fileBase).getParentFile().mkdirs();
        
        writePNG(plot, fileBase + ".png");
        writePNG(thumb, fileBase + "_thumb.png");
    }
    
    /**
     * 
     * @param plot
     * @param fileName
     */
    private void writePNG(Plot plot, String fileName) {
        LOGGER.log(Level.FINE, "writting " + fileName);
        try {
            plot.writePNG(fileName);
        } catch (PlotException e) {
            LOGGER.log(Level.SEVERE, "Cannot write " + fileName + ": " + e.getLocalizedMessage());
        }
    }
    
    
    /**
     * Generate a file file by applying a SimpleDateFormat
     * 
     * @param end time of plot
     * @return generated file path
     */
    private String generateFileBase(long time) {
        StringBuilder sb = new StringBuilder();
        sb.append(pathRoot + '/'); 
        if (networkName != null)
            sb.append(networkName + '/');
        sb.append(subnetName + '/');
        sb.append(Time.format(FILE_PATH_FORMAT, time));

        sb.append('/' + subnetName);
        sb.append(Time.format(FILE_SUFFIX_FORMAT, time));
        
        String name = sb.toString();
        name = name.replaceAll("/+", Matcher.quoteReplacement(File.separator));
        
        return name;
    }
}