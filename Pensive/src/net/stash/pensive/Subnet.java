package net.stash.pensive;

import gov.usgs.plot.Plot;
import gov.usgs.plot.PlotException;
import gov.usgs.plot.render.BasicFrameRenderer;
import gov.usgs.swarm.data.SeismicDataSource;
import gov.usgs.util.ConfigFile;
import gov.usgs.util.Log;
import gov.usgs.util.Time;
import gov.usgs.util.Util;

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
    public static final String DEFAULT_FILE_PATH_FORMAT = "yyyy/DDD";
    public static final String DEFAULT_FILE_SUFFIX_FORMAT = "_yyyyMMdd-HHmm";

    public static final int DEFAULT_WIDTH = 576;
    public static final int DEFAULT_HEIGHT = 756;
    public static final int DEFAULT_EMBARGO = 0;

    public static final int LABEL_HEIGHT = 35;
    public static final int LABEL_WIDTH = 30;

    /** The duration of a single plot */
    public static final int DURATION_S = 10 * 60;

    /** Root of plot directory */
    private final String pathRoot;

    /** format of file path */
    private final String filePathFormat;

    /** format of file name */
    private final String fileSuffixFormat;

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

    /** Channels on this plot */
    private final List<Channel> channels;

    /** height of a single channel plot */
    private int channelHeight;
    
    public Subnet(String networkName, String subnetName, ConfigFile config) {
        this.pathRoot = Util.stringToString(config.getString("pathRoot"), DEFAULT_PATH_ROOT);
        this.filePathFormat = Util.stringToString(config.getString("filePathFormat"), DEFAULT_FILE_PATH_FORMAT);
        this.fileSuffixFormat = Util.stringToString(config.getString("fileSuffixFormat"), DEFAULT_FILE_SUFFIX_FORMAT);
        this.subnetName = subnetName;
        this.networkName = networkName;
        plotWidth = Util.stringToInt(config.getString("width"), DEFAULT_WIDTH);
        plotHeight = Util.stringToInt(config.getString("height"), DEFAULT_HEIGHT);
        embargoS = Util.stringToInt(config.getString("embargo"), DEFAULT_EMBARGO);

        channels = new ArrayList<Channel>();
        createChannels(config);
    }

    private void createChannels(ConfigFile config) {

        List<String> chans = config.getList("channel");
        channelHeight = (plotHeight - 2*Channel.LABEL_HEIGHT) / chans.size();
        
        int idx = 0;
        for (String channel : chans) {
            int top = idx++ * channelHeight;
            boolean decorate = (idx == chans.size()) ? true : false;
            ConfigFile c = config.getSubConfig(channel, true);
            Channel chan = new Channel(channel, top, channelHeight, plotWidth, decorate, c);
            channels.add(chan);
        }
    }


    /**
     * 
     * @param plotEnd
     * @param dataSource
     * @return
     */
    public void plot(long plotEnd, SeismicDataSource dataSource) {
        Plot plot = new Plot(plotWidth, plotHeight);

        int idx = 0;
        for (Channel channel : channels) {
            BasicFrameRenderer chanPlot = channel.plot(plotEnd, dataSource);
//            chanPlot.setLocation(labelWidth, top, plotWidth, channelHeight);
            plot.addRenderer(chanPlot);
            idx++;
        }

        String filename = generateFileName(plotEnd);
        LOGGER.log(Level.FINE, "writting " + filename);
        new File(filename).getParentFile().mkdirs();
        try {
            plot.writePNG(filename);
        } catch (PlotException e) {
            LOGGER.log(Level.SEVERE, "Cannot write " + filename + ": " + e.getLocalizedMessage());
        }

    }

    /**
     * Generate a file file by applying a SimpleDateFormat
     * 
     * @param end time of plot
     * @return generated file path
     */
    private String generateFileName(long time) {
        StringBuilder sb = new StringBuilder();
        sb.append(pathRoot + '/'); 
        if (networkName != null)
            sb.append(networkName + '/');
        sb.append(subnetName + '/');
        sb.append(Time.format(filePathFormat, time));

        sb.append('/' + subnetName);
        sb.append(Time.format(fileSuffixFormat, time));
        sb.append(".png");
        
        String name = sb.toString();
        name = name.replaceAll("/+", Matcher.quoteReplacement(File.separator));
        
        return name;
    }
}