package com.example.basketballsupervisor.db;

import android.content.Context;
import android.provider.BaseColumns;

public class GameDb extends BaseDb {
	
	public GameDb(Context context) {
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
		sb.append(Table.PLATFORM_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.NAME).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.REMARK).append(COLUMN_TYPE.TEXT).append(COMMA);
//		sb.append(Table.GROUP_LIST).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.ROLE).append(COLUMN_TYPE.INTEGER);
		sb.append(Table.TIME).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.LOCATION).append(COLUMN_TYPE.TEXT);
		sb.append(BRACKET_RIGHT);

		return sb.toString();
	}
	
	public static class Table implements BaseColumns {

		public static final String TABLE_NAME = "tb_game";

		public static final String PLATFORM_ID = "p_id"; 
		
		public static final String NAME = "name"; 
		public static final String REMARK = "remark"; 
//		public static final String GROUP_LIST = "group_list"; 
		public static final String ROLE = "role"; 
		public static final String TIME = "time"; 
		public static final String LOCATION = "location"; 

		public static final String DEFAULT_SORT_ORDER = Table._ID + " DESC";

		public static final String[] PROJECTION = { _ID, PLATFORM_ID, NAME, REMARK, /**GROUP_LIST,**/ ROLE, TIME, LOCATION };
	}

	@Override
	protected String getDropTableSQL() {
		return DROP_TABLE_PREFIX + Table.TABLE_NAME;
	}

}
