package net.stash.pensive.page.mosaic.daily;

import gov.usgs.util.ConfigFile;
import gov.usgs.util.Time;

import java.util.Date;
import java.util.Map;

import net.stash.pensive.page.mosaic.SgramMosaicSettings;

public class SgramDailyMosaicSettings extends SgramMosaicSettings {

	public final Date startTime;
	public final int duration;

	public SgramDailyMosaicSettings(ConfigFile cf) {
		super(cf);
		long now = System.currentTimeMillis();
		startTime = new Date(now - (now % (1000 * 60 * 60 * 24)));
		duration = 1440;
	}

	protected void setDefaults(ConfigFile cf) {
		super.setDefaults(cf);
	}

	public String generateFileName(Date time, String dateFormat,
			String subnetName) {
		StringBuilder sb = new StringBuilder();
		sb.append(subnetName);

		if (dateFormat != null)
			sb.append(Time.format(dateFormat, time));

		sb.append(fileSuffix);
		return sb.toString();
	}

	/**
	 * 
	 */
	public String getBareFileName() {
		return getBareDailyFileName();
	}

	/**
	 * 
	 */
	public String getTimeStampFileName() {
		return getTimeStampDailyFileName();
	}

	public String getNextMosaicTitle() {
		return "Recent Mosaic";
	}

	public String getNextMosaicPage() {
		return super.getBareFileName();
	}

	/**
	 * {@inheritDoc}
	 */
	public void applySettings(Map<String, Object> root) {
		super.applySettings(root);

		root.put("startTime", startTime);
		root.put("duration", duration);
		root.put("nextMosaicTitle", getNextMosaicTitle());
	}
}
