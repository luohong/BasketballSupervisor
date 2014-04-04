package com.example.basketballsupervisor.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.example.basketballsupervisor.model.Game;
import com.example.basketballsupervisor.model.Group;
import com.example.basketballsupervisor.model.Member;

/**
 * 球员表
 * @author Administrator
 *
 */
public class MemberDb extends BaseDb {
	
	public static class Table implements BaseColumns {

		public static final String TABLE_NAME = "tb_member";

		public static final String GAME_ID = "g_id";
		public static final String GROUP_ID = "t_id"; 
		
		public static final String NAME = "name"; 
		public static final String NUMBER = "number"; 
		public static final String SITE = "site"; 
		public static final String IS_LEADER = "is_leader"; 

		public static final String DEFAULT_SORT_ORDER = Table._ID + " DESC";

		public static final String[] PROJECTION = { _ID, GAME_ID, GROUP_ID, NAME, NUMBER, SITE, IS_LEADER };
	}
	
	public MemberDb(Context context) {
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
		sb.append(Table.GAME_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.GROUP_ID).append(COLUMN_TYPE.LONG).append(COMMA);
		sb.append(Table.NAME).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.NUMBER).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.SITE).append(COLUMN_TYPE.TEXT).append(COMMA);
		sb.append(Table.IS_LEADER).append(COLUMN_TYPE.INTEGER);
		sb.append(BRACKET_RIGHT);

		return sb.toString();
	}

	protected static String getDropTableSQL() {
		return DROP_TABLE_PREFIX + Table.TABLE_NAME;
	}

	@Override
	protected Object parseCursor(Cursor cursor) {
		Member member = new Member();
		
		member.memberId = cursor.getLong(cursor.getColumnIndexOrThrow(Table._ID));
		member.name = cursor.getString(cursor.getColumnIndexOrThrow(Table.NAME));
		member.number = cursor.getString(cursor.getColumnIndexOrThrow(Table.NUMBER));
		member.site = cursor.getString(cursor.getColumnIndexOrThrow(Table.SITE));
		member.isLeader = cursor.getInt(cursor.getColumnIndexOrThrow(Table.IS_LEADER));
		
		return member;
	}

	public List<Member> getGroupMembers(long gruopId) {
		List<Member> memberList = new ArrayList<Member>();
		
        String selection = String.format(" %s = ? ", Table.GROUP_ID);
        String[] selectionArgs = new String[] { String.valueOf(gruopId) };
        
        Cursor cursor = null;
        try {
        	checkDb();
            cursor = db.query(Table.TABLE_NAME, Table.PROJECTION, selection, selectionArgs, null, null, Table.DEFAULT_SORT_ORDER);
            while (cursor != null && cursor.moveToNext()) {
            	Member member = (Member)parseCursor(cursor);
            	memberList.add(member);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return memberList;
	}
	
	public void saveAll(List<Member> memberList, Group group, Game game) {
		checkDb();
		beginTransaction();
		try {
			if (memberList != null && memberList.size() > 0) {
				clearAllData();
				for (Member member : memberList) {
					insert(member, group, game);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			endTransaction();
		}
	}

	public void insert(Member member, Group group, Game game) {
		if (member != null && group != null && game != null) {
			checkDb();
			
			ContentValues values = new ContentValues();
			values.put(Table._ID, member.memberId);
			values.put(Table.GROUP_ID, group.groupId);
			values.put(Table.GAME_ID, game.gId);
			values.put(Table.NAME, member.name);
			values.put(Table.NUMBER, member.number);
			values.put(Table.SITE, member.site);
			values.put(Table.IS_LEADER, member.isLeader);
			
			db.insert(Table.TABLE_NAME, null, values);
		}
	}

}
