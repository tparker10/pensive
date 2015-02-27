package net.stash.pensive;

import gov.usgs.plot.data.SliceWave;
import gov.usgs.plot.data.Wave;
import gov.usgs.plot.render.BasicFrameRenderer;
import gov.usgs.plot.render.TextRenderer;
import gov.usgs.plot.render.wave.MinuteMarkingWaveRenderer;
import gov.usgs.plot.render.wave.SliceWaveRenderer;
import gov.usgs.plot.render.wave.SpectrogramRenderer;
import gov.usgs.swarm.data.SeismicDataSource;
import gov.usgs.util.ConfigFile;
import gov.usgs.util.Log;
import gov.usgs.util.Util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.logging.Logger;

/**
 * A single channel of seismic data on a single subnet plot.
 * 
 * @author Tom Parker
 * 
 */
public class Channel {

    /** my logger */
    private static final Logger LOGGER = Log.getLogger("gov.usgs");

    public static final Color NO_DATA_TEXT_COLOR = new Color(160, 41, 41);
    public static final int THUMBNAIL_FONT_HEIGHT_THREASHOLD = 200;
    public static final double DEFAULT_OVERLAP = 0.859375;
    public static final boolean DEFAULT_LOG_POWER = true;
    public static final double DEFAULT_MIN_FREQ = 0;
    public static final double DEFAULT_MAX_FREQ = 10;
    public static final int DEFAULT_NFFT = 0;
    public static final int DEFAULT_BIN_SIZE = 256;
    public static final int DEFAULT_MAX_POWER = 120;
    public static final int DEFAULT_MIN_POWER = 30;

    public static final Color NO_DATA_COLOR = new Color(160, 41, 41);
    public static final Font NO_DATA_FONT = Font.decode("dialog-PLAIN-36");
    public static final Font NO_DATA_THUMBNAIL_FONT = Font.decode("dialog-PLAIN-12");
    public static final int IMAGE_HEIGHT_FONT_THREASHOLD = 200;

    public static final double WAVE_RATIO = .25;

    public static final int LABEL_HEIGHT = 35;
    public static final int LABEL_WIDTH = 30;

    /** channel name in config file format */
    private final String name;

    /** plot dimension */
    private final Dimension plotDimension;

    /** thumb dimension */
    private final Dimension thumbDimension;
    
    /** my plot order */
    private final int index;

    /** add x-axis labels? */
    private boolean decorateX;

    /** my wave renderer */
    private final SliceWaveRenderer plotWaveRenderer;

    /** my spectrogram renderer */
    private final SpectrogramRenderer plotSpectrogramRenderer;

    /** my wave renderer */
    private final SliceWaveRenderer thumbWaveRenderer;

    /** my spectrogram renderer */
    private final SpectrogramRenderer thumbSpectrogramRenderer;


    /** my frame renderer */
    private final BasicFrameRenderer plotFrame;

    /** */
    private final BasicFrameRenderer thumbFrame;
    
    /** height of the wave panel */
    private final int plotWaveHeight;
    
    /** */
    private final int thumbWaveHeight;
    
    /** my wave data */
    private SliceWave wave;
    
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
    public Channel(String channel, int index, Dimension plotDimension, Dimension thumbDimension, boolean decorateX, ConfigFile config) {
        this.name = channel;
        this.index = index;
        this.decorateX = decorateX;
        
        this.plotDimension = plotDimension;
        this.thumbDimension = thumbDimension;
        
        plotWaveHeight = (int)(plotDimension.height * WAVE_RATIO);
        thumbWaveHeight = (int)(thumbDimension.height * WAVE_RATIO);


        plotWaveRenderer = createPlotWaveRenderer();
        plotSpectrogramRenderer = createPlotSpectrogramRenderer(config);

        plotFrame = new BasicFrameRenderer();
        plotFrame.addRenderer(plotWaveRenderer);
        plotFrame.addRenderer(plotSpectrogramRenderer);
        

        thumbWaveRenderer = createThumbWaveRenderer();
        thumbSpectrogramRenderer = createThumbSpectrogramRenderer(config);
        
        thumbFrame = new BasicFrameRenderer();
        thumbFrame.addRenderer(thumbWaveRenderer);
        thumbFrame.addRenderer(thumbSpectrogramRenderer);
    }

 
    /**
     * 
     * @return
     */
    private SliceWaveRenderer createPlotWaveRenderer() {

        SliceWaveRenderer wr = new MinuteMarkingWaveRenderer();
        wr.xTickMarks = true;
        wr.xTickValues = false;
        wr.xUnits = false;
        wr.xLabel = false;
        wr.yTickMarks = false;
        wr.yTickValues = false;
        wr.setColor(Color.BLACK);
        
        int waveHeight = (int) (plotDimension.height * WAVE_RATIO);
        int top = index * plotDimension.height + LABEL_HEIGHT;
        int width = plotDimension.width - (2*LABEL_WIDTH);
        
        wr.setLocation(LABEL_WIDTH, top, width, waveHeight);
        
        return wr;
    }

    /**
     * 
     * @return
     */
    private SliceWaveRenderer createThumbWaveRenderer() {

        SliceWaveRenderer wr = new MinuteMarkingWaveRenderer();

        wr.xTickMarks = false;
        wr.xTickValues = false;
        wr.xUnits = false;
        wr.xLabel = false;
        wr.yTickMarks = false;
        wr.yTickValues = false;
        wr.setColor(Color.BLACK);
        
        int waveHeight = (int) (thumbDimension.height * WAVE_RATIO);
        int top = index * thumbDimension.height;
        int width = thumbDimension.width;
        
        wr.setLocation(0, top, width, waveHeight);
        
        return wr;
    }

 
    /**
     * 
     * @param config
     * @return
     */
    private SpectrogramRenderer createPlotSpectrogramRenderer(ConfigFile config) {

        SpectrogramRenderer sr = new SpectrogramRenderer();

        // y-axis labels will sometimes not be displayed if x-axis tick marks
        // are not displayed. Note sure why.
        sr.yTickMarks = true;
        sr.yTickValues = true;
        sr.xTickMarks = true;
        sr.xTickValues = decorateX;
        sr.xUnits = decorateX;
        sr.xLabel = decorateX;
        sr.setOverlap(Util.stringToDouble(config.getString("overlap"), DEFAULT_OVERLAP));
        sr.setLogPower(Util.stringToBoolean(config.getString("logPower"), DEFAULT_LOG_POWER));
        sr.setMinFreq(Util.stringToDouble(config.getString("minFreq"), DEFAULT_MIN_FREQ));
        sr.setMaxFreq(Util.stringToDouble(config.getString("maxFreq"), DEFAULT_MAX_FREQ));
        sr.setNfft(Util.stringToInt(config.getString("nfft"), DEFAULT_NFFT));
        sr.setBinSize(Util.stringToInt(config.getString("binSize"), DEFAULT_BIN_SIZE));
        sr.setMinPower(Util.stringToInt(config.getString("minPower"), DEFAULT_MIN_POWER));
        sr.setMaxPower(Util.stringToInt(config.getString("maxPower"), DEFAULT_MAX_POWER));
        sr.setYLabelText(name);
        sr.setTimeZone("UTC");
        
        int top = index * plotDimension.height;
        sr.setLocation(LABEL_WIDTH, top + plotWaveHeight + LABEL_HEIGHT, plotDimension.width - (2*LABEL_WIDTH), plotDimension.height - plotWaveHeight);

        return sr;
    }

    /**
     * 
     * @param config
     * @return
     */
    private SpectrogramRenderer createThumbSpectrogramRenderer(ConfigFile config) {

        SpectrogramRenderer sr = new SpectrogramRenderer();

        // y-axis labels will sometimes not be displayed if x-axis tick marks
        // are not displayed. Note sure why.
        sr.yTickMarks = false;
        sr.yTickValues = false;
        sr.xTickMarks = false;
        sr.xTickValues = false;
        sr.xUnits = false;
        sr.xLabel = false;
        
        sr.setOverlap(Util.stringToDouble(config.getString("overlap"), DEFAULT_OVERLAP));
        sr.setLogPower(Util.stringToBoolean(config.getString("logPower"), DEFAULT_LOG_POWER));
        sr.setMinFreq(Util.stringToDouble(config.getString("minFreq"), DEFAULT_MIN_FREQ));
        sr.setMaxFreq(Util.stringToDouble(config.getString("maxFreq"), DEFAULT_MAX_FREQ));
        sr.setNfft(Util.stringToInt(config.getString("nfft"), DEFAULT_NFFT));
        sr.setBinSize(Util.stringToInt(config.getString("binSize"), DEFAULT_BIN_SIZE));
        sr.setMinPower(Util.stringToInt(config.getString("minPower"), DEFAULT_MIN_POWER));
        sr.setMaxPower(Util.stringToInt(config.getString("maxPower"), DEFAULT_MAX_POWER));
        sr.setTimeZone("UTC");
        
        int top = index * thumbDimension.height + thumbWaveHeight;
        sr.setLocation(0, top, thumbDimension.width, thumbDimension.height - thumbWaveHeight);

        return sr;
    }

    private BasicFrameRenderer noDataPlotRenderer() {
        BasicFrameRenderer fr = new BasicFrameRenderer();
        int top = index * plotDimension.height;
        TextRenderer tr = new TextRenderer(plotDimension.width / 2, top + plotDimension.height / 2, name + " - no data");

        tr.horizJustification = TextRenderer.CENTER;
        tr.vertJustification = TextRenderer.CENTER;
        tr.color = NO_DATA_TEXT_COLOR;
        if (plotDimension.height < THUMBNAIL_FONT_HEIGHT_THREASHOLD)
            tr.font = NO_DATA_THUMBNAIL_FONT;
        else
            tr.font = NO_DATA_FONT;

        fr.addRenderer(tr);
        return fr;
    }

    private BasicFrameRenderer noDataThumbRenderer() {
        BasicFrameRenderer fr = new BasicFrameRenderer();
        
        int top = index * thumbDimension.height;
        TextRenderer tr = new TextRenderer(plotDimension.width / 2, top + plotDimension.height / 2, name + " - no data");

        tr.horizJustification = TextRenderer.CENTER;
        tr.vertJustification = TextRenderer.CENTER;
        tr.color = NO_DATA_TEXT_COLOR;
        if (plotDimension.height < THUMBNAIL_FONT_HEIGHT_THREASHOLD)
            tr.font = NO_DATA_THUMBNAIL_FONT;
        else
            tr.font = NO_DATA_FONT;

        fr.addRenderer(tr);
        return fr;
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
        if (w != null) {
            wave = new SliceWave(w);
            wave.setSlice(t1, t2);
        } else 
            wave = null;
    }
    
    
    /**
     * 
     * @param the
     *            wave server connection
     * @param if true decorate
     * @return frame renderer containing plot or error message
     */
    public BasicFrameRenderer plot() {

        if (wave == null) {
            return noDataPlotRenderer();

        } else {
            plotWaveRenderer.setMinY(wave.min());
            plotWaveRenderer.setMaxY(wave.max());
            plotWaveRenderer.setWave(wave);
            plotWaveRenderer.setViewTimes(wave.getStartTime(), wave.getEndTime(), "UTC");
            plotWaveRenderer.update();

            plotSpectrogramRenderer.setWave(wave);
            plotSpectrogramRenderer.setViewStartTime(wave.getStartTime());
            plotSpectrogramRenderer.setViewEndTime(wave.getEndTime());
            plotSpectrogramRenderer.update();
            return plotFrame;
        }
    }
    
    /**
     * 
     * @param the
     *            wave server connection
     * @param if true decorate
     * @return frame renderer containing plot or error message
     */
    public BasicFrameRenderer plotThumb() {

        if (wave == null) {
            return noDataThumbRenderer();

        } else {
            thumbWaveRenderer.setMinY(wave.min());
            thumbWaveRenderer.setMaxY(wave.max());
            thumbWaveRenderer.setWave(wave);
            thumbWaveRenderer.setViewTimes(wave.getStartTime(), wave.getEndTime(), "UTC");
            thumbWaveRenderer.update();

            thumbSpectrogramRenderer.setWave(wave);
            thumbSpectrogramRenderer.setViewStartTime(wave.getStartTime());
            thumbSpectrogramRenderer.setViewEndTime(wave.getEndTime());
            thumbSpectrogramRenderer.update();
            return thumbFrame;
        }
    }

}