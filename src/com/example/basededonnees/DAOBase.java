package com.example.basededonnees;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public abstract class DAOBase {

	protected SQLiteDatabase mDb = null;
	protected DataBaseHandler mHandler = null;

	public DAOBase(Context pContext) {
		this.mHandler = new DataBaseHandler(pContext);
	}

	public SQLiteDatabase open() {
		// Pas besoin de fermer la derniere base puisque getWritableDatabase
		// s'en charge
		mDb = mHandler.getWritableDatabase();
		return mDb;
	}

	public void close() {
		mDb.close();
	}

	public SQLiteDatabase getDb() {
		return mDb;
	}

}
