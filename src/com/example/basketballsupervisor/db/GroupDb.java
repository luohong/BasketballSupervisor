package com.example.basketballsupervisor.db;

import android.content.Context;
import android.provider.BaseColumns;

public class GroupDb extends BaseDb {
	
	public GroupDb(Context context) {
		super(context);
	}

	@Override
	public String getTableName() {
		return Table.TABLE_NAME;
	}

	@Override
	protected String getCreateTableSQL() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(CREATE_TABLE_PREFIX).append(Table.TABLE_NAME).append(BRACKET_LEFT);
		sb.append(Table._ID).append(COLUMN_TYPE.LONG).append(PRIMARY_KEY_AUTOINCREMENT).append(COMMA);
		sb.append(Table.GAME_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.NAME).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.SLOGAN).append(COLUMN_TYPE.TEXT);
		sb.append(BRACKET_RIGHT);

		return sb.toString();
	}
	
	public static class Table implements BaseColumns {

		public static final String TABLE_NAME = "tb_group";

		public static final String GAME_ID = "g_id";
		
		public static final String NAME = "name"; 
		public static final String SLOGAN = "slogan"; 

		public static final String DEFAULT_SORT_ORDER = Table._ID + " DESC";

		public static final String[] PROJECTION = { _ID, GAME_ID, NAME, SLOGAN };
	}

	@Override
	protected String getDropTableSQL() {
		return DROP_TABLE_PREFIX + Table.TABLE_NAME;
	}

}
