package com.salmon.app.io.extdb;

import java.util.ArrayList;

public interface IDatabaseProvider {
	public ArrayList<String> getDataFromDatabase(int queryType, String[] strValue);
}
