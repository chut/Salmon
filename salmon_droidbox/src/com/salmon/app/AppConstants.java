package com.salmon.app;

import com.salmon.activities.AppPreferencesActivity;
import com.salmon.activities.FirstActivity;
import com.salmon.activities.MapViewActivity;

public final class AppConstants {
	public static final String GLOBAL_IMG_STORAGE_LOC = ""; //some location on android storage
	public static final String GLOBAL_WEB_SERVER_ADDRESS = "wayfinder.mapsdb.com";
	public static final String GLOBAL_WEB_SITE = "http://" + GLOBAL_WEB_SERVER_ADDRESS + "/cs460spr2012/mobile/route.jsp";
	public static final boolean GLOBAL_METHOD_TRACING = false;	// not used at this time
	public static final boolean GLOBAL_DEBUGGING = false;	// not used at this time
	
	public static final Class CLASS_HOME = FirstActivity.class;
	public static final Class CLASS_MAP = MapViewActivity.class;
	public static final Class CLASS_PREF = AppPreferencesActivity.class;
	
	// this is a huge change
	// another change
	
}
