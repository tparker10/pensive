package net.stash.pensive.page;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import gov.usgs.util.ConfigFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.stash.pensive.image.SgramImageSettings;

/**
 * Create a page with a single subnetOgram image.
 * 
 * @author Tom Parker
 * 
 */
public class SgramSinglePage implements SgramPage {

    protected ConfigFile cf;
    protected static final Logger logger = Logger.getLogger("gov.usgs.subnetogram");
    protected SgramImageSettings imageSettings;
    protected SgramSinglePageSettings settings;
    protected String templateName;
    protected LinkedHashMap<String, String> subnetLinks;
    protected LinkedHashMap<String, String> networkLinks;

    protected String previousSubnetFile;
    protected String nextSubnetFile;
    protected String nextFile;
    protected boolean isCurrent;
    protected String fileName;
    protected String imageName;
    protected String nextPath;
    protected String nextRelPath;
    protected String nextFileName;
    protected String previousPath;
    protected String previousRelPath;
    protected String previousFileName;
    protected String currentMosaic;
    protected String currentFile;
    protected String currentDailyMosaic;
    protected String currentRelPath;
    protected Date nextTime;
    protected Date previousTime;
    protected String pathToRoot;

    /** Time of earliest data point */
    protected Date startTime;

    /** Time of last data point */
    protected Date endTime;

    /** Time zone offset in milliseconds */
    public final int timeZoneOffset;

    Configuration cfg;

    public SgramSinglePage(SgramSinglePageSettings pageSettings, SgramImageSettings imageSettings) {

        if (pageSettings == null)
            throw new RuntimeException("null pageSettings");

        if (imageSettings == null)
            throw new RuntimeException("null imageSettings");

        this.imageSettings = imageSettings;
        this.settings = pageSettings;

        pathToRoot = settings.getRelativePath(settings.getBareFilePath(), settings.pathRoot);

        networkLinks = getNetworkLinks();
        subnetLinks = getSubnetLinks();
        previousSubnetFile = generateSubnetAddress(settings.previousSubnetName);
        nextSubnetFile = generateSubnetAddress(settings.nextSubnetName);

        // startTime is optional, but has rules. Lots of rules.
        // TODO: figure this stuff out
        Calendar cal = Calendar.getInstance();
        cal.setTime(settings.startTime);
        cal.setTimeZone(settings.timeZone);
        cal.add(Calendar.MINUTE, -settings.embargo);

        // always reference zero milliseconds
        cal.add(Calendar.MILLISECOND, -1 * ((int) (cal.getTimeInMillis() % 1000)));

        if (settings.onMark)
            cal.add(Calendar.SECOND, -1 * ((int) (cal.getTimeInMillis() / 1000) % (settings.period * 60)));

        startTime = cal.getTime();

        cal.add(Calendar.SECOND, settings.duration * 60);
        endTime = cal.getTime();

        timeZoneOffset = settings.timeZone.getOffset(endTime.getTime());

        cal.setTime(endTime);
        cal.add(Calendar.MINUTE, settings.duration);
        nextTime = cal.getTime();

        cal.add(Calendar.MINUTE, -2 * settings.duration);
        previousTime = cal.getTime();

        nextPath = settings.generateFilePath(nextTime, settings.nextFilePathDateFormat, settings.subnetName);
        nextRelPath = settings.getRelativePath(settings.getBareFilePath(), nextPath);
        nextFileName = settings.generateFileName(nextTime, settings.nextFileNameDateFormat, settings.subnetName);

        isCurrent = true;

        templateName = "subnetOgramImage";
        logger.finest("creating new SgramPage");

        fileName = settings.getBareFilePath() + settings.getBareFileName() + settings.fileExtension;
        imageName = settings.getRelativePath(settings.getBareFilePath(), imageSettings.getBareFilePath())
                + imageSettings.getBareFileName();

        previousPath = settings
                .generateFilePath(previousTime, settings.previousFilePathDateFormat, settings.subnetName);
        previousRelPath = settings.getRelativePath(settings.getBareFilePath(), previousPath);
        previousFileName = settings.generateFileName(previousTime, settings.previousFileNameDateFormat,
                settings.subnetName);

        currentRelPath = settings.getRelativePath(settings.getBareFilePath(), settings.pathRoot);
        if (settings.networkName != null)
            currentRelPath += settings.networkName + '/';
        currentFile = currentRelPath + settings.subnetName + '/' + settings.subnetName + settings.fileSuffix
                + settings.fileExtension;
        currentDailyMosaic = currentRelPath + settings.subnetName + '/' + settings.subnetName + "Daily"
                + settings.mosaicSuffix + settings.fileExtension;
        currentMosaic = currentRelPath + settings.subnetName + '/' + settings.subnetName + settings.mosaicSuffix
                + settings.fileExtension;

        try {
            initializeTemplateEngine();
        } catch (IOException e) {
            fatalError(e.getLocalizedMessage());
        }
    }

    protected void initializeTemplateEngine() throws IOException {
        cfg = new Configuration();

        TemplateLoader[] loaders = new TemplateLoader[2];
        loaders[0] = new FileTemplateLoader(new File("templates"));
        loaders[1] = new ClassTemplateLoader(getClass(), "/templates/");
        MultiTemplateLoader mtl = new MultiTemplateLoader(loaders);
        cfg.setTemplateLoader(mtl);

        DefaultObjectWrapper obj = new DefaultObjectWrapper();
        obj.setExposeFields(true);
        cfg.setObjectWrapper(obj);

        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        cfg.setIncompatibleImprovements(new Version(2, 3, 20));
    }

    protected String generateSubnetAddress(String subnet) {
        String nextSubnetPath = settings.generateFilePath(endTime, null, subnet);
        String nextSubnetRelPath = settings.getRelativePath(settings.getBareFilePath(), nextSubnetPath);
        String nextSubnetFileName = settings.generateFileName(endTime, null, subnet);

        return nextSubnetRelPath + nextSubnetFileName + settings.fileExtension;
    }

    protected String generateNetworkAddress(String network) {
        return pathToRoot + network + File.separator + "index" + settings.fileExtension;
    }

    protected LinkedHashMap<String, String> getSubnetLinks() {
        LinkedHashMap<String, String> subnetLinks = new LinkedHashMap<String, String>();
        for (String subnet : settings.subnets)
            subnetLinks.put(subnet, generateSubnetAddress(subnet));

        return subnetLinks;
    }

    protected LinkedHashMap<String, String> getNetworkLinks() {
        LinkedHashMap<String, String> networkLinks = new LinkedHashMap<String, String>();
        for (String network : settings.networks)
            networkLinks.put(network, generateNetworkAddress(network));

        return networkLinks;
    }

    protected void applySettings(Map<String, Object> root) {
        root.put("imageSettings", imageSettings);
        root.put("fileName", fileName);
        root.put("imageName", imageName);
        root.put("mosaicAddress", currentMosaic);
        root.put("nextMosaicAddress", currentMosaic);

        root.put("subnetLinks", subnetLinks);
        root.put("networkLinks", networkLinks);
        root.put("previousSubnetFile", previousSubnetFile);
        root.put("nextSubnetFile", nextSubnetFile);
        root.put("nextFile", nextRelPath + nextFileName + settings.fileExtension);
        root.put("previousFile", previousRelPath + previousFileName + settings.fileExtension);
        root.put("isCurrent", isCurrent);
        root.put("currentFile", currentFile);
        root.put("currentDailyMosaic", currentDailyMosaic);
        root.put("startTime", startTime);
        root.put("endTime", endTime);
        root.put("timeZoneOffset", timeZoneOffset);
        root.put("textStartTime", settings.textTimeFormat.format(startTime));
        root.put("textStartDate", settings.textDateFormat.format(startTime));
        root.put("pathToRoot", pathToRoot);
    }

    protected void applyTemplate() {
        Map<String, Object> root = new HashMap<String, Object>();
        settings.applySettings(root);
        applySettings(root);

        try {
            Template template = cfg.getTemplate("duff/" + templateName + ".html");
            FileWriter fw = new FileWriter(fileName);
            template.process(root, fw);
            fw.close();
        } catch (IOException e) {
            fatalError(e.getLocalizedMessage());
        } catch (TemplateException e) {
            fatalError(e.getLocalizedMessage());
        }
    }

    public void fatalError(String msg) {
        logger.severe(msg);
        if (settings.onError.equals("exit"))
            System.exit(1);
    }

    public void generateHTML() {
        applyTemplate();
    }

}