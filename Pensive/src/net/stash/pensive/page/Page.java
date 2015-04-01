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
import gov.usgs.util.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import net.stash.pensive.plot.SubnetPlotter;

/**
 * 
 * @author Tom Parker
 * 
 */
public class Page {
    /** my logger */
    private static final Logger LOGGER = Log.getLogger("gov.usgs");

    public static final String DEFAULT_PATH_ROOT = "html/";
    
    /** filename of html file */
    public static final String FILENAME = "index.html";

    /** Freemarker settings */
    private Map<String, Object> root;
    
    /** my configuration */
    private Configuration cfg;
    
    /** My subnets */
    private Map<String, List<String>> subnets;
    
    /** root of output*/
    private final String pathRoot;
    
    /**
     * Class constructor
     * 
     * @param config
     *          My configuration stanza
     */
    public Page(ConfigFile config) {
        pathRoot = Util.stringToString(config.getString("pathRoot"), DEFAULT_PATH_ROOT);
        
        root = new HashMap<String, Object>();
        
        subnets = new HashMap<String, List<String>>();
        root.put("subnets", subnets);
        
        root.put("refreshPeriod", SubnetPlotter.DURATION_S);
        root.put("filePathFormat", config.getString("filePathFormat"));
        root.put("fileSuffixFormat", config.getString("fileNameSuffixFormat"));
        root.put("selectedNetwork", config.getString("selectedNetwork"));
        
        try {
            initializeTemplateEngine();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "cannot write HTML");
        }

    }

    /**
     * Initialize FreeMarker
     * @throws IOException
     */
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
    
    /**
     * Write my html page
     */
    public void writeHTML() {
        try {
            Template template = cfg.getTemplate("pensive.html");
            String file = pathRoot + '/' + FILENAME;
            file.replace("/+", "/");
            file.replace("/", Matcher.quoteReplacement(File.separator));
            FileWriter fw = new FileWriter(file);
            template.process(root, fw);
            fw.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
        } catch (TemplateException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
        }
    }

    /** add a subnet to a network list */
    public void addSubnet(String network, String subnet) {
    	List<String> s = subnets.get(network);
    	if (s == null) {
    		s = new ArrayList<String>();
    		subnets.put(network, s);
    	}
        s.add(subnet);
    }
}
