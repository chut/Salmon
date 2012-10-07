package com.salmon.app.io;

public class DatabaseConstants {
	// External Database Constants
	public static final String BASE_URL = "http://wayfinder.mapsdb.com/cs460spr2012dev/DBservlet";
	public static final int BUFFERSIZE = 1000;
	
	// Internal (SQLite) Constants
	public static final String DATABASE_NAME = "wayfinder.db";
	public static final int DATABASE_VERSION = 1;
	
	public static final String TABLE_NAME = "alldata";
	
	public static final String KEY_NODE_ID = "nodeID";
	public static final String KEY_NODE_LABEL = "nodeLabel";
	public static final String KEY_NODE_TYPE = "typeName";
	public static final String KEY_NODE_PHOTO = "photoImg";
	public static final String KEY_NODE_X = "x";
	public static final String KEY_NODE_Y = "y";
	public static final String KEY_NODE_IS_CONNECTOR = "isConnector";
	public static final String KEY_NODE_IS_POI = "isPOI";
	public static final String KEY_NODE_POI_Img = "poiIconImg";
	
	public static final String KEY_BUILDING_ID = "buildingID";
	public static final String KEY_BUILDING_NAME = "buildingName";
	public static final String KEY_FLOOR_ID = "floorID";
	public static final String KEY_FLOOR_LEVEL = "floorLevel";
	public static final String KEY_FLOOR_MAP = "mapImg";
	
	public static final String KEY_NEIGHBOR_NODE = "neighborNode";
	public static final String KEY_NEIGHBOR_DISTANCE = "distance";
	
	public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" 
			+ KEY_NODE_ID + " text," 
			+ KEY_NODE_LABEL + " text," 
			+ KEY_NODE_TYPE + " text,"
			+ KEY_NODE_PHOTO + " text,"
			+ KEY_NODE_X + " integer,"
			+ KEY_NODE_Y + " integer,"
			+ KEY_NODE_IS_CONNECTOR + " integer,"
			+ KEY_NODE_IS_POI + " integer,"
			+ KEY_NODE_POI_Img + " text,"
			+ KEY_BUILDING_ID + " text,"
			+ KEY_BUILDING_NAME + " text,"
			+ KEY_FLOOR_ID + " text,"
			+ KEY_FLOOR_LEVEL + " integer,"
			+ KEY_FLOOR_MAP + " text,"
			+ KEY_NEIGHBOR_NODE + " text,"
			+ KEY_NEIGHBOR_DISTANCE + " integer);";
	
	public static final String[] ALL_COLUMNS = { 
			KEY_NODE_ID,
			KEY_NODE_LABEL, 
			KEY_NODE_TYPE,
			KEY_NODE_PHOTO,
			KEY_NODE_X,
			KEY_NODE_Y,
			KEY_NODE_IS_CONNECTOR,
			KEY_NODE_IS_POI,
			KEY_NODE_POI_Img,
			KEY_BUILDING_ID,
			KEY_BUILDING_NAME,
			KEY_FLOOR_ID,
			KEY_FLOOR_LEVEL,
			KEY_FLOOR_MAP,
			KEY_NEIGHBOR_NODE,
			KEY_NEIGHBOR_DISTANCE
		};
	
	// Common - query types
	public static final int QUERY_NEIGHBORS = 1;
	public static final int QUERY_ALLDATA = 2;
	public static final int QUERY_DEBUGMAPDATA = 3;			// used in web-based database verify tool
	
	public static final int QUERY_SYNC_DB = 4;
	public static final int QUERY_LOAD_APP = 5;
	public static final int QUERY_NODES_ALL = 6;
	public static final int QUERY_NODES_BY_FLOOR = 7;
	public static final int QUERY_NODES_BY_TYPE = 8;
	public static final int QUERY_DISPLAY_ALLDATA = 9;		// used in testing - synchs, and then displays all data in text view
	
	// Common - query status codes
	public static final String RESULT_SUCCESS = "success";
	public static final String RESULT_FAILED = "failed";
	
			
			
}
