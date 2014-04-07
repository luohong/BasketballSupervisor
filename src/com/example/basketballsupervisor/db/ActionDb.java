package com.example.basketballsupervisor.db;

import java.util.ArrayList;
import java.util.List;

import com.example.basketballsupervisor.config.Config;
import com.example.basketballsupervisor.db.GameDb.Table;
import com.example.basketballsupervisor.model.Action;
import com.example.basketballsupervisor.model.Game;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * 球员发生动作行为表（包括投篮二分不中等）
 * @author Administrator
 *
 */
public class ActionDb extends BaseDb {
	
	public static class Table implements BaseColumns {

		public static final String TABLE_NAME = "tb_action";

		public static final String NEXT_ACTION_ID = "next_action_id";
		
		public static final String NAME = "name"; 
		public static final String SCORE = "score"; 
		public static final String CANCELABLE = "cancelable"; 

		public static final String DEFAULT_SORT_ORDER = Table._ID + " DESC";

		public static final String[] PROJECTION = { _ID, NEXT_ACTION_ID, NAME, SCORE };
	}
	
	public ActionDb(Context context) {
		super(context);
	}

	@Override
	public String getTableName() {
		return Table.TABLE_NAME;
	}

	protected static String getCreateTableSQL() {
		
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

	protected static String getDropTableSQL() {
		return DROP_TABLE_PREFIX + Table.TABLE_NAME;
	}

	@Override
	protected Object parseCursor(Cursor cursor) {
		Action action = new Action();
		
		action.id = cursor.getLong(cursor.getColumnIndexOrThrow(Table._ID));
		action.name = cursor.getString(cursor.getColumnIndexOrThrow(Table.NAME));
		action.nextActionId = cursor.getLong(cursor.getColumnIndexOrThrow(Table.NEXT_ACTION_ID));
		action.score = cursor.getInt(cursor.getColumnIndexOrThrow(Table.SCORE));
		action.cancelable = cursor.getInt(cursor.getColumnIndexOrThrow(Table.CANCELABLE));
		
		return action;
	}

	public List<Action> getAll() {
		List<Action> actionList = new ArrayList<Action>();
        Cursor cursor = null;
        try {
        	checkDb();
            cursor = db.query(Table.TABLE_NAME, Table.PROJECTION, null, null, null, null, Table.DEFAULT_SORT_ORDER);
            while (cursor != null && cursor.moveToNext()) {
            	Action action = (Action)parseCursor(cursor);
            	actionList.add(action);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return actionList;
	}

}
