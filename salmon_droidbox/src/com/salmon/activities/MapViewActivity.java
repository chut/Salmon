package com.salmon.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class MapViewActivity extends Activity {
	private TextView textView;
	private String mode, startID, endID, bldgID, floorID;
		
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);	// this is required for indeterminate in title bar
		setContentView(R.layout.mapview);
        
		textView = (TextView) findViewById(R.id.textView1);
        
		textView.setText("MAP VIEW - the native custom graphics view will go here\n\n");
		
		Bundle extras = getIntent().getExtras();
	    if (extras != null)	 {
	    	mode = extras.getString("mode");
	    	AppPrefs.setMapMode(mode, this);
	    	if (mode.equals("route")) {
		    	startID = extras.getString("start");
		    	endID = extras.getString("end");
		    	String verbose = extras.getString("verbose");
		    	String soe = extras.getString("soe");
		    	
		    	textView.append("Mode: " + mode + "\n");
	 	        
		    	if (startID != null && endID != null && startID.length() != 0 && endID.length() != 0) {
		    		textView.append("startNode: " + startID + "\n");
		    		textView.append("endNode: " + endID + "\n");
		    		textView.append("verbose: " + verbose + "\n");
		    		textView.append("stairsOrElevator: " + soe + "\n");
		    	} else {
		    		textView.append("error passing data\n");
		    	}
	    	} else if (mode.equals("browse")) {
	    		bldgID = extras.getString("bldg");
	    		floorID = extras.getString("floor");
	    		
	    		textView.append("Mode: " + mode + "\n");
	    		
	    		if (bldgID != null && floorID != null && bldgID.length() != 0 && floorID.length() != 0) {
	    			textView.append("Building ID: " + bldgID + "\n");
	    			textView.append("Floor ID: " + floorID + "\n");
	    		} else {
	    			textView.append("error passing data\n");
	    		}
	    	}
	    } else {
	    	textView.append("error - no data passed");
	    }
	    
	}

}
