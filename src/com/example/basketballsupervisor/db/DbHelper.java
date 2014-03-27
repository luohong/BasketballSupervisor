package com.example.basketballsupervisor.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "basketballsupervisor";

	private static final int DATABASE_VERSION = 1;

	private static DbHelper mDbHelper;
	
	private GameDb gameDb;
	private GroupDb groupDb;
	private MemberDb memberDb;
	private GameTimeDb gameTimeDb;
	private PlayingTimeDb playingTimeDb;
	private ActionDb actionDb;
	private RecordDb recordDb;

	public static DbHelper getInstance(Context context) {
		if (mDbHelper == null) {
			mDbHelper = new DbHelper(context);
		}
		return mDbHelper;
	}

	private DbHelper(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
		
		gameDb = new GameDb(context);
		groupDb = new GroupDb(context);
		memberDb = new MemberDb(context);
		gameTimeDb = new GameTimeDb(context);
		playingTimeDb = new PlayingTimeDb(context);
		actionDb = new ActionDb(context);
		recordDb = new RecordDb(context);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(getTag(), "onCreate");
		createTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(getTag(), "onUpgrade");
		db.execSQL(gameDb.getDropTableSQL());
		db.execSQL(groupDb.getDropTableSQL());
		db.execSQL(memberDb.getDropTableSQL());
		db.execSQL(gameTimeDb.getDropTableSQL());
		db.execSQL(playingTimeDb.getDropTableSQL());
		db.execSQL(actionDb.getDropTableSQL());
		db.execSQL(recordDb.getDropTableSQL());
		
		createTable(db);
	}

	private void createTable(SQLiteDatabase db) {
		db.execSQL(gameDb.getCreateTableSQL());
		db.execSQL(groupDb.getCreateTableSQL());
		db.execSQL(memberDb.getCreateTableSQL());
		db.execSQL(gameTimeDb.getCreateTableSQL());
		db.execSQL(playingTimeDb.getCreateTableSQL());
		db.execSQL(actionDb.getCreateTableSQL());
		db.execSQL(recordDb.getCreateTableSQL());
	}

	private String getTag() {
		return this.getClass().toString();
	}

	public static DbHelper getmDbHelper() {
		return mDbHelper;
	}

	public GameDb getGameDb() {
		return gameDb;
	}

	public GroupDb getGroupDb() {
		return groupDb;
	}

	public MemberDb getMemberDb() {
		return memberDb;
	}

	public GameTimeDb getGameTimeDb() {
		return gameTimeDb;
	}

	public PlayingTimeDb getPlayingTimeDb() {
		return playingTimeDb;
	}

	public ActionDb getActionDb() {
		return actionDb;
	}

	public RecordDb getRecordDb() {
		return recordDb;
	}
	
}