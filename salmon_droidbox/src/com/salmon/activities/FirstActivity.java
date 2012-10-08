package com.salmon.activities;

import java.util.ArrayList;
import java.util.Date;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.salmon.app.AppConstants;
import com.salmon.app.Route;
import com.salmon.app.async_core.AsyncConstants;
import com.salmon.app.io.AppIO;
import com.salmon.app.io.DatabaseConstants;

public class FirstActivity extends ListActivity implements OnClickListener {
	private EditText startEdit;
	private EditText endEdit;
	private CheckBox verboseChk;
	private CheckBox elevatorChk;
	private TextView resultsText;
	private Button calcBtn;
	private Button mapBtn;
	private Button alldataBtn;
	private Button asyncBtn;
	private Button noasyncBtn;
	private Button fourBtn;
	private Button fiveBtn;
	private Button sixBtn;
	
	private ArrayList<String> listItems	= new ArrayList<String>();
	private ArrayAdapter<String> aa;
	
	private AppIO appIO;
	private Route r2;
	ArrayList<String> myResultsList = new ArrayList<String>();
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);	// this is required for indeterminate in title bar
		setContentView(R.layout.main);
        
        startEdit = (EditText) findViewById(R.id.start_edit);
        endEdit = (EditText) findViewById(R.id.end_edit);
        verboseChk = (CheckBox) findViewById(R.id.verbose_chk);
        elevatorChk = (CheckBox) findViewById(R.id.elevator_chk);
        resultsText = (TextView) findViewById(R.id.results_text);
        calcBtn = (Button) findViewById(R.id.calc_btn);
        mapBtn = (Button) findViewById(R.id.map_btn);
        alldataBtn = (Button) findViewById(R.id.allData_btn);
        asyncBtn = (Button) findViewById(R.id.async_btn);
        noasyncBtn = (Button) findViewById(R.id.noAsync_btn);
        fourBtn = (Button) findViewById(R.id.four_btn);
        fiveBtn = (Button) findViewById(R.id.five_btn);
        sixBtn = (Button) findViewById(R.id.six_btn);
        
        calcBtn.setOnClickListener(this);
        mapBtn.setOnClickListener(this);
        alldataBtn.setOnClickListener(this);
        asyncBtn.setOnClickListener(this);
        noasyncBtn.setOnClickListener(this);
        fourBtn.setOnClickListener(this);
        fiveBtn.setOnClickListener(this);
        sixBtn.setOnClickListener(this);
        
        aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        setListAdapter(aa);    //connect ArrayAdapter to <ListView>
        
        appIO = new AppIO(AsyncConstants.DEFAULT_THREAD_POOL_SIZE);
        
        r2 = new Route(this);
        //async.handleRetainedTask(getLastNonConfigurationInstance());
        
    	resultsText.setText("Blackbox started at " + new Date() + "\n");
    	resultsText.append("UI Database Provider: " + appIO.getUIDatabaseProviderName() + "\n");
        //resultsText.append("Algorithm Database Provider: " + r2.getDatabaseProvider() + "\n");
	}

	public void onClick(View button) {
		// close the keyboard
		InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
        in.hideSoftInputFromWindow(startEdit.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS); 
        resultsText.setText("Calculate Route started at " + new Date() + '\n'); 
        resultsText.append("UI Database Provider: " + appIO.getUIDatabaseProviderName() + "\n");
        resultsText.append("Algorithm Database Provider: " + r2.getDatabaseProvider() + "\n");
        
        //save prefs
        AppPrefs.setStartID(startEdit.getText().toString(), this);
        AppPrefs.setEndID(endEdit.getText().toString(), this);
        AppPrefs.setVerbose(verboseChk.isChecked(), this);
        AppPrefs.setStairs(elevatorChk.isChecked()? "elevator" : "stairs", this);
        
        String vState, eState;
		
        switch (button.getId()) {
		case R.id.calc_btn:
			resultsText.append("GO button - testing engine with async threading" + "\n");
			
			if (verboseChk.isChecked()) {
				vState = "verbose=true";
			} else {
				vState = "verbose=false";
			}
			if (elevatorChk.isChecked()) {
				eState = "soe=elevator";
			} else {
				eState = "soe=stairs";
			}
			
	        r2.setup("testRoute2", startEdit.getText().toString(), endEdit.getText().toString(), vState, eState);
	        
	        //resultsText.append("\nstartNode: " + r2.getStartNode().getNodeID() + ", Building: " + r2.getStartNode().getBuildingID() + ", Floor: " + r2.getStartNode().getFloorID() + ", mapImg: " + r2.getStartNode().getMapImg() + ", photoImg: " + r2.getStartNode().getPhotoImg() + ", X: " + r2.getStartNode().getX() + ", Y: " + r2.getStartNode().getY() + "\n");
	    	//resultsText.append("endNode: " + r2.getEndNode().getNodeID() + ", Building: " + r2.getEndNode().getBuildingID() + ", Floor: " + r2.getEndNode().getFloorID() + ", mapImg: " + r2.getEndNode().getMapImg() + ", photoImg: " + r2.getEndNode().getPhotoImg() + ", X: " + r2.getEndNode().getX() + ", Y: " + r2.getEndNode().getY() + "\n");
	        resultsText.append("\nstartNode: " + r2.getStartNodeID() + "\n");
	        resultsText.append("endNode: " + r2.getEndNodeID() + "\n");
	        //resultsText.append("nodeList.size() = " + r2.getNodeList().size() + "\n");
	    	resultsText.append("verbose: " + r2.getVerbose() + "\n");
	    	resultsText.append("stairsOrElevator: " + r2.getStairsOrElevator() + "\n");
	    	resultsText.append("Initialization done" + "\n");
			
	    	// calculate route
	    	appIO.calculateRoute(this, r2, resultsText);
	    	break;
		case R.id.map_btn:
			
			if (verboseChk.isChecked()) {
				vState = "verbose=true";
			} else {
				vState = "verbose=false";
			}
			if (elevatorChk.isChecked()) {
				eState = "soe=elevator";
			} else {
				eState = "soe=stairs";
			}
			Intent intent = new Intent(this, MapViewActivity.class);
			intent.putExtra("mode", "route");
			intent.putExtra("start", startEdit.getText().toString());
			intent.putExtra("end", endEdit.getText().toString());
			intent.putExtra("verbose", vState);
			intent.putExtra("soe", eState);
			startActivity(intent);
			
			break;
		case R.id.allData_btn:
			resultsText.append("All Data button - i.e. syncing with SQLite" + "\n");
			
			//appIO.sqlite_async_getData(SQLiteConstants.QUERY_DISPLAY_ALLDATA, SQLiteConstants.PROGRESS_DIALOG, resultsText, null, this);
			appIO.updateTextView_sqlite_async(DatabaseConstants.QUERY_DISPLAY_ALLDATA, AppConstants.PROGRESS_DIALOG, resultsText, this);
			break;
		case R.id.async_btn:
			resultsText.append("Async button - sqlite call - Async w/indeterminate" + "\n");
			
			//appIO.sqlite_async_getData(SQLiteConstants.QUERY_ALLDATA, SQLiteConstants.PROGRESS_BAR_INDETERMINATE, resultsText, null, this);
			appIO.updateTextView_sqlite_async(DatabaseConstants.QUERY_ALLDATA, AppConstants.PROGRESS_BAR_INDETERMINATE, resultsText, this);
			break;
		case R.id.noAsync_btn:
			resultsText.append("Async2 button - sqlite call - no progress" + "\n");
			
			//appIO.sqlite_async_getData(SQLiteConstants.QUERY_ALLDATA, SQLiteConstants.PROGRESS_NONE, resultsText, null, this);
			appIO.updateTextView_sqlite_async(DatabaseConstants.QUERY_ALLDATA, AppConstants.PROGRESS_NONE, resultsText, this);
			break;
		case R.id.four_btn:
			resultsText.append("All Nodes button - sqlite - lists all nodes in a Bldg (e.g. wang)" + "\n");
			
			//appIO.sqlite_async_getData(SQLiteConstants.QUERY_NODES_ALL, SQLiteConstants.PROGRESS_BAR_INDETERMINATE, listItems, aa, this, "wang");
			appIO.updateListView_sqlite_async(DatabaseConstants.QUERY_NODES_ALL, AppConstants.PROGRESS_BAR_INDETERMINATE, listItems, aa, this, "wang");
			break;
		case R.id.five_btn:
			resultsText.append("By Floor button - sqlite - all 1st floor nodes (e.g. wang 2nd floor)" + "\n");
			
			//appIO.sqlite_async_getData(SQLiteConstants.QUERY_NODES_BY_FLOOR, SQLiteConstants.PROGRESS_BAR_INDETERMINATE, listItems, aa, this, "wa2", "wang");
			appIO.updateListView_sqlite_async(DatabaseConstants.QUERY_NODES_BY_FLOOR, AppConstants.PROGRESS_BAR_INDETERMINATE, listItems, aa, this, "wa2", "wang");
			break;
		case R.id.six_btn:
			resultsText.append("By Type button - sqlite - all Room type nodes (e.g. wang rooms)" + "\n");
			
			//appIO.sqlite_async_getData(SQLiteConstants.QUERY_NODES_BY_TYPE, SQLiteConstants.PROGRESS_BAR_INDETERMINATE, listItems, aa, this, "room", "wang");
			appIO.updateListView_sqlite_async(DatabaseConstants.QUERY_NODES_BY_TYPE, AppConstants.PROGRESS_BAR_INDETERMINATE, listItems, aa, this, "room","wang");
			break;
	
		default:
			break;
		}
        
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		super.onCreateOptionsMenu(menu);
		AppOptionsMenu.buildOptionMenu(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		return AppOptionsMenu.handleMenuSelection(item, this, startEdit.getText().toString(), endEdit.getText().toString());
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		// Delegate task retain to manager
		//return async.retainTask();
		return null;
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		//Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
		//save prefs
        AppPrefs.setStartID(startEdit.getText().toString(), this);
        AppPrefs.setEndID(endEdit.getText().toString(), this);
        AppPrefs.setVerbose(verboseChk.isChecked(), this);
        AppPrefs.setStairs(elevatorChk.isChecked()? "elevator" : "stairs", this);
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		//Toast.makeText(this, "onRestart", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
		// retreive prefs
		startEdit.setText(AppPrefs.getStartID(this));
		endEdit.setText(AppPrefs.getEndID(this));
		verboseChk.setChecked(AppPrefs.getVerbose(this));
		elevatorChk.setChecked(AppPrefs.getStairs(this).equals("elevator"));
		r2.setDatabaseProvider(Integer.parseInt(AppPrefs.getAlogrithmDatabaseProvider(this)));
		resultsText.append("Algorithm Database Provider: " + r2.getDatabaseProvider() + "\n");
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		//Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		//Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
	}
}