var refreshPeriodMS = ${refreshPeriod} * 1000;
var timeFormatter = new SimpleDateFormat("HH:mm");
var pathFormatter = new SimpleDataFormat(${filePathFormat});
var fileFormatter = new SimpleDataFormat(${fileSuffixFormat});

var endTime;
var startTime;
var rowSpanM;
var mosaicSpanM;

function init() {
	rowSpanM = 60;
	mosaicSpanM = 180;
	endTime = new Date(getMostRecentEnd());
	registerEventHandlers();

	startTime = new Date(endTime.getTime() - (refreshPeriodMS));
	updateTime();
	updateMosaic();
}


function registerEventHandlers() {
	$("#nextSubnet").on('click', {step: 1}, incrementSubnet);
	$("#perviousSubnet").on('click', {step: -1}, incrementSubnet);
	$("#nextImage").on('click', {step: 1}, incrementTime);
	$("#previousImage").on('click', {step: -1}, incrementTime);
	
	$("#subnet").on('change', updateSubnet);
	$("#subnet").trigger("change");	
}


/* The subnet changed, now what? */
function updateSubnet() {
	var subnet=$("#subnet option:selected").text();
	$("#subnetName").text(subnet);
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
function updateTime() {
		$("#timeSpan").text(timeFormatter.format(startTime) + " - " + timeFormatter.format(endTime));
}

function incrementTime(e) {
	var step = e.data.step;
	var span = endTime.getTime() - startTime.getTime();
	
	endTime.setTime(endTime.getTime() + (step * span));
	startTime.setTime(startTime.getTime() + (step * span));
	
	updateTime();
	updateMosaic();
}

function getMostRecentEnd() {
	endTime = new Date();
	var n = endTime.getTime();

	return (n - (n % (refreshPeriodMS)));
}


function updateMosaic() {
	var frame = $("#mainFrame");
	var mosaicStartMS = endTime.getTime() - (mosaicSpanM * 60 * 1000);
	var rowEndMS = mosaicStartMS + rowSpanM * 60 * 1000;
	var mosaicEndMS = endTime.getTime();
	var endTimeMS = endTime.getTime();
	
	startTime.setTime(mosaicStartMS);
	updateTime();
	
	frame.empty();
	var table = $(document.createElement('table'));
	frame.append(table);
	while (rowEndMS <= endTimeMS) {
		var rowEnd = new Date(rowEndMS);
		var rowStart = new Date(rowEndMS - (rowSpanM * 60 * 1000));
		var row = $(document.createElement('tr'));
		row.addClass("mosaic");
		table.append(row);
		
		var cell = $(document.createElement('td'));
		cell.addClass("mosaicTitle");
		cell.text(timeFormatter.format(rowStart));
		row.append(cell);

		while (cellEnd < rowEnd) {
			cell = $(document.createElement('td'));
			row.append(cell);
			var image = $(document.createElement('img'));
			cell.append(image);
			
			var url = 
			image.
			cellEnd += refreshPeriodMS;
		}		
		cell = $(document.createElement('td'));
		cell.addClass("mosaicTitle");
		cell.text(timeFormatter.format(rowEnd));
		row.append(cell);
		
		rowEndMS += rowSpanM * 60 * 1000;
	}
}












