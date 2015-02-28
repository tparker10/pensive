package net.stash.pensive.plot;

import gov.usgs.plot.render.wave.SliceWaveRenderer;
import gov.usgs.plot.render.wave.SpectrogramRenderer;
import gov.usgs.util.ConfigFile;

import java.awt.Dimension;
import java.awt.Font;

/**
 * 
 * @author Tom Parker
 * 
 */
public class ChannelThumbnail extends AbstractChannelPlot {
    public static final Font NO_DATA_FONT = Font.decode("dialog-PLAIN-12");

    public ChannelThumbnail(String name, int index, Dimension plotDimension, ConfigFile config) {
        super(name, index, plotDimension, config);
        // TODO Auto-generated constructor stub
    }

    /**
     * 
     * @param config
     * @return
     */
    protected void tweakSpectrogramRenderer(SpectrogramRenderer spectrogramRenderer) {

        // y-axis labels will sometimes not be displayed if x-axis tick marks
        // are not displayed. Note sure why.
        spectrogramRenderer.yTickMarks = false;
        spectrogramRenderer.yTickValues = false;
        spectrogramRenderer.xTickMarks = false;
        spectrogramRenderer.xTickValues = false;
        spectrogramRenderer.xUnits = false;
        spectrogramRenderer.xLabel = false;

        int top = index * plotDimension.height + waveHeight;
        spectrogramRenderer.setLocation(0, top, plotDimension.width, plotDimension.height - waveHeight);
    }

    @Override
    protected void tweakWaveRenderer(SliceWaveRenderer waveRenderer) {
        int top = index * plotDimension.height;
        int width = plotDimension.width;
        waveRenderer.setLocation(0, top, width, waveHeight);

    }

}
