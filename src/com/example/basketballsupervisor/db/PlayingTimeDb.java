package com.example.basketballsupervisor.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.basketballsupervisor.model.Game;
import com.example.basketballsupervisor.model.Group;
import com.example.basketballsupervisor.model.Member;
import com.example.basketballsupervisor.model.PlayingTime;

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
		sb.append(Table._ID).append(COLUMN_TYPE.INTEGER).append(PRIMARY_KEY_AUTOINCREMENT).append(COMMA);
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
		PlayingTime playingTime = new PlayingTime();
		
		playingTime.memberId = cursor.getLong(cursor.getColumnIndexOrThrow(Table._ID));
		playingTime.gameId = cursor.getLong(cursor.getColumnIndexOrThrow(Table.GAME_ID));
		playingTime.groupId = cursor.getLong(cursor.getColumnIndexOrThrow(Table.GROUP_ID));
		playingTime.memberId = cursor.getLong(cursor.getColumnIndexOrThrow(Table.MEMBER_ID));
		playingTime.startTime = cursor.getString(cursor.getColumnIndexOrThrow(Table.START_TIME));
		playingTime.endTime = cursor.getString(cursor.getColumnIndexOrThrow(Table.END_TIME));
		
		return playingTime;
	}
	
	public void startOrContinueGame(Game game, Group group, List<Member> mGroupPlayingMemberList, long time) {
		checkDb();
		beginTransaction();
		try {
			if (game != null && group != null && time > 0 && mGroupPlayingMemberList != null && mGroupPlayingMemberList.size() > 0) {
				for (Member member : mGroupPlayingMemberList) {
					
					PlayingTime playingTime = new PlayingTime();
					playingTime.gameId = game.gId;
					playingTime.groupId = group.groupId;
					playingTime.memberId = member.memberId;
					playingTime.startTime = String.valueOf(time);
					
					insert(playingTime);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			endTransaction();
		}
	}

	public void insert(PlayingTime playingTime) {
		if (playingTime != null) {
			checkDb();
			
			ContentValues values = new ContentValues();
			values.put(Table.GAME_ID, playingTime.gameId);
			values.put(Table.GROUP_ID, playingTime.groupId);
			values.put(Table.MEMBER_ID, playingTime.memberId);
			values.put(Table.START_TIME, playingTime.startTime);
			values.put(Table.END_TIME, playingTime.endTime);
			
			db.insert(Table.TABLE_NAME, null, values);
		}
	}
	
	public void pauseOrEndGame(Game game, Group group, List<Member> mGroupAPlayingMemberList, long time) {
		checkDb();
		beginTransaction();
		try {
			if (game != null && group != null && time > 0 && mGroupAPlayingMemberList != null && mGroupAPlayingMemberList.size() > 0) {
				for (Member member : mGroupAPlayingMemberList) {
					
					PlayingTime playingTime = new PlayingTime();
					playingTime.gameId = game.gId;
					playingTime.groupId = group.groupId;
					playingTime.memberId = member.memberId;
					playingTime.endTime = String.valueOf(time);
					
					update(playingTime);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			endTransaction();
		}
	}
	
	public void update(PlayingTime playingTime) {
		if (playingTime != null) {
			checkDb();
			
			String whereClause = String.format(" %s = ? and %s = ? and %s = ? and %s is null ", Table.GAME_ID, Table.GROUP_ID, Table.MEMBER_ID, Table.END_TIME);
	        String[] whereArgs = new String[] { String.valueOf(playingTime.gameId),
	        		String.valueOf(playingTime.groupId), String.valueOf(playingTime.memberId)};
			
			ContentValues values = new ContentValues();
			values.put(Table.END_TIME, playingTime.endTime);
			
			db.update(Table.TABLE_NAME, values, whereClause, whereArgs);
		}
	}

	public List<PlayingTime> getGroupMemberPlayingTime(Game game, Group group) {
		List<PlayingTime> playingTimeList = new ArrayList<PlayingTime>();
		
        String selection = String.format(" %s = ? and %s = ? ", Table.GAME_ID, Table.GROUP_ID);
        String[] selectionArgs = new String[] { String.valueOf(game.gId), String.valueOf(group.groupId) };
        
        Cursor cursor = null;
        try {
        	checkDb();
            cursor = db.query(Table.TABLE_NAME, Table.PROJECTION, selection, selectionArgs, null, null, null );
            while (cursor != null && cursor.moveToNext()) {
            	PlayingTime playingTime = (PlayingTime)parseCursor(cursor);
            	playingTimeList.add(playingTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return playingTimeList;
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
