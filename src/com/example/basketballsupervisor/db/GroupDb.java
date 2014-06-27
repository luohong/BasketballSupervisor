package com.example.basketballsupervisor.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.example.basketballsupervisor.model.Game;
import com.example.basketballsupervisor.model.Group;

/**
 * 球队表
 * @author Administrator
 *
 */
public class GroupDb extends BaseDb {
	
	public static class Table implements BaseColumns {

		public static final String TABLE_NAME = "tb_group";

		public static final String GROUP_ID = "group_id";
		public static final String GAME_ID = "g_id";
		
		public static final String NAME = "name"; 
		public static final String SLOGAN = "slogan"; 

		public static final String DEFAULT_SORT_ORDER = Table._ID + " DESC";

		public static final String[] PROJECTION = { _ID, GROUP_ID, GAME_ID, NAME, SLOGAN };
	}
	
	public GroupDb(Context context) {
		super(context);
	}

	@Override
	public String getTableName() {
		return Table.TABLE_NAME;
	}

	protected static String getCreateTableSQL() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(CREATE_TABLE_PREFIX).append(Table.TABLE_NAME).append(BRACKET_LEFT);
		sb.append(Table._ID).append(COLUMN_TYPE.INTEGER).append(PRIMARY_KEY).append(COMMA);
		sb.append(Table.GROUP_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.GAME_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.NAME).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.SLOGAN).append(COLUMN_TYPE.TEXT);
		sb.append(BRACKET_RIGHT);

		return sb.toString();
	}

	protected static String getDropTableSQL() {
		return DROP_TABLE_PREFIX + Table.TABLE_NAME;
	}

	@Override
	protected Object parseCursor(Cursor cursor) {
		Group group = new Group();
		
		group.groupId = cursor.getLong(cursor.getColumnIndexOrThrow(Table.GROUP_ID));
		group.Slogan = cursor.getString(cursor.getColumnIndexOrThrow(Table.SLOGAN));
		group.groupName = cursor.getString(cursor.getColumnIndexOrThrow(Table.NAME));
		
		return group;
	}

	public List<Group> getGameGroups(long gId) {
		List<Group> groupList = new ArrayList<Group>();
		
        String selection = String.format(" %s = ? ", Table.GAME_ID);
        String[] selectionArgs = new String[] { String.valueOf(gId) };

        Cursor cursor = null;
        try {
        	checkDb();
            cursor = db.query(Table.TABLE_NAME, Table.PROJECTION, selection, selectionArgs, null, null, Table.GROUP_ID + " asc" );
            while (cursor != null && cursor.moveToNext()) {
                Group group = (Group) parseCursor(cursor);
                groupList.add(group);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return groupList;
	}
	
	public void saveAll(List<Group> groupList, Game game) {
		checkDb();
		beginTransaction();
		try {
			if (groupList != null && groupList.size() > 0) {
				for (Group group : groupList) {
					insert(group, game);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			endTransaction();
		}
	}

	public void insert(Group group, Game game) {
		if (group != null && game != null) {
			checkDb();
			
			ContentValues values = new ContentValues();
			values.put(Table.GROUP_ID, group.groupId);
			values.put(Table.GAME_ID, game.gId);
			values.put(Table.NAME, group.groupName);
			values.put(Table.SLOGAN, group.Slogan);
			
			db.insert(Table.TABLE_NAME, null, values);
		}
	}

}
