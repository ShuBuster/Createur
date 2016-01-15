package com.example.basededonnees;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHandler extends SQLiteOpenHelper {
	
	private static final String NAME = DbSchema.DB_NAME;
	private static final int VERSION = 1;

	public DataBaseHandler(Context context) {
		super(context, NAME, null, VERSION);
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DbSchema.EDT_TABLE_CREATE);
		db.execSQL(DbSchema.TASK_TABLE_CREATE);
		db.execSQL(DbSchema.HEUREM_TABLE_CREATE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		 db.execSQL(DbSchema.EDT_TABLE_DROP);
		 db.execSQL(DbSchema.TASK_TABLE_DROP);
		 db.execSQL(DbSchema.HEUREM_TABLE_DROP);
		  onCreate(db);
	}
	
	@Override
	public void onOpen(SQLiteDatabase db){
		
	}
}
