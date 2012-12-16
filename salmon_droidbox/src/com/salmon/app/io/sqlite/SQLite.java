package com.salmon.app.io.sqlite;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.salmon.app.AppConstants;
import com.salmon.app.io.DatabaseConstants;
import com.salmon.app.io.IDatabaseProvider;
import com.salmon.app.io.Task_DatabaseIO;

public class SQLite implements IDatabaseProvider {
	
	private final SQLiteHelper sqliteHelper;
//	private static SQLite mInstance = null;
	private String[] params;
	private Context context;
	private SQLiteDatabase db = null;
	private Cursor cursor = null;
	private final ArrayList<String> results = new ArrayList<String>();
	
//	public static SQLite getInstance(Context context) {
//		if (mInstance == null) {
//			mInstance = new SQLite(context);
//		}
//		return mInstance;
//	}
	
	public SQLite(Context context) {
		this.context = context;
		
		// setup helper
		this.sqliteHelper = SQLiteHelper.getInstance(context);
		
		// open the SQLite database
		openDatabaseConnection();
//		try {
//			db = sqliteHelper.getWritableDatabase();
//        } catch(SQLException e) { 
//        	// TODO error handling
//        	db = null;
//        }
	}
	
	public ArrayList<String> getData() {
		return this.results;
	}
	
	public void close() {
		if (this.cursor != null) this.cursor.close();
		if (this.db != null) this.db.close();
		this.cursor = null;
		this.db = null;
	}
	
	public SQLite submitQuery(int queryType, String[] params) {
		//Log.i("SQLITE","getDataFromDatabase - begin");
		//final ArrayList<String> results = new ArrayList<String>();
		this.params = params;
		this.results.clear();
		
		switch (queryType) {
		case DatabaseConstants.QUERY_SYNC_DB:
			query_SynchDB();
			return this;
			
//		case SQLiteConstants.QUERY_LOAD_APP:	// used with splash screen
//			results.addAll(query_SynchDB());
//			
//			toggleProgress();
//			handlerUI.post((Runnable) element1);			
//			return results;
		case DatabaseConstants.QUERY_ALLDATA:
			query_AllData();
			return this;
			
		case DatabaseConstants.QUERY_NODES_ALL:
			query_NodesAll();
			return this;
			
		case DatabaseConstants.QUERY_NODES_BY_FLOOR:
			query_NodesByFloor();
			return this;
			
		case DatabaseConstants.QUERY_NODES_BY_TYPE:
			query_NodesByType();
			return this;
			
		case DatabaseConstants.QUERY_DISPLAY_ALLDATA:
			query_DisplayAllData();
			return this;
			
		case DatabaseConstants.QUERY_NEIGHBORS:
			query_Neighbors();
			return this;
			
		case DatabaseConstants.QUERY_BLDG_FLR_BY_NODEID:
			query_BldgFloor();
			return this;
		
		case DatabaseConstants.QUERY_ROUTESTEP_BY_NODEID:
			query_RoutStepInfo();
			return this;
		
		default:
			// an unknown querytype has been passed, fail.
			return null;
		}
		
	}
	
	private boolean openDatabaseConnection() {
		if (this.db == null || !this.db.isOpen()) {
			// open the SQLite database
			try {
				this.db = sqliteHelper.getWritableDatabase();
	        } catch(SQLException e) { 
	        	// TODO error handling
	        	this.db = null;
	        	return false;
	        }
			return true;
		}
		return true;
		
	}
		
	private boolean query_SynchDB () {
		Log.i("SQLITE", "sync db");
		
		//SQLiteDatabase db = null;
		//Cursor cursor = null;
		//final ArrayList<String> results = new ArrayList<String>();
		
		// obtain data from external database
		Task_DatabaseIO<String, String> dbTask = new Task_DatabaseIO<String, String>(context, null, DatabaseConstants.QUERY_ALLDATA, null, AppConstants.PROVIDER_EXT_HTTP_APACHE);
		try {
			sqliteHelper.setTableData(dbTask.call());
//			ArrayList<String> temp = dbTask.call();
//			Log.i("SQLITE","dbTask result.size: " + temp.size());
//			sqliteHelper.setTableData(temp);
		} catch (Exception e) {
			// TODO error handling of dbTask failure
			//results.add(DatabaseConstants.RESULT_FAILED);
        	return false;
		}
		
		// open/create the SQLite database 
		if (!openDatabaseConnection()) {return false;}
		//if (this.future == null || !this.future.isCancelled()) {
			try {
				//db = sqliteHelper.getWritableDatabase();
				Log.i("SQLITE", "db version: " + this.db.getVersion());
				sqliteHelper.onCreate(this.db);
				Log.i("SQLITE", "db version2: " + this.db.getVersion());
				this.cursor = this.db.rawQuery("SELECT 1", null); 
	        } catch(SQLException e) { 
	        	// TODO error handling
	        	//if (db != null) {db.close();}
	        	close();
	        	//results.add(DatabaseConstants.RESULT_FAILED);
	        	return false;
	        }
		//}
		
		// close the database connection
		//if (cursor != null) {cursor.close();}
		//if (db != null) {db.close();}
		//close();
		
//		if (this.future == null || !this.future.isCancelled()) {
			//results.add(DatabaseConstants.RESULT_SUCCESS);
			Log.i("SQLITE","sync success");
	    	return true;
//		} else {
//			return null;
//		}
	}
	
	private boolean query_AllData() {
		Log.i("SQLITE", "all data");
		
		//SQLiteDatabase db = null;
		//Cursor cursor = null;
		//final ArrayList<String> results = new ArrayList<String>();
		
		// open the SQLite database
		if (!openDatabaseConnection()) {return false;}
//		try {
//			db = sqliteHelper.getWritableDatabase();
//        } catch(SQLException e) { 
//        	// TODO error handling
//        	if (db != null) {db.close();}
//        	//results.add(DatabaseConstants.RESULT_FAILED);
//        	return null;
//        }
		
		// obtain data from sqlite database
		this.cursor = this.db.query(
				DatabaseConstants.TABLE_NAME, 	// table
				DatabaseConstants.ALL_COLUMNS, 	// columns
				null, 							// where clause
				null, 							// selection args
				null, 							// groupBy
				null, 							// having
				DatabaseConstants.KEY_NODE_ID		// orderBy
			);
        
        // create a comma delimited ArrayList<String> from cursor results
		if (this.cursor != null) {
			final int numCols = 16;
			StringBuilder sbRow;
			while (this.cursor.moveToNext()) {
				sbRow = new StringBuilder(128);
				for (int i = 0; i < numCols; i++) {
					sbRow.append(this.cursor.getString(i)).append(",");
				}
				this.results.add(sbRow.toString());
	        	sbRow = null;
			}
		} else {
			this.results.add(DatabaseConstants.RESULT_FAILED);
		}
		//if (this.future == null || !this.future.isCancelled()) {
//			while (cursor.moveToNext()) {
//	        	results.add(""
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_ID)) + "," 
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_LABEL)) + "," 
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_TYPE)) + ","
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_PHOTO)) + ","
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_X)) + ","
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_Y)) + ","
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_IS_CONNECTOR)) + ","
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_IS_POI)) + ","
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_POI_Img)) + ","
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_BUILDING_ID)) + ","
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_BUILDING_NAME)) + ","
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_FLOOR_ID)) + ","
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_FLOOR_LEVEL)) + ","
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_FLOOR_MAP)) + ","
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NEIGHBOR_NODE)) + ","
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NEIGHBOR_DISTANCE))); 
//			}
		//}
		
		
		
		// close the database connection, and return results
		//if (cursor != null) {cursor.close();}
		//if (db != null) {db.close();}
		
//		if (this.future == null || !this.future.isCancelled()) {
			Log.i("SQLITE","AllData - results.size: " + this.results.size());
			return true;
//		} else {
//			return null;
//		}
	}
	
	private boolean query_NodesAll() {
		//SQLiteDatabase db = null;
		//Cursor cursor = null;
		//final ArrayList<String> results = new ArrayList<String>();
		
		// open the SQLite database
		if (!openDatabaseConnection()) {return false;}
//		try {
//			db = sqliteHelper.getWritableDatabase();
//        } catch(SQLException e) { 
//        	// TODO error handling
//        	if (db != null) {db.close();}
//        	//results.add(DatabaseConstants.RESULT_FAILED);
//        	return null;
//        }
		
		// obtain data from sqlite database
		this.cursor = this.db.query(
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
		if (this.cursor != null) {
			final int numCols = 2;
			StringBuilder sbRow;
			while (this.cursor.moveToNext()) {
				sbRow = new StringBuilder(32);
				for (int i = 0; i < numCols; i++) {
					sbRow.append(this.cursor.getString(i)).append(",");
				}
				this.results.add(sbRow.toString());
	        	sbRow = null;
			}
		} else {
			this.results.add(DatabaseConstants.RESULT_FAILED);
		}
		//if (this.future == null || !this.future.isCancelled()) {
//			while (cursor.moveToNext()) {
//				results.add(""
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_ID)) + " | " 
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_LABEL)));
//			}
		//}
		
		// close the database connection, and return results
		//if (cursor != null) {cursor.close();}
		//if (db != null) {db.close();}
		
//		if (this.future == null || !this.future.isCancelled()) {
			Log.i("SQLITE","query_NodesAll - results.size: " + this.results.size());
			return true;
//		} else {
//			return null;
//		}
	}
	
	private boolean query_NodesByFloor() {
		//SQLiteDatabase db = null;
		//Cursor cursor = null;
		//final ArrayList<String> results = new ArrayList<String>();
		
		// open the SQLite database
		if (!openDatabaseConnection()) {return false;}
//		try {
//			db = sqliteHelper.getWritableDatabase();
//        } catch(SQLException e) { 
//        	// TODO error handling
//        	if (db != null) {db.close();}
//        	//results.add(DatabaseConstants.RESULT_FAILED);
//        	return null;
//        }
		
		// obtain data from sqlite database
		this.cursor = this.db.query(
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
		if (this.cursor != null) {
			final int numCols = 2;
			StringBuilder sbRow;
			while (this.cursor.moveToNext()) {
				sbRow = new StringBuilder(32);
				for (int i = 0; i < numCols; i++) {
					sbRow.append(this.cursor.getString(i)).append(",");
				}
				this.results.add(sbRow.toString());
	        	sbRow = null;
			}
		} else {
			this.results.add(DatabaseConstants.RESULT_FAILED);
		}
		//if (this.future == null || !this.future.isCancelled()) {
//			while (cursor.moveToNext()) {
//				results.add(""
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_ID)) + " | " 
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_LABEL)));
//			}
		//}
		
		// close the database connection, and return results
		//if (cursor != null) {cursor.close();}
		//if (db != null) {db.close();}
		
//		if (this.future == null || !this.future.isCancelled()) {
			Log.i("SQLITE","query_NodesByFloor - results.size: " + this.results.size());
			return true;
//		} else {
//			return null;
//		}
	}
	
	private boolean query_NodesByType() {
		//SQLiteDatabase db = null;
		//Cursor cursor = null;
		//final ArrayList<String> results = new ArrayList<String>();
		
		// open the SQLite database
		if (!openDatabaseConnection()) {return false;}
//		try {
//			db = sqliteHelper.getWritableDatabase();
//        } catch(SQLException e) { 
//        	// TODO error handling
//        	if (db != null) {db.close();}
//        	//results.add(DatabaseConstants.RESULT_FAILED);
//        	return null;
//        }
		
		// obtain data from sqlite database
		this.cursor = this.db.query(
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
		if (this.cursor != null) {
			final int numCols = 2;
			StringBuilder sbRow;
			while (this.cursor.moveToNext()) {
				sbRow = new StringBuilder(32);
				for (int i = 0; i < numCols; i++) {
					sbRow.append(this.cursor.getString(i)).append(",");
				}
				this.results.add(sbRow.toString());
	        	sbRow = null;
			}
		} else {
			this.results.add(DatabaseConstants.RESULT_FAILED);
		}
		//if (this.future == null || !this.future.isCancelled()) {
//			while (cursor.moveToNext()) {
//				results.add(""
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_ID)) + " | " 
//	        			+ cursor.getString(cursor.getColumnIndex(DatabaseConstants.KEY_NODE_LABEL)));
//			}
		//}
		
		// close the database connection, and return results
		//if (cursor != null) {cursor.close();}
		//if (db != null) {db.close();}
		
//		if (this.future == null || !this.future.isCancelled()) {
			Log.i("SQLITE","query_NodesByType - results.size: " + this.results.size());
			return true;
//		} else {
//			return null;
//		}
	}

	private boolean query_DisplayAllData() {
		//final ArrayList<String> results = new ArrayList<String>();
		//Cursor cursor = null;
		
		// first, synch database
		// second, get all data
		//if (this.future == null || !this.future.isCancelled()) {
			if (query_SynchDB()) {
				this.cursor.close();
				return query_AllData();
			}
		//}
			
		return false;
//		Log.i("SQLITE","query_DisplayAllData - results.size: " + this.results.size());
//		return this.cursor;
	}
	
	private boolean query_Neighbors() {
		//long methodStart = System.currentTimeMillis();
		//long startTime = System.currentTimeMillis();
		Log.i("SQLITE", "query_Neighbors - begin");
				
		//Log.i("SQLITE","Params.length:" + params.length);
		//Log.i("MICRO","part 1: " + (System.currentTimeMillis() - startTime));
		// open the SQLite database
		//startTime = System.currentTimeMillis();
		if (!openDatabaseConnection()) {return false;}
		//Log.i("MICRO","open db took: " + (System.currentTimeMillis() - startTime) + " milliseconds");
		
		// construct SQL statements
		//startTime = System.currentTimeMillis();
		StringBuilder sbSQL = new StringBuilder(512);
		if (params.length == 1) {
			// nodeID (params[0]) was passed.  base SQL off nodeID
			//Log.i("SQLITE","base SQL off nodeID: " + params[0]);
			sbSQL.append(DatabaseConstants.SQL_NODEID_1);
			sbSQL.append(params[0]);
			sbSQL.append(DatabaseConstants.SQL_NODEID_2);
			sbSQL.append(params[0]);
			sbSQL.append(DatabaseConstants.SQL_NODEID_3);
			sbSQL.append(params[0]);
			sbSQL.append(DatabaseConstants.SQL_NODEID_4);
			sbSQL.append(params[0]);
			sbSQL.append(DatabaseConstants.SQL_NODEID_5);
		
		} else {
			// buildingID (params[0]) and floorID (params[1]) were passed.  base SQL off of those.
			//Log.i("SQLITE","base SQL off buildingID and floorID: " + params[0] + ", " + params[1]);
			sbSQL.append(DatabaseConstants.SQL_BLDFLR_1);
			sbSQL.append(params[0]);
			sbSQL.append(DatabaseConstants.SQL_BLDFLR_2);
			sbSQL.append(params[1]);
			sbSQL.append(DatabaseConstants.SQL_BLDFLR_3);
			sbSQL.append(params[0]);
			sbSQL.append(DatabaseConstants.SQL_BLDFLR_4);
			sbSQL.append(params[1]);
			sbSQL.append(DatabaseConstants.SQL_BLDFLR_5);
		}
		//Log.i("MICRO","create SQL statement took: " + (System.currentTimeMillis() - startTime) + " milliseconds");
		//Log.i("SQLITE","SQL: " + sbSQL.toString());
		
		// obtain data from sqlite database
		//startTime = System.currentTimeMillis();
		this.cursor = this.db.rawQuery(sbSQL.toString(), null);
		//Log.i("MICRO","get cursor data took: " + (System.currentTimeMillis() - startTime) + " milliseconds");
		
		//Log.i("SQLITE","cursor: " + cursor.getColumnCount());
		// create an ArrayList<String> from cursor results
		if (this.cursor != null) {
			final int numCols = 16;
			StringBuilder sbRow;
			while (this.cursor.moveToNext()) {
				sbRow = new StringBuilder(128);
				for (int i = 0; i < numCols; i++) {
					//Log.i("SQLITE",i + ": " + this.cursor.getString(i));
					sbRow.append(this.cursor.getString(i)).append(",");
				}
				this.results.add(sbRow.toString());
	        	sbRow = null;
			}
		} else {
			this.results.add(DatabaseConstants.RESULT_FAILED);
		}
		
//		if (this.future == null || !this.future.isCancelled()) {
			//Log.i("SQLITE","query_Neighbors - results.size: " + this.results.size());
			//Log.i("MICRO","query_Neighbors: " + (System.currentTimeMillis() - methodStart));
			return true;
//		} else {
//			return null;
//		}

	}
	
	private boolean query_BldgFloor() {
		Log.i("SQLITE", "query_BldgFloor - begin");
		
		// for testing
		//long startTime = 0;
		//long endTime = 0;

		
		// open the SQLite database
		//startTime = System.currentTimeMillis();
		if (!openDatabaseConnection()) {return false;}
		//Log.i("SQLITE","Database open");
		//endTime = System.currentTimeMillis();
		//Log.i("MICRO","open db took: " + (endTime - startTime) + " milliseconds");
		
		// construct SQL statements
		//startTime = System.currentTimeMillis();
		final String strSQL = "SELECT " + DatabaseConstants.KEY_BUILDING_ID + ", "
										+ DatabaseConstants.KEY_FLOOR_ID + 
							 " FROM " + DatabaseConstants.TABLE_NAME + 
							 " WHERE " + DatabaseConstants.KEY_NODE_ID + "=\"" + params[0] + "\"";
		//endTime = System.currentTimeMillis();
		//Log.i("MICRO","create SQL statement took: " + (endTime - startTime) + " milliseconds");
		//Log.i("SQLITE","SQL: " + strSQL);
		
		// obtain data from sqlite database
		//startTime = System.currentTimeMillis();
		this.cursor = this.db.rawQuery(strSQL, null);
		//Log.i("SQLITE","data obtained");
		//endTime = System.currentTimeMillis();
		//Log.i("MICRO","get cursor data took: " + (endTime - startTime) + " milliseconds");
		
		// create an ArrayList<String> from cursor results
		if (this.cursor != null) {
			final int numCols = 2;
			StringBuilder sbRow;
			while (this.cursor.moveToNext()) {
				sbRow = new StringBuilder(32);
				for (int i = 0; i < numCols; i++) {
					sbRow.append(this.cursor.getString(i)).append(",");
				}
				this.results.add(sbRow.toString());
	        	sbRow = null;
			}
		} else {
			this.results.add(DatabaseConstants.RESULT_FAILED);
		}
		
//		if (this.future == null || !this.future.isCancelled()) {
		
			//Log.i("SQLITE","query_BldgFloor - results.size: " + this.results.size());
			return true;
//		} else {
//			return null;
//		}
		        
	}
	
	private boolean query_RoutStepInfo() {
		//Log.i("SQLITE", "query_RoutStepInfo - begin");
		
		// for testing
		//long startTime = 0;
		//long endTime = 0;

		
		// open the SQLite database
		//startTime = System.currentTimeMillis();
		if (!openDatabaseConnection()) {return false;}
		//Log.i("SQLITE","Database open");
		//endTime = System.currentTimeMillis();
		//Log.i("MICRO","open db took: " + (endTime - startTime) + " milliseconds");
		
		// construct SQL statements
		//startTime = System.currentTimeMillis();
		final String strSQL = DatabaseConstants.SQL_ROUTESTEP_INFO_1 + params[0] + DatabaseConstants.SQL_ROUTESTEP_INFO_2;
		//endTime = System.currentTimeMillis();
		//Log.i("MICRO","create SQL statement took: " + (endTime - startTime) + " milliseconds");
		//Log.i("SQLITE","SQL: " + strSQL);
		
		// obtain data from sqlite database
		//startTime = System.currentTimeMillis();
		this.cursor = this.db.rawQuery(strSQL, null);
		//Log.i("SQLITE","data obtained");
		//endTime = System.currentTimeMillis();
		//Log.i("MICRO","get cursor data took: " + (endTime - startTime) + " milliseconds");
		
		// create an ArrayList<String> from cursor results
		if (this.cursor != null) {
			final int numCols = 9;
			StringBuilder sbRow;
			while (this.cursor.moveToNext()) {
				sbRow = new StringBuilder(64);
				for (int i = 0; i < numCols; i++) {
					sbRow.append(this.cursor.getString(i)).append(",");
				}
				this.results.add(sbRow.toString());
	        	sbRow = null;
			}
		} else {
			this.results.add(DatabaseConstants.RESULT_FAILED);
		}

//		if (this.future == null || !this.future.isCancelled()) {
		
			//Log.i("SQLITE","query_BldgFloor - results.size: " + this.results.size());
			return true;
//		} else {
//			return null;
//		}
		        
	}
}
