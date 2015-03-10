var refreshPeriodMS = ${refreshPeriod} * 1000;
var timeFormatter = new SimpleDateFormat("HH:mm");
var dateFormatter = new SimpleDateFormat("d MMMM, yyyy");
var pathFormatter = new SimpleDateFormat("${filePathFormat}");
var fileFormatter = new SimpleDateFormat("${fileSuffixFormat}");

var endTime;
var startTime;
var rowSpanMS;
var mosaicSpanMS;

function init() {
	console.trace();
	rowSpanMS = 60 * 60 * 1000;
	mosaicSpanMS = 180 * 60 * 1000;
	endTime = new Date(getMostRecentEnd());
	startTime = new Date(endTime.getTime() - (refreshPeriodMS));

	parseParameters();	
	registerEventHandlers();
	$("#subnet").trigger("change");	

	updateTimeLabel();
	updateMosaic();
	
	/* leanModal init */
	$('form').submit(function(e){return false;});
    $("a[rel*='leanModal']").leanModal({ top: 110, overlay: 0.75, closeButton: ".hidemodal" });
}

function parseParameters() {
	var param = getUrlParameter("rowSpan");
	if ($.isNumeric(param)) {
		rowSpanMS = param * 60 * 1000;
	}

	param = getUrlParameter("mosaicSpan");
	if ($.isNumeric(param)) {
		mosaicSpanMS = param * 60 * 60 * 1000;
	}	
}

function registerEventHandlers() {
	$("#nextSubnet").on('click', {step: 1}, incrementSubnet);
	$("#perviousSubnet").on('click', {step: -1}, incrementSubnet);
	$("#currentImage").on('click', {step: (Math.pow(2,32) - 1)}, incrementTime);
	$("#nextImage").on('click', {step: 1}, incrementTime);
	$("#previousImage").on('click', {step: -1}, incrementTime);
	$("#mosaicButton").on('click', updateMosaic);
	$("#subnet").on('change', updateSubnet);
	
	$(".positiveInt").on('keyup', function () {this.value = this.value.replace(/[^0-9]/g,'');});
	}

/* The subnet changed, now what? */
function updateSubnet() {
	var subnet=$("#subnet option:selected").text();
	$("#subnetName").text(subnet);
	updateMosaic();
}

function updateMosaicOptions() {
	var spanMS=$("#mosaicSpan").val()  * 60 * 60 * 1000;
	mosaicSpanMS = spanMS;

	spanMS=$("#mosaicRowSpan").val() * 60 * 1000;
	rowSpanMS = refreshPeriodMS * Math.ceil(spanMS / refreshPeriodMS);
	
	updateMosaic();

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
	var span = endTime.getTime() - startTime.getTime();
	
	var newEnd = endTime.getTime() + (step * span);
	var newStart = startTime.getTime() + (step * span);
	
	if (newEnd <= new Date().getTime())
		endTime.setTime(endTime.getTime() + (step * span));
	else
		endTime.setTime(getMostRecentEnd());
	
	
	startTime.setTime(endTime.getTime() - span);
	updateMosaic(); 
}

function getMostRecentEnd() {
	var endTime = new Date();
	var n = endTime.getTime();

	return (n - (n % (refreshPeriodMS)));
}


function updateMosaic() {
	$("#mosaicButton").hide();
	$("#mosaicOptionsButton").show();

	var network = $("#network option:selected").text();
	var subnet = $("#subnet option:selected").text();

	var frame = $("#mainFrame");
	frame.empty();

	var mosaicEndMS = endTime.getTime();
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
			image.attr('src', url);
			image.on('click', {endTime: cellEnd}, displayPlot);
			
			cellEndMS += refreshPeriodMS;
		}		
		
		cell = $(document.createElement('td'));
		row.append(cell);
		cell.addClass("mosaicTitle");
		cell.html(timeFormatter.format(new Date(rowEndMS)) + " <span class=\"small\">UTC</span>");
		
		rowStartMS += rowSpanMS;
	}
}

function displayPlot(e) {
	$("#mosaicButton").show();
	$("#mosaicOptionsButton").hide();

	endTime = e.data.endTime;
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