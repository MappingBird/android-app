package com.mpbd.report;

import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;


public class ReportUtils {

	public void report(Context context, String category, String action, String label, long value) {
		  // May return null if a EasyTracker has not yet been initialized with a
		  // property ID.
		  EasyTracker easyTracker = EasyTracker.getInstance(context);

		  // MapBuilder.createEvent().build() returns a Map of event fields and values
		  // that are set and sent with the hit.
		  easyTracker.send(MapBuilder
		      .createEvent(category,     // Event category (required)
		    		  		action,  // Event action (required)
		    		  		label,   // Event label
		    		  		value)            // Event value
		      .build()
		  );
	}
}
