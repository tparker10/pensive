package net.stash.pensive.page.mosaic;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import net.stash.pensive.image.SgramThumbnailSettings;
import net.stash.pensive.page.SgramSinglePage;

/**
 * A class to create a page with a mosaiced array of SubnetOgram thumbnail
 * images, each with a link to the full page.
 * 
 * @author Tom Parker
 */
public class SgramMosaic extends SgramSinglePage {

	protected LinkedList<SgramThumbnailSettings> thumbnails;
	protected SgramMosaicSettings settings;

	public SgramMosaic(SgramMosaicSettings settings, SgramThumbnailSettings thumbnailSettings) {
		super(settings, thumbnailSettings);

		isCurrent = true;
		this.settings = settings;

		this.templateName = "subnetOgramMosaic";
		try {
			initializeTemplateEngine();
		} catch (IOException e) {
			fatalError(e.getLocalizedMessage());
		}

		thumbnails = createThumbnailSettings(startTime, thumbnailSettings);
	}

	protected LinkedList<SgramThumbnailSettings> createThumbnailSettings(Date startTime,
			SgramThumbnailSettings thumbnailSettings) {

		LinkedList<SgramThumbnailSettings> thumbnails = new LinkedList<SgramThumbnailSettings>();

		LinkedList<SgramThumbnailSettings> thumbs = new LinkedList<SgramThumbnailSettings>();
		while (!thumbnailSettings.startTime.before(startTime)) {
			thumbs.offerFirst(thumbnailSettings);
			thumbnailSettings = thumbnailSettings.getPrevious();
		}

		while (!thumbs.isEmpty())
			thumbnails.add(thumbs.removeFirst());

		return thumbnails;
	}


	protected void applySettings(Map<String, Object> root) {
		super.applySettings(root);
		root.put("thumbnails", thumbnails);
		root.put("templateDir", settings.getRelativePath(fileName, settings.pathRoot) + "templates/"
				+ settings.template + "/");
		root.put("nextMosaicAddress",
				settings.getRelativePath(fileName, settings.getBareFilePath()) + settings.getNextMosaicPage()
						+ settings.fileExtension);
	}
}