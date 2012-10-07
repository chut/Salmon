package com.salmon.app.io;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.salmon.app.async_core.TaskBase;
import com.salmon.app.async_core.UIHandler;
import com.salmon.app.io.sqlite.SQLiteHelper;

public class Task_SQLiteIO<E1, E2>  extends TaskBase<ArrayList<String>, E1, E2> {
	
	private final SQLiteHelper sqliteHelper;
	private final int querytype;
	private final String[] params;
	private final Context context;
	//private final E1 element1; // element to be updated - i.e. ArrayList, TextView, etc...
	//private final E2 element2; // element to be updated - i.e. ArrayList, TextView, etc...
	
	/* Current (e.g. UI) Thread */
	public Task_SQLiteIO(Activity activity, UIHandler handlerUI, int querytype, String[] params, E1 element1, E2 element2) {
		super(handlerUI);

		this.context = activity;
		this.querytype = querytype;
		this.params = params;
		//this.element1 = element1;
		//this.element2 = element2;
		
		// setup helper
		this.sqliteHelper = new SQLiteHelper(activity);
	}

	/* Separate Thread */
	public ArrayList<String> call() throws Exception {
		toggleProgress();
		
		final ArrayList<String> results = new ArrayList<String>();
		
		switch (querytype) {
		case DatabaseConstants.QUERY_SYNC_DB:
			results.addAll(query_SynchDB());
			
			toggleProgress();
			handlerUI.post(new Runnable() {				
				public void run() {
					Toast.makeText(context, "Database sync completed!", Toast.LENGTH_SHORT).show();
				}
			});			
			return results;
		
		case DatabaseConstants.QUERY_LOAD_APP:
			results.addAll(query_SynchDB());
			
			toggleProgress();
//			handlerUI.post((Runnable) element1);			
			return results;
		case DatabaseConstants.QUERY_ALLDATA:
			results.addAll(query_AllData());
			break;
	    
		case DatabaseConstants.QUERY_NODES_ALL:
			results.addAll(query_NodesAll());
			break;
			
		case DatabaseConstants.QUERY_NODES_BY_FLOOR:
			results.addAll(query_NodesByFloor());
			break;
			
		case DatabaseConstants.QUERY_NODES_BY_TYPE:
			results.addAll(query_NodesByType());
			break;
			
		case DatabaseConstants.QUERY_DISPLAY_ALLDATA:
			results.addAll(query_DisplayAllData());
			break;
	    
		default:
			// an unknown querytype has been passed, fail.
			results.add(DatabaseConstants.RESULT_FAILED);
			break;
		}
				
//		this.handlerUI.post(new Runnable() {			
//			public void run() {
//				Toast.makeText(context, "Task completed", Toast.LENGTH_SHORT).show();				
//			}
//		});	
		
		if (this.future == null || !this.future.isCancelled()) {
			this.resultTask.setResults(results);
			handlerUI.post(this.resultTask);
		}
		
		toggleProgress();				
		return results;
	}

	private ArrayList<String> query_SynchDB () {
		Log.i("SQLITE", "sync db");
		
		SQLiteDatabase db = null;
		//Cursor cursor = null;
		final ArrayList<String> results = new ArrayList<String>();
		
		// obtain data from external database
		Task_DatabaseIO dbTask = new Task_DatabaseIO(context, handlerUI, DatabaseConstants.QUERY_ALLDATA, null);
		try {
			Log.i("SQLITE","before dbTask.call");
//			sqliteHelper.setTableData(dbTask.call());
			ArrayList<String> temp = dbTask.call();
			Log.i("SQLITE","dbTask result.size: " + temp.size());
			sqliteHelper.setTableData(temp);
		} catch (Exception e) {
			// TODO error handling of dbTask failure
			Log.i("SQLITE","we failed!!!!!!!!!");
			results.add(DatabaseConstants.RESULT_FAILED);
        	return results;
		}
		
		// open/create the SQLite database 
		if (this.future == null || !this.future.isCancelled()) {
			try {
				db = sqliteHelper.getWritableDatabase();
				Log.i("SQLITE", "db version: " + db.getVersion());
				sqliteHelper.onCreate(db);
				Log.i("SQLITE", "db version2: " + db.getVersion());
	        } catch(SQLException e) { 
	        	// TODO error handling
	        	if (db != null) {db.close();}
	        	results.add(DatabaseConstants.RESULT_FAILED);
	        	return results;
	        }
		}
		
		// close the database connection
		//if (cursor != null) {cursor.close();}
		if (db != null) {db.close();}
		
		if (this.future == null || !this.future.isCancelled()) {
			results.add(DatabaseConstants.RESULT_SUCCESS);
			Log.i("SQLITE","sync success");
	    	return results;
		} else {
			return null;
		}
	}
	
	private ArrayList<String> query_AllData() {
		Log.i("SQLITE", "all data");
		
		SQLiteDatabase db = null;
		Cursor cursor = null;
		final ArrayList<String> results = new ArrayList<String>();
		
		// open the SQLite database
		try {
			db = sqliteHelper.getWritableDatabase();
        } catch(SQLException e) { 
        	// TODO error handling
        	if (db != null) {db.close();}
        	results.add(DatabaseConstants.RESULT_FAILED);
        	return results;
        }
		
		// obtain data from sqlite database
		cursor = db.query(
				DatabaseConstants.TABLE_NAME, 	// table
				DatabaseConstants.ALL_COLUMNS, 	// columns
				null, 							// where clause
				null, 							// selection args
				null, 							// groupBy
				null, 							// having
				DatabaseConstants.KEY_NODE_ID		// orderBy
			);
        
        // create a comma delimited ArrayList<String> from cursor results
		if (this.future == null || !this.future.isCancelled()) {
			while (cursor.moveToNext()) {
	        	results.add(""
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_ID)) + "," 
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_LABEL)) + "," 
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_TYPE)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_PHOTO)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_X)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_Y)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_IS_CONNECTOR)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_IS_POI)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_POI_Img)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_BUILDING_ID)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_BUILDING_NAME)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_FLOOR_ID)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_FLOOR_LEVEL)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_FLOOR_MAP)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NEIGHBOR_NODE)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NEIGHBOR_DISTANCE))); 
			}
		}
		
		// close the database connection, and return results
		if (cursor != null) {cursor.close();}
		if (db != null) {db.close();}
		
		if (this.future == null || !this.future.isCancelled()) {
			Log.i("SQLITE","AllData - results.size: " + results.size());
			return results;
		} else {
			return null;
		}
	}
	
	private ArrayList<String> query_NodesAll() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		final ArrayList<String> results = new ArrayList<String>();
		
		// open the SQLite database
		try {
			db = sqliteHelper.getWritableDatabase();
        } catch(SQLException e) { 
        	// TODO error handling
        	if (db != null) {db.close();}
        	results.add(DatabaseConstants.RESULT_FAILED);
        	return results;
        }
		
		// obtain data from sqlite database
		cursor = db.query(
				true,							// distinct
				DatabaseConstants.TABLE_NAME, 	// table name
				new String[] {DatabaseConstants.KEY_NODE_ID, DatabaseConstants.KEY_NODE_LABEL},	// table columns returned
				DatabaseConstants.KEY_BUILDING_ID + "='" + params[0] + "'",	// where clause
				null, 							// selection args
				null, 							// groupBy
				null, 							// having
				DatabaseConstants.KEY_NODE_ID,	// orderBy
				null							// limit
			);
        
        // create an ArrayList<String> from cursor results
		if (this.future == null || !this.future.isCancelled()) {
			while (cursor.moveToNext()) {
				results.add(""
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_ID)) + " | " 
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_LABEL)));
			}
		}
		
		// close the database connection, and return results
		if (cursor != null) {cursor.close();}
		if (db != null) {db.close();}
		
		if (this.future == null || !this.future.isCancelled()) {
			Log.i("SQLITE","query_NodesAll - results.size: " + results.size());
			return results;
		} else {
			return null;
		}
	}
	
	private ArrayList<String> query_NodesByFloor() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		final ArrayList<String> results = new ArrayList<String>();
		
		// open the SQLite database
		try {
			db = sqliteHelper.getWritableDatabase();
        } catch(SQLException e) { 
        	// TODO error handling
        	if (db != null) {db.close();}
        	results.add(DatabaseConstants.RESULT_FAILED);
        	return results;
        }
		
		// obtain data from sqlite database
		cursor = db.query(
				true,							// distinct
				DatabaseConstants.TABLE_NAME, 	// table name
				new String[] {DatabaseConstants.KEY_NODE_ID, DatabaseConstants.KEY_NODE_LABEL},	// table columns returned
				DatabaseConstants.KEY_FLOOR_ID + "='" + params[0] + "' AND " + DatabaseConstants.KEY_BUILDING_ID + "='" + params[1] + "'", 	// where clause
				null, 							// selection args
				null, 							// groupBy
				null, 							// having
				DatabaseConstants.KEY_NODE_ID,	// orderBy
				null							// limit
			);
        
        // create an ArrayList<String> from cursor results
		if (this.future == null || !this.future.isCancelled()) {
			while (cursor.moveToNext()) {
				results.add(""
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_ID)) + " | " 
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_LABEL)));
			}
		}
		
		// close the database connection, and return results
		if (cursor != null) {cursor.close();}
		if (db != null) {db.close();}
		
		if (this.future == null || !this.future.isCancelled()) {
			Log.i("SQLITE","query_NodesByFloor - results.size: " + results.size());
			return results;
		} else {
			return null;
		}
	}
	
	private ArrayList<String> query_NodesByType() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		final ArrayList<String> results = new ArrayList<String>();
		
		// open the SQLite database
		try {
			db = sqliteHelper.getWritableDatabase();
        } catch(SQLException e) { 
        	// TODO error handling
        	if (db != null) {db.close();}
        	results.add(DatabaseConstants.RESULT_FAILED);
        	return results;
        }
		
		// obtain data from sqlite database
		cursor = db.query(
				true,							// distinct
				DatabaseConstants.TABLE_NAME, 	// table name
				new String[] {DatabaseConstants.KEY_NODE_ID, DatabaseConstants.KEY_NODE_LABEL},	// table columns returned
				DatabaseConstants.KEY_NODE_TYPE + "='" + params[0] + "' AND " + DatabaseConstants.KEY_BUILDING_ID + "='" + params[1] + "'", 	// where clause
				null, 							// selection args
				null, 							// groupBy
				null, 							// having
				DatabaseConstants.KEY_NODE_ID,	// orderBy
				null							// limit
			);
        
        // create an ArrayList<String> from cursor results
		if (this.future == null || !this.future.isCancelled()) {
			while (cursor.moveToNext()) {
				results.add(""
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_ID)) + " | " 
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_LABEL)));
			}
		}
		
		// close the database connection, and return results
		if (cursor != null) {cursor.close();}
		if (db != null) {db.close();}
		
		if (this.future == null || !this.future.isCancelled()) {
			Log.i("SQLITE","query_NodesByType - results.size: " + results.size());
			return results;
		} else {
			return null;
		}
	}

	private ArrayList<String> query_DisplayAllData() {
		final ArrayList<String> results = new ArrayList<String>();
		
		// first, synch database
		results.addAll(query_SynchDB());
		
		// second, get all data
		if (this.future == null || !this.future.isCancelled()) {
			if (results.size() == 1 && results.get(0).equals(DatabaseConstants.RESULT_SUCCESS)) {
				results.clear();
				results.addAll(query_AllData());
			}
		}
		Log.i("SQLITE","query_DisplayAllData - results.size: " + results.size());
		return results;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if (this.future != null) this.future.cancel(true);
		Toast.makeText(context, "Task canceled!", Toast.LENGTH_SHORT).show();
	}

}
