package com.example.basketballsupervisor.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.framework.core.widget.ConfirmDialog;
import com.example.basketballsupervisor.IApplication;
import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.config.Config;
import com.example.basketballsupervisor.config.Config.CallBack;
import com.example.basketballsupervisor.db.ActionDb;
import com.example.basketballsupervisor.db.GameDb;
import com.example.basketballsupervisor.db.GameTimeDb;
import com.example.basketballsupervisor.db.GroupDb;
import com.example.basketballsupervisor.db.MemberDb;
import com.example.basketballsupervisor.db.PlayingTimeDb;
import com.example.basketballsupervisor.db.RecordDb;
import com.example.basketballsupervisor.http.QueryBBGameRecordRequest;
import com.example.basketballsupervisor.http.QueryBBGameRecordResponse;
import com.example.basketballsupervisor.http.QueryPlayInfoRequest;
import com.example.basketballsupervisor.http.QueryPlayInfoResponse;
import com.example.basketballsupervisor.http.ReportGameRecordRequest;
import com.example.basketballsupervisor.http.ReportGameRecordResponse;
import com.example.basketballsupervisor.model.Action;
import com.example.basketballsupervisor.model.BBGameRecord;
import com.example.basketballsupervisor.model.Game;
import com.example.basketballsupervisor.model.GameRecord;
import com.example.basketballsupervisor.model.Group;
import com.example.basketballsupervisor.model.Member;
import com.example.basketballsupervisor.model.Record;
import com.example.basketballsupervisor.model.RoleRecord;
import com.example.basketballsupervisor.util.CountDown;
import com.example.basketballsupervisor.util.CountDown.OnCountDownListener;
import com.example.basketballsupervisor.widget.DataStatDialog;
import com.example.basketballsupervisor.widget.RecordEventDialog;
import com.example.basketballsupervisor.widget.SelectPlayersDialog;

public class MainActivity extends BaseActivity implements OnClickListener, OnCountDownListener, OnItemClickListener {

	private int mRole = 1;
	private long mGameTime = 0l;
	private int mGroupAScore = 0;
	private int mGroupBScore = 0;
	
	private Game mGame;
	private Group mGroupA;
	private Group mGroupB;
	
	private List<Member> mGroupAPlayingMemberList;
	private List<Member> mGroupBPlayingMemberList;
	
	private List<Member> mGroupAMemberList;
	private List<Member> mGroupBMemberList;
	
	private TextView mTvGameStart, mTvGameTime, mTvGameFirstHalf, mTvGameSecondHalf;
	private TextView mTvGroupAName, mTvGroupBName;
	private TextView mTvGroupAScore, mTvGroupBScore;
	private ImageView mIvSubstitueLeft, mIvSubstitueRight;
	private ImageView mIvPauseLeft, mIvPauseRight;
	private ImageView mIvInfoLeft, mIvInfoRight;
	private LinearLayout mLlUpload;
	
	private GridView mGvCourt;
	private CourtAdapter mCourtAdapter;
	private ArrayList<Integer> mCourtPositions;
	
	private CountDown mCountDown;
	
	private boolean running = false;
	private boolean pausing = false;
	
	private List<Action> mActionList;
	private boolean isRequiredUpdateGameRecord = true;// 默认需要获取最新的比赛记录

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		loadDataInBackground();
	}

	@Override
	public void onInit() {
		mGroupAPlayingMemberList = new ArrayList<Member>();
		mGroupBPlayingMemberList = new ArrayList<Member>();
		
		int timeout = 4 * 10 * 60 * 1000;// 四节比赛，每节比赛10分钟
		mCountDown = new CountDown(timeout, 1000);
		mCountDown.setOnCountDownListener(this);
	}

	@Override
	public void onFindViews() {
		mTvGameStart = (TextView) findViewById(R.id.tv_game_start);
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
		
		mGvCourt = (GridView) findViewById(R.id.gv_court);
	}

	@Override
	public void onInitViewData() {
		onCountDownIntervalReach(0);

		int drawable = R.drawable.st_07;
		
		// TODO 目前仅支持一种角色
		if (mGame != null && mGame.role != null) {
			mRole = mGame.role.get(0);
		}
		if (mRole == 1) {// 记录A队数据
			mTvGroupAName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, drawable);
			mTvGroupBName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		} else if (mRole == 2) {// 记录B队数据
			mTvGroupAName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			mTvGroupBName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, drawable);
		} else if (mRole == 3) {// 记录创新数据
			mTvGroupAName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			mTvGroupBName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		
		String groupAName = (mGroupA != null && !TextUtils.isEmpty(mGroupA.groupName)) ? mGroupA.groupName : "A队";
		mTvGroupAName.setText(groupAName);
		
		String groupBName = (mGroupB != null && !TextUtils.isEmpty(mGroupB.groupName)) ? mGroupB.groupName : "B队";
		mTvGroupBName.setText(groupBName);
		
		mTvGroupAScore.setText(String.valueOf(mGroupAScore));
		mTvGroupBScore.setText(String.valueOf(mGroupBScore));
		
		// 填充网格
		mCourtPositions = new ArrayList<Integer>(17 * 32);// 长32个格子，宽17个格子
		for (int i = 0; i < 17 * 32; i++) {
			mCourtPositions.add(null);
		}
		
		mCourtAdapter = new CourtAdapter(this, mCourtPositions);
		mGvCourt.setAdapter(mCourtAdapter);
	}

	private String formGameTime(int count) {
		StringBuffer gameTime = new StringBuffer();
		
		int time = count / 1000;
		
		int minutes = time / 60;
		if (minutes < 10) {
			gameTime.append("0");
		}
		gameTime.append(minutes);

		gameTime.append(":");
		
		int second = time % 60;
		if (second < 10) {
			gameTime.append("0");
		}
		gameTime.append(second);
		
		gameTime.append(" | ");
		int milisecends = (count % 1000) / 100;
		if (milisecends < 10) {
			gameTime.append("0");
		}
		gameTime.append(milisecends);
		
		return gameTime.toString();
	}

	@Override
	public void onBindListener() {
		mTvGameStart.setOnClickListener(this);
		mTvGameTime.setOnClickListener(this);
		
		mIvSubstitueLeft.setOnClickListener(this);
		mIvSubstitueRight.setOnClickListener(this);
		
		mIvPauseLeft.setOnClickListener(this);
		mIvPauseRight.setOnClickListener(this);
		
		mIvInfoLeft.setOnClickListener(this);
		mIvInfoRight.setOnClickListener(this);
		
		mLlUpload.setOnClickListener(this);
		
		mGvCourt.setOnItemClickListener(this);
	}

	private void loadDataInBackground() {
		GameDb gameDb = new GameDb(this);
		if (gameDb.isHasData()) {
			loadLocalData();
		} else {
			requestGameData();
		}
		
		loadActionData();
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
					mGroupBMemberList = mGroupB.memberList;
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
					
					memberDb.clearAllData();
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
	
	private void loadActionData() {
		// 加载动作数据
		ActionDb db = new ActionDb(this);
		if (!db.isHasData()) {
			db.insertSampleData();
		}
		
		mActionList = db.getAll();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.tv_game_start:
			startGame();
			break;
		case R.id.tv_game_time:
			doPauseGame();
			break;
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
			updateGameRecord();
			break;
		case R.id.ll_upload:
			uploadGameData();
			break;
		}
	}

	private void startGame() {
		// 开始比赛
		
		// 选择首发球员
		if (mRole != 3) {// 记录创新数据无需选择首发球员
			selectStartPlayers();
		} else {// 记录创新数据
			doStartGame();
		}
	}

	private void doStartGame() {
		running = true;
		
		mTvGameStart.setVisibility(View.GONE);
		
		mCountDown.start();// 计时开始
		mGameTime = System.currentTimeMillis();
		
		RecordDb db = new RecordDb(this);
		db.clearAllData();
	}

	private void selectStartPlayers() {
		// 选择首发球员
		showToastShort("选择首发球员");
		
		SelectPlayersDialog dialog = new SelectPlayersDialog(this, SelectPlayersDialog.MODE_SELECT_STARTS);
		dialog.show();
		dialog.fillGroupData(mGroupA, mGroupB);
		if (mRole == 1) {// 记录A队数据
			dialog.fillPlayersData(mGroupAPlayingMemberList, mGroupAMemberList);
		} else if (mRole == 2) {// 记录B队数据
			dialog.fillPlayersData(mGroupBPlayingMemberList, mGroupBMemberList);
		} else if (mRole == 3) {// 记录创新数据
			// 不处理
		}
		dialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				showToastShort("比赛开始");
				doStartGame();
				
				// 记录比赛开始时间
				GameTimeDb gameTimeDb = new GameTimeDb(getActivity());
				gameTimeDb.startOrContinueGame(mGame, null, mGameTime);
				
				// 记录首发球员上场时间
				PlayingTimeDb db = new PlayingTimeDb(getActivity());
				if (mRole == 1) {// 记录A队数据
					db.startOrContinueGame(mGame, mGroupA, mGroupAPlayingMemberList, mGameTime);
				} else if (mRole == 2) {// 记录B队数据
					db.startOrContinueGame(mGame, mGroupB, mGroupBPlayingMemberList, mGameTime);
				} else if (mRole == 3) {// 记录创新数据
					// 不处理
				}
			}
		});
	}

	private void substitute() {
		// 换人
		
		// 判断当前比赛状态是否允许换人
		boolean allowSubstitute = running && mRole != 3;// 记录创新数据的角色不允许换人
		if (allowSubstitute) {
//			if (mRole == 1) {// 记录A队数据
//				allowSubstitute = !(mGroupAPlayingMemberList.size() == mGroupAMemberList.size());
//			} else if (mRole == 2) {// 记录B队数据
//				allowSubstitute = !(mGroupBPlayingMemberList.size() == mGroupBMemberList.size());
//			} else if (mRole == 3) {// 记录创新数据
//				// 不处理
//			}
//			
//			if (allowSubstitute) {
				showSubstituteDialog();
//			} else {
//				showToastLong("无替补队员");
//			}
		} else {
			showToastLong("不允许换人");
		}
	}

	private void showSubstituteDialog() {
		// 显示换人面板
		if (!pausing) {
			doPauseGame();
		}
		
		// 左边场上球员playerInTheGame，右边整队球员groupMembers
		SelectPlayersDialog dialog = new SelectPlayersDialog(this, SelectPlayersDialog.MODE_SUBSTITUTE);
		dialog.show();
		dialog.fillGroupData(mGroupA, mGroupB);
		if (mRole == 1) {// 记录A队数据
			dialog.fillPlayersData(mGroupAPlayingMemberList, mGroupAMemberList);
		} else if (mRole == 2) {// 记录B队数据
			dialog.fillPlayersData(mGroupBPlayingMemberList, mGroupBMemberList);
		} else if (mRole == 3) {// 记录创新数据
			// 不处理
		}
	}

	private void pauseGame() {
		// 暂停比赛
		
		// 判断当前比赛状态是否允许请求暂停比赛, 比如A队暂停已经用完，或者出现事故临时暂停比赛
		boolean allowPause = running;
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
				doPauseGame();
			}
		});
		dialog.show();
	}
	
	private void continueGame() {
		// 继续比赛
		showToastShort("比赛继续");
		
		pausing = false;
		mIvPauseLeft.setImageResource(R.drawable.btn_pause);
		mIvPauseRight.setImageResource(R.drawable.btn_pause);
		
		mCountDown.setPauseWork(false);
		
		mGameTime = System.currentTimeMillis();
		
		// 记录比赛开始时间
		GameTimeDb gameTimeDb = new GameTimeDb(getActivity());
		if (mRole == 1) {// 记录A队数据
			gameTimeDb.startOrContinueGame(mGame, mGroupA, mGameTime);
		} else if (mRole == 2) {// 记录B队数据
			gameTimeDb.startOrContinueGame(mGame, mGroupB, mGameTime);
		} else if (mRole == 3) {// 记录创新数据
			// 无需处理
		}
		
		// 记录首发球员上场时间
		PlayingTimeDb db = new PlayingTimeDb(getActivity());
		if (mRole == 1) {// 记录A队数据
			db.startOrContinueGame(mGame, mGroupA, mGroupAPlayingMemberList, mGameTime);
		} else if (mRole == 2) {// 记录B队数据
			db.startOrContinueGame(mGame, mGroupB, mGroupBPlayingMemberList, mGameTime);
		} else if (mRole == 3) {// 记录创新数据
			// 无需处理
		}
	}

	private void updateGameRecord() {
		// 更新比赛记录，显示统计信息表
		
		if (isRequiredUpdateGameRecord) {
			final QueryBBGameRecordRequest request = new QueryBBGameRecordRequest(mGame.gId);
			Config.asynPost(this, "正在更新比赛记录，请稍候...", request.getData(), new CallBack() {
				
				@Override
				public void onSuccess(String o) {
					QueryBBGameRecordResponse response = request.getObject(o);
					if (response != null) {
						if (response.isSuccess()) {
							showToastShort("比赛记录更新成功");	
							
							isRequiredUpdateGameRecord = response.is_complete == 0;
							
							saveGameRecord(response.game_record_list);
							showStatPanel();
						} else {
							onFail(response.error_remark);
						}
					} else {
						onFail(null);
					}
				}

				private void saveGameRecord(List<GameRecord> gameRecordList) {
					if (gameRecordList != null && gameRecordList.size() > 0) {
						List<Record> recordList = new ArrayList<Record>();
						
						for (GameRecord gameRecord : gameRecordList) {
							Record record = new Record(gameRecord);
							recordList.add(record);
						}
						
						RecordDb db = new RecordDb(getActivity());
						db.saveAll(recordList);
					}
				}
				
				@Override
				public void onFinish(Object obj) {
				}
				
				@Override
				public void onFail(String msg) {
					if (TextUtils.isEmpty(msg)) {
						msg = "上传数据失败，请检查网络后重试";
					}
					showToastShort(msg);
				}
			});
		} else {
			showStatPanel();
		}
	}

	private void showStatPanel() {
		// 显示统计信息画板
		RecordDb db = new RecordDb(this);
		List<Record> recordList = db.getAll(mGame);
		for (Record record : recordList) {
			if (TextUtils.isEmpty(record.coordinate)) {
				String[] split = record.coordinate.split(",");
				int position = Integer.parseInt(split[0]) + Integer.parseInt(split[1]) * 32;
				mCourtPositions.set(position, 0);
			}
		}
		mCourtAdapter.notifyDataSetChanged();
		
		DataStatDialog dialog = new DataStatDialog(this);
		dialog.show();
	}

	private void uploadGameData() {
		// 上传比赛数据
		
		// TODO 比赛是否正在进行，是则等待比赛完成再上传数据，否则不允许上传
		
		// 检查是否有比赛数据，有则准备比赛数据，无则提示用户无比赛数据
		RecordDb db = new RecordDb(this);
		
		boolean hasGameData = db.isHasData();
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
		List<RoleRecord> records = new ArrayList<RoleRecord>();
		
		if (mRole == 1) {// 记录A队数据
			records.add(getRoleRecord(mGroupA));
		} else if (mRole == 2) {// 记录B队数据
			records.add(getRoleRecord(mGroupB));
		} else if (mRole == 3) {// 记录创新数据
			records.add(getRoleRecord(mGroupA));
			records.add(getRoleRecord(mGroupB));
		}
		
		final ReportGameRecordRequest request = new ReportGameRecordRequest(records);
		Config.asynPost(this, "正在上传，请稍候...", request.getData(), new CallBack() {
			
			@Override
			public void onSuccess(String o) {
				ReportGameRecordResponse response = request.getObject(o);
				if (response != null) {
					if (response.isSuccess()) {
						showToastShort("比赛数据上传成功");	
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
					msg = "上传数据失败，请检查网络后重试";
				}
				showToastShort(msg);
			}
		});
	}

	/**
	 * 获取角色记录
	 * @param group 队伍
	 * @return
	 */
	private RoleRecord getRoleRecord(Group group) {
		RoleRecord roleRecord = new RoleRecord();
		roleRecord.game_id = mGame.gId;
		roleRecord.group_id = group.groupId;
		roleRecord.recordType = mRole;
		
		List<BBGameRecord> gameRecords = new ArrayList<BBGameRecord>();
		roleRecord.bb_game_record = gameRecords;

		RecordDb db = new RecordDb(this);
		List<Record> recordList = db.getAll(mGame, group);
		for (Record record : recordList) {
			BBGameRecord gameRecord = new BBGameRecord(record);
			gameRecords.add(gameRecord);
		}
		
		return roleRecord;
	}

	@Override
	public void OnCountDownTimeout() {
		// 比赛完成
		showToastLong("比赛结束");
	}

	@Override
	public void onCountDownIntervalReach(int last) {
		mTvGameTime.setText(formGameTime(last));
		
		if (last > 0 && last <= 20 * 60 * 1000) {
			mTvGameFirstHalf.setTextColor(getResources().getColor(R.color.game_progress));
			mTvGameSecondHalf.setTextColor(getResources().getColor(R.color.white));
		} else if (last > 20 * 60 * 1000) {
			mTvGameFirstHalf.setTextColor(getResources().getColor(R.color.white));
			mTvGameSecondHalf.setTextColor(getResources().getColor(R.color.game_progress));
		} else {
			mTvGameFirstHalf.setTextColor(getResources().getColor(R.color.white));
			mTvGameSecondHalf.setTextColor(getResources().getColor(R.color.white));
		}
	}

	public void updateGroupAScore(int score) {
		mGroupAScore += score;
		mTvGroupAScore.setText(String.valueOf(mGroupAScore));
	}

	public void updateGroupBScore(int score) {
		mGroupBScore += score;
		mTvGroupBScore.setText(String.valueOf(mGroupBScore));
	}

	public void setCurrentRecordCoordinate(Action action, String coordinate) {
		String[] split = coordinate.split(",");
		
		int position = Integer.parseInt(split[0]) + Integer.parseInt(split[1]) * 32;
		
		mCourtPositions.set(position, action.type);
		mCourtAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		boolean allowRecordEvent = running;
		if (allowRecordEvent) {
			showRecordEventDialog(position);
		} else {
			showToast("当前不允许记录球员事件");
		}
	}

	private void showRecordEventDialog(int position) {
		// 显示记录球员事件的对话框
		
		// TODO only for test
		if (mGroupAPlayingMemberList.isEmpty()) {
			mGroupAPlayingMemberList.addAll(mGroupAMemberList);
		}
		if (mGroupBPlayingMemberList.isEmpty()) {
			mGroupBPlayingMemberList.addAll(mGroupBMemberList);
		}
		
		mGameTime = System.currentTimeMillis();
		
		String coordinate = parseCoordinate(position);

		RecordEventDialog dialog = new RecordEventDialog(this, mActionList);
		dialog.show();
		dialog.fillGameData(mGame, mRole, mGameTime, coordinate);
		dialog.fillGroupData(mGroupA, mGroupB);
		dialog.fillPlayersData(mGroupAPlayingMemberList, mGroupBPlayingMemberList);
	}
	
	private String parseCoordinate(int position) {
		// 解析位置的坐标
		StringBuffer coordinate = new StringBuffer();
		coordinate.append(position % 32).append(",").append(position / 32);
		return coordinate.toString();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			int columnWidth = mGvCourt.getWidth() / 32;
			mGvCourt.setColumnWidth(columnWidth);
			
			int columnHeight  = mGvCourt.getHeight() / 17;
			mCourtAdapter.setColumn(columnWidth, columnHeight);
		}
	}
	
	private void doPauseGame() {
		showToastShort("比赛暂停");
		
		pausing = true;
		
		mIvPauseLeft.setImageResource(R.drawable.btn_continue);
		mIvPauseRight.setImageResource(R.drawable.btn_continue);
		
		mCountDown.setPauseWork(true);
		
		mGameTime = System.currentTimeMillis();
		
		// 记录比赛开始时间
		GameTimeDb gameTimeDb = new GameTimeDb(getActivity());
		if (mRole == 1) {// 记录A队数据
			gameTimeDb.pauseOrEndGame(mGame, mGroupA, mGameTime);
		} else if (mRole == 2) {// 记录B队数据
			gameTimeDb.pauseOrEndGame(mGame, mGroupB, mGameTime);
		} else if (mRole == 3) {// 记录创新数据
			// 无需处理
		}
		
		// 记录首发球员上场时间
		PlayingTimeDb db = new PlayingTimeDb(getActivity());
		if (mRole == 1) {// 记录A队数据
			db.pauseOrEndGame(mGame, mGroupA, mGroupAPlayingMemberList, mGameTime);
		} else if (mRole == 2) {// 记录B队数据
			db.pauseOrEndGame(mGame, mGroupB, mGroupBPlayingMemberList, mGameTime);
		} else if (mRole == 3) {// 记录创新数据
			// 无需处理
		}
	}

	private class CourtAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private List<Integer> positions;
		
		private int columnWidth;
		private int columnHeight;

		public CourtAdapter(Context context, List<Integer> positions) {
			mInflater = LayoutInflater.from(context);
			this.positions = positions;
		}

		public void setColumn(int columnWidth, int columnHeight) {
			this.columnWidth = columnWidth;
			this.columnHeight = columnHeight;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return positions.size();
		}

		@Override
		public Object getItem(int position) {
			return positions.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_event_coordinate, parent, false);
			}
			
			if (columnHeight > 0) {
				AbsListView.LayoutParams params = new AbsListView.LayoutParams(columnWidth, columnHeight);
				convertView.setLayoutParams(params);
			}
			
			ImageView image = (ImageView) convertView.findViewById(R.id.iv_coordinate);

			Integer type = (Integer) getItem(position);
			if (type == null) {
//				image.setImageResource(R.drawable.basketball_square);
				image.setImageResource(0);
			} else {
				if (type == 0) {
					image.setImageResource(R.drawable.position_03);
				} else {
					image.setImageResource(R.drawable.position_07);
				}
			}
			
			return convertView;
		}
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		IApplication.hasStart = false;
	}

}
