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

	/** channel name in config file format */
	private final String name;

	
	/** plot width */
	private final int width;
	
	/** plot height */
	private final int height;
	
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
	 * @param plot height
	 * @param plot width
	 * @param my config file
	 */
	public Channel(String channel, int height, int width, ConfigFile config) {
		this.name = channel;
		this.height = height;
		this.width = width;

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
	 * 
	 * @param wave
	 *            data
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

	/**
	 * 
	 * @param the
	 *            wave server connection
	 * @param if true decorate
	 * @return frame renderer containing plot or error message
	 */
	public BasicFrameRenderer plot(long plotEnd, SeismicDataSource dataSource, boolean decorate) {
//		Wave wave = dataSource.getWave(name, plotEnd - Subnet.DURATION_S, plotEnd);
		BasicFrameRenderer channelFrame = new BasicFrameRenderer();
		
		if (wave == null) {
			TextRenderer tr = new TextRenderer(width / 2, height / 2, name + " - no data");

//			tr.horizJustification = TextRenderer.CENTER;
//			tr.vertJustification = TextRenderer.CENTER;
//			tr.color = NO_DATA_TEXT_COLOR;
//			if (height < THUMBNAIL_FONT_HEIGHT_THREASHOLD)
//				tr.font = NO_DATA_THUMBNAIL_FONT;
//			else
//				tr.font = NO_DATA_FONT;
//			channelFrame.addRenderer(tr);
		} else {
//			SliceWaveRenderer swr = getSliceWaveRenderer(wave);
//
//			// skip 1 pixel border
//			swr.setLocation(LABEL_WIDTH - 1, 0, 200, 200);
//			swr.update();
//
//			channelFrame.addRenderer(swr);
//
//			SpectrogramRenderer sr = getSpectrogramRenderer(channel, wave, decorate);
//
//			// skip 1 pixel border
//			sr.setLocation(labelWidth - 1, top + waveHeight, settings.width - (labelWidth * 2) + 1, sgramHeight);
//			sr.update();
		}
		return channelFrame;
	}
}