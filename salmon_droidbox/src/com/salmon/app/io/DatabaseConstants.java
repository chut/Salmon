package com.salmon.app.io;

public class DatabaseConstants {
	// External Database Constants
	public static final String BASE_URL = "http://wayfinder.mapsdb.com/cs401fall2012/DBservlet2";
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
	public static final int QUERY_BLDG_FLR_BY_NODEID = 10;
	public static final int QUERY_ROUTESTEP_BY_NODEID = 11;
	
	// Common - query status codes
	public static final String RESULT_SUCCESS = "success";
	public static final String RESULT_FAILED = "failed";
	
	// Query String - algorithm data - one variable = nodeID
	public static final String SQL_NODEID_1 = ""	
			+ "SELECT " + DatabaseConstants.KEY_NODE_ID + ", "
						+ DatabaseConstants.KEY_NODE_TYPE + ", " 
						+ DatabaseConstants.KEY_NODE_IS_CONNECTOR + ", " 
						+ DatabaseConstants.KEY_BUILDING_ID + ", " 
						+ DatabaseConstants.KEY_FLOOR_ID + ", "
						+ DatabaseConstants.KEY_NEIGHBOR_NODE + ", "
						+ DatabaseConstants.KEY_NEIGHBOR_DISTANCE 
			+ " FROM " + DatabaseConstants.TABLE_NAME 
			+ " WHERE " 
				+ DatabaseConstants.KEY_BUILDING_ID + " IN "
					+ "(SELECT " + DatabaseConstants.KEY_BUILDING_ID + " FROM " + DatabaseConstants.TABLE_NAME 
						+ " WHERE " + DatabaseConstants.KEY_NODE_ID + " = \""; //+ nodeID +		 
	public static final String SQL_NODEID_2	= "\") "
				+ "AND " + DatabaseConstants.KEY_FLOOR_ID + " IN "
					+ "(SELECT " + DatabaseConstants.KEY_FLOOR_ID + " FROM " + DatabaseConstants.TABLE_NAME 
						+ " WHERE " + DatabaseConstants.KEY_NODE_ID + " = \""; //+ nodeID + 
	public static final String SQL_NODEID_3	= "\") " 
			+ "UNION ALL " 
			+ "SELECT " + DatabaseConstants.KEY_NODE_ID + ", "
						+ DatabaseConstants.KEY_NODE_TYPE + ", " 
						+ DatabaseConstants.KEY_NODE_IS_CONNECTOR + ", " 
						+ DatabaseConstants.KEY_BUILDING_ID + ", " 
						+ DatabaseConstants.KEY_FLOOR_ID + ", "
						+ DatabaseConstants.KEY_NEIGHBOR_NODE + ", "
						+ DatabaseConstants.KEY_NEIGHBOR_DISTANCE
			+ " FROM " + DatabaseConstants.TABLE_NAME 
			+ " WHERE "
				+ DatabaseConstants.KEY_NODE_IS_CONNECTOR + " = 1 "
				+ "AND " + DatabaseConstants.KEY_NEIGHBOR_NODE + " IN "
					+ "(SELECT " + DatabaseConstants.KEY_NODE_ID + " FROM " + DatabaseConstants.TABLE_NAME
						+ " WHERE " + DatabaseConstants.KEY_NODE_IS_CONNECTOR + " = 1 "
							+ "AND " + DatabaseConstants.KEY_BUILDING_ID + " IN "
								+ "(SELECT " + DatabaseConstants.KEY_BUILDING_ID + " FROM " + DatabaseConstants.TABLE_NAME
									+ " WHERE " + DatabaseConstants.KEY_NODE_ID + " = \""; //+ nodeID + 
	public static final String SQL_NODEID_4	= "\") "
							+ "AND " + DatabaseConstants.KEY_FLOOR_ID + " IN "
								+ "(SELECT " + DatabaseConstants.KEY_FLOOR_ID + " FROM " + DatabaseConstants.TABLE_NAME
									+ " WHERE " + DatabaseConstants.KEY_NODE_ID + " = \""; // + nodeID + 
	public static final String SQL_NODEID_5	= "\")) "
			+ "ORDER BY " + DatabaseConstants.KEY_NODE_ID;
	
	// Query String - algrorithm data - two variables = buildingID, floorID
	public static final String SQL_BLDFLR_1	= ""
			+ "SELECT " + DatabaseConstants.KEY_NODE_ID + ", "
						+ DatabaseConstants.KEY_NODE_TYPE + ", " 
						+ DatabaseConstants.KEY_NODE_IS_CONNECTOR + ", " 
						+ DatabaseConstants.KEY_BUILDING_ID + ", " 
						+ DatabaseConstants.KEY_FLOOR_ID + ", "
						+ DatabaseConstants.KEY_NEIGHBOR_NODE + ", "
						+ DatabaseConstants.KEY_NEIGHBOR_DISTANCE 
			+ " FROM " + DatabaseConstants.TABLE_NAME
			+ " WHERE " + DatabaseConstants.KEY_BUILDING_ID + " = \""; //+ buildingID + 
	public static final String SQL_BLDFLR_2	= "\" "
				+ "AND " + DatabaseConstants.KEY_FLOOR_ID + " = \""; //+ floorID + 
	public static final String SQL_BLDFLR_3	= "\" " 
			+ " UNION ALL "
			+ "SELECT " + DatabaseConstants.KEY_NODE_ID + ", "
						+ DatabaseConstants.KEY_NODE_TYPE + ", " 
						+ DatabaseConstants.KEY_NODE_IS_CONNECTOR + ", " 
						+ DatabaseConstants.KEY_BUILDING_ID + ", " 
						+ DatabaseConstants.KEY_FLOOR_ID + ", "
						+ DatabaseConstants.KEY_NEIGHBOR_NODE + ", "
						+ DatabaseConstants.KEY_NEIGHBOR_DISTANCE
			+ " FROM " + DatabaseConstants.TABLE_NAME
			+ " WHERE " + DatabaseConstants.KEY_NODE_IS_CONNECTOR + " = 1 "
				+ "AND " + DatabaseConstants.KEY_NEIGHBOR_NODE + " IN "
					+ "(SELECT " + DatabaseConstants.KEY_NODE_ID + " FROM " + DatabaseConstants.TABLE_NAME
						+ " WHERE " + DatabaseConstants.KEY_NODE_IS_CONNECTOR + " = 1 "
							+ "AND " + DatabaseConstants.KEY_BUILDING_ID + " = \""; //+ buildingID + 
	public static final String SQL_BLDFLR_4	= "\" "
							+ "AND " + DatabaseConstants.KEY_FLOOR_ID + " = \""; //+ floorID + 
	public static final String SQL_BLDFLR_5	= "\") "
			+ "ORDER BY " + DatabaseConstants.KEY_NODE_ID;
	
	// Query String - route step info - one variable = routeID
	public static final String SQL_ROUTESTEP_INFO_1 = ""
			+ "SELECT "	+ DatabaseConstants.KEY_NODE_LABEL + ", "
						+ DatabaseConstants.KEY_NODE_PHOTO + ", " 
						+ DatabaseConstants.KEY_NODE_X + ", " 
						+ DatabaseConstants.KEY_NODE_Y + ", " 
						+ DatabaseConstants.KEY_NODE_IS_POI + ", " 
						+ DatabaseConstants.KEY_NODE_POI_Img + ", " 
						+ DatabaseConstants.KEY_BUILDING_NAME + ", " 
						+ DatabaseConstants.KEY_FLOOR_LEVEL + ", " 
						+ DatabaseConstants.KEY_FLOOR_MAP
			+ " FROM "  + DatabaseConstants.TABLE_NAME
			+ " WHERE " + DatabaseConstants.KEY_NODE_ID + " = \""; // + nodeID +
	public static final String SQL_ROUTESTEP_INFO_2 = "\"";
						
			
}
