package com.example.basketballsupervisor.db;

import java.util.ArrayList;
import java.util.List;

import com.example.basketballsupervisor.config.Config;
import com.example.basketballsupervisor.db.GameDb.Table;
import com.example.basketballsupervisor.model.Action;
import com.example.basketballsupervisor.model.Game;
import com.google.gson.reflect.TypeToken;

import android.content.ContentValues;
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

		public static final String[] PROJECTION = { _ID, NEXT_ACTION_ID, NAME, SCORE, CANCELABLE };
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
		
		action.id = cursor.getInt(cursor.getColumnIndexOrThrow(Table._ID));
		action.name = cursor.getString(cursor.getColumnIndexOrThrow(Table.NAME));
		action.nextActionId = cursor.getInt(cursor.getColumnIndexOrThrow(Table.NEXT_ACTION_ID));
		action.score = cursor.getInt(cursor.getColumnIndexOrThrow(Table.SCORE));
		action.cancelable = cursor.getInt(cursor.getColumnIndexOrThrow(Table.CANCELABLE));
		
		return action;
	}

	public List<Action> getAll() {
		List<Action> actionList = new ArrayList<Action>();
        Cursor cursor = null;
        try {
        	checkDb();
            cursor = db.query(Table.TABLE_NAME, Table.PROJECTION, null, null, null, null, null);
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

	public void insertSampleData() {
		
		List<Action> sampleActions = new ArrayList<Action>();
		
		sampleActions.add(new Action(1,8,"2分命中",2,1));
		sampleActions.add(new Action(2,10,"2分不中",0,1));
		sampleActions.add(new Action(3,8,"3分命中",3,1));
		sampleActions.add(new Action(4,10,"3分不中",0,1));
		sampleActions.add(new Action(5,0,"罚球命中",1,0));
		sampleActions.add(new Action(6,10,"罚球不中",0,1));
		sampleActions.add(new Action(7,0,"抢断",0,0));
		sampleActions.add(new Action(8,0,"助攻",0,0));
		sampleActions.add(new Action(9,0,"前场篮板",0,0));
		sampleActions.add(new Action(10,0,"后场篮板",0,0));
		sampleActions.add(new Action(11,0,"失误",0,0));
		sampleActions.add(new Action(12,0,"封盖",0,0));
		sampleActions.add(new Action(13,0,"犯规",0,0));
		sampleActions.add(new Action(14,0,"被犯规",0,0));
		sampleActions.add(new Action(15,-1,"创新",0,1));
		sampleActions.add(new Action(16,-2,"超远3分",0,0));
		sampleActions.add(new Action(17,-2,"绝杀",0,0));
		sampleActions.add(new Action(18,-2,"最后3秒得分",0,0));
		sampleActions.add(new Action(19,-2,"晃倒",0,0));
		sampleActions.add(new Action(20,-2,"2+1",0,0));
		sampleActions.add(new Action(21,-2,"3+1",0,0));
		sampleActions.add(new Action(22,-2,"扣篮",0,0));
		sampleActions.add(new Action(23,-2,"快攻",0,0));
		sampleActions.add(new Action(24,-2,"2罚不中",0,0));
		sampleActions.add(new Action(25,-2,"3罚不中",0,0));
		sampleActions.add(new Action(26,-2,"被晃倒",0,0));
		
		saveAll(sampleActions);
	}
	
	public void saveAll(List<Action> actionList) {
		checkDb();
		beginTransaction();
		try {
			if (actionList != null && actionList.size() > 0) {
				clearAllData();
				for (Action action : actionList) {
					insert(action);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			endTransaction();
		}
	}

	public void insert(Action action) {
		if (action != null) {
			checkDb();
			
			ContentValues values = new ContentValues();
			values.put(Table._ID, action.id);
			values.put(Table.NAME, action.name);
			values.put(Table.NEXT_ACTION_ID, action.nextActionId);
			values.put(Table.SCORE, action.score);
			values.put(Table.CANCELABLE, action.cancelable);
			
			db.insert(Table.TABLE_NAME, null, values);
		}
	}

}
