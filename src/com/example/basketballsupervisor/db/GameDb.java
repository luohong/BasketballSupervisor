package com.example.basketballsupervisor.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.example.basketballsupervisor.config.Config;
import com.example.basketballsupervisor.model.Game;
import com.google.gson.reflect.TypeToken;

/**
 * 比赛表
 * @author Administrator
 *
 */
public class GameDb extends BaseDb {
	
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
	
	public GameDb(Context context) {
		super(context);
	}

	@Override
	public String getTableName() {
		return Table.TABLE_NAME;
	}

	protected static String getCreateTableSQL() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(CREATE_TABLE_PREFIX).append(Table.TABLE_NAME).append(BRACKET_LEFT);
		sb.append(Table._ID).append(COLUMN_TYPE.LONG).append(PRIMARY_KEY).append(COMMA);
		sb.append(Table.PLATFORM_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.NAME).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.REMARK).append(COLUMN_TYPE.TEXT).append(COMMA);
//		sb.append(Table.GROUP_LIST).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.ROLE).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.TIME).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.LOCATION).append(COLUMN_TYPE.TEXT);
		sb.append(BRACKET_RIGHT);

		return sb.toString();
	}

	protected static String getDropTableSQL() {
		return DROP_TABLE_PREFIX + Table.TABLE_NAME;
	}
	
	@Override
	protected Object parseCursor(Cursor cursor) {
		Game game = new Game();
		
		game.gId = cursor.getLong(cursor.getColumnIndexOrThrow(Table._ID));
		game.pId = cursor.getLong(cursor.getColumnIndexOrThrow(Table.PLATFORM_ID));
		game.gameName = cursor.getString(cursor.getColumnIndexOrThrow(Table.NAME));
		game.gameRemark = cursor.getString(cursor.getColumnIndexOrThrow(Table.REMARK));
		String role = cursor.getString(cursor.getColumnIndexOrThrow(Table.ROLE));
		List<Integer> roles = Config.mGson.fromJson(role, new TypeToken<List<Integer>>() {}.getType());
		game.role = roles;
		game.time = cursor.getString(cursor.getColumnIndexOrThrow(Table.TIME));
		game.location = cursor.getString(cursor.getColumnIndexOrThrow(Table.LOCATION));
		
		return game;
	}

	public List<Game> getAll() {
		List<Game> gameList = new ArrayList<Game>();
        Cursor cursor = null;
        try {
        	checkDb();
            cursor = db.query(Table.TABLE_NAME, Table.PROJECTION, null, null, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
            	Game game = (Game)parseCursor(cursor);
            	gameList.add(game);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return gameList;
	}

	public void saveAll(List<Game> gameList) {
		checkDb();
		beginTransaction();
		try {
			if (gameList != null && gameList.size() > 0) {
				clearAllData();
				for (Game game : gameList) {
					insert(game);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			endTransaction();
		}
	}

	public void insert(Game game) {
		if (game != null) {
			checkDb();
			
			ContentValues values = new ContentValues();
			values.put(Table._ID, game.gId);
			values.put(Table.PLATFORM_ID, game.pId);
			values.put(Table.NAME, game.gameName);
			values.put(Table.REMARK, game.gameRemark);
			String json = Config.mGson.toJson(game.role);
			values.put(Table.ROLE, json);
			values.put(Table.TIME, game.time);
			values.put(Table.LOCATION, game.location);
			
			db.insert(Table.TABLE_NAME, null, values);
		}
	}

}
