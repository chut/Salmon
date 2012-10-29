package com.salmon.app.io.extdb;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.database.Cursor;

import com.salmon.app.io.DatabaseConstants;
import com.salmon.app.io.IDatabaseProvider;

public class HTTP_Apache implements IDatabaseProvider {
	
	private final ArrayList<String> resultArray = new ArrayList<String>();
	
	public HTTP_Apache() {
		
	}
	
	public HTTP_Apache submitQuery(int queryType, String[] strValue) {
		//System.out.println("getDataFromDatabase - strSQL = " + strSQL);
		//ArrayList<String> resultArray = new ArrayList<String>();
		
		HttpClient httpclient = null;
		HttpGet httpget = null;
		HttpResponse response = null;
		HttpEntity entity = null;
		InputStream instream = null;
		
		httpclient = new DefaultHttpClient();
		
		String url = DatabaseConstants.BASE_URL;
		
		switch (queryType) {
		case DatabaseConstants.QUERY_NEIGHBORS:
			//System.out.println("neighbors");
			
			// construct URL address
			if (strValue.length == 1) {
				// nodeID was passed.  base SQL off nodeID
				url = url + "?type=" + DatabaseConstants.QUERY_NEIGHBORS + "&nid=" + strValue[0];
			} else {
				// buildingID and floorID were passed.  base SQL off of those.
				url = url + "?type=" + DatabaseConstants.QUERY_NEIGHBORS + "&bid=" + strValue[0] + "&fid=" + strValue[1].replaceAll("\\s", "*");
			}
			
			break;
		case DatabaseConstants.QUERY_ALLDATA:
			url = url + "?type=" + DatabaseConstants.QUERY_ALLDATA;
			
			break;	
		default:
			return this;
		}
		
		System.out.println("url: " + url);
		httpget = new HttpGet(url);
		
		try {
			response = httpclient.execute(httpget);
			System.out.println("Status: " + response.getStatusLine().toString());
			
			entity = response.getEntity();
			if (entity != null) {
				//System.out.println("byte size of respone: " + entity.getContentLength());
				System.out.println("type: " + entity.getContentType());
				
				instream = entity.getContent();
				
				int bufferLen = DatabaseConstants.BUFFERSIZE;
				byte[] b = new byte[DatabaseConstants.BUFFERSIZE];
				String[] buffer = null;
				
				//System.out.println("Start - bufferLen: " + bufferLen);
				
				resultArray.add("");
				while (bufferLen != -1) {					
					bufferLen = instream.read(b);
					//System.out.println("bufferLen: " + bufferLen);
					
					if (bufferLen != -1) {
						String bStr = new String(b, 0, bufferLen);
						//System.out.println("bLen: " + bLen + ", bStr: \n" + bStr);
						buffer = bStr.split(";");
						//System.out.println("buffer.length: " + buffer.length);
						//System.out.println("size: " + resultArray.size());
						if(buffer[0].matches("^\\r\\n.*")) {
							resultArray.add(buffer[0].replaceAll("\\r\\n", ""));
						} else {	
							resultArray.set((resultArray.size() -1), resultArray.get(resultArray.size() -1).concat(buffer[0]).replaceAll("\\r\\n", ""));
						}
						for (int i = 1; i < buffer.length; i++) {
							resultArray.add(buffer[i].replaceAll("\\r\\n", ""));
						}
						//System.out.println("****");
					}
				}
				
				if (resultArray.get(resultArray.size() -1).equals("") || resultArray.get(resultArray.size() -1).length() == 0) {
					//System.out.println("resultArray.get(" + (resultArray.size() - 1) + "): " + resultArray.get(resultArray.size() - 1));
					resultArray.remove(resultArray.size() - 1);
				}
				
				System.out.println("resultArray.size: " + resultArray.size());
				//return resultArray;
				// TODO fix this!!!!!
				return this;
			}

			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (instream != null) {try {instream.close();} catch (IOException e) {e.printStackTrace();}}
			if (entity != null) {try {entity.consumeContent();} catch (IOException e) {e.printStackTrace();}}
//			if (response != null) {response = null;}
//			if (httpget != null) {httpget = null;}
//			if (httpclient != null) {httpclient = null;}	
		}
		
		return null;
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public ArrayList<String> getData() {
		// TODO Auto-generated method stub
		return this.resultArray;
	}
}
