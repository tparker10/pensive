package net.stash.pensive.plot;

import gov.usgs.plot.data.SliceWave;
import gov.usgs.plot.render.BasicFrameRenderer;
import gov.usgs.plot.render.TextRenderer;
import gov.usgs.plot.render.wave.MinuteMarkingWaveRenderer;
import gov.usgs.plot.render.wave.SliceWaveRenderer;
import gov.usgs.plot.render.wave.SpectrogramRenderer;
import gov.usgs.util.ConfigFile;
import gov.usgs.util.Util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * 
 * @author Tom Parker
 * 
 */
public abstract class AbstractChannelPlot {
    public static final Color NO_DATA_TEXT_COLOR = new Color(160, 41, 41);
    public static final double WAVE_RATIO = .25;

    public static final double DEFAULT_OVERLAP = 0.859375;
    public static final boolean DEFAULT_LOG_POWER = true;
    public static final double DEFAULT_MIN_FREQ = 0;
    public static final double DEFAULT_MAX_FREQ = 10;
    public static final int DEFAULT_NFFT = 0;
    public static final int DEFAULT_BIN_SIZE = 256;
    public static final int DEFAULT_MAX_POWER = 120;
    public static final int DEFAULT_MIN_POWER = 30;

    /** my wave data */
    protected SliceWave wave;

    /** Font to use for no data message */
    protected Font noDataFont;

    /** Channel position in plot */
    protected int index;

    /** Dimension of channel plot */
    protected Dimension plotDimension;

    /** my config stanza */
    protected ConfigFile config;

    /** my wave renderer */
    private final SliceWaveRenderer waveRenderer;

    /** my spectrogram renderer */
    protected final SpectrogramRenderer spectrogramRenderer;

    /** my frame renderer */
    private final BasicFrameRenderer plotFrame;

    /** height of the wave panel */
    protected final int waveHeight;

    /** my name */
    protected final String name;

    protected abstract void tweakSpectrogramRenderer(SpectrogramRenderer spectrogramRenderer);

    protected abstract void tweakWaveRenderer(SliceWaveRenderer waveRenderer);

    public AbstractChannelPlot(String name, int index, Dimension plotDimension, ConfigFile config) {
        this.name = name;
        this.index = index;
        this.plotDimension = plotDimension;
        this.config = config;
        waveHeight = (int) (plotDimension.height * WAVE_RATIO);

        waveRenderer = createWaveRenderer();
        spectrogramRenderer = createSpectrogramRenderer(config);

        plotFrame = new BasicFrameRenderer();
        plotFrame.addRenderer(waveRenderer);
        plotFrame.addRenderer(spectrogramRenderer);
    }

    /**
     * 
     * @param config
     * @return
     */
    protected SpectrogramRenderer createSpectrogramRenderer(ConfigFile config) {

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

        tweakSpectrogramRenderer(sr);

        return sr;
    }

    /**
     * 
     * @return
     */
    protected SliceWaveRenderer createWaveRenderer() {

        SliceWaveRenderer wr = new MinuteMarkingWaveRenderer();

        wr.xTickMarks = false;
        wr.xTickValues = false;
        wr.xUnits = false;
        wr.xLabel = false;
        wr.yTickMarks = false;
        wr.yTickValues = false;
        wr.setColor(Color.BLACK);
        tweakWaveRenderer(wr);

        return wr;
    }

    public void setWave(SliceWave wave) {
        this.wave = wave;
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
            return noDataRenderer();

        } else {
            waveRenderer.setMinY(wave.min());
            waveRenderer.setMaxY(wave.max());
            waveRenderer.setWave(wave);
            waveRenderer.setViewTimes(wave.getStartTime(), wave.getEndTime(), "UTC");
            waveRenderer.update();

            spectrogramRenderer.setWave(wave);
            spectrogramRenderer.setViewStartTime(wave.getStartTime());
            spectrogramRenderer.setViewEndTime(wave.getEndTime());
            spectrogramRenderer.update();

            return plotFrame;
        }
    }

    private BasicFrameRenderer noDataRenderer() {
        BasicFrameRenderer fr = new BasicFrameRenderer();
        int top = index * plotDimension.height;
        TextRenderer tr = new TextRenderer(plotDimension.width / 2, top + plotDimension.height / 2, name + " - no data");

        tr.horizJustification = TextRenderer.CENTER;
        tr.vertJustification = TextRenderer.CENTER;
        tr.color = NO_DATA_TEXT_COLOR;
        tr.font = noDataFont;

        fr.addRenderer(tr);
        return fr;
    }
}
