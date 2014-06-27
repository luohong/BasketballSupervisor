package com.example.basketballsupervisor.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "basketballsupervisor";

	private static final int DATABASE_VERSION = 3;

	private static DbHelper mDbHelper;
	
	public static DbHelper getInstance(Context context) {
		if (mDbHelper == null) {
			mDbHelper = new DbHelper(context);
		}
		return mDbHelper;
	}

	private DbHelper(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(getTag(), "onCreate");
		createTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(getTag(), "onUpgrade");
		db.execSQL(GameDb.getDropTableSQL());
		db.execSQL(GroupDb.getDropTableSQL());
		db.execSQL(MemberDb.getDropTableSQL());
		db.execSQL(GameTimeDb.getDropTableSQL());
		db.execSQL(PlayingTimeDb.getDropTableSQL());
		db.execSQL(ActionDb.getDropTableSQL());
		db.execSQL(RecordDb.getDropTableSQL());
		
		createTable(db);
	}

	private void createTable(SQLiteDatabase db) {
		db.execSQL(GameDb.getCreateTableSQL());
		db.execSQL(GroupDb.getCreateTableSQL());
		db.execSQL(MemberDb.getCreateTableSQL());
		db.execSQL(GameTimeDb.getCreateTableSQL());
		db.execSQL(PlayingTimeDb.getCreateTableSQL());
		db.execSQL(ActionDb.getCreateTableSQL());
		db.execSQL(RecordDb.getCreateTableSQL());
	}

	private String getTag() {
		return this.getClass().toString();
	}

}