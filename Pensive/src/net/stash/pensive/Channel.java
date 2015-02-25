package net.stash.pensive;

import gov.usgs.plot.data.SliceWave;
import gov.usgs.plot.render.BasicFrameRenderer;
import gov.usgs.plot.render.FrameRenderer;
import gov.usgs.plot.render.TextRenderer;
import gov.usgs.plot.render.wave.MinuteMarkingWaveRenderer;
import gov.usgs.plot.render.wave.SliceWaveRenderer;
import gov.usgs.plot.render.wave.SpectrogramRenderer;
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

    public static final double OVERLAP = 0.859375;
    public static final boolean LOG_POWER = true;
    public static final double MIN_FREQ = 0;
    public static final double MAX_FREQ = 10;
    public static final int NFFT = 0;
    public static final int BIN_SIZE = 256;
    public static final int MAX_POWER = 120;
    public static final int MIN_POWER = 30;
    public static final Color NO_DATA_COLOR = new Color(160, 41, 41);
    public static final Font NO_DATA_FONT = Font.decode("dialog-PLAIN-36");
    public static final Font NO_DATA_THUMBNAIL_FONT = Font.decode("dialog-PLAIN-12");
    public static final int IMAGE_HEIGHT_FONT_THREASHOLD = 200;
    public static final double WAVE_RATIO = .25;
    public static final int LABEL_HEIGHT = 35;
    public static final int LABEL_WIDTH = 30;

    /** channel name in config file format*/
    private final String channel;
    
    /** FFT overlap */
    private final double overlap;
    
    /** if yes plot log power */
    private final boolean logPower;

    /** maximum power to plot */
    private final int maxPower;
    
    /** minimum power to plot */
    private final int minPower;

    /** minimum frequency of FFT */
    private final double minFreq;
    
    /** maximum frequency of FFT */
    private final double maxFreq;
    
    /** number of FFT iterations */
    private final int nfft;
    
    /** FFT bin size */
    private final int binSize;
    
    /** wave data */
    private SliceWave wave;

    /**
     * Class constructor
     * 
     * @param my channel in config file format
     * @param my config file
     */
    public Channel(String channel, ConfigFile config) {
        this.channel = channel;

        overlap = Util.stringToDouble(config.getString("overlap"), OVERLAP);
        logPower = Util.stringToBoolean(config.getString("logPower"), LOG_POWER);
        minFreq = Util.stringToDouble(config.getString("minFreq"), MIN_FREQ);
        maxFreq = Util.stringToDouble(config.getString("maxFreq"), MAX_FREQ);
        nfft = Util.stringToInt(config.getString("nfft"), NFFT);
        binSize = Util.stringToInt(config.getString("binSize"), BIN_SIZE);
        minPower = Util.stringToInt(config.getString("minPower"), MIN_POWER);
        maxPower = Util.stringToInt(config.getString("maxPower"), MAX_POWER);
  }

    /**
     * wave data mutator
     * @param wave data
     */
    public void setWave(SliceWave wave) {
        this.wave = wave;
    }

    /**
     * 
     */
    private SliceWaveRenderer createWaveRenderer(ConfigFile config) {

        SliceWaveRenderer wr = new MinuteMarkingWaveRenderer();

        wr.xTickMarks = true;
        wr.xTickValues = false;
        wr.xUnits = false;
        wr.xLabel = false;
        wr.yTickMarks = false;
        wr.yTickValues = false;
        wr.setColor(Color.BLACK);

        return wr;
    }

//    public SliceWaveRenderer getWaveRenderer() {
//        return waveRenderer;
//    }
//
//    private SpectrogramRenderer createSpectrogramRenderer(ConfigFile config) {
//        SpectrogramRenderer sr = new SpectrogramRenderer();
//
//        sr.xTickMarks = true;
//        sr.yTickMarks = true;
//        sr.yTickValues = true;
////
////        double overlap = Util.stringToDouble(config.getString("overlap"), OVERLAP);
////        boolean logPower = Util.stringToBoolean(config.getString("logPower"), LOG_POWER);
////        double minFreq = Util.stringToDouble(config.getString("minFreq"), MIN_FREQ);
////        double maxFreq = Util.stringToDouble(config.getString("maxFreq"), MAX_FREQ);
////        int nfft = Util.stringToInt(config.getString("nfft"), NFFT);
////        int binSize = Util.stringToInt(config.getString("binSize"), BIN_SIZE);
//        sr.setOverlap(overlap);
//
//        sr.setLogPower(logPower);
//
//        sr.setMinFreq(minFreq);
//
//        sr.setMaxFreq(maxFreq);
//
//        sr.setNfft(nfft);
//
//        sr.setBinSize(binSize);
//
//        sr.setYLabelText(channel);
//
//        // sr.xTickValues = decorateX;
//        // sr.xUnits = decorateX;
//        // sr.xLabel = decorateX;
//        return sr;
//    }
//
//    public SpectrogramRenderer getSpectrogramRenderer() {
//        return spectrogramRenderer;
//    }
//
//    public FrameRenderer getChannelRenderer(int height, int width, boolean decorateX) {
//        BasicFrameRenderer fr = new BasicFrameRenderer();
//
//        if (wave == null) {
//            TextRenderer tr = new TextRenderer(width / 2, height / 2, channel + " - no data");
//            tr.horizJustification = TextRenderer.CENTER;
//            tr.vertJustification = TextRenderer.CENTER;
//            tr.color = NO_DATA_COLOR;
//            if (height < IMAGE_HEIGHT_FONT_THREASHOLD)
//                tr.font = NO_DATA_THUMBNAIL_FONT;
//            else
//                tr.font = NO_DATA_FONT;
//            fr.addRenderer(tr);
//        } else {
//            int waveHeight = (int) (height * WAVE_RATIO);
//            waveRenderer.setWave(wave);
//            waveRenderer.setMinY(wave.min());
//            waveRenderer.setMaxY(wave.max());
//            waveRenderer.setViewTimes(startTime, startTime + Subnet.DURATION, "UTC");
//
//            // skip 1 pixel border
//            waveRenderer.setLocation(LABEL_WIDTH - 1, 0, width - (LABEL_WIDTH * 2) + 1, waveHeight);
//            waveRenderer.update();
//
//            fr.addRenderer(waveRenderer);
//
//            spectrogramRenderer.setWave(wave);
//            spectrogramRenderer.setViewStartTime(startTime);
//            spectrogramRenderer.setViewEndTime(startTime + Subnet.DURATION);
//            spectrogramRenderer.setTimeZone("UTC");
//
//             // skip 1 pixel border
//             spectrogramRenderer.setLocation(LABEL_WIDTH - 1, waveHeight, width
//             - (LABEL_WIDTH * 2) + 1, height-waveHeight);
//             spectrogramRenderer.update();
//             
//             fr.addRenderer(spectrogramRenderer);
//             
//        }
//        return fr;
//    }
}