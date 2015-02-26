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

    /** plot width */
    private final int width;

    /** plot height */
    private final int height;

    /** my verticle location */
    private final int top;

    /** add x-axis labels? */
    private boolean decorate;

    /** my wave renderer */
    private final SliceWaveRenderer waveRenderer;

    /** my spectrogram renderer */
    private final SpectrogramRenderer spectrogramRenderer;

    /** my frame renderer */
    private final BasicFrameRenderer channelFrame;

    /** height of the wave panel */
    private final int waveHeight;
    
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
    public Channel(String channel, int top, int height, int width, boolean decorate, ConfigFile config) {
        this.name = channel;
        this.top = top + LABEL_HEIGHT;
        this.height = height;
        this.width = width;
        this.decorate = decorate;
        
        waveHeight = (int)(height * WAVE_RATIO);

        channelFrame = new BasicFrameRenderer();

        waveRenderer = createWaveRenderer();
        channelFrame.addRenderer(waveRenderer);

        spectrogramRenderer = createSpectrogramRenderer(config);
        channelFrame.addRenderer(spectrogramRenderer);
    }

    /**
     * 
     */
    private SliceWaveRenderer createWaveRenderer() {

        SliceWaveRenderer wr = new MinuteMarkingWaveRenderer();

        wr.xTickMarks = true;
        wr.xTickValues = false;
        wr.xUnits = false;
        wr.xLabel = false;
        wr.yTickMarks = false;
        wr.yTickValues = false;
        wr.setColor(Color.BLACK);
        
        int waveHeight = (int) (height * WAVE_RATIO);
        wr.setLocation(LABEL_WIDTH, top, width - (2*LABEL_WIDTH), waveHeight);

        return wr;
    }

    /**
     * 
     */
    private SpectrogramRenderer createSpectrogramRenderer(ConfigFile config) {

        SpectrogramRenderer sr = new SpectrogramRenderer();

        // y-axis labels will sometimes not be displayed if x-axis tick marks
        // are not displayed. Note sure why.
        sr.yTickMarks = true;
        sr.yTickValues = true;
        sr.xTickMarks = true;
        sr.xTickValues = decorate;
        sr.xUnits = decorate;
        sr.xLabel = decorate;
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
        sr.setLocation(LABEL_WIDTH, top + waveHeight, width - (2*LABEL_WIDTH), height - waveHeight);

        return sr;
    }

    private BasicFrameRenderer noDataRenderer(int top) {
        BasicFrameRenderer fr = new BasicFrameRenderer();
        TextRenderer tr = new TextRenderer(width / 2, top + height / 2, name + " - no data");

        tr.horizJustification = TextRenderer.CENTER;
        tr.vertJustification = TextRenderer.CENTER;
        tr.color = NO_DATA_TEXT_COLOR;
        if (height < THUMBNAIL_FONT_HEIGHT_THREASHOLD)
            tr.font = NO_DATA_THUMBNAIL_FONT;
        else
            tr.font = NO_DATA_FONT;

        fr.addRenderer(tr);
        return fr;
    }

    /**
     * 
     * @param the
     *            wave server connection
     * @param if true decorate
     * @return frame renderer containing plot or error message
     */
    public BasicFrameRenderer plot(long plotEnd, SeismicDataSource dataSource) {

        double t2 = Util.ewToJ2K(plotEnd / 1000);
        double t1 = t2 - Subnet.DURATION_S;
        Wave wave = dataSource.getWave(name, t1, t2);

        if (wave == null) {
            return noDataRenderer(top);

        } else {
            SliceWave sw = new SliceWave(wave);
            sw.setSlice(t1, t2);

            waveRenderer.setMinY(sw.min());
            waveRenderer.setMaxY(sw.max());
            waveRenderer.setWave(sw);
            waveRenderer.setViewTimes(t1, t2, "UTC");
            waveRenderer.update();

            spectrogramRenderer.setWave(sw);
            spectrogramRenderer.setViewStartTime(t1);
            spectrogramRenderer.setViewEndTime(t2);
            spectrogramRenderer.update();

            return channelFrame;
        }
    }
}