package com.example.basketballsupervisor.activity;

import java.util.List;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;

import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.config.Config;
import com.example.basketballsupervisor.config.Config.CallBack;
import com.example.basketballsupervisor.db.GameDb;
import com.example.basketballsupervisor.db.GroupDb;
import com.example.basketballsupervisor.db.MemberDb;
import com.example.basketballsupervisor.http.QueryPlayInfoRequest;
import com.example.basketballsupervisor.http.QueryPlayInfoResponse;
import com.example.basketballsupervisor.model.Game;
import com.example.basketballsupervisor.model.Group;
import com.example.basketballsupervisor.model.Member;

public class MainActivity extends BaseActivity {

	private Game mGame;
	private Group mThisGroup;
	private Group mRivalGroup;
	private List<Member> mThisMemberList;
	private List<Member> mRivalMemberList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.activity_main);
		
		loadDataInBackground();
	}

	@Override
	public void onInit() {
		
	}

	@Override
	public void onFindViews() {
		
	}

	@Override
	public void onInitViewData() {
		
	}

	@Override
	public void onBindListener() {
		
	}

	private void loadDataInBackground() {
		GameDb gameDb = Config.getDbHelper().getGameDb();
		if (gameDb.isHasData()) {
			loadLocalData();
		} else {
			requestGameData();
		}
	}

	private void loadLocalData() {
		GameDb gameDb = Config.getDbHelper().getGameDb();
		List<Game> gameList = gameDb.getAll();
		if (gameList != null && gameList.size() > 0) {
			mGame = gameList.get(0);
			
			if (mGame != null) {
				GroupDb groupDb = Config.getDbHelper().getGroupDb();
				List<Group> groupList = groupDb.getGameGroups(mGame.gId);
				
				mGame.groupList = groupList;
				
				if (groupList != null && groupList.size() >= 2) {
					MemberDb memberDb = Config.getDbHelper().getMemberDb();
					
					mThisGroup = groupList.get(0);
					if (mThisGroup != null) {
						mThisMemberList = memberDb.getGroupMembers(mThisGroup.gruopId);
						mThisGroup.memberList = mThisMemberList;
					}
					
					mRivalGroup = groupList.get(1);
					if (mRivalGroup != null) {
						mRivalMemberList = memberDb.getGroupMembers(mRivalGroup.gruopId);
						mThisGroup.memberList = mRivalMemberList;
					}
				}
			}
		}
	}

	private void requestGameData() {
		final QueryPlayInfoRequest request = new QueryPlayInfoRequest(1);
		Config.asynPost(this, "正在获取球队数据...", request.getData(), new CallBack() {
			
			@Override
			public void onSuccess(String o) {
				QueryPlayInfoResponse response = request.getObject(o);
				if (response != null) {
					if (response.isSuccess()) {
						List<Game> gameList = response.gameList;
						if (gameList != null && gameList.size() > 0) {
							showToastShort("球队数据获取成功");
							
							saveGameData(gameList);
							initGameData(gameList);
						} else {
							showToastShort("球队数据获取不完整，请检查网络后重试");
						}
					} else {
						onFail(response.error_remark);
					}
				} else {
					onFail(null);
				}
			}
			
			@Override
			public void onFinish(Object obj) {
			}
			
			@Override
			public void onFail(String msg) {
				if (TextUtils.isEmpty(msg)) {
					msg = "球队数据获取失败，请检查网络后重试";
				}
				showToastShort(msg);
			}
		});
	}

	private void initGameData(List<Game> gameList) {
		mGame = gameList.get(0);
		if (mGame != null) {
			List<Group> groupList = mGame.groupList;
			if (groupList != null && groupList.size() >= 2) {
				mThisGroup = groupList.get(0);
				if (mThisGroup != null) {
					mThisMemberList = mThisGroup.memberList;
				}
				
				mRivalGroup = groupList.get(1);
				if (mRivalGroup != null) {
					mRivalMemberList = mThisGroup.memberList;
				}
			}
		}
	}

	private void saveGameData(List<Game> gameList) {
		GameDb gameDb = Config.getDbHelper().getGameDb();
		gameDb.saveAll(gameList);

		GroupDb groupDb = Config.getDbHelper().getGroupDb();
		MemberDb memberDb = Config.getDbHelper().getMemberDb();
		for (Game game : gameList) {
			if (game != null) {
				List<Group> groupList = game.groupList;
				if (groupList != null && groupList.size() > 0) {
					groupDb.saveAll(groupList, game);
					
					for (Group group : groupList) {
						if (group != null) {
							List<Member> memberList = group.memberList;
							
							if (memberList != null && memberList.size() > 0) {
								memberDb.saveAll(memberList, group, game);
							}
						}
					}
				}
			}
		}
	}

}
