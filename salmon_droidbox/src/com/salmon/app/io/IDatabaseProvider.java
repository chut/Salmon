package com.salmon.app.io;

import java.util.ArrayList;

public interface IDatabaseProvider {
	public IDatabaseProvider submitQuery(int queryType, String[] strValue);
	public ArrayList<String> getData();
	public void close();
}
