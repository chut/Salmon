package com.salmon.app.io;

import java.util.ArrayList;

public interface IDatabaseProvider {
	public ArrayList<String> getDataFromDatabase(int queryType, String[] strValue);
}
