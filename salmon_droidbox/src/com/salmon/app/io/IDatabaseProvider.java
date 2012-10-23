package com.salmon.app.io;

import android.database.Cursor;

public interface IDatabaseProvider {
	public Cursor getDataFromDatabase(int queryType, String[] strValue);
	public Cursor getCursor();
	public void close();
}
