var refreshPeriodMS = ${refreshPeriod} * 1000;
var timeFormatter = new SimpleDateFormat("HH:mm");
var pathFormatter = new SimpleDateFormat("${filePathFormat}");
var fileFormatter = new SimpleDateFormat("${fileSuffixFormat}");

var endTime;
var startTime;
var rowSpanM;
var mosaicSpanM;

function init() {
	rowSpanM = 60;
	mosaicSpanM = 180;
	endTime = new Date(getMostRecentEnd());
	startTime = new Date(endTime.getTime() - (refreshPeriodMS));
	
	registerEventHandlers();

	updateTime();
	updateMosaic();
}


function registerEventHandlers() {
	$("#nextSubnet").on('click', {step: 1}, incrementSubnet);
	$("#perviousSubnet").on('click', {step: -1}, incrementSubnet);
	$("#currentImage").on('click', {step: (Math.pow(2,32) - 1)}, incrementTime);
	$("#nextImage").on('click', {step: 1}, incrementTime);
	$("#previousImage").on('click', {step: -1}, incrementTime);
	$("#mosaicSpan").on('change', updateMosaicSpan);
	$("#mosaicRowSpan").on('change', updateMosaicRowSpan);
	
	$("#subnet").on('change', updateSubnet);
	$("#subnet").trigger("change");	
}


/* The subnet changed, now what? */
function updateSubnet() {
	var subnet=$("#subnet option:selected").text();
	$("#subnetName").text(subnet);
	updateMosaic();
}

/* The subnet changed, now what? */
function updateMosaicSpan() {
	var span=$("#mosaicSpan option:selected").text();
	mosaicSpanM = (span * 60);
	updateMosaic();
}

/* The subnet changed, now what? */
function updateMosaicRowSpan() {
	var span=$("#mosaicRowSpan").text();
	alert(span);
	rowSpanM = span * refreshPeriodMS;
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
function updateTime() {
		$("#timeSpan").text(timeFormatter.format(startTime) + " - " + timeFormatter.format(endTime));
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
	var frame = $("#mainFrame");
	var mosaicStartMS = endTime.getTime() - (mosaicSpanM * 60 * 1000);
	var rowEndMS = mosaicStartMS + rowSpanM * 60 * 1000;
	var mosaicEndMS = endTime.getTime();
	var endTimeMS = endTime.getTime();
	
	startTime.setTime(mosaicStartMS);
	updateTime();
	
	frame.empty();
	var table = $(document.createElement('table'));
	table.addClass('center');
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

		var cellEndMS = rowStart.getTime() + refreshPeriodMS; 
		while (cellEndMS < rowEndMS) {
			cell = $(document.createElement('td'));
			row.append(cell);
			var image = $(document.createElement('img'));
			cell.append(image);
			
			var network = $("#network option:selected").text();
			var subnet = $("#subnet option:selected").text();
			var cellEnd = new Date(cellEndMS);
			var url = network + "/" + subnet + "/" + pathFormatter.format(cellEnd) + "/" + subnet + fileFormatter.format(cellEnd) + "_thumb.png";
			image.attr('src', url);
			cellEndMS += refreshPeriodMS;
		}		
		cell = $(document.createElement('td'));
		cell.addClass("mosaicTitle");
		cell.text(timeFormatter.format(rowEnd));
		row.append(cell);
		
		rowEndMS += rowSpanM * 60 * 1000;
	}
}












