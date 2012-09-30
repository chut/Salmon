package com.salmon.app.io;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.salmon.app.async_core.TaskBase;
import com.salmon.app.async_core.UIHandler;
import com.salmon.app.io.extdb.ExtDbConstants;
import com.salmon.app.io.extdb.HTTP_Apache;
import com.salmon.app.io.extdb.IDatabaseProvider;
import com.salmon.app.io.extdb.Socket;

public class ExtDatabaseIOTask  extends TaskBase<ArrayList<String>> {
	
	private final IDatabaseProvider dbConn;
	private final int querytype;
	private final String[] params;
	private final Context context;
	
	/* Current (e.g. UI) Thread */
	public ExtDatabaseIOTask(Context context, UIHandler handlerUI, int querytype, String[] params) {
		super(handlerUI);

		this.context = context;
		this.querytype = querytype;
		this.params = params;
		
		// setup database connection provider
		switch (ExtDbConstants.DATABASE_PROVIDER) {
		case ExtDbConstants.PROVIDER_HTTP_APACHE:
			dbConn = new HTTP_Apache();
			break;
		
		case ExtDbConstants.PROVIDER_SOCKET:
			dbConn = new Socket();
			break;

		default:
			// default to HTTP_APACHE
			dbConn = new HTTP_Apache();
			
			break;
		}
		
	}
	
	/* Separate Thread */
	@Override
	public ArrayList<String> call() throws Exception {
		return dbConn.getDataFromDatabase(querytype, params);
	}

	/* Current (e.g. UI) Thread */
	@Override
	public void onCancel(DialogInterface dialog) {
		Toast.makeText(context, "Task canceled!", Toast.LENGTH_SHORT).show();
	}
    
}
