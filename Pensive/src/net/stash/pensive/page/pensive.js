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
var refreshPeriodMS = 1000 * +'${refreshPeriod}';

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

/** startTime */
var startTime;

/** span of a single mosaic row in ms.*/
var rowSpanMS;

/** span of a mosaic in ms */
var mosaicSpanMS;

var mode;

/**
 *  Handle all initialization stuff here.
 */
function init() {
	rowSpanMS = 60 * 60 * 1000;
	mosaicSpanMS = 180 * 60 * 1000;
	mosaicEnd = new Date(getMostRecentEnd());
	startTime = new Date(mosaicEnd.getTime() - (refreshPeriodMS));
	mode = "mosaic";
	
	parseParameters();	
	registerEventHandlers();

	$("#subnet").trigger("change");	

	updateTimeLabel();

	/* leanModal init */
	$('form').submit(function(e){return false;});
	$("a[rel*='leanModal']").leanModal({ top: 110, overlay: 0.75, closeButton: ".hidemodal" });
}

function updateMainFrame(e) {
	if (e != null) {
		if (e.data.mode != null)
			mode = e.data.mode;
		if (e.data.cellEnd != null)
			cellEnd = e.data.cellEnd;
	}

	if (mode == "singlePlot") {
		displayPlot(e.data.endTime);
	} else {		
		updateMosaic();
	}	
}
/**
 * Parse request parameters. Only used when page is first loaded.
 */
function parseParameters() {
	
	/** a single URL parameter */
	var param = getUrlParameter("rowSpan");
	if ($.isNumeric(param)) {
		rowSpanMS = param * 60 * 1000;
	}

	param = getUrlParameter("mosaicSpan");
	if ($.isNumeric(param)) {
		mosaicSpanMS = param * 60 * 60 * 1000;
	}	
	
	param = getUrlParameter("mode");
	if (param != null) {
		mode = param;
	}
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
	$("#permalinkURL").text("ss" + window.location.href);
}

/* The subnet changed, now what? */
function updateSubnet() {
	var subnet=$("#subnet option:selected").text();
	$("#subnetName").text(subnet);
	updateMainFrame();
}

function updateMosaicOptions() {
	var spanMS=$("#mosaicSpan").val()  * 60 * 60 * 1000;
	mosaicSpanMS = spanMS;

	spanMS=$("#mosaicRowSpan").val() * 60 * 1000;
	rowSpanMS = refreshPeriodMS * Math.ceil(spanMS / refreshPeriodMS);
	
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
		$("#timeSpan").text(dateFormatter.format(startTime) + " " + timeFormatter.format(startTime) + " - " + timeFormatter.format(mosaicEnd) + " UTC");
}

function incrementTime(e) {
	var step = e.data.step;
	var span = mosaicEnd.getTime() - startTime.getTime();
	
	var newEnd = mosaicEnd.getTime() + (step * span);
	var newStart = startTime.getTime() + (step * span);
	
	if (newEnd <= getMostRecentEnd())
		mosaicEnd.setTime(mosaicEnd.getTime() + (step * span));
	else
		mosaicEnd.setTime(getMostRecentEnd());
	
	
	startTime.setTime(mosaicEnd.getTime() - span);
	updateMainFrame(); 
}

function getMostRecentEnd() {
	var endTime = new Date();
	var n = endTime.getTime();
	n += endTime.getTimezoneOffset()*60*1000
	return (n - (n % (refreshPeriodMS)));
}


function updateMosaic() {
	$("#mosaicButton").hide();
	$("#mosaicOptionsButton").show();

	var network = $("#network option:selected").text();
	var subnet = $("#subnet option:selected").text();

	var frame = $("#mainFrame");
	frame.empty();

	var mosaicEndMS = mosaicEnd.getTime();
	var mosaicStartMS = mosaicEndMS - mosaicSpanMS;
	startTime.setTime(mosaicStartMS);
	updateTimeLabel();

	var table = $(document.createElement('table'));
	frame.append(table);
	table.addClass('center');

	var rowStartMS = mosaicStartMS;
	while (rowStartMS < mosaicEndMS) {
		var cell;
		
		var rowStart = new Date(rowStartMS);
		var rowEndMS = rowStartMS + rowSpanMS;

		var row = $(document.createElement('tr'));
		table.append(row);
		row.addClass("mosaic");

		cell = $(document.createElement('td'));
		row.append(cell);
		cell.addClass("mosaicTitle");
		cell.html(timeFormatter.format(rowStart) + " <span class=\"small\">UTC</span>");

		var cellEndMS = rowStart.getTime() + refreshPeriodMS; 
		while (cellEndMS <= rowEndMS) {
			cell = $(document.createElement('td'));
			row.append(cell);
			cell.addClass("mosaic");

			var cellEnd = new Date(cellEndMS);
			var url = network + "/" + subnet + "/" + pathFormatter.format(cellEnd) + "/" + subnet + fileFormatter.format(cellEnd) + "_thumb.png";

			var image = $(document.createElement('img'));
			cell.append(image);
			image.addClass("mosaic");
			image.attr('src', url);
			image.on('click', { mode: "singlePlot", endTime: cellEnd }, updateMainFrame);
			
			cellEndMS += refreshPeriodMS;
		}		
		
		cell = $(document.createElement('td'));
		row.append(cell);
		cell.addClass("mosaicTitle");
		cell.html(timeFormatter.format(new Date(rowEndMS)) + " <span class=\"small\">UTC</span>");
		
		rowStartMS += rowSpanMS;
	}
}

function displayPlot(et) {
	$("#mosaicButton").show();
	$("#mosaicOptionsButton").hide();
	var endTime = et;
	startTime.setTime(endTime - refreshPeriodMS);
	updateTimeLabel();
	
	var frame = $("#mainFrame");
	frame.empty();

	var image = $(document.createElement('img'));
	frame.append(image);

	var network = $("#network option:selected").text();
	var subnet = $("#subnet option:selected").text();
	var url = network + "/" + subnet + "/" + pathFormatter.format(endTime) + "/" + subnet + fileFormatter.format(endTime) + ".png";
	image.attr('src', url);
}

/* http://www.jquerybyexample.net/2012/06/get-url-parameters-using-jquery.html */
function getUrlParameter(sParam)
{
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++) 
    {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] == sParam) 
        {
            return sParameterName[1];
        }
    }
}   