package com.salmonGUI.app.io.sqlite;

import java.util.ArrayList;

import com.salmonGUI.app.io.DatabaseConstants;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper{

	private static SQLiteHelper mInstance = null;
	private ArrayList<String> tabledata = null;
	
	public static SQLiteHelper getInstance(Context context) {
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
		
		//this.tabledata = null;
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
				// fields[1] = nodeLabel	
				// fields[2] = typeName		
				// fields[3] = photoImg	
				// fields[4] = x			
				// fields[5] = y			
				// fields[6] = isConnector	
				// fields[7] = isPOI		
				// fields[8] = poiIconImg	
				// fields[9] = buildingID	
				// fields[10] = buildingName
				// fields[11] = floorID		
				// fields[12] = floorLevel	
				// fields[13] = mapImg		
				// fields[14] = neighborNode	
				// fields[15] = distance		
				
				//insert record row
		        values.clear();
		        values.put(DatabaseConstants.KEY_NODE_ID, fields[0]);
		        values.put(DatabaseConstants.KEY_NODE_LABEL, fields[1]);
		        values.put(DatabaseConstants.KEY_NODE_TYPE, fields[2]);
		        values.put(DatabaseConstants.KEY_NODE_PHOTO, fields[3]);
		        values.put(DatabaseConstants.KEY_NODE_X, Integer.parseInt(fields[4]));//
		        values.put(DatabaseConstants.KEY_NODE_Y, Integer.parseInt(fields[5]));//
		        values.put(DatabaseConstants.KEY_NODE_IS_CONNECTOR, Integer.parseInt(fields[6]));//
		        values.put(DatabaseConstants.KEY_NODE_IS_POI, Integer.parseInt(fields[7]));//
		        values.put(DatabaseConstants.KEY_NODE_POI_Img, fields[8]);
		        values.put(DatabaseConstants.KEY_BUILDING_ID, fields[9]);
		        values.put(DatabaseConstants.KEY_BUILDING_NAME, fields[10]);
		        values.put(DatabaseConstants.KEY_FLOOR_ID, fields[11]);
		        values.put(DatabaseConstants.KEY_FLOOR_LEVEL, Integer.parseInt(fields[12]));//
		        values.put(DatabaseConstants.KEY_FLOOR_MAP, fields[13]);
		        values.put(DatabaseConstants.KEY_NEIGHBOR_NODE, fields[14]);
		        values.put(DatabaseConstants.KEY_NEIGHBOR_DISTANCE, Integer.parseInt(fields[15]));//
		        
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
