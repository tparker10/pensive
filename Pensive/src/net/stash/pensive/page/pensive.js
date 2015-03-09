var refreshPeriodMS = ${refreshPeriod} * 1000;
var timeFormatter = new SimpleDateFormat("HH:mm");
var pathFormatter = new SimpleDateFormat("${filePathFormat}");
var fileFormatter = new SimpleDateFormat("${fileSuffixFormat}");

var endTime;
var startTime;
var rowSpanMS;
var mosaicSpanMS;

function init() {
	rowSpanMS = 60 * 60 * 1000;
	mosaicSpanMS = 180 * 60 * 1000;
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

	$("#subnet").on('change', updateSubnet);
	$("#subnet").trigger("change");	
	
	jQuery('.positiveInt').keyup(function () { 
	    this.value = this.value.replace(/[^0-9]/g,'');
	});
}

function displayMosaicOptions() {
	vex.dialog.confirm({
		  message: 'Are you absolutely sure you want to destroy the alien planet?',
		  callback: function(value) {
		    return console.log(value ? 'Successfully destroyed the planet.' : 'Chicken.');
		  }
		});
}

/* The subnet changed, now what? */
function updateSubnet() {
	var subnet=$("#subnet option:selected").text();
	$("#subnetName").text(subnet);
	updateMosaic();
}

/* The subnet changed, now what? */
function updateMosaicSpan() {
	var span=$("#mosaicSpan option:selected").val();
	mosaicSpanMS = span * 60 * 60 * 1000;
	updateMosaic();
}

/* The subnet changed, now what? */
function updateMosaicRowSpan() {
	var span=$("#mosaicRowSpan").val();
	rowSpanMS = span * refreshPeriodMS;
	updateMosaic();
}

function updateMosaicOptions() {
	var span=$("#mosaicSpan option:selected").val();
	mosaicSpanMS = span * 60 * 60 * 1000;

	var span=$("#mosaicRowSpan").val();
	rowSpanMS = span * refreshPeriodMS;
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

	var mosaicEndMS = endTime.getTime();
	var mosaicStartMS = mosaicEndMS - mosaicSpanMS;
	
	startTime.setTime(mosaicStartMS);
	updateTime();
	
	frame.empty();
	var table = $(document.createElement('table'));
	table.addClass('center');
	frame.append(table);

	var rowStartMS = mosaicStartMS;
	while (rowStartMS <= mosaicEndMS) {
		var rowStart = new Date(rowStartMS);
		var rowEndMS = rowStartMS + rowSpanMS;
		var row = $(document.createElement('tr'));
		row.addClass("mosaic");
		table.append(row);
		var cell = $(document.createElement('td'));
		cell.addClass("mosaicTitle");
		cell.text(timeFormatter.format(rowStart));
		row.append(cell);
		var cellEndMS = rowStart.getTime() + refreshPeriodMS; 

		while (cellEndMS <= rowEndMS) {
			cell = $(document.createElement('td'));
			cell.addClass("mosaic");
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
		cell.text(timeFormatter.format(new Date(rowEndMS)));
		row.append(cell);
		
		rowStartMS += rowSpanMS;
	}
}












