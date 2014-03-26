package com.example.basketballsupervisor.db;

import android.content.Context;
import android.provider.BaseColumns;

public class ActionDb extends BaseDb {
	
	public ActionDb(Context context) {
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
		sb.append(Table._ID).append(COLUMN_TYPE.INTEGER).append(PRIMARY_KEY_AUTOINCREMENT).append(COMMA);
		sb.append(Table.NEXT_ACTION_ID).append(COLUMN_TYPE.INTEGER).append(COMMA);
		sb.append(Table.NAME).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.SCORE).append(COLUMN_TYPE.INTEGER).append(COMMA);
		sb.append(Table.CANCELABLE).append(COLUMN_TYPE.INTEGER);
		sb.append(BRACKET_RIGHT);

		return sb.toString();
	}
	
	public static class Table implements BaseColumns {

		public static final String TABLE_NAME = "tb_action";

		public static final String NEXT_ACTION_ID = "next_action_id";
		
		public static final String NAME = "name"; 
		public static final String SCORE = "score"; 
		public static final String CANCELABLE = "cancelable"; 

		public static final String DEFAULT_SORT_ORDER = Table._ID + " DESC";

		public static final String[] PROJECTION = { _ID, NEXT_ACTION_ID, NAME, SCORE };
	}

	@Override
	protected String getDropTableSQL() {
		return DROP_TABLE_PREFIX + Table.TABLE_NAME;
	}

}
