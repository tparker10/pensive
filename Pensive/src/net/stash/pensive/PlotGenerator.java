package net.stash.pensive;

public class PlotGenerator extends Thread {

    String host;
    int port;
    
    public PlotGenerator(String host, int port) {
        this.host = host;
        this.port = port;
    }
}
