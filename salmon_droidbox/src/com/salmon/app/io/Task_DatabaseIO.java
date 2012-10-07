package com.salmon.app.io;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import com.salmon.app.AppConstants;
import com.salmon.app.async_core.TaskBase;
import com.salmon.app.async_core.UIHandler;
import com.salmon.app.io.extdb.HTTP_Apache;
import com.salmon.app.io.extdb.Socket;
import com.salmon.app.io.sqlite.SQLite;

public class Task_DatabaseIO<E1, E2> extends TaskBase<ArrayList<String>, E1, E2> {
	
	private IDatabaseProvider dbConn;
	private final int querytype;
	private final String[] params;
	private final Context context;
	
	/* Current (e.g. UI) Thread */
	public Task_DatabaseIO(Context context, UIHandler handlerUI, int querytype, String[] params) {
		super(handlerUI);

		this.context = context;
		this.querytype = querytype;
		this.params = params;
		
		// setup database connection provider
		switch (AppConstants.DATABASE_PROVIDER) {
		case AppConstants.PROVIDER_EXT_HTTP_APACHE:
			dbConn = new HTTP_Apache();
			break;
		
		case AppConstants.PROVIDER_EXT_SOCKET:
			dbConn = new Socket();
			break;
		
		case AppConstants.PROVIDER_INT_SQLITE:
			// TODO SQLITE 
			dbConn = new SQLite(context);
			break;

		default:
			// default to SQLITE
			// TODO SQLITE 
			dbConn = new SQLite(context);
			
			break;
		}
		
	}
	
	/* Current (e.g. UI) Thread */
	public Task_DatabaseIO(Context context, UIHandler handlerUI, int querytype, String[] params, int provider) {
		super(handlerUI);

		this.context = context;
		this.querytype = querytype;
		this.params = params;
		
		// setup database connection provider
		switch (provider) {
		case AppConstants.PROVIDER_EXT_HTTP_APACHE:
			dbConn = new HTTP_Apache();
			break;
		
		case AppConstants.PROVIDER_EXT_SOCKET:
			dbConn = new Socket();
			break;
		
		case AppConstants.PROVIDER_INT_SQLITE:
			// TODO SQLITE 
			dbConn = new SQLite(context);
			break;

		default:
			// default to SQLITE
			// TODO SQLITE 
			dbConn = new SQLite(context);
			
			break;
		}
		
	}
		
	/* Separate Thread */
	@Override
	public ArrayList<String> call() throws Exception {
		toggleProgress();
		
		final ArrayList<String> results = new ArrayList<String>();
		
//		switch(querytype) {
//		case DatabaseConstants.QUERY_DISPLAY_ALLDATA: 
//			// first, synch database
//			results.addAll(dbConn.getDataFromDatabase(DatabaseConstants.QUERY_SYNC_DB, params));
//			
//			// second, display all data
//			
//			break;
//		default:
			results.addAll(dbConn.getDataFromDatabase(querytype, params));
//			break;
//		}
		
		Log.i("DBTASK", "results.size: " + results.size());
		
		if ((this.future == null || !this.future.isCancelled()) && this.resultTask != null && handlerUI != null) {
			Log.i("DBTASK","setting results");
			this.resultTask.setResults(results);
			Log.i("DBTASK","posting results");
			handlerUI.post(this.resultTask);
		}
		
		Log.i("DBTASK","toggling Progress");
		toggleProgress();				
		return results;
	}
	
	/* Current (e.g. UI) Thread */
	@Override
	public void onCancel(DialogInterface dialog) {
		if (this.future != null) this.future.cancel(true);
		Toast.makeText(context, "Task canceled!", Toast.LENGTH_SHORT).show();
	}
	
}
