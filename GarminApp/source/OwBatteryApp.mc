using Toybox.Application;
using Toybox.Communications;
using Toybox.WatchUi;
using Toybox.System;

var batteryPercentage = -1;
var rangeString = "";
var notificationTitle = "";
var errorMessage = "";

var deviceSupported = true;

class OwBatteryApp extends Application.AppBase {

    function initialize() {
        Application.AppBase.initialize();

        if(Communications has :registerForPhoneAppMessages) {
            Communications.registerForPhoneAppMessages(method(:onPhone));
        } else if(Communications has :setMailboxListener) {
            Communications.setMailboxListener(method(:onMail));
        } else {
            deviceSupported = false;
        }     
    }

    function onStart(state) {
    	// Uncomment following block to test display of data without companion mobile app
    	/*
    	var batteryPercentage = 98;
    	var rangeString = "5 mi";
    	var notificationTitle = "Connected to Onewheel";
    	var data = [batteryPercentage, rangeString, notificationTitle];
    	parseReceivedData(data);
    	*/
    }

    function onStop(state) {
    }

    function getInitialView() {
        return [new CommView()];
    }

	// Incoming message from phone app using mail method
    function onMail(mailIter) {
        var mail;
        var receivedData;

        mail = mailIter.next();

        while(mail != null) {
            receivedData = mail.data;
            mail = mailIter.next();
        }

		parseReceivedData(receivedData);
		
        Communications.emptyMailbox();
    }

	// Incoming message from phone app using app messages method
    function onPhone(msg) {
        parseReceivedData(msg.data);
    }
    
    function parseReceivedData(receivedData) {
    	if(receivedData.size()>=3) {
        	batteryPercentage = receivedData[0];
        	rangeString = receivedData[1];
        	notificationTitle = receivedData[2];
        	errorMessage = "";
        } else {
        	errorMessage = "Error: Size " + receivedData.size().toString() + "\nsupport@floatangels.com";
        }

        WatchUi.requestUpdate();
    
    }

}