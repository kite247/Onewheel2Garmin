//
// Copyright 2016 by Garmin Ltd. or its subsidiaries.
// Subject to Garmin SDK License Agreement and Wearables
// Application Developer Agreement.
//

using Toybox.WatchUi;
using Toybox.Graphics;
using Toybox.Communications;
using Toybox.System;

class CommView extends WatchUi.View {
    var screenShape;
    

    function initialize() {
        View.initialize();
    }

    function onLayout(dc) {
        screenShape = System.getDeviceSettings().screenShape;
    }

    

    function onUpdate(dc) {
    	dc.setColor(Graphics.COLOR_TRANSPARENT, Graphics.COLOR_BLACK);
    	dc.clear();
        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_TRANSPARENT);

        if(deviceSupported==false) {
        	dc.drawText(dc.getWidth() / 2, dc.getHeight() / 2, Graphics.FONT_XTINY, "App is not compatible\nwith this device\n\nsupport@floatangels.com", Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
        } else if(errorMessage.length()>0) {
	    	dc.drawText(dc.getWidth() / 2, dc.getHeight() / 2,  Graphics.FONT_XTINY, errorMessage, Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
	    } else if(batteryPercentage==-1) {
	    	dc.drawText(dc.getWidth() / 2, dc.getHeight() / 2,  Graphics.FONT_XTINY, "Waiting for data\nfrom mobile app...", Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
	    } else {
	    	var percentageTextY = 0;
	     	var batteryPercentageStr = batteryPercentage.toString();
	     	dc.drawText(dc.getWidth() / 2, percentageTextY,  Graphics.FONT_NUMBER_THAI_HOT, batteryPercentageStr, Graphics.TEXT_JUSTIFY_CENTER);
	     	
	     	var textDimensions = dc.getTextDimensions(batteryPercentageStr, Graphics.FONT_NUMBER_THAI_HOT);
	     	var textWidth = textDimensions[0];
	     	var textHeight = textDimensions[1];
	     	
	     	var percentageSymbolFont = Graphics.FONT_LARGE;
	     	var percentageSymbolDimensions = dc.getTextDimensions("%", percentageSymbolFont);
	     	var percentageSymbolWidth = percentageSymbolDimensions[0];
	     	var percentageSymbolHeight = percentageSymbolDimensions[1];
	     	// Percentage symbol next to number
			dc.drawText(dc.getWidth()/2+textWidth/2+3, 87, percentageSymbolFont, "%", Graphics.TEXT_JUSTIFY_LEFT | Graphics.TEXT_JUSTIFY_VCENTER);
			
			if(rangeString.length()>0) {
				// Range text, e.g. "Range: 3mi"
				dc.drawText(dc.getWidth()/2, dc.getHeight()/2+50, Graphics.FONT_TINY, "Range: " + rangeString, Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
			}
			
	     }
	     
	     if(notificationTitle.length()>0) {
	     	// Show the notification title from the Onewheel App. Example strings: "Connected to Onewheel", "Reconnecting..."
	     	dc.drawText(dc.getWidth()/2, dc.getHeight()/2+10, Graphics.FONT_TINY, notificationTitle, Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
	     }				
         
    }


}