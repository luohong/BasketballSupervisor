package com.example.basketballsupervisor.db;

import android.content.Context;
import android.provider.BaseColumns;

/**
 * 得分记录表
 * @author Administrator
 *
 */
public class RecordDb extends BaseDb {
	
	public RecordDb(Context context) {
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
		sb.append(Table.GROUP_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.MEMBER_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.ACTION_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.SHOW_TIME).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.CRATE_TIME).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.REMARK).append(COLUMN_TYPE.TEXT);
		sb.append(BRACKET_RIGHT);

		return sb.toString();
	}
	
	public static class Table implements BaseColumns {

		public static final String TABLE_NAME = "tb_record";

		public static final String GAME_ID = "g_id";
		public static final String GROUP_ID = "t_id"; 
		public static final String MEMBER_ID = "m_id";
		
		public static final String ACTION_ID = "a_id"; 
		public static final String SHOW_TIME = "show_time"; 
		public static final String CRATE_TIME = "create_time"; 
		public static final String REMARK = "remark"; 

		public static final String DEFAULT_SORT_ORDER = Table._ID + " DESC";

		public static final String[] PROJECTION = { _ID, GAME_ID, GROUP_ID, MEMBER_ID, ACTION_ID, SHOW_TIME, CRATE_TIME, REMARK };
	}

	@Override
	protected String getDropTableSQL() {
		return DROP_TABLE_PREFIX + Table.TABLE_NAME;
	}

}
