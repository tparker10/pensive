package net.stash.pensive.plot;

import gov.usgs.plot.render.TextRenderer;
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
public class FullPlotter extends ChannelPlotter {

    public static final Font NO_DATA_FONT = Font.decode("dialog-PLAIN-36");
    public static final int LABEL_HEIGHT = 35;
    public static final int LABEL_WIDTH = 30;

    public FullPlotter(String name, int index, Dimension plotDimension, boolean decorateX, ConfigFile config) {
        super(name, index, plotDimension, config);

        spectrogramRenderer.xTickValues = decorateX;
        spectrogramRenderer.xUnits = decorateX;
        spectrogramRenderer.xLabel = decorateX;

        noDataFont = NO_DATA_FONT;
    }

    protected void tweakSpectrogramRenderer(SpectrogramRenderer spectrogramRenderer) {
        spectrogramRenderer.yTickMarks = true;
        spectrogramRenderer.yTickValues = true;
        spectrogramRenderer.xTickMarks = true;
        spectrogramRenderer.setYLabelText(name);

        int top = index * plotDimension.height;
        spectrogramRenderer.setLocation(LABEL_WIDTH, top + waveHeight + LABEL_HEIGHT, plotDimension.width
                - (2 * LABEL_WIDTH), plotDimension.height - waveHeight);
    }

    protected void tweakWaveRenderer(SliceWaveRenderer waveRenderer) {
        int top = index * plotDimension.height + LABEL_HEIGHT;
        int width = plotDimension.width;
        waveRenderer.setLocation(LABEL_WIDTH, top, width - 2 * LABEL_WIDTH, waveHeight);

        waveRenderer.xTickMarks = true;
    }

    protected void tweakNoDataRenderer(TextRenderer textRenderer) {
        textRenderer.y += LABEL_HEIGHT;
    }

}
