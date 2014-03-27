package com.example.basketballsupervisor.db;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * 球员上场时间表
 * @author Administrator
 *
 */
public class PlayingTimeDb extends BaseDb {
	
	public static class Table implements BaseColumns {

		public static final String TABLE_NAME = "tb_playing_time";

		public static final String GAME_ID = "g_id";
		public static final String GROUP_ID = "t_id"; 
		public static final String MEMBER_ID = "m_id"; 
		
		public static final String START_TIME = "start_time"; 
		public static final String END_TIME = "end_time"; 

		public static final String DEFAULT_SORT_ORDER = Table._ID + " DESC";

		public static final String[] PROJECTION = { _ID, GAME_ID, GROUP_ID, MEMBER_ID, START_TIME, END_TIME };
	}
	
	public PlayingTimeDb(Context context) {
		super(context);
	}

	@Override
	public String getTableName() {
		return Table.TABLE_NAME;
	}

	protected static String getCreateTableSQL() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(CREATE_TABLE_PREFIX).append(Table.TABLE_NAME).append(BRACKET_LEFT);
		sb.append(Table._ID).append(COLUMN_TYPE.LONG).append(PRIMARY_KEY_AUTOINCREMENT).append(COMMA);
		sb.append(Table.GAME_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.GROUP_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.MEMBER_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.START_TIME).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.END_TIME).append(COLUMN_TYPE.TEXT);
		sb.append(BRACKET_RIGHT);

		return sb.toString();
	}

	protected static String getDropTableSQL() {
		return DROP_TABLE_PREFIX + Table.TABLE_NAME;
	}

	@Override
	protected Object parseCursor(Cursor cursor) {
		return null;
	}

}
