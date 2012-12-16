package com.salmonGUI.activities;

import java.util.Date;

import com.salmonGUI.app.async_core.AsyncConstants;
import com.salmonGUI.app.io.AppIO;
import com.salmonGUI.app.Route;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class MapViewActivity extends Activity {
	private TextView textView;
	private String mode, startID, endID, soe;
	private boolean verbose;
	
	private AppIO appIO;
	private Route r2;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);	// this is required for indeterminate in title bar
		setContentView(R.layout.mapview);
        
		textView = (TextView) findViewById(R.id.textView1);
        
		textView.setText("MAP VIEW - the native custom graphics view will go here\n\n");
		
		startID = AppPrefs.getStartID(this);
		endID = AppPrefs.getEndID(this);
		verbose = AppPrefs.getVerbose(this);
		soe = AppPrefs.getStairs(this);
		
		appIO = new AppIO(AsyncConstants.DEFAULT_THREAD_POOL_SIZE);
		r2 = Route.getInstance(this);
		
		if (startID != null && endID != null && startID.length() != 0 && endID.length() != 0) {
    		textView.append("startNode: " + startID + "\n");
    		textView.append("endNode: " + endID + "\n");
    		textView.append("verbose: " + verbose + "\n");
    		textView.append("stairsOrElevator: " + soe + "\n");
    		
    		textView.append("\n");
    		textView.append("---------------------------------------------------\n");
    		textView.append("\n");
    		
    		textView.append("Algorithm started at " + new Date() + "\n");
    		textView.append("UI Database Provider: " + appIO.getUIDatabaseProviderName() + "\n");
    		textView.append("Algorithm Database Provider: " + r2.getDatabaseProvider() + "\n");
    		textView.append("\n");
    		textView.append("initializing algorithm....\n");
    		
    		r2.setup("testRoute2", startID, endID, verbose? "true": "false", soe);
    		
    		textView.append("startNode: " + r2.getStartNodeID() + "\n");
    		textView.append("endNode: " + r2.getEndNodeID() + "\n");
    		textView.append("verbose: " + r2.getVerbose() + "\n");
    		textView.append("stairsOrElevator: " + r2.getStairsOrElevator() + "\n");
    		textView.append("---------------------------------------------------\n");
    		textView.append("Initialization done" + "\n");
    		textView.append("---------------------------------------------------\n");
    		textView.append("\n");
    		
    		textView.append("calculating route....\n");
    		textView.append("\n");
    		textView.append("---------------------------------------------------\n");
    		
    		appIO.calculateRoute(this, r2, textView);
    		
    	} else {
    		textView.append("error passing data\n");
    	}
		
		
	    
	}

}
