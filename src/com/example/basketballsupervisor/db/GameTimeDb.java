package com.example.basketballsupervisor.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.basketballsupervisor.model.Game;
import com.example.basketballsupervisor.model.GameTime;
import com.example.basketballsupervisor.model.Group;

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
		sb.append(Table._ID).append(COLUMN_TYPE.INTEGER).append(PRIMARY_KEY_AUTOINCREMENT).append(COMMA);
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

	public void pauseOrEndGame(Game game, Group group, long time) {
		checkDb();
		beginTransaction();
		try {
			if (game != null && group != null && time > 0) {
				GameTime gameTime = new GameTime();
				gameTime.gameId = game.gId;
				gameTime.groupRequestId = group.groupId;
				gameTime.suspendTime = String.valueOf(time);
				
				update(gameTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			endTransaction();
		}
	}
	
	public void update(GameTime gameTime) {
		if (gameTime != null) {
			checkDb();
			
			String whereClause = String.format(" %s = ? and %s is null ", Table.GAME_ID, Table.SUSPEND_TIME);
	        String[] whereArgs = new String[] { String.valueOf(gameTime.gameId)};
			
			ContentValues values = new ContentValues();
			values.put(Table.GROUP_REQUEST_ID, gameTime.groupRequestId);
			values.put(Table.SUSPEND_TIME, gameTime.suspendTime);
			
			db.update(Table.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public void insert(GameTime gameTime) {
		if (gameTime != null) {
			checkDb();
			
			ContentValues values = new ContentValues();
			values.put(Table.GAME_ID, gameTime.gameId);
			values.put(Table.GROUP_REQUEST_ID, gameTime.groupRequestId);
			values.put(Table.SUSPEND_TIME, gameTime.suspendTime);
			values.put(Table.CONTINUE_TIME, gameTime.continueTime);
			
			db.insert(Table.TABLE_NAME, null, values);
		}
	}

	public void startOrContinueGame(Game game, Group group, long time) {
		checkDb();
		beginTransaction();
		try {
			if (game != null && time > 0) {
				GameTime gameTime = new GameTime();
				gameTime.gameId = game.gId;
				gameTime.groupRequestId = group != null ? group.groupId : 0;
				gameTime.continueTime = String.valueOf(time);
				
				insert(gameTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			endTransaction();
		}
	}

	public void clearGameDataById(long gId) {
		try {
			checkDb();
			String sql = "delete from " + getTableName() + " where " + Table.GAME_ID + " = " + gId;
			Log.e("SQL", sql);
			db.execSQL(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeDbAndCursor();
		}
	}

}
