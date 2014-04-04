package com.example.basketballsupervisor.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.framework.core.widget.ConfirmDialog;
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

public class MainActivity extends BaseActivity implements OnClickListener {

	private long mGameTime = 0l;
	private int mGroupAScore = 0;
	private int mGroupBScore = 0;
	
	private Game mGame;
	private Group mGroupA;
	private Group mGroupB;
	private List<Member> mGroupAMemberList;
	private List<Member> mGroupBMemberList;
	
	private TextView mTvGameTime, mTvGameFirstHalf, mTvGameSecondHalf;
	private TextView mTvGroupAName, mTvGroupBName;
	private TextView mTvGroupAScore, mTvGroupBScore;
	private ImageView mIvSubstitueLeft, mIvSubstitueRight;
	private ImageView mIvPauseLeft, mIvPauseRight;
	private ImageView mIvInfoLeft, mIvInfoRight;
	private LinearLayout mLlUpload;
	
	private boolean pausing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		loadDataInBackground();
	}

	@Override
	public void onInit() {
		
	}

	@Override
	public void onFindViews() {
		mTvGameTime = (TextView) findViewById(R.id.tv_game_time);
		mTvGameFirstHalf = (TextView) findViewById(R.id.tv_game_first_half);
		mTvGameSecondHalf = (TextView) findViewById(R.id.tv_game_second_half);
		
		mTvGroupAName = (TextView) findViewById(R.id.tv_group_a_name);
		mTvGroupBName = (TextView) findViewById(R.id.tv_group_b_name);
		
		mTvGroupAScore = (TextView) findViewById(R.id.tv_group_a_score);
		mTvGroupBScore = (TextView) findViewById(R.id.tv_group_b_score);
		
		mIvSubstitueLeft = (ImageView) findViewById(R.id.iv_substitute_left);
		mIvSubstitueRight = (ImageView) findViewById(R.id.iv_substitute_right);
		
		mIvPauseLeft = (ImageView) findViewById(R.id.iv_pause_left);
		mIvPauseRight = (ImageView) findViewById(R.id.iv_pause_right);
		
		mIvInfoLeft = (ImageView) findViewById(R.id.iv_info_left);
		mIvInfoRight = (ImageView) findViewById(R.id.iv_info_right);
		
		mLlUpload = (LinearLayout) findViewById(R.id.ll_upload);
	}

	@Override
	public void onInitViewData() {
		String formedGameTime = formGameTime();
		mTvGameTime.setText(formedGameTime);
		
		if (mGameTime <= 20 * 60 * 1000) {
			mTvGameFirstHalf.setTextColor(getResources().getColor(R.color.red));
			mTvGameSecondHalf.setTextColor(getResources().getColor(R.color.white));
		} else {
			mTvGameFirstHalf.setTextColor(getResources().getColor(R.color.white));
			mTvGameSecondHalf.setTextColor(getResources().getColor(R.color.red));
		}
		
		String groupAName = (mGroupA != null && !TextUtils.isEmpty(mGroupA.groupName)) ? mGroupA.groupName : "A队";
		mTvGroupAName.setText(groupAName);
		
		String groupBName = (mGroupB != null && !TextUtils.isEmpty(mGroupB.groupName)) ? mGroupB.groupName : "B队";
		mTvGroupBName.setText(groupBName);
		
		mTvGroupAScore.setText(String.valueOf(mGroupAScore));
		mTvGroupBScore.setText(String.valueOf(mGroupBScore));
	}

	private String formGameTime() {
		return "00:00 | 00";
	}

	@Override
	public void onBindListener() {
		mIvSubstitueLeft.setOnClickListener(this);
		mIvSubstitueRight.setOnClickListener(this);
		
		mIvPauseLeft.setOnClickListener(this);
		mIvPauseRight.setOnClickListener(this);
		
		mIvInfoLeft.setOnClickListener(this);
		mIvInfoRight.setOnClickListener(this);
		
		mLlUpload.setOnClickListener(this);
	}

	private void loadDataInBackground() {
		GameDb gameDb = new GameDb(this);
		if (gameDb.isHasData()) {
			loadLocalData();
		} else {
			requestGameData();
		}
	}

	private void loadLocalData() {
		GameDb gameDb = new GameDb(this);
		List<Game> gameList = gameDb.getAll();
		if (gameList != null && gameList.size() > 0) {
			mGame = gameList.get(0);
			
			if (mGame != null) {
				GroupDb groupDb = new GroupDb(this);
				List<Group> groupList = groupDb.getGameGroups(mGame.gId);
				
				mGame.groupList = groupList;
				
				if (groupList != null && groupList.size() >= 2) {
					MemberDb memberDb = new MemberDb(this);
					
					mGroupA = groupList.get(0);
					if (mGroupA != null) {
						mGroupAMemberList = memberDb.getGroupMembers(mGroupA.groupId);						
						mGroupA.memberList = mGroupAMemberList;
					}
					
					mGroupB = groupList.get(1);
					if (mGroupB != null) {
						mGroupBMemberList = memberDb.getGroupMembers(mGroupB.groupId);		
						
						// TODO only for test
						if (mGroupBMemberList == null || mGroupBMemberList.size() == 0) {
							mGroupBMemberList = new ArrayList<Member>();

							String[] names = new String[]{"张三", "李四", "王五", "黄阁", "王伟"};
							String[] pos = new String[]{"中锋", "前锋", "后卫", "大前锋", "中卫"};
							
							for (int i = 0; i < 5; i++) {
								Member member = new Member();
								member.isLeader = i == 0 ? 1 : 0;
								member.memberId = i + 1;
								member.name = names[i];
								member.site = pos[i];
							}
						}
						
						mGroupB.memberList = mGroupBMemberList;
					}
				}
			}
			
			onInitViewData();
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
				mGroupA = groupList.get(0);
				if (mGroupA != null) {
					mGroupAMemberList = mGroupA.memberList;
				}
				
				mGroupB = groupList.get(1);
				if (mGroupB != null) {
					mGroupBMemberList = mGroupA.memberList;
				}
			}
		}
		
		onInitViewData();
	}

	private void saveGameData(List<Game> gameList) {
		GameDb gameDb = new GameDb(this);
		gameDb.saveAll(gameList);

		GroupDb groupDb = new GroupDb(this);
		MemberDb memberDb = new MemberDb(this);
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

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.iv_substitute_left:
		case R.id.iv_substitute_right:
			substitute();
			break;
		case R.id.iv_pause_left:
		case R.id.iv_pause_right:
			if (!pausing) {
				pauseGame();
			} else {
				continueGame();
			}
			break;
		case R.id.iv_info_left:
		case R.id.iv_info_right:
			
			break;
		case R.id.ll_upload:
			uploadGameData();
			break;
		}
	}

	private void substitute() {
		// 换人
		
		// 判断当前比赛状态是否允许换人
		boolean allowSubstitute = true;
		if (allowSubstitute) {
			showSubstitutePanel();
		} else {
			showToastLong("不允许换人");
		}
	}

	private void showSubstitutePanel() {
		// 显示换人面板
		
		// 左边场上球员playerInTheGame，右边整队球员groupMembers
		
		// 1.选择首发球员
		// 2.开始比赛的按钮
	}

	private void pauseGame() {
		// 暂停比赛
		
		// 判断当前比赛状态是否允许请求暂停比赛, 比如A队暂停已经用完，或者出现事故临时暂停比赛
		boolean allowPause = true;
		if (allowPause) {
			showPauseGameDialog();
		} else {
			showToastLong("不允许暂停比赛");
		}
	}

	private void showPauseGameDialog() {
		ConfirmDialog dialog = new ConfirmDialog(this, "是否暂停比赛？", new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				pausing = true;
				
				mIvPauseLeft.setImageResource(R.drawable.btn_continue);
				mIvPauseRight.setImageResource(R.drawable.btn_continue);
			}
		});
		dialog.show();
	}
	
	private void continueGame() {
		// 继续比赛
		
		pausing = false;
		mIvPauseLeft.setImageResource(R.drawable.btn_pause);
		mIvPauseRight.setImageResource(R.drawable.btn_pause);
	}

	private void uploadGameData() {
		// 上传比赛数据
		// 检查是否有比赛数据，有则准备比赛数据，无则提示用户无比赛数据
		
		boolean hasGameData = true;
		if (hasGameData) {
			showConfirmUploadGameDataDialog();
		} else {
			showToastLong("无比赛数据");
		}
	}

	private void showConfirmUploadGameDataDialog() {
		ConfirmDialog dialog = new ConfirmDialog(this, "是否上传比赛数据？", new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				reportGameData();
			}
		});
		dialog.show();
	}

	private void reportGameData() {
		
		
	}

}
