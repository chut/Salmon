package com.salmonGUI.app.io;

import java.util.ArrayList;
import java.util.concurrent.Future;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.salmonGUI.activities.MapViewActivity;
import com.salmonGUI.app.AppConstants;
import com.salmonGUI.app.Route;
import com.salmonGUI.app.async_core.PostRunnableBase;
import com.salmonGUI.app.async_core.ThreadManagerBase;
import com.salmonGUI.app.async_core.UIHandler;


public class AppIO extends ThreadManagerBase{
	
private final UIHandler handlerUI;
	
	public AppIO(int threadPoolSize) {
		super(threadPoolSize);
		this.handlerUI = new UIHandler();
	}
	
	// calculate route
	public int calculateRoute(Context context, Route route, MapViewActivity activity) {
		// create task
		Task_Route<String, String> routeTask = new Task_Route<String, String>(context, handlerUI, route, activity);
		
		// attach progress dialog
		routeTask.addProgressDialog(context, null, "Working, please wait...");	// TODO refactor this to use @String
		
		// submit task
		Future<Integer> future = this.executorService.submit(routeTask);
		
		// enable task canceling
		routeTask.initOnCancelListener(future);

		return 0;
	}
		
	public void syncDatabase_async(int progressBar, Activity activity) {
		// create task
		Task_DatabaseIO<String, Context> databaseTask = new Task_DatabaseIO<String, Context>(activity, handlerUI, DatabaseConstants.QUERY_SYNC_DB, null, AppConstants.PROVIDER_INT_SQLITE);
		
		// attach progress bar/dialog
		switch (progressBar) {
		case AppConstants.PROGRESS_BAR:
			// app is not using progress bars (do nothing)
			break;
		case AppConstants.PROGRESS_BAR_INDETERMINATE:
			databaseTask.addIndeterminateProgressBar(activity);
			break;
		case AppConstants.PROGRESS_DIALOG:
			databaseTask.addProgressDialog(activity, null, "Working, please wait...");
			break;

		default:
			// do nothing - no progress bar/dialog
			break;
		}
		
		// create and set resultTask
		Post_ToastMessage resultTask = new Post_ToastMessage("Database sync completed!", activity);
		databaseTask.setResultTask(resultTask);
		
		// submit task
		Future<ArrayList<String>> future = this.executorService.submit(databaseTask);
				
		// enable task canceling
		databaseTask.initOnCancelListener(future);

		
	}
	
	public void updateTextView_async(int querytype, int progressBar, TextView element1, Activity activity, String ... params) {
		// create task
		//Task_SQLiteIO<TextView, String> sqliteTask = new Task_SQLiteIO<TextView, String>(activity, handlerUI, querytype, params, element1, null);
		Task_DatabaseIO<TextView, String> sqliteTask = new Task_DatabaseIO<TextView, String>(activity, handlerUI, querytype, params);
		
		// attach progress bar/dialog
		switch (progressBar) {
		case AppConstants.PROGRESS_BAR:
			// app is not using progress bars (do nothing)
			break;
		case AppConstants.PROGRESS_BAR_INDETERMINATE:
			sqliteTask.addIndeterminateProgressBar(activity);
			break;
		case AppConstants.PROGRESS_DIALOG:
			sqliteTask.addProgressDialog(activity, null, "Working, please wait...");
			break;

		default:
			// do nothing - no progress bar/dialog
			break;
		}
		
		// create and set resultTask
		Post_UpdateTextView resultTask = new Post_UpdateTextView(element1);
		sqliteTask.setResultTask(resultTask);
		
		// submit task
		Future<ArrayList<String>> future = this.executorService.submit(sqliteTask);
				
		// enable task canceling
		sqliteTask.initOnCancelListener(future);

	}
	
	public void updateListView_async(int querytype, int progressBar, ArrayList<String> element1, ArrayAdapter<String> element2, Activity activity, String ... params) {
		Log.i("APPIO","begin updateListView");
		// create task
		//Task_SQLiteIO<ArrayList<String>, ArrayAdapter<String>> sqliteTask = new Task_SQLiteIO<ArrayList<String>, ArrayAdapter<String>>(activity, handlerUI, querytype, params, element1, element2);
		Task_DatabaseIO<ArrayList<String>, ArrayAdapter<String>> sqliteTask = new Task_DatabaseIO<ArrayList<String>, ArrayAdapter<String>>(activity, handlerUI, querytype, params);
		
		// attach progress bar/dialog
		switch (progressBar) {
		case AppConstants.PROGRESS_BAR:
			// app is not using progress bars (do nothing)
			break;
		case AppConstants.PROGRESS_BAR_INDETERMINATE:
			sqliteTask.addIndeterminateProgressBar(activity);
			break;
		case AppConstants.PROGRESS_DIALOG:
			sqliteTask.addProgressDialog(activity, null, "Working, please wait...");
			break;

		default:
			// do nothing - no progress bar/dialog
			break;
		}
		
		// create and set resultTask
		Post_UpdateList resultTask = new Post_UpdateList(element1, element2);
		sqliteTask.setResultTask(resultTask);
		
		// submit task
		Future<ArrayList<String>> future = this.executorService.submit(sqliteTask);
				
		// enable task canceling
		sqliteTask.initOnCancelListener(future);

	}
	
	public void updateNodeInfo_async(int querytype, int progressBar, ArrayList<NodeInfo> element1, ArrayAdapter<NodeInfo> element2, Activity activity, String ... params) {
		Log.i("APPIO","begin updateListView");
		// create task
		//Task_SQLiteIO<ArrayList<String>, ArrayAdapter<String>> sqliteTask = new Task_SQLiteIO<ArrayList<String>, ArrayAdapter<String>>(activity, handlerUI, querytype, params, element1, element2);
		Task_DatabaseIO<ArrayList<NodeInfo>, ArrayAdapter<NodeInfo>> sqliteTask = new Task_DatabaseIO<ArrayList<NodeInfo>, ArrayAdapter<NodeInfo>>(activity, handlerUI, querytype, params);
		
		// attach progress bar/dialog
		switch (progressBar) {
		case AppConstants.PROGRESS_BAR:
			// app is not using progress bars (do nothing)
			break;
		case AppConstants.PROGRESS_BAR_INDETERMINATE:
			sqliteTask.addIndeterminateProgressBar(activity);
			break;
		case AppConstants.PROGRESS_DIALOG:
			sqliteTask.addProgressDialog(activity, null, "Working, please wait...");
			break;

		default:
			// do nothing - no progress bar/dialog
			break;
		}
		
		// create and set resultTask
		Post_UpdateNodeInfo resultTask = new Post_UpdateNodeInfo(element1, element2);
		sqliteTask.setResultTask(resultTask);
		
		// submit task
		Future<ArrayList<String>> future = this.executorService.submit(sqliteTask);
				
		// enable task canceling
		sqliteTask.initOnCancelListener(future);

	}
	
	public final String getUIDatabaseProviderName() {
		switch (AppConstants.DATABASE_PROVIDER_UI) {
		case AppConstants.PROVIDER_EXT_HTTP_APACHE:
			return "HTTP_Apache";
						
		case AppConstants.PROVIDER_EXT_SOCKET:
			return "Socket";
			
		case AppConstants.PROVIDER_INT_SQLITE:
			return "SQLITE";
			
		default:
			return "unknown";
			
		}
		
	}
	
	private class Post_UpdateTextView extends PostRunnableBase<TextView, String> {
		
		public Post_UpdateTextView(TextView element1) {
			super(element1, null);
		}

		@Override
		public void run() {
			// how costly is this type casting???
			if (results.size() > 0) {
				handlerUI.post(new Runnable() {
					public void run() {
						int mCount = 1;
				    	for (String string : results) {
					    	element1.append(mCount + ". " + string + "\n");
					    	mCount++;
						}
					}
				});
				
			} else {
				handlerUI.post(new Runnable() {
					public void run() {
				    	element1.append("\n** no records found in sqlite database\n");
					}
				});
			}
		}
	}

	private class Post_UpdateList extends PostRunnableBase<ArrayList<String>, ArrayAdapter<String>> {
		
		public Post_UpdateList(ArrayList<String> element1, ArrayAdapter<String> element2) {
			super(element1, element2);
		}

		@Override
		public void run() {
			// first, clear the items list
			element1.clear();
			
			// update arrayList
			element1.addAll(results);			
			
			// notify array adapter
			element2.notifyDataSetChanged();
		}
	}
	
	private class Post_UpdateNodeInfo extends PostRunnableBase<ArrayList<NodeInfo>, ArrayAdapter<NodeInfo>> {
		
		public Post_UpdateNodeInfo(ArrayList<NodeInfo> element1, ArrayAdapter<NodeInfo> element2) {
			super(element1, element2);
		}

		@Override
		public void run() {
			// first, clear the items list
			element1.clear();
			
			// always have a blank entry as first item
			element1.add(new NodeInfo("",""));
			for (int i = 0; i < results.size(); i++) {
				String[] column = results.get(i).split(",");
				element1.add(new NodeInfo(column[0],column[1]));
			}
			
			// notify array adapter
			element2.notifyDataSetChanged();
		}
	}

	private class Post_ToastMessage extends PostRunnableBase<String, Context> {
				
		public Post_ToastMessage(String message, Context context) {
			super(message, context);	// super(element1, element2)
		}

		@Override
		public void run() {
			//Log.i("RESULT","toasting message: " + element1);
			Toast.makeText(element2, element1, Toast.LENGTH_SHORT).show();
		}
	}
}
