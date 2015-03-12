package net.stash.pensive.page;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import gov.usgs.util.ConfigFile;
import gov.usgs.util.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.stash.pensive.plot.SubnetPlotter;

public class Page {
    /** my logger */
    private static final Logger LOGGER = Log.getLogger("gov.usgs");

    public static final String FILENAME = "html/pensive.html";

    Map<String, Object> root;
    Configuration cfg;

    List<String> networks;
    List<String> subnets;

    public Page(ConfigFile config) {
        root = new HashMap<String, Object>();
        
        networks = new LinkedList<String>();
        root.put("networks", networks);
        
        subnets = new LinkedList<String>();
        root.put("subnets", subnets);
        
        root.put("refreshPeriod", SubnetPlotter.DURATION_S);
        root.put("filePathFormat", config.getString("filePathFormat"));
        root.put("fileSuffixFormat", config.getString("fileNameSuffixFormat"));
     
        try {
            initializeTemplateEngine();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "cannot write HTML");
        }

    }

    protected void initializeTemplateEngine() throws IOException {
        cfg = new Configuration();
        cfg.setTemplateLoader(new ClassTemplateLoader(getClass(), "/net/stash/pensive/page"));
        DefaultObjectWrapper obj = new DefaultObjectWrapper();
        obj.setExposeFields(true);
        cfg.setObjectWrapper(obj);

        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        cfg.setIncompatibleImprovements(new Version(2, 3, 20));
    }

    public void writeHTML() {
        try {
            Template template = cfg.getTemplate("pensive.html");
            FileWriter fw = new FileWriter("html/pensive.html");
            template.process(root, fw);
            fw.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
        } catch (TemplateException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
        }
    }

    public void addNetwork(String network) {
        networks.add(network);
    }

    public void addSubnet(String subnet) {
        subnets.add(subnet);
    }

}
