package com.salmon.app.io.sqlite;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.salmon.app.AppConstants;
import com.salmon.app.io.DatabaseConstants;
import com.salmon.app.io.IDatabaseProvider;
import com.salmon.app.io.Task_DatabaseIO;

public class SQLite implements IDatabaseProvider {
	
	private final SQLiteHelper sqliteHelper;
	private String[] params;
	private Context context;
	
	public SQLite(Context context) {
		this.context = context;
		
		// setup helper
		this.sqliteHelper = new SQLiteHelper(context);
	}
	
	public ArrayList<String> getDataFromDatabase(int queryType,	String[] params) {
		Log.i("SQLITE","getDataFromDatabase - begin");
		final ArrayList<String> results = new ArrayList<String>();
		this.params = params;
		
		switch (queryType) {
		case DatabaseConstants.QUERY_SYNC_DB:
			results.addAll(query_SynchDB());

			break;
			
//		case SQLiteConstants.QUERY_LOAD_APP:	// used with splash screen
//			results.addAll(query_SynchDB());
//			
//			toggleProgress();
//			handlerUI.post((Runnable) element1);			
//			return results;
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
	    
		case DatabaseConstants.QUERY_NEIGHBORS:
			results.addAll(query_Neighbors());
			break;
	    
		default:
			// an unknown querytype has been passed, fail.
			results.add(DatabaseConstants.RESULT_FAILED);
			break;
		}
		
		return results;
	}
	
	
	
	
	private ArrayList<String> query_SynchDB () {
		Log.i("SQLITE", "sync db");
		
		SQLiteDatabase db = null;
		//Cursor cursor = null;
		final ArrayList<String> results = new ArrayList<String>();
		
		// obtain data from external database
		Task_DatabaseIO<String, String> dbTask = new Task_DatabaseIO<String, String>(context, null, DatabaseConstants.QUERY_ALLDATA, null, AppConstants.PROVIDER_EXT_HTTP_APACHE);
		try {
			sqliteHelper.setTableData(dbTask.call());
//			ArrayList<String> temp = dbTask.call();
//			Log.i("SQLITE","dbTask result.size: " + temp.size());
//			sqliteHelper.setTableData(temp);
		} catch (Exception e) {
			// TODO error handling of dbTask failure
			results.add(DatabaseConstants.RESULT_FAILED);
        	return results;
		}
		
		// open/create the SQLite database 
		//if (this.future == null || !this.future.isCancelled()) {
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
		//}
		
		// close the database connection
		//if (cursor != null) {cursor.close();}
		if (db != null) {db.close();}
		
//		if (this.future == null || !this.future.isCancelled()) {
			results.add(DatabaseConstants.RESULT_SUCCESS);
			Log.i("SQLITE","sync success");
	    	return results;
//		} else {
//			return null;
//		}
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
		//if (this.future == null || !this.future.isCancelled()) {
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
		//}
		
		// close the database connection, and return results
		if (cursor != null) {cursor.close();}
		if (db != null) {db.close();}
		
//		if (this.future == null || !this.future.isCancelled()) {
			Log.i("SQLITE","AllData - results.size: " + results.size());
			return results;
//		} else {
//			return null;
//		}
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
		//if (this.future == null || !this.future.isCancelled()) {
			while (cursor.moveToNext()) {
				results.add(""
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_ID)) + " | " 
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_LABEL)));
			}
		//}
		
		// close the database connection, and return results
		if (cursor != null) {cursor.close();}
		if (db != null) {db.close();}
		
//		if (this.future == null || !this.future.isCancelled()) {
			Log.i("SQLITE","query_NodesAll - results.size: " + results.size());
			return results;
//		} else {
//			return null;
//		}
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
		//if (this.future == null || !this.future.isCancelled()) {
			while (cursor.moveToNext()) {
				results.add(""
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_ID)) + " | " 
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_LABEL)));
			}
		//}
		
		// close the database connection, and return results
		if (cursor != null) {cursor.close();}
		if (db != null) {db.close();}
		
//		if (this.future == null || !this.future.isCancelled()) {
			Log.i("SQLITE","query_NodesByFloor - results.size: " + results.size());
			return results;
//		} else {
//			return null;
//		}
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
		//if (this.future == null || !this.future.isCancelled()) {
			while (cursor.moveToNext()) {
				results.add(""
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_ID)) + " | " 
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_LABEL)));
			}
		//}
		
		// close the database connection, and return results
		if (cursor != null) {cursor.close();}
		if (db != null) {db.close();}
		
//		if (this.future == null || !this.future.isCancelled()) {
			Log.i("SQLITE","query_NodesByType - results.size: " + results.size());
			return results;
//		} else {
//			return null;
//		}
	}

	private ArrayList<String> query_DisplayAllData() {
		final ArrayList<String> results = new ArrayList<String>();
		
		// first, synch database
		results.addAll(query_SynchDB());
		
		// second, get all data
		//if (this.future == null || !this.future.isCancelled()) {
			if (results.size() == 1 && results.get(0).equals(DatabaseConstants.RESULT_SUCCESS)) {
				results.clear();
				results.addAll(query_AllData());
			}
		//}
		Log.i("SQLITE","query_DisplayAllData - results.size: " + results.size());
		return results;
	}
	
	private ArrayList<String> query_Neighbors() {
		Log.i("SQLITE", "query_Neighbors - begin");
		
		SQLiteDatabase db = null;
		Cursor cursor = null;
		final ArrayList<String> results = new ArrayList<String>();
		String strSQL = null;
		
		Log.i("SQLITE","Params.length:" + params.length);
		// construct SQL statements
		if (params.length == 1) {
			// nodeID (params[0]) was passed.  base SQL off nodeID
			Log.i("SQLITE","base SQL off nodeID: " + params[0]);
			strSQL = ""
				+ "SELECT * FROM " + DatabaseConstants.TABLE_NAME 
				+ " WHERE " 
					+ DatabaseConstants.KEY_BUILDING_ID + " IN "
						+ "(SELECT " + DatabaseConstants.KEY_BUILDING_ID + " FROM " + DatabaseConstants.TABLE_NAME 
							+ " WHERE " + DatabaseConstants.KEY_NODE_ID + " = \"" + params[0] + "\") "
					+ "AND " + DatabaseConstants.KEY_FLOOR_ID + " IN "
						+ "(SELECT " + DatabaseConstants.KEY_FLOOR_ID + " FROM " + DatabaseConstants.TABLE_NAME 
							+ " WHERE " + DatabaseConstants.KEY_NODE_ID + " = \"" + params[0] + "\") " 
				+ "UNION ALL " 
				+ "SELECT * FROM " + DatabaseConstants.TABLE_NAME
				+ " WHERE "
					+ DatabaseConstants.KEY_NODE_IS_CONNECTOR + " = 1 "
					+ "AND " + DatabaseConstants.KEY_NEIGHBOR_NODE + " IN "
						+ "(SELECT " + DatabaseConstants.KEY_NODE_ID + " FROM " + DatabaseConstants.TABLE_NAME
							+ " WHERE " + DatabaseConstants.KEY_NODE_IS_CONNECTOR + " = 1 "
								+ "AND " + DatabaseConstants.KEY_BUILDING_ID + " IN "
									+ "(SELECT " + DatabaseConstants.KEY_BUILDING_ID + " FROM " + DatabaseConstants.TABLE_NAME
										+ " WHERE " + DatabaseConstants.KEY_NODE_ID + " = \"" + params[0] + "\") "
								+ "AND " + DatabaseConstants.KEY_FLOOR_ID + " IN "
									+ "(SELECT " + DatabaseConstants.KEY_FLOOR_ID + " FROM " + DatabaseConstants.TABLE_NAME
										+ " WHERE " + DatabaseConstants.KEY_NODE_ID + " = \"" + params[0] + "\")) "
				+ "ORDER BY " + DatabaseConstants.KEY_NODE_ID;
			
		} else {
			// buildingID (params[0]) and floorID (params[1]) were passed.  base SQL off of those.
			Log.i("SQLITE","base SQL off buildingID and floorID: " + params[0] + ", " + params[1]);
			strSQL = ""
				+ "SELECT * FROM " + DatabaseConstants.TABLE_NAME
				+ " WHERE " + DatabaseConstants.KEY_BUILDING_ID + " = \"" + params[0] + "\" "
					+ "AND " + DatabaseConstants.KEY_FLOOR_ID + " = \"" + params[1] + "\" " 
				+ " UNION ALL "
				+ "SELECT * FROM " + DatabaseConstants.TABLE_NAME
				+ " WHERE " + DatabaseConstants.KEY_NODE_IS_CONNECTOR + " = 1 "
					+ "AND " + DatabaseConstants.KEY_NEIGHBOR_NODE + " IN "
						+ "(SELECT " + DatabaseConstants.KEY_NODE_ID + " FROM " + DatabaseConstants.TABLE_NAME
							+ " WHERE " + DatabaseConstants.KEY_NODE_IS_CONNECTOR + " = 1 "
								+ "AND " + DatabaseConstants.KEY_BUILDING_ID + " = \"" + params[0] + "\" "
								+ "AND " + DatabaseConstants.KEY_FLOOR_ID + " = \"" + params[1] + "\") "
				+ "ORDER BY " + DatabaseConstants.KEY_NODE_ID;
		}
		
		//Log.i("SQLITE","SQL: " + strSQL);
		
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
		cursor = db.rawQuery(strSQL, null);
        
        // create a comma delimited ArrayList<String> from cursor results
		//if (this.future == null || !this.future.isCancelled()) {
			while (cursor.moveToNext()) {
	        	results.add(""
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_ID)) + "," 
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NEIGHBOR_NODE)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_LABEL)) + "," 
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NEIGHBOR_DISTANCE)) + "," 
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_TYPE)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_BUILDING_ID)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_FLOOR_ID)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_FLOOR_LEVEL)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_IS_CONNECTOR)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_FLOOR_MAP)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_PHOTO)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_X)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_Y)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_IS_POI)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_POI_Img)) + ","
	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_BUILDING_NAME)));
			}
		//}
		
		// close the database connection, and return results
		if (cursor != null) {cursor.close();}
		if (db != null) {db.close();}
		
//		if (this.future == null || !this.future.isCancelled()) {
			Log.i("SQLITE","AllData - results.size: " + results.size());
			return results;
//		} else {
//			return null;
//		}

	}
}
