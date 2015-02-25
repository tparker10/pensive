package net.stash.pensive;

import gov.usgs.util.Log;

import java.util.logging.Logger;

public class PlotGenerator extends Thread {
    private static final Logger LOGGER = Log.getLogger("net.stash.pensive");

    private String host;
    private int port;
    
    public PlotGenerator(String host, int port) {
        this.host = host;
        this.port = port;
    }
}
