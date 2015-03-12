function msToM(ms) {
	return ms / 1000 / 60;
}

function mToMs(m) {
	return m * 60 * 1000;
}

function hToMs(h) {
	return h * 60 * 60 * 1000;
}

function msToH(ms) {
	return ms / 1000 / 60 / 60;
}

/* Code from: 
 * http://www.jquerybyexample.net/2012/06/get-url-parameters-using-jquery.html */
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