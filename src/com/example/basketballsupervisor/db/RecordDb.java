package com.example.basketballsupervisor.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.basketballsupervisor.config.Config;
import com.example.basketballsupervisor.db.GameDb.Table;
import com.example.basketballsupervisor.model.Action;
import com.example.basketballsupervisor.model.Game;
import com.example.basketballsupervisor.model.Group;
import com.example.basketballsupervisor.model.Member;
import com.example.basketballsupervisor.model.Record;
import com.google.gson.reflect.TypeToken;

/**
 * 得分记录表
 * @author Administrator
 *
 */
public class RecordDb extends BaseDb {
	
	public static final String TAG = RecordDb.class.getSimpleName();
	
	public static class Table implements BaseColumns {

		public static final String TABLE_NAME = "tb_record";

		public static final String GAME_ID = "g_id";
		public static final String GROUP_ID = "t_id"; 
		public static final String MEMBER_ID = "m_id";
		
		public static final String ACTION_ID = "a_id"; 
		public static final String SHOW_TIME = "show_time"; 
		public static final String CREATE_TIME = "create_time"; 
		public static final String COORDINATE = "coordinate"; 

		public static final String DEFAULT_SORT_ORDER = Table._ID + " DESC";

		public static final String[] PROJECTION = { _ID, GAME_ID, GROUP_ID, MEMBER_ID, ACTION_ID, SHOW_TIME, CREATE_TIME, COORDINATE };
	}
	
	public RecordDb(Context context) {
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
		sb.append(Table.GAME_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.GROUP_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.MEMBER_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.ACTION_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.SHOW_TIME).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.CREATE_TIME).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.COORDINATE).append(COLUMN_TYPE.TEXT);
		sb.append(BRACKET_RIGHT);

		return sb.toString();
	}

	protected static String getDropTableSQL() {
		return DROP_TABLE_PREFIX + Table.TABLE_NAME;
	}
	
	@Override
	protected Object parseCursor(Cursor cursor) {
		Record record = new Record();
		
		record.id = cursor.getLong(cursor.getColumnIndexOrThrow(Table._ID));
		record.gameId = cursor.getLong(cursor.getColumnIndexOrThrow(Table.GAME_ID));
		record.groupId = cursor.getLong(cursor.getColumnIndexOrThrow(Table.GROUP_ID));
		record.memberId = cursor.getLong(cursor.getColumnIndexOrThrow(Table.MEMBER_ID));
		record.actionId = cursor.getLong(cursor.getColumnIndexOrThrow(Table.ACTION_ID));
		record.showTime = cursor.getString(cursor.getColumnIndexOrThrow(Table.SHOW_TIME));
		record.createTime = cursor.getString(cursor.getColumnIndexOrThrow(Table.CREATE_TIME));
		record.coordinate = cursor.getString(cursor.getColumnIndexOrThrow(Table.COORDINATE));
		
		return record;
	}

	public List<Record> getAll(Game game, Group group) {
		List<Record> recordList = new ArrayList<Record>();
        Cursor cursor = null;
        try {
        	checkDb();
    		
            String selection = String.format(" %s = ? and %s = ? ", Table.GAME_ID, Table.GROUP_ID);
            String[] selectionArgs = new String[] { String.valueOf(game.gId), String.valueOf(group.groupId) };
        	
            cursor = db.query(Table.TABLE_NAME, Table.PROJECTION, selection, selectionArgs, null, null, Table.DEFAULT_SORT_ORDER);
            while (cursor != null && cursor.moveToNext()) {
            	Record record = (Record)parseCursor(cursor);
            	recordList.add(record);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return recordList;
	}

	public void saveRecord(Game game, Group group, Member member, Action action, long time, String coordinate) {
		if (game != null && group != null && time > 0 && member != null && action != null) {
			Record record = new Record();
			record.gameId = game.gId;
			record.groupId = group.groupId;
			record.memberId = member.memberId;
			record.actionId = action.id;
			record.showTime = String.valueOf(time);
			record.createTime = String.valueOf(System.currentTimeMillis());
			record.coordinate = coordinate;

			insert(record);
		}
	}

	public void insert(Record record) {
		if (record != null) {
			checkDb();
			
			ContentValues values = new ContentValues();
			values.put(Table.GAME_ID, record.gameId);
			values.put(Table.GROUP_ID, record.groupId);
			values.put(Table.MEMBER_ID, record.memberId);
			values.put(Table.ACTION_ID, record.actionId);
			values.put(Table.CREATE_TIME, record.createTime);
			values.put(Table.SHOW_TIME, record.showTime);
			values.put(Table.COORDINATE, record.coordinate);
			
			long result = db.insert(Table.TABLE_NAME, null, values);
			Log.d(TAG, "insert result : " + result + ", actionId: " + record.actionId);
		}
	}

}
