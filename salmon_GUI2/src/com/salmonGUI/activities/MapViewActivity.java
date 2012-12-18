package com.salmonGUI.activities;

import java.util.ArrayList;
import java.util.Date;

import com.salmonGUI.app.async_core.AsyncConstants;
import com.salmonGUI.app.io.AppIO;
import com.salmonGUI.app.Route;
import com.salmonGUI.app.RouteStep;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

public class MapViewActivity extends Activity {
	private TextView textView;
	private String mode, startID, endID, soe, verbose;
	
	private AppIO appIO;
	private Route r2;
	ArrayList<RouteStep> routePut;
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
		String bVerbose = AppPrefs.getVerbose(this)? "true":"false";
		verbose = "verbose=" + bVerbose;
		soe = "soe=" + AppPrefs.getStairs(this);
		
		appIO = new AppIO(AsyncConstants.DEFAULT_THREAD_POOL_SIZE);
		r2 = Route.getInstance(this);
		
		if (startID != null && endID != null && startID.length() != 0 && endID.length() != 0) {
    		textView.append("startNode: " + startID + "\n");
    		textView.append("endNode: " + endID + "\n");
    		textView.append("verbose: " + verbose + "\n");
    		textView.append("stairsOrElevator: " + soe + "\n");
    		
    		textView.append("\n");
    		textView.append("-------------------here i am --------------------\n");
    		textView.append("\n");
    		
    		textView.append("Algorithm started at " + new Date() + "\n");
    		textView.append("UI Database Provider: " + appIO.getUIDatabaseProviderName() + "\n");
    		textView.append("Algorithm Database Provider: " + r2.getDatabaseProvider() + "\n");
    		textView.append("\n");
    		textView.append("initializing algorithm....\n");
    		
    		r2.setup("testRoute2", startID, endID, verbose, soe);
    		
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
    		
    		appIO.calculateRoute(this, r2, this);
    		//ned to get these return values in a reasonable way 
    	} else {
    		textView.append("error passing data\n");
    	}
		
//		int nodeX = r2.getRouteStepList().get(1).getStepNode().getX();
//		Log.v("node", Integer.toString(nodeX));
		
		
		
		
		
	}//end oncreate

	public void routeConvertData(Route routeObj) {
		//textView.append("runResult = " + result + "\n");
		textView.append(routeObj.toString() + "\n");
		textView.append(routeObj.getMyMetrics().toString() + "\n");
		
		routePut = routeObj.getRouteStepList();
		
		
		for(int i = 0; i < routePut.size(); i++){
			int nodex = routePut.get(i).getStepNode().getX();
			Log.v("X", Integer.toString(nodex));
			int nodey = routePut.get(i).getStepNode().getY();
			Log.v("Y", Integer.toString(nodey));
			//boolean nod = routePut.get(1).getStepNode().
		}
		
	}//end routeconvert
	
	
	
	
}
