package com.example.basketballsupervisor.util;

import java.util.ArrayList;
import java.util.List;

import com.example.basketballsupervisor.config.Config;
import com.example.basketballsupervisor.model.Member;
import com.example.basketballsupervisor.model.User;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;

/**
 * 
 * sharePreference 存储工具类 <功能详细描述>
 * 
 * @author Administrator
 * @version [版本号, 2013-12-16]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class SpUtil {
	private static final String NAME = "preferences";

	private static final String TAG = SpUtil.class.getSimpleName();

	public static SpUtil instance = null;

	private Context context;

	private SpUtil(Context context) {
		this.context = context;
	}

	public static SpUtil getInstance(Context context) {
		Context applicationContext = context.getApplicationContext();
		if (null == instance || instance.context != applicationContext) {
			instance = new SpUtil(context);
		}
		return instance;
	}

	private SharedPreferences sp;

	public SharedPreferences getSp() {
		if (sp == null) {
			sp = context.getSharedPreferences(getSpFileName(),
					Context.MODE_PRIVATE);
		}
		return sp;
	}

	public Editor getEdit() {
		return getSp().edit();
	}

	private String getSpFileName() {
		return NAME;
	}

	public void logout(){
		getEdit().clear().commit();
	}
	
	public void setUser(User user) {
		String json = Config.mGson.toJson(user);
		getEdit().putString("user", json).commit();
	}
	
	public User getUser() {
		String json = getSp().getString("user", "");
		User user = Config.mGson.fromJson(json, User.class);
		if (user == null) {
			user = new User();
		}
		return user;
	}

	public void setSelectedGameId(long gId, boolean hasSelectedGame) {
		getEdit().putLong("selected_game_id", gId).commit();
		
		if (hasSelectedGame) {
			resetGameState();
		}
	}
	
	public void resetGameState() {
		// 清除之前的比赛状态
		setGameStateRunning(false);
		setGameStatePausing(false);
		setGroupAPlayingMemberList(null);
		setGroupBPlayingMemberList(null);
		setLastGameTime(0);
	}

	public long getSelectedGameId() {
		return getSp().getLong("selected_game_id", -1);
	}

	public void setGameStateRunning(boolean running) {
		getEdit().putBoolean("game_state_running", running).commit();
	}

	public void setGameStatePausing(boolean pausing) {
		getEdit().putBoolean("game_state_pausing", pausing).commit();
	}

	public boolean getGameStateRunning() {
		return getSp().getBoolean("game_state_running", false);
	}

	public boolean getGameStatePausing() {
		return getSp().getBoolean("game_state_pausing", false);
	}

	public void setGroupAPlayingMemberList(List<Member> mGroupAPlayingMemberList) {
		String memberIds = getGroupMemberIds(mGroupAPlayingMemberList);
		Log.d(TAG, "onSavePlayingPlayers()... GroupA memberIds:" + memberIds);
		getEdit().putString("GroupAPlayingMemberList", memberIds).commit();
	}
	
	public void setGroupBPlayingMemberList(List<Member> mGroupBPlayingMemberList) {
		String memberIds = getGroupMemberIds(mGroupBPlayingMemberList);
		Log.d(TAG, "onSavePlayingPlayers()... GroupA memberIds:" + memberIds);
		getEdit().putString("GroupAPlayingMemberList", memberIds).commit();
	}
	
	private String getGroupMemberIds(List<Member> mGroupPlayingMemberList) {
		StringBuffer memberIds = new StringBuffer();
		if (mGroupPlayingMemberList != null && mGroupPlayingMemberList.size() > 0) {
			for (Member member : mGroupPlayingMemberList) {
				memberIds.append(member.memberId).append(",");
			}
			if (memberIds.length() > 0) {
				memberIds.deleteCharAt(memberIds.length() - 1);
			}
		}
		return memberIds.toString();
	}

	public String getGroupAPlayingMemberList() {
		return getSp().getString("GroupAPlayingMemberList", "");
	}

	public String getGroupBPlayingMemberList() {
		return getSp().getString("GroupBPlayingMemberList", "");
	}

	public int getLastGameTime() {
		return getSp().getInt("last_game_time", 0);
	}

	public void setLastGameTime(int i) {
		getEdit().putInt("last_game_time", i).commit();
	}

	public List<Integer> getQuarterTimeList() {
		List<Integer> mQuarterTimeList = new ArrayList<Integer>();
		
		String times = getSp().getString("quarterTimeList", "");
		if (!TextUtils.isEmpty(times)) {
			String[] time = times.split(",");
			for (int i = 0; i < time.length; i++) {
				int t = Integer.parseInt(time[i]);
				mQuarterTimeList.add(t);
			}
		}
		
		return mQuarterTimeList;
	}

	public void setQuarterTimeList(List<Integer> mQuarterTimeList) {
		StringBuffer times = new StringBuffer();		
		for (Integer time : mQuarterTimeList) {
			times.append(time).append(",");
		}
		
		if (times.length() > 0) {
			times.deleteCharAt(times.length() - 1);
		}
		getEdit().putString("quarterTimeList", times.toString()).commit();
	}
	
}
