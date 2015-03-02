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

import net.stash.pensive.plot.ChannelPlotter;
import net.stash.pensive.plot.FullPlotter;
import net.stash.pensive.plot.SubnetPlotter;
import net.stash.pensive.plot.ThumbnailPlotter;

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

	private final ChannelPlotter plot;
	private final ChannelPlotter thumb;

	/**
	 * Class constructor
	 * 
	 * @param channel
	 *            my channel
	 * @param index
	 *            my index into the plot
	 * @param plotDimension
	 *            Dimension of the full plot
	 * @param thumbDimension
	 *            Dimension of the thumbnail plot
	 * @param decorateX
	 *            If true decorate x-axis on full plot
	 * @param config
	 *            My config stanza
	 */
	public Channel(String channel, int index, Dimension plotDimension, Dimension thumbDimension, boolean decorateX,
			ConfigFile config) {
		this.name = channel;

		plot = new FullPlotter(channel, index, plotDimension, decorateX, config);
		thumb = new ThumbnailPlotter(channel, index, thumbDimension, config);

	}

	/**
	 * Gather new wave data and offer to plotters
	 * 
	 * @param plotEnd
	 *            Time of last sample of waveform
	 * @param dataSource
	 *            Who to ask for data
	 */
	public void updateWave(long plotEnd, SeismicDataSource dataSource) {
		double t2 = Util.ewToJ2K(plotEnd / 1000);
		double t1 = t2 - SubnetPlotter.DURATION_S;
		Wave w = dataSource.getWave(name.replace('_', ' '), t1, t2);
		SliceWave wave = null;
		if (w != null) {
			wave = new SliceWave(w);
			wave.setSlice(t1, t2);
		}
		plot.setWave(wave);
		thumb.setWave(wave);
	}

	/**
	 * Create a full plot
	 * 
	 * @return The plot Renderer
	 */
	public Renderer plot() {
		return plot.plot();
	}

	/**
	 * Create a thumbnail plot
	 * 
	 * @return The thumbnail Renderer
	 */
	public Renderer plotThumb() {
		return thumb.plot();
	}

}