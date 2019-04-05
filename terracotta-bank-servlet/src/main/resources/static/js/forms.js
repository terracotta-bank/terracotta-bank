/**
 *
 * jquery.binarytransport.js
 *
 * @description. jQuery ajax transport for making binary data type requests.
 * @version 1.0 
 * @author Henry Algus <henryalgus@gmail.com>
 *
 */

/**
 * 
 */
$(function() {
	// use this transport for "binary" data type
	$.ajaxTransport("+binary", function(options, originalOptions, jqXHR){
	    // check for conditions and support for blob / arraybuffer response type
	    if (window.FormData && ((options.dataType && (options.dataType == 'binary')) || (options.data && ((window.ArrayBuffer && options.data instanceof ArrayBuffer) || (window.Blob && options.data instanceof Blob)))))
	    {
	        return {
	            // create new XMLHttpRequest
	            send: function(headers, callback){
	            	// setup all variables
	                var xhr = new XMLHttpRequest(),
	                url = options.url,
	                type = options.type,
	                async = options.async || true,
	                // blob or arraybuffer. Default is blob
	                dataType = options.responseType || "blob",
	                data = options.data || null,
	                username = options.username || null,
	                password = options.password || null;
						
	                xhr.addEventListener('load', function(){
	                	var data = {};
	                	data[options.dataType] = xhr.response;
	                	// make callback and send data
	                	callback(xhr.status, xhr.statusText, data, xhr.getAllResponseHeaders());
	                });
	                
	                xhr.open(type, url, async, username, password);
					
	                // setup custom headers
	                for (var i in headers ) {
	                	xhr.setRequestHeader(i, headers[i] );
	                }
					
	                xhr.responseType = dataType;
	                xhr.send(data);
	            },
	            abort: function(){
	                jqXHR.abort();
	            }
	        };
	    }
	});
	
	$("#lookup").submit(submitCheckLookup);
	$("#transfer").submit(submitMoneyTransfer);
	$("#deposit").submit(submitMoneyDeposit);
	$("#contact form").submit(submitContactUs);
	$("#showAccounts").submit(submitShowAccounts);
	$("#showMessages").submit(submitShowMessages);
	$("#change").submit(submitChangePassword);
	$("#restore").submit(submitForgotPassword);
});


var submitCheckLookup = function(event) {
	event.preventDefault();
	
	var formData = $(this).serialize();
	
	$.ajax({
		url : $("body").data("page-context") + "/checkLookup?c=finance",
		type : "POST",
		data : formData,
		dataType : "binary",
		responseType : "arraybuffer",
		success : function ( result ) {
			$("#lookup :not([type~=submit]).form-control").val("");
			var arrayBufferView = new Uint8Array( result )
			var blob = new Blob([ arrayBufferView ]);//, {type: "image/jpg"});
			var urlCreator = window.URL || window.webkitURL;
			var url = urlCreator.createObjectURL(blob);
			$("#deposit .messages").html("");
			document.getElementById("checkImageDisplay").src = url;
			document.getElementById("checkImageDisplay").style = "margin-bottom: 20px";
		},
		error : function ( jqXhr, status, message ) {
			$("#lookup .messages").html(message);
			document.getElementById("checkImageDisplay").src = "";
			document.getElementById("checkImageDisplay").style = "";
		}
	});
};

var submitMoneyTransfer = function(event) {
	event.preventDefault();

	var formData =
		"<request>" +
			"<fromAccountNumber>" + this.fromAccountNumber.value + "</fromAccountNumber>" +
		   	"<toAccountNumber>" + this.toAccountNumber.value + "</toAccountNumber>" +
			"<transferAmount>" + this.transferAmount.value + "</transferAmount>" +
		"</request>";

	$.ajax({
		url : $("body").data("page-context") + "/transferMoney?c=finance",
		type : "POST",
		contentType : "application/xml",
		data : formData,
		cache : false,
		success : function ( response ) {
			$("#accountBalance-" + response.number).html(response.amount);
			$("#transfer :not([type~=submit]).form-control").val("");
			$("#transfer .messages").html("Transferred!");
		},
		error : function ( jqXhr, status, message ) {
			var message = JSON.parse(jqXhr.responseText).message;
			$("#transfer .messages").html(message);
		}
	});
};

var submitMoneyDeposit = function(event) {
	event.preventDefault();
	
	var formData = new FormData($(this)[0]);
	
	$.ajax({
		url : $("body").data("page-context") + "/makeDeposit?c=finance",
		type : "POST",
		data : formData,
		async : false,
		cache : false,
		contentType : false,
		processData : false,
		success : function ( response ) {
			$("#accountBalance-" + response.number).html(response.amount);
			$("#deposit :not([type~=submit]).form-control").val("");
			$("#deposit .messages").html("Deposited!");
		},
		error : function ( jqXhr, status, message ) {
			var message = JSON.parse(jqXhr.responseText).message;
			$("#deposit .messages").html(message);
		}
	});
};

var submitChangePassword = function(event) {
	event.preventDefault();

	var formData = $(this).serialize();

	$.ajax({
		url : $("body").data("page-context") + "/changePassword?c=account",
		type : "POST",
		data : formData,
		cache : false,
		success : function ( response ) {
			$("#change :not([type~=submit]).form-control").val("");
			$("#change .messages").html("Password Updated!");
		},
		error : function ( jqXhr, status, message ) {
			var message = JSON.parse(jqXhr.responseText).message;
			$("#change .messages").html(message);
		}
	});
};

var submitForgotPassword = function(event) {
	event.preventDefault();

	var formData = $(this).serialize();

	$.ajax({
		url : $("body").data("page-context") + "/forgotPassword?c=account",
		type : "POST",
		data : formData,
		cache : false,
		success : function ( response ) {
			$("#restore :not([type~=submit]).form-control").val("");
			var message = JSON.parse(response).message;
			$("#restore .messages").html(message);
		},
		error : function ( jqXhr, status, message ) {
		    var message = JSON.parse(jqXhr.responseText).message;
			$("#restore .messages").html(message);
		}
	});
};

var submitContactUs = function(event) {
	event.preventDefault();
	
	var formData = $(this).serialize();
	
	$.ajax({
		url : $("body").data("page-context") + "/contactus?c=helpdesk",
		type : "POST",
		data : formData,
		cache : false,
		success : function ( response ) {
			$("#contact [type~=submit].form-control").val("");
			$("#contact .messages").html("Delivered!");
		},
		error : function ( jqXhr, status, message ) {
			var message = JSON.parse(jqXhr.responseText).message;
			$("#contact .messages").html(message);
		}
	});
};

var submitShowAccounts = function(event) {
	event.preventDefault();

	var formData = $(this).serialize();

	$.ajax({
		url : $("body").data("page-context") + "/showAccounts?c=helpdesk",
		type : "POST",
		data : formData,
		cache : false,
		success : function ( response ) {
			$("#results").html(response);
			location.hash = "#results";
		},
		error : function ( jqXhr, status, message ) {
			var message = JSON.parse(jqXhr.responseText).message;
			$("#showAccounts .messages").html(message);
		}
	});
};

var submitShowMessages = function(event) {
	event.preventDefault();
	
	$.ajax({
		url : $("body").data("page-context") + "/showMessages?c=helpdesk",
		type : "GET",
		cache : false,
		success : function ( response ) {
			$("#results").html(response);
			location.hash = "#results";
		},
		error : function ( jqXhr, status, message ) {
			var message = JSON.parse(jqXhr.responseText).message;
			$("#showMessages .messages").html(message);
		}
	});
};