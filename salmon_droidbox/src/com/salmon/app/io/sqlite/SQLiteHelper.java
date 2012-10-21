package com.salmon.app.io.sqlite;

import java.util.ArrayList;

import com.salmon.app.io.DatabaseConstants;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper{

	private static SQLiteHelper mInstance = null;
	private ArrayList<String> tabledata;
	
	public static SQLiteHelper getInstanstance(Context context) {
		if (mInstance == null) {
			mInstance = new SQLiteHelper(context);
		}
		return mInstance;
	}
	
//	public static SQLiteHelper getInstanstance(Context context, ArrayList<String> tabledata) {
//		if (mInstance == null) {
//			mInstance = new SQLiteHelper(context, tabledata);
//		} else {
//			mInstance.tabledata = tabledata;
//		}
//		
//		return mInstance;
//	}
	
	private SQLiteHelper(Context context) {
		super(context, DatabaseConstants.DATABASE_NAME, null, DatabaseConstants.DATABASE_VERSION);
		
		this.tabledata = null;
	}

//	private SQLiteHelper(Context context, ArrayList<String> tabledata) {
//		super(context, DatabaseConstants.DATABASE_NAME, null, DatabaseConstants.DATABASE_VERSION);
//		
//		this.tabledata = tabledata;
//	}
	
	
	public void setTableData (ArrayList<String> tabledata) {
		Log.i("SQLITE","setTableData table size: " + tabledata.size());
		mInstance.tabledata = tabledata;
	}
	
	// called to create table
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i("SQLITE","onCreate database");
		db.execSQL("DROP TABLE IF EXISTS " + DatabaseConstants.TABLE_NAME);
		String sql = DatabaseConstants.CREATE_TABLE;
		//db.setVersion((int) System.currentTimeMillis());
		db.execSQL(sql);
		ContentValues values = new ContentValues();
		
		String[] fields;
		if (tabledata != null) {
			Log.i("SQLITE","creating table");
			for (String record : tabledata) {
				fields = record.split(",");
				// fields[0] = nodeID		
				// fields[1] = neighborNode	
				// fields[2] = nodeLabel	
				// fields[3] = distance		
				// fields[4] = typeName		
				// fields[5] = buildingID	
				// fields[6] = floorID		
				// fields[7] = floorLevel	
				// fields[8] = isConnector	
				// fields[9] = mapImg		
				// fields[10] = photoImg	
				// fields[11] = x			
				// fields[12] = y			
				// fields[13] = isPOI		
				// fields[14] = poiIconImg	
				// fields[15] = buildingName
				
				//insert record row
		        values.clear();
		        values.put(DatabaseConstants.KEY_NODE_ID, fields[0]);
		        values.put(DatabaseConstants.KEY_NODE_LABEL, fields[2]);
		        values.put(DatabaseConstants.KEY_NODE_TYPE, fields[4]);
		        values.put(DatabaseConstants.KEY_NODE_PHOTO, fields[10]);
		        values.put(DatabaseConstants.KEY_NODE_X, Integer.parseInt(fields[11]));//
		        values.put(DatabaseConstants.KEY_NODE_Y, Integer.parseInt(fields[12]));//
		        values.put(DatabaseConstants.KEY_NODE_IS_CONNECTOR, Integer.parseInt(fields[8]));//
		        values.put(DatabaseConstants.KEY_NODE_IS_POI, Integer.parseInt(fields[13]));//
		        values.put(DatabaseConstants.KEY_NODE_POI_Img, fields[14]);
		        values.put(DatabaseConstants.KEY_BUILDING_ID, fields[5]);
		        values.put(DatabaseConstants.KEY_BUILDING_NAME, fields[15]);
		        values.put(DatabaseConstants.KEY_FLOOR_ID, fields[6]);
		        values.put(DatabaseConstants.KEY_FLOOR_LEVEL, Integer.parseInt(fields[7]));//
		        values.put(DatabaseConstants.KEY_FLOOR_MAP, fields[9]);
		        values.put(DatabaseConstants.KEY_NEIGHBOR_NODE, fields[1]);
		        values.put(DatabaseConstants.KEY_NEIGHBOR_DISTANCE, Integer.parseInt(fields[3]));//
		        
		        db.insert(DatabaseConstants.TABLE_NAME, null, values);
			}
		}
		
	}

	//called when database version mismatch
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i("SQLITE","onUpgrade database");
		newVersion++;	// always force an update
		if (oldVersion >= newVersion) return;

		db.execSQL("DROP TABLE IF EXISTS " + DatabaseConstants.TABLE_NAME);
		onCreate(db);
	}

}
