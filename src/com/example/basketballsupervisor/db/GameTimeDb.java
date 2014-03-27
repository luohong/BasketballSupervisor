package com.example.basketballsupervisor.db;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * 比赛时间表（包括暂停时间等）
 * @author Administrator
 *
 */
public class GameTimeDb extends BaseDb {
	
	public static class Table implements BaseColumns {

		public static final String TABLE_NAME = "tb_game_time";

		public static final String GAME_ID = "g_id";
		public static final String GROUP_REQUEST_ID = "t_id"; 
		
		public static final String SUSPEND_TIME = "suspend_time"; 
		public static final String CONTINUE_TIME = "continue_time"; 

		public static final String DEFAULT_SORT_ORDER = Table._ID + " DESC";

		public static final String[] PROJECTION = { _ID, GAME_ID, GROUP_REQUEST_ID, SUSPEND_TIME, CONTINUE_TIME };
	}
	
	public GameTimeDb(Context context) {
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
		sb.append(Table.GROUP_REQUEST_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.SUSPEND_TIME).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.CONTINUE_TIME).append(COLUMN_TYPE.TEXT);
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
