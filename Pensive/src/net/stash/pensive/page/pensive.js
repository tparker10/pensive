/**
 * pensive.js
 *
 * A faithful JavaScript implementation of Java's SimpleDateFormat's format
 * method. All pattern layouts present in the Java implementation are
 * implemented here except for z, the text version of the date's time zone.
 *
 * Author: Tom Parker
 */
 
/* ------------------------------------------------------------------------- */

/**  time span of a single image in ms. */
var refreshPeriodMs = 1000 * +'${refreshPeriod}';

/** format used to display time tags  */
var timeFormatter = new SimpleDateFormat("HH:mm");

/** format used to display long time string at top of page */
var dateFormatter = new SimpleDateFormat("d MMMM, yyyy");

/** format used to form path to images. Passed in from config file */
var pathFormatter = new SimpleDateFormat("${filePathFormat}");

/** format used to form image suffix. Passed in from config file */
var fileFormatter = new SimpleDateFormat("${fileSuffixFormat}");

/** end time */
var mosaicEnd;

var cellEnd;

/** start time displayed */
var startTime;

/** end time displayed */
var endTime;

/** span of a single mosaic row in ms.*/
var rowSpanMs;

/** span of a mosaic in ms */
var mosaicSpanMs;

var mode;

/**
 *  Handle all initialization stuff here.
 */
function init() {
	mode = "mosaic";
	mosaicEnd = new Date(getMostRecentEnd());
	cellEnd = new Date(getMostRecentEnd());
	rowSpanMs = hToMs(1);
	mosaicSpanMs = hToMs(3);
	endTime = new Date();
	startTime = new Date();
	
	if ($("#network option").size() == 1) {
		$("#network").hide();
	}

	parseParameters();	
	registerEventHandlers();

	// leanModal init
	$('form').submit(function(e){return false;});
	$("a[rel*='leanModal']").leanModal({ top: 110, overlay: 0.75, closeButton: ".hidemodal" });

	// fire subnet change trigger to get things rolling
	$("#subnet").trigger("change");	
}

function getMostRecentEnd() {
	var endTime = new Date();
	var n = endTime.getTime();
	n += endTime.getTimezoneOffset()*60*1000
	return (n - (n % (refreshPeriodMs)));
}

function updateMainFrame(e) {
	if (e != null) {
		if (e.data.mode != null)
			mode = e.data.mode;
		if (e.data.cellEnd != null)
			cellEnd = e.data.cellEnd;
	}

	if (mode == "singlePlot") {
		displayPlot();
	} else {		
		displayMosaic();
	}	
}
/**
 * Parse request parameters. Only used when page is first loaded.
 */
function parseParameters() {
	var param;
	
	param = getUrlParameter("mode");
	if (param != null)
		mode = param;
	
	param = getUrlParameter("subnet");
	if (param != null)
		$("#subnet").val(decodeURIComponent(param));
	
	param = getUrlParameter("cellEndM");
	if (param != null)
		cellEnd = new Date(mToMs(param));

	param = getUrlParameter("mosaicEndM");
	if (param != null)
		mosaicEnd = new Date(mToMs(param));

	/** a single URL parameter */
	param = getUrlParameter("rowSpanM");
	if ($.isNumeric(param))
		rowSpanMs = mToMs(param);
	
	param = getUrlParameter("mosaicSpanH");
	if ($.isNumeric(param))
		mosaicSpanMs = hToMs(param);
}

/**
 * register my event handlers.
 */
function registerEventHandlers() {
	$("#nextSubnet").on('click', {step: 1}, incrementSubnet);
	$("#perviousSubnet").on('click', {step: -1}, incrementSubnet);
	$("#currentImage").on('click', {step: (Math.pow(2,32) - 1)}, incrementTime);
	$("#nextImage").on('click', {step: 1}, incrementTime);
	$("#previousImage").on('click', {step: -1}, incrementTime);
	$("#mosaicButton").on('click', {mode: "mosaic"}, updateMainFrame);
	$("#subnet").on('change', updateSubnet);
	$("#permalinkButton").on('click', populatePermalink);
	$(".positiveInt").on('keyup', function () {this.value = this.value.replace(/[^0-9]/g,'');});
}

function populatePermalink() {
	var URL = window.location.href
	
	var q = URL.indexOf('?');
	if (q != -1)
		URL = URL.substring(0,q);
	
	URL += "?mode=" + mode;
	URL += "&subnet=" + encodeURIComponent($("#subnet option:selected").val());
	
	if (mode == "singlePlot") {
		URL += "&cellEndM=" + msToM(cellEnd.getTime());
		
	} else {
		URL += "&mosaicEndM=" + msToM(mosaicEnd.getTime());
		URL += "&rowSpanM=" + msToM(rowSpanMs);
		URL += "&mosaicSpanH=" + msToH(mosaicSpanMs);
	}
	
	var link = $(document.createElement('a'));
	$("#permalinkURL").empty();
	$("#permalinkURL").append(link);
	link.attr('href', URL);
	link.addClass('permalink');
	link.text(URL);
}

/* The subnet changed, now what? */
function updateSubnet() {
	var subnet=$("#subnet option:selected").text();
	$("#subnetName").text(subnet);
	updateMainFrame();
}

function updateMosaicOptions() {
	var spanMs=hToMs($("#mosaicSpanH").val());
	mosaicSpanMs = spanMs;

	spanMs=mToMs($("#mosaicRowSpanM").val());
	rowSpanMs = refreshPeriodMs * Math.ceil(spanMs / refreshPeriodMs);
	
	updateMainFrame();

}

/* move selected subnet up or down */
function incrementSubnet(e) {
	var step = e.data.step;
	var idx = $("#subnet").prop("selectedIndex");
	var count = $("#subnet option").length;
	$("#subnet").prop("selectedIndex",(idx+step+count) % count);
	$("#subnet").trigger("change");	
}

/* The time changed, now what? */
function updateTimeLabel() {
		$("#timeSpan").text(dateFormatter.format(startTime) + " " + timeFormatter.format(startTime) + " - " + timeFormatter.format(endTime) + " UTC");
}

function incrementTime(e) {
	var step = e.data.step;
	if (mode == "singlePlot") {
		var newEndMs = cellEnd.getTime() + (step * refreshPeriodMs);
		
		if (newEndMs <= getMostRecentEnd())
			cellEnd.setTime(newEndMs);
		else
			cellEnd.setTime(getMostRecentEnd());		
	} else {
		var newEndMs = mosaicEnd.getTime() + (step * mosaicSpanMs);
		
		if (newEndMs <= getMostRecentEnd())
			mosaicEnd.setTime(newEndMs);
		else
			mosaicEnd.setTime(getMostRecentEnd());		
	}
	
	updateMainFrame(); 
}

function displayMosaic() {
	$("#mosaicButton").hide();
	$("#mosaicOptionsButton").show();
	mode = "mosaic";
	
	var network = $("#network option:selected").text();
	var subnet = $("#subnet option:selected").text();

	var frame = $("#mainFrame");
	frame.empty();

	var mosaicEndMs = mosaicEnd.getTime();
	var mosaicStartMs = mosaicEndMs - mosaicSpanMs;
	startTime.setTime(mosaicStartMs);
	endTime.setTime(mosaicEndMs);
	updateTimeLabel();
	var table = $(document.createElement('table'));
	frame.append(table);
	table.addClass('center');

	var rowStartMs = mosaicStartMs;
	while (rowStartMs < mosaicEndMs) {
		var cell;
		
		var rowStart = new Date(rowStartMs);
		var rowEndMs = rowStartMs + rowSpanMs;

		var row = $(document.createElement('tr'));
		table.append(row);
		row.addClass("mosaic");

		cell = $(document.createElement('td'));
		row.append(cell);
		cell.addClass("mosaicTitle");
		cell.html(timeFormatter.format(rowStart) + " <span class=\"small\">UTC</span>");

		var cellEndMs = rowStart.getTime() + refreshPeriodMs; 
		while (cellEndMs <= rowEndMs) {
			cell = $(document.createElement('td'));
			row.append(cell);
			cell.addClass("mosaic");

			var cellEnd = new Date(cellEndMs);
			var url = network + "/" + subnet + "/" + pathFormatter.format(cellEnd) + "/" + subnet + fileFormatter.format(cellEnd) + "_thumb.png";

			var image = $(document.createElement('img'));
			cell.append(image);
			image.addClass("mosaic");
			image.attr('src', url);
			image.on('click', { mode: "singlePlot", cellEnd: cellEnd }, updateMainFrame);
			image.on('error', imageNotFound);
			
			cellEndMs += refreshPeriodMs;
		}		
		
		cell = $(document.createElement('td'));
		row.append(cell);
		cell.addClass("mosaicTitle");
		cell.html(timeFormatter.format(new Date(rowEndMs)) + " <span class=\"small\">UTC</span>");
		
		rowStartMs += rowSpanMs;
	}
}

function imageNotFound(e) {
	var URL = $(e.target).attr('src');
	var cell = $(e.target).parent();
	cell.empty();
	cell.removeClass("mosaic");
	cell.addClass("badImage");
	var link = $(document.createElement('a'));
	cell.append(link);
	link.attr('href', URL);
	link.html("Image not<br>available");
	link.addClass("badImage");
}

function displayPlot() {
	$("#mosaicButton").show();
	$("#mosaicOptionsButton").hide();
	
	endTime.setTime(cellEnd);
	startTime.setTime(cellEnd - refreshPeriodMs);
	updateTimeLabel();
	
	var frame = $("#mainFrame");
	frame.empty();

	var image = $(document.createElement('img'));
	frame.append(image);

	var network = $("#network option:selected").text();
	var subnet = $("#subnet option:selected").text();
	var url = network + "/" + subnet + "/" + pathFormatter.format(cellEnd) + "/" + subnet + fileFormatter.format(cellEnd) + ".png";
	image.attr('src', url);
}

