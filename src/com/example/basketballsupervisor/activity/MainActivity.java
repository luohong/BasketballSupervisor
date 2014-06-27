package com.example.basketballsupervisor.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.framework.core.widget.ConfirmDialog;
import com.example.basketballsupervisor.IApplication;
import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.adapter.CourtAdapter;
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
import com.example.basketballsupervisor.model.DataStat;
import com.example.basketballsupervisor.model.Game;
import com.example.basketballsupervisor.model.GameRecord;
import com.example.basketballsupervisor.model.Group;
import com.example.basketballsupervisor.model.Member;
import com.example.basketballsupervisor.model.PlayingTime;
import com.example.basketballsupervisor.model.Record;
import com.example.basketballsupervisor.model.RoleRecord;
import com.example.basketballsupervisor.util.Constants;
import com.example.basketballsupervisor.util.CountDown;
import com.example.basketballsupervisor.util.CountDown.OnCountDownListener;
import com.example.basketballsupervisor.util.GameUtils;
import com.example.basketballsupervisor.util.HomeWatcher;
import com.example.basketballsupervisor.util.HomeWatcher.OnHomePressedListener;
import com.example.basketballsupervisor.util.SpUtil;
import com.example.basketballsupervisor.widget.DataStatDialog;
import com.example.basketballsupervisor.widget.GameListDialog;
import com.example.basketballsupervisor.widget.QuickAction;
import com.example.basketballsupervisor.widget.RecordEventDialog;
import com.example.basketballsupervisor.widget.SelectPlayersDialog;

public class MainActivity extends BaseActivity implements OnClickListener, OnCountDownListener, OnItemClickListener, OnLongClickListener {
	
	private static final int CLICK_POS_LEFT = 0;
	private static final int CLICK_POS_RIGHT = 1;
	private static final String TAG = MainActivity.class.getSimpleName();

	private List<Integer> mRoles = new ArrayList<Integer>(3);
	private long mGameTime = 0l;
	private int mGroupAScore = 0;
	private int mGroupBScore = 0;
	
	private int mCurrentQuarter = 1;
	private int mQuarterCount = 1;
	private int mQuarterTime = 10;
	private List<Integer> mQuarterTimeList = new ArrayList<Integer>();
	
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
	private ImageView mIvUndo;
	private ImageView mIvSubstitueLeft, mIvSubstitueRight;
	private ImageView mIvPauseLeft, mIvPauseRight;
	private ImageView mIvInfoLeft, mIvInfoRight;
	private ImageView mIvDataStatLeft, mIvDataStatRight;
	private ImageView mIvNewGame;
	private ImageView mIvLogout;
	private LinearLayout mLlUpload;
	
	private GridView mGvCourt;
	private CourtAdapter mCourtAdapter;
	private ArrayList<Integer> mCourtPositions;
	
	private CountDown mCountDown;
	
	private HomeWatcher mHomeWatcher;
	
	private PowerManager.WakeLock mWakeLock;
	
	private boolean running = false;
	private boolean pausing = false;
	
	private List<Action> mActionList;
	private Map<Integer, Action> mActionMap;
	private boolean isRequiredUpdateGameRecord = true;// 默认需要获取最新的比赛记录
	private boolean isRequiredRecord = true;
	private SpUtil mSpUtil;
	private int mRunningTime = 0;
	private long mSelectedGameId = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		loadData();
	}

	@Override
	public void onInit() {
		mSpUtil = SpUtil.getInstance(this);
		
		mGroupAPlayingMemberList = new ArrayList<Member>();
		mGroupBPlayingMemberList = new ArrayList<Member>();
		
		mHomeWatcher = new HomeWatcher(this);  
        mHomeWatcher.setOnHomePressedListener(new OnHomePressedListener() {  
            @Override  
            public void onHomePressed() {  
            	moveAppToBack();
            }  
  
            @Override  
            public void onHomeLongPressed() {  
            }  
        });  
        mHomeWatcher.startWatch();
        
        PowerManager mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag"); 
	}

	@Override
	public void onFindViews() {
		mTvGameStart = (TextView) findViewById(R.id.tv_game_start);
		mTvGameTime = (TextView) findViewById(R.id.tv_game_time);
		mTvGameFirstHalf = (TextView) findViewById(R.id.tv_game_first_half);
		mTvGameSecondHalf = (TextView) findViewById(R.id.tv_game_second_half);
		
		mIvUndo = (ImageView) findViewById(R.id.iv_undo);
		
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
		
		mIvDataStatLeft = (ImageView) findViewById(R.id.iv_stat_left);
		mIvDataStatRight = (ImageView) findViewById(R.id.iv_stat_right);

		mIvNewGame = (ImageView) findViewById(R.id.iv_new_game);
		
		mIvLogout = (ImageView) findViewById(R.id.iv_logout);
		
		mLlUpload = (LinearLayout) findViewById(R.id.ll_upload);
		
		mGvCourt = (GridView) findViewById(R.id.gv_court);
	}

	@Override
	public void onInitViewData() {
		mRoles = new ArrayList<Integer>();
		
		// 填充网格
		mCourtPositions = new ArrayList<Integer>(17 * 32);// 长32个格子，宽17个格子
		for (int i = 0; i < 17 * 32; i++) {
			mCourtPositions.add(null);
		}
		
		mTvGroupAName.setText("A队");
		mTvGroupBName.setText("B队");
		
		updateGroupAScore(0);
		updateGroupBScore(0);
		
		mIvUndo.setVisibility(View.GONE);
		
		mCourtAdapter = new CourtAdapter(this, mCourtPositions);
		mGvCourt.setAdapter(mCourtAdapter);
		
		mActionMap = new HashMap<Integer, Action>();
	}

	@Override
	public void onBindListener() {
		mTvGameStart.setOnClickListener(this);
		mTvGameTime.setOnClickListener(this);
		
		mTvGameTime.setOnLongClickListener(this);
		
		mTvGroupAScore.setOnLongClickListener(this);
		mTvGroupBScore.setOnLongClickListener(this);
		
		mIvSubstitueLeft.setOnClickListener(this);
		mIvSubstitueRight.setOnClickListener(this);
		
		mIvPauseLeft.setOnClickListener(this);
		mIvPauseRight.setOnClickListener(this);
		
		mIvInfoLeft.setOnClickListener(this);
		mIvInfoRight.setOnClickListener(this);
		
		mIvDataStatLeft.setOnClickListener(this);
		mIvDataStatRight.setOnClickListener(this);

		mIvNewGame.setOnClickListener(this);
		mIvLogout.setOnClickListener(this);
		
		mLlUpload.setOnClickListener(this);
		
		mGvCourt.setOnItemClickListener(this);
	}

	private void loadData() {
		loadActionData();
		
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
			Game selectedGame = null;
			mSelectedGameId = mSpUtil.getSelectedGameId();
			if (mSelectedGameId > -1) {
				for (Game game : gameList) {
					if (game.gId == mSelectedGameId) {
						selectedGame = game;
						break;
					}
				}
			}
			
			if (selectedGame != null) {
				onGameItemClick(selectedGame);
			} else {
				showGameListDialog(gameList);
			}
		}
	}

	private void showGameListDialog(List<Game> gameList) {
		GameListDialog dialog = new GameListDialog(this, gameList);
		dialog.show();
	}

	public void onGameItemClick(Game game) {
		boolean hasSelectedGame = mGame != null;
		if (game != null && (!hasSelectedGame || game.gId != mSelectedGameId)) {
			mGame = game;
			isRequiredUpdateGameRecord = true;
			
			mSelectedGameId = mGame.gId;
			mSpUtil.setSelectedGameId(mSelectedGameId, hasSelectedGame);
			
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
			} else {
				mGroupA = null;
				mGroupB = null;
				mGroupAMemberList = new ArrayList<Member>();
				mGroupBMemberList = new ArrayList<Member>();
			}
			
			onInitGameData();
			onRestoreGameState();
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
							showGameListDialog(gameList);
						} else {
							showToastShort("没有最新的比赛数据");
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

	private void onInitGameData() {
		
		mTvGameStart.setVisibility(View.VISIBLE);
		if (mCountDown != null) {
			mCountDown.stop();
		}
		
		// 支持多种角色
		if (mGame.role != null) {
			mRoles = mGame.role;
		}
		
		if (mRoles.isEmpty()) {
			mRoles.add(1);// 默认记录A队数据
		}

		int drawable = R.drawable.st_07;		
		if (mRoles.contains(1)) {// 记录A队数据
			mTvGroupAName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, drawable);
			mTvGroupBName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		} 
		if (mRoles.contains(2)) {// 记录B队数据
			mTvGroupAName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			mTvGroupBName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, drawable);
		} 
		if (mRoles.contains(3) && !mRoles.contains(1) && !mRoles.contains(2)) {// 记录创新数据
			mTvGroupAName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			mTvGroupBName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		
		// 比赛有4节，每节10分钟
		mQuarterCount = mGame.section;
		mQuarterTime = mGame.section_time;
		
		String groupAName = (mGroupA != null && !TextUtils.isEmpty(mGroupA.groupName)) ? mGroupA.groupName : "A队";
		mTvGroupAName.setText(groupAName);
		
		String groupBName = (mGroupB != null && !TextUtils.isEmpty(mGroupB.groupName)) ? mGroupB.groupName : "B队";
		mTvGroupBName.setText(groupBName);
		
		mIvPauseLeft.setImageResource(R.drawable.btn_pause);
		mIvPauseRight.setImageResource(R.drawable.btn_pause);
		
		initCourtRecord();
	}

	private List<Record> initCourtRecord() {
		
		resetCourtPositions();

		int groupAScore = 0;
		int groupBScore = 0;
		RecordDb db = new RecordDb(this);
		List<Record> recordList = db.getAll(mGame);
		for (Record record : recordList) {
			// 记录坐标
			if (!TextUtils.isEmpty(record.coordinate)) {
				String[] split = record.coordinate.split(",");
				int position = Integer.parseInt(split[0]) + Integer.parseInt(split[1]) * 32;
				mCourtPositions.set(position, 0);
			}
			
			// 记录得分
			Integer actionId = Integer.valueOf((int)record.actionId);
			if (mActionMap.containsKey(actionId)) {
				if (mGroupA.groupId == record.groupId) {
					groupAScore += mActionMap.get(actionId).score;
				} else if (mGroupB.groupId == record.groupId) {
					groupBScore += mActionMap.get(actionId).score;
				}
			} else {
				Log.e(TAG, "actionId " + actionId + " is not exist in action map");
			}
		}
		// 更新坐标
		mCourtAdapter.notifyDataSetChanged();

		// 更新记录得分
		updateGroupAScore(groupAScore);
		updateGroupBScore(groupBScore);
		
		mIvUndo.setVisibility(recordList.size() > 0 ? View.VISIBLE : View.GONE);
		
		return recordList;
	}

	private void resetCourtPositions() {
		mGroupAScore = mGroupBScore = 0;
		updateGroupAScore(mGroupAScore);
		updateGroupBScore(mGroupBScore);
		
		for (int i = 0; i < mCourtPositions.size(); i++) {
			mCourtPositions.set(i, null);
		}
		mCourtAdapter.notifyDataSetChanged();
	}

	private void saveGameData(List<Game> gameList) {
		GameDb gameDb = new GameDb(this);
		gameDb.saveAll(gameList);

		GroupDb groupDb = new GroupDb(this);
		groupDb.clearAllData();
		
		MemberDb memberDb = new MemberDb(this);
		memberDb.clearAllData();
		
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
	
	private void loadActionData() {
		// 加载动作数据
		ActionDb db = new ActionDb(this);
		if (!db.isHasData()) {
			db.insertSampleData();
		}
		
		mActionList = db.getAll();
		for (Action action : mActionList) {
			mActionMap.put(action.id, action);
		}
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
			substitute(CLICK_POS_LEFT);
			break;
		case R.id.iv_substitute_right:
			substitute(CLICK_POS_RIGHT);
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
			showFoulDialog(CLICK_POS_LEFT);// 犯规
			break;
		case R.id.iv_info_right:
			showFoulDialog(CLICK_POS_RIGHT);// 犯规
			break;
		case R.id.iv_stat_left:
		case R.id.iv_stat_right:
			updateGameRecord();
			break;
		case R.id.iv_new_game:
			requestNewGameData();
			break;
		case R.id.iv_logout:
			logout();
			break;
		case R.id.ll_upload:
			uploadGameData();
			break;
		case R.id.iv_undo:
			undoGameRecord();
			break;
		}
	}

	private void undoGameRecord() {
		
	}

	private void showFoulDialog(final int clickPos) {
		// 技术犯规
		// 判断当前比赛状态是否允许记录技术犯规
		boolean allow = running || pausing;// 记录创新数据的角色不允许记录技术犯规
		if (!allow) {
			showToastShort("比赛尚未开始，不允许记录犯规");
			return ;
		} else if (!mRoles.contains(1) && !mRoles.contains(2)) {
			showToastShort("创新数据记录角色不允许记录技术犯规");
			return ;
		}
		
		ConfirmDialog dialog = new ConfirmDialog(this, "犯规球员选项", true, "技术犯规", "犯规", new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				commonFouls(clickPos);
			}
		}, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				technicalFouls(clickPos);
			}
		});
		dialog.show();
	}
	
	protected void commonFouls(int clickPos) {
		if (mGroupAPlayingMemberList.isEmpty()) {
			mGroupAPlayingMemberList = filterMember(mGroupAMemberList);
		}
		if (mGroupBPlayingMemberList.isEmpty()) {
			mGroupBPlayingMemberList = filterMember(mGroupBMemberList);
		}
		
		mGameTime = System.currentTimeMillis();
		
		List<Action> actionList = new ArrayList<Action>();
		actionList.add(Action.newFoulAction());
		actionList.add(Action.newFouledAction());
		
		RecordEventDialog dialog = new RecordEventDialog(this, actionList);
		dialog.show();
		dialog.fillGameData(mGame, mRoles, mGameTime, "");
		dialog.fillGroupData(mGroupA, mGroupB);
		dialog.fillPlayersData(mGroupAPlayingMemberList, mGroupBPlayingMemberList);
	}

	private void technicalFouls(int clickPos) {
		// 技术犯规
		
		// 成员记录次数map
		Map<Long, Integer> mMemberActionMap = new HashMap<Long, Integer>();
		
		RecordDb db = new RecordDb(this);
		List<Record> recordList = db.getAll(mGame, 28);
		for (Record record : recordList) {
			Integer memberActionCount = mMemberActionMap.get(record.memberId);
			if (memberActionCount == null) {
				memberActionCount = 1;
			} else {
				memberActionCount ++;
			}
			mMemberActionMap.put(record.memberId, memberActionCount);
		}
		
		// 左边场上球员playerInTheGame，右边整队球员groupMembers
		SelectPlayersDialog dialog = new SelectPlayersDialog(this, SelectPlayersDialog.MODE_TECHNICAL_FOULS);
		dialog.show();
		dialog.fillGameData(mGame, mRoles, mGameTime, null);
		dialog.fillGroupData(mGroupA, mGroupB);
		switch (clickPos) {
		case CLICK_POS_LEFT:
			if (mRoles.contains(1)) {// 记录A队数据
				dialog.fillGroupData(mGroupA);
				dialog.fillPlayersData(mGroupAMemberList, mMemberActionMap);
			} else if (mRoles.contains(2)) {// 记录B队数据
				dialog.fillGroupData(mGroupB);
				dialog.fillPlayersData(mGroupBMemberList, mMemberActionMap);
			} else if (mRoles.contains(3)) {// 记录创新数据
				// 不处理
			}
			break;
		case CLICK_POS_RIGHT:
			if (mRoles.contains(2)) {// 记录B队数据
				dialog.fillGroupData(mGroupB);
				dialog.fillPlayersData(mGroupBMemberList, mMemberActionMap);
			} else if (mRoles.contains(1)) {// 记录A队数据
				dialog.fillGroupData(mGroupA);
				dialog.fillPlayersData(mGroupAMemberList, mMemberActionMap);
			} else if (mRoles.contains(3)) {// 记录创新数据
				// 不处理
			}
			break;
		}
	}

	private void startGame() {
		// 开始比赛
		if (mGame == null || mGroupA == null || mGroupB == null) {
			showToastShort("没有最新比赛的数据，不能进行比赛");
			return ;
		}
		
		// 选择首发球员
		if (mRoles.contains(1) || mRoles.contains(2)) {// 记录A队或B队数据无需选择首发球员
			selectStartPlayers();
		} else {// 仅记录创新数据，则直接开始
			doStartGame();
		}
	}

	private void doStartGame() {
		
		mCountDown.start();// 计时开始
		mGameTime = System.currentTimeMillis();
		
		mTvGameStart.setVisibility(View.GONE);
		
		if (!running && !pausing) {
			running = true;
			if (isRequiredRecord) {
				mSpUtil.setGameStateRunning(running);
			}
			
			resetCourtPositions();
		
			RecordDb db = new RecordDb(this);
			db.clearGameDataById(mGame.gId);
			
			PlayingTimeDb playingTimeDb = new PlayingTimeDb(this);
			playingTimeDb.clearGameDataById(mGame.gId);
			
			GameTimeDb gameTimeDb = new GameTimeDb(this);
			gameTimeDb.clearGameDataById(mGame.gId);
		}
	}
	
	private void selectStartPlayers() {
		// 选择首发球员
		if (mRoles.contains(1) && mRoles.contains(2)) {
			showToastShort("选择" + mGroupA.groupName+ "首发球员");
			
			SelectPlayersDialog dialog = new SelectPlayersDialog(this, SelectPlayersDialog.MODE_SELECT_STARTS);
			dialog.show();
//			dialog.fillGroupData(mGroupA, mGroupB);
			dialog.fillGroupData(mGroupA);
			dialog.fillPlayersData(mGroupAPlayingMemberList, mGroupAMemberList);
			dialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					showToastShort("选择" + mGroupB.groupName+ "首发球员");
					
					SelectPlayersDialog selectDialog = new SelectPlayersDialog(MainActivity.this, SelectPlayersDialog.MODE_SELECT_STARTS);
					selectDialog.show();
					selectDialog.fillGroupData(mGroupB);
					selectDialog.fillPlayersData(mGroupBPlayingMemberList, mGroupBMemberList);
					selectDialog.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							onPostStartPlayers();
						}
					});
				}
			});
		} else {
			SelectPlayersDialog dialog = new SelectPlayersDialog(this, SelectPlayersDialog.MODE_SELECT_STARTS);
			dialog.show();
//			dialog.fillGroupData(mGroupA, mGroupB);
			if (mRoles.contains(1)) {// 记录A队数据
				showToastShort("选择" + mGroupA.groupName+ "首发球员");
				dialog.fillGroupData(mGroupA);
				dialog.fillPlayersData(mGroupAPlayingMemberList, mGroupAMemberList);
			} else if (mRoles.contains(2)) {// 记录B队数据
				showToastShort("选择" + mGroupB.groupName+ "首发球员");
				dialog.fillGroupData(mGroupB);
				dialog.fillPlayersData(mGroupBPlayingMemberList, mGroupBMemberList);
			} else if (mRoles.contains(3)) {// 记录创新数据
				// 不处理
			}
			dialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					onPostStartPlayers();
				}
			});
		}
		
	}

	private void substitute(int clickPos) {
		// 换人
		
		// 判断当前比赛状态是否允许换人
		boolean allowSubstitute = (running || pausing);// 记录创新数据的角色不允许换人
		if (allowSubstitute) {
//			if (mRole == 1) {// 记录A队数据
//				allowSubstitute = !(mGroupAPlayingMemberList.size() == mGroupAMemberList.size());
//			} else if (mRole == 2) {// 记录B队数据
//				allowSubstitute = !(mGroupBPlayingMemberList.size() == mGroupBMemberList.size());
//			} else if (mRole == 3) {// 记录创新数据
//				// 不处理
//			}
//			
			allowSubstitute = (mRoles.contains(1) || mRoles.contains(2));
			if (allowSubstitute) {
				showSubstituteDialog(clickPos);
			} else {
				showToastShort("记录创新数据员没有权限换人");
			}
		} else {
			showToastShort("比赛尚未开始，不允许换人");
		}
	}

	private void showSubstituteDialog(int clickPos) {
		// 显示换人面板
		if (!pausing) {
			doPauseGame();
		}
		
		// 左边场上球员playerInTheGame，右边整队球员groupMembers
		SelectPlayersDialog dialog = new SelectPlayersDialog(this, SelectPlayersDialog.MODE_SUBSTITUTE);
		dialog.show();
//		dialog.fillGroupData(mGroupA, mGroupB);
		switch (clickPos) {
		case CLICK_POS_LEFT:
			if (mRoles.contains(1)) {// 记录A队数据
				dialog.fillGroupData(mGroupA);
				dialog.fillPlayersData(mGroupAPlayingMemberList, mGroupAMemberList);
			} else if (mRoles.contains(2)) {// 记录B队数据
				dialog.fillGroupData(mGroupB);
				dialog.fillPlayersData(mGroupBPlayingMemberList, mGroupBMemberList);
			} else if (mRoles.contains(3)) {// 记录创新数据
				// 不处理
			}
			break;
		case CLICK_POS_RIGHT:
			if (mRoles.contains(2)) {// 记录B队数据
				dialog.fillGroupData(mGroupB);
				dialog.fillPlayersData(mGroupBPlayingMemberList, mGroupBMemberList);
			} else if (mRoles.contains(1)) {// 记录A队数据
				dialog.fillGroupData(mGroupA);
				dialog.fillPlayersData(mGroupAPlayingMemberList, mGroupAMemberList);
			} else if (mRoles.contains(3)) {// 记录创新数据
				// 不处理
			}
			break;
		}
		dialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				onSavePlayingPlayers();
			}
		});
	}

	protected void onSavePlayingPlayers() {
		// 记录上场球员，在某种特殊条件下，因发生异常导致应用退出，用于重启应用恢复比赛现场
		if (mRoles.contains(1)) {// 记录A队数据
			mSpUtil.setGroupAPlayingMemberList(mGroupAPlayingMemberList);
		} 
		if (mRoles.contains(2)) {// 记录B队数据
			mSpUtil.setGroupBPlayingMemberList(mGroupBPlayingMemberList);
		} 
		if (mRoles.contains(3)) {// 记录创新数据
			// 不处理
		}
	}
	
	private void onRestoreGameState() {
		
		if (mRoles.contains(1)) {
			String memberIds = mSpUtil.getGroupAPlayingMemberList();
			Log.d(TAG, "onRestoreGameState()... GroupA memberIds:" + memberIds);
			
			findPlayingMemberList(memberIds, mGroupAPlayingMemberList, mGroupAMemberList);
		}
		
		if (mRoles.contains(2)) {
			String memberIds = mSpUtil.getGroupBPlayingMemberList();
			Log.d(TAG, "onRestoreGameState()... GroupB memberIds:" + memberIds);
			
			findPlayingMemberList(memberIds, mGroupBPlayingMemberList, mGroupBMemberList);
		}
		
		mQuarterTimeList = mSpUtil.getQuarterTimeList();
		if (mQuarterTimeList.isEmpty()) {
			for (int i = 0; i < mQuarterCount; i++) {
				mQuarterTimeList.add(mQuarterTime);
			}
		}

		int lastGameTime = mSpUtil.getLastGameTime();
		int timeout = getTimeout();// 四节比赛，每节比赛10分钟
		mCountDown = new CountDown(lastGameTime, timeout, 1000);
		mCountDown.setOnCountDownListener(this);
		
		onCountDownIntervalReach(lastGameTime);

		// 恢复比赛现场
		running = mSpUtil.getGameStateRunning();
		pausing = mSpUtil.getGameStatePausing();
		if (running) {
			doStartGame();
		} else if (pausing) {
			doStartGame();
			
			mIvPauseLeft.setImageResource(R.drawable.btn_continue);
			mIvPauseRight.setImageResource(R.drawable.btn_continue);
			mCountDown.setPauseWork(true);
		}
	}

	private void findPlayingMemberList(String memberIds, List<Member> groupPlayingMemberList, List<Member> groupMemberList) {
		String[] ids = memberIds.split(",");
		if (groupPlayingMemberList == null) {
			groupPlayingMemberList = new ArrayList<Member>();
		} else {
			groupPlayingMemberList.clear();
		}
		if (groupMemberList != null && ids.length > 0) {
			Arrays.sort(ids);
			for (Member member : groupMemberList) {
				int index = Arrays.binarySearch(ids, String.valueOf(member.memberId));
				if (index >= 0) {
					groupPlayingMemberList.add(member);
				}
			}
		}
	}

	private void pauseGame() {
		// 暂停比赛
		
		// 判断当前比赛状态是否允许请求暂停比赛, 比如A队暂停已经用完，或者出现事故临时暂停比赛
		boolean allowPause = running;
		if (allowPause) {
			showPauseGameDialog();
		} else {
			showToastShort("比赛尚未开始，不允许暂停比赛");
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
		
		running = true;
		pausing = false;
		
		if (isRequiredRecord) {
			mSpUtil.setGameStateRunning(running);
			mSpUtil.setGameStatePausing(pausing);
		}
		
		mIvPauseLeft.setImageResource(R.drawable.btn_pause);
		mIvPauseRight.setImageResource(R.drawable.btn_pause);
		
		mCountDown.setPauseWork(false);
		
		mGameTime = System.currentTimeMillis();
		
		// 记录比赛开始时间
		GameTimeDb gameTimeDb = new GameTimeDb(getActivity());
		
		// 记录首发球员上场时间
		PlayingTimeDb playingTimeDb = new PlayingTimeDb(getActivity());
		
		if (mRoles.contains(1)) {// 记录A队数据
			gameTimeDb.startOrContinueGame(mGame, mGroupA, mGameTime);
			playingTimeDb.startOrContinueGame(mGame, mGroupA, mGroupAPlayingMemberList, mGameTime);
		} 
		if (mRoles.contains(2)) {// 记录B队数据
			gameTimeDb.startOrContinueGame(mGame, mGroupB, mGameTime);
			playingTimeDb.startOrContinueGame(mGame, mGroupB, mGroupBPlayingMemberList, mGameTime);
		}
		if (mRoles.contains(3)) {// 记录创新数据
			// 无需处理
		}
	}

	private void updateGameRecord() {
		// 更新比赛记录，显示统计信息表
//		boolean allowUpdate = running || pausing;
//		if (!allowUpdate) {
//			showToastShort("比赛尚未开始，不允许显示统计列表");
//			return;
//		}
		
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
		List<Record> recordList = initCourtRecord();
		List<DataStat> list = initDataStat(recordList);
		
		DataStatDialog dialog = new DataStatDialog(this, list);
		dialog.show();
	}

	private List<DataStat> initDataStat(List<Record> recordList) {
		// 队记录次数map
		Map<Long, Map<Integer, Integer>> mGroupActionMap = new HashMap<Long, Map<Integer, Integer>>();
		// 成员记录次数map
		Map<Long, Map<Integer, Integer>> mMemberActionMap = new HashMap<Long, Map<Integer, Integer>>();
		
		for (Record record : recordList) {
			Long groupId = Long.valueOf(record.groupId);
			
			Map<Integer, Integer> groupActionCountMap = mGroupActionMap.get(groupId);
			if (groupActionCountMap == null) {
				groupActionCountMap = new HashMap<Integer, Integer>();
			}
			
			Integer groupActionCount = groupActionCountMap.get((int)record.actionId);
			if (groupActionCount == null) {
				groupActionCount = 1;
			} else {
				groupActionCount ++;
			}
			groupActionCountMap.put((int)record.actionId, groupActionCount);
			
			mGroupActionMap.put(groupId, groupActionCountMap);
			
			
			Map<Integer, Integer> memberActionCountMap = mMemberActionMap.get(record.memberId);
			if (memberActionCountMap == null) {
				memberActionCountMap = new HashMap<Integer, Integer>();
			}
			
			Integer memberActionCount = memberActionCountMap.get((int)record.actionId);
			if (memberActionCount == null) {
				memberActionCount = 1;
			} else {
				memberActionCount ++;
			}
			memberActionCountMap.put((int)record.actionId, memberActionCount);
			
			mMemberActionMap.put(record.memberId, memberActionCountMap);
		}
		
		List<DataStat> list = new ArrayList<DataStat>();
		
		List<String> groupColumns = Arrays.asList(Constants.GROUP_DATA_STAT_COLUMNS);
		List<String> memberColumns = Arrays.asList(Constants.MEMBER_DATA_STAT_COLUMNS);
		List<String> innovateColumns = Arrays.asList(Constants.INNOVATE_DATA_STAT_COLUMNS);
		
		List<Member> groupAMemberList = filterMember(mGroupAMemberList);
		List<Member> groupBMemberList = filterMember(mGroupBMemberList);
		
		// 球队统计标题
		DataStat groupDataStatTitle = new DataStat();
		groupDataStatTitle.type = DataStatDialog.TYPE_TITLE;
		groupDataStatTitle.dataList = new ArrayList<String>();
		groupDataStatTitle.dataList.add("球队统计");
		list.add(groupDataStatTitle);
		
		// 球队统计列名
		// 总得分 总出手命中次数（不含罚球） 总出手次数（不含罚球） 总命中率（总命中率中不含罚球命中率） 2分球命中次数 2分球出手次数 2分球命中率 3分球命中次数 3分球出手次数 3分球命中率 罚球命中次数 罚球出手次数 罚球命中率 前场篮板 后场篮板 总篮板 助攻 抢断 封盖 被犯规 犯规 失误
		DataStat groupDataStatColumn = new DataStat();
		groupDataStatColumn.type = DataStatDialog.TYPE_COLUMN;
		groupDataStatColumn.dataList = groupColumns;
		list.add(groupDataStatColumn);
		
		// 球队统计列值
		addGroupDataStatContent(mGroupActionMap, list, mGroupA);// A队球员统计列值
		addGroupDataStatContent(mGroupActionMap, list, mGroupB);// B队球员统计列值
		
		// 球员统计标题
		DataStat memberDataStatTitle = new DataStat();
		memberDataStatTitle.type = DataStatDialog.TYPE_TITLE;
		memberDataStatTitle.dataList = new ArrayList<String>();
		memberDataStatTitle.dataList.add("球员统计");
		list.add(memberDataStatTitle);
		
		// 球员统计列名
		// 总得分 总出手命中次数（不含罚球） 总出手次数（不含罚球） 总命中率（总命中率中不含罚球命中率） 2分球命中次数 2分球出手次数 2分球命中率 3分球命中次数 3分球出手次数 3分球命中率 罚球命中次数 罚球出手次数 罚球命中率 前场篮板 后场篮板 总篮板 助攻 抢断 封盖 被犯规 犯规 失误 上场时间
		DataStat memberDataStatColumn = new DataStat();
		memberDataStatColumn.type = DataStatDialog.TYPE_COLUMN;
		memberDataStatColumn.dataList = memberColumns;
		list.add(memberDataStatColumn);
		
		// 球员统计列值
		addMemberDataStatContent(mMemberActionMap, list, groupAMemberList, mGroupA);// A队球员统计列值
		addMemberDataStatContent(mMemberActionMap, list, groupBMemberList, mGroupB);// B队球员统计列值
		
		// 创新数据标题
		DataStat innovateDataStatTitle = new DataStat();
		innovateDataStatTitle.type = DataStatDialog.TYPE_TITLE;
		innovateDataStatTitle.dataList = new ArrayList<String>();
		innovateDataStatTitle.dataList.add("创新数据");
		list.add(innovateDataStatTitle);
		
		// 创新数据列名
		// 一条龙，超远三分，绝杀，最后三秒得分，晃倒，2+1,3+1，扣篮，快攻，2罚不中，三罚不中，被晃倒
		DataStat innovateDataStatColumn = new DataStat();
		innovateDataStatColumn.type = DataStatDialog.TYPE_COLUMN;
		innovateDataStatColumn.dataList = innovateColumns;
		list.add(innovateDataStatColumn);
		
		// 创新数据列值
		addInnovateDataStatContent(mMemberActionMap, list, groupAMemberList, mGroupA);// A队球员创新数据列值
		addInnovateDataStatContent(mMemberActionMap, list, groupBMemberList, mGroupB);// B队球员创新数据列值
		
		return list;
	}
	
	private void addGroupDataStatContent(Map<Long, Map<Integer, Integer>> mGroupActionMap, List<DataStat> list, Group group) {
		
		// A队球员创新数据列值
		List<String> dataList = new ArrayList<String>();
		// A队队名
		dataList.add(group.groupName);
		
		// 总得分 总出手命中次数（不含罚球） 总出手次数（不含罚球） 总命中率（总命中率中不含罚球命中率） 2分球命中次数 2分球出手次数 2分球命中率 3分球命中次数 3分球出手次数 3分球命中率 罚球命中次数 罚球出手次数 罚球命中率 前场篮板 后场篮板 总篮板 助攻 抢断 封盖 被犯规 犯规 失误
		Map<Integer, Integer> actionCountMap = mGroupActionMap.get(group.groupId);
		if (actionCountMap != null) {
		
			int dichotomyHitCount = actionCountMap.containsKey(1) ? actionCountMap.get(1) : 0;// 2分球命中次数
			int dichotomyMissCount = actionCountMap.containsKey(2) ? actionCountMap.get(2) : 0;// 2分球不中次数
			int dichotomyShootCount = dichotomyHitCount + dichotomyMissCount;// 2分球出手次数
			float dichotomyHitRate = ((float) dichotomyHitCount / (float) dichotomyShootCount) * 100;// 2分球命中率 
			int dichotomyScore = dichotomyHitCount * mActionMap.get(1).score;// 2分球总得分
			
			int trisectionHitCount = actionCountMap.containsKey(3) ? actionCountMap.get(3) : 0;// 3分球命中次数
			int trisectionMissCount = actionCountMap.containsKey(4) ? actionCountMap.get(4) : 0;// 3分球不中次数
			int trisectionShootCount = trisectionHitCount + trisectionMissCount;// 3分球出手次数
			float trisectionHitRate = ((float) trisectionHitCount / (float) trisectionShootCount) * 100;// 3分球命中率 
			int trisectionScore = trisectionHitCount * mActionMap.get(3).score;// 3分球总得分
			
			int penaltyHitCount = actionCountMap.containsKey(5) ? actionCountMap.get(5) : 0;// 罚球命中次数
			int penaltyMissCount = actionCountMap.containsKey(6) ? actionCountMap.get(6) : 0;// 罚球命中次数
			int penaltyShootCount = penaltyHitCount + penaltyMissCount;// 罚球出手次数
			float penaltyHitRate = ((float) penaltyHitCount / (float) penaltyShootCount) * 100;// 罚球命中率 
			int penaltyScore = penaltyHitCount * mActionMap.get(5).score;// 罚球总得分
			
			int totalScore = dichotomyScore + trisectionScore + penaltyScore;// 总得分
			int totalHitCount = dichotomyHitCount + trisectionHitCount;// 总出手命中次数（不含罚球）
			int totalShootCount = dichotomyShootCount + trisectionShootCount;// 总出手次数（不含罚球） 
			float totalHitRate = ((float) totalHitCount / (float) totalShootCount) * 100;// 总命中率（总命中率中不含罚球命中率）  
			
			int offensiveReboundCount = actionCountMap.containsKey(9) ? actionCountMap.get(9) : 0;// 前场篮板
			int defensiveReboundCount = actionCountMap.containsKey(10) ? actionCountMap.get(10) : 0;// 后场篮板 
			int totalReboundCount = offensiveReboundCount + defensiveReboundCount;// 总篮板
			int assistCount = actionCountMap.containsKey(8) ? actionCountMap.get(8) : 0;// 助攻 
			int stealCount = actionCountMap.containsKey(7) ? actionCountMap.get(7) : 0;// 抢断 
			int blockedShotsCount = actionCountMap.containsKey(12) ? actionCountMap.get(12) : 0;// 封盖 
			int fouledCount = actionCountMap.containsKey(14) ? actionCountMap.get(14) : 0;// 被犯规 
			int foulCount = actionCountMap.containsKey(13) ? actionCountMap.get(13) : 0;// 犯规 
			int missCount = actionCountMap.containsKey(11) ? actionCountMap.get(11) : 0;// 失误 
			
			dataList.add(String.valueOf(totalScore));// 总得分 
			dataList.add(String.valueOf(totalHitCount));// 总出手命中次数（不含罚球）
			dataList.add(String.valueOf(totalShootCount));// 总出手次数（不含罚球） 
			dataList.add(GameUtils.formatHitRatePercent(totalHitRate));// 总命中率（总命中率中不含罚球命中率） 
			dataList.add(String.valueOf(dichotomyHitCount));// 2分球命中次数 
			dataList.add(String.valueOf(dichotomyShootCount));// 2分球出手次数 
			dataList.add(GameUtils.formatHitRatePercent(dichotomyHitRate));// 2分球命中率 
			dataList.add(String.valueOf(trisectionHitCount));// 3分球命中次数 
			dataList.add(String.valueOf(trisectionShootCount));// 3分球出手次数 
			dataList.add(GameUtils.formatHitRatePercent(trisectionHitRate));// 3分球命中率 
			dataList.add(String.valueOf(penaltyHitCount));// 罚球命中次数 
			dataList.add(String.valueOf(penaltyShootCount));// 罚球出手次数 
			dataList.add(GameUtils.formatHitRatePercent(penaltyHitRate));// 罚球命中率 
			dataList.add(String.valueOf(offensiveReboundCount));// 前场篮板 
			dataList.add(String.valueOf(defensiveReboundCount));// 后场篮板 
			dataList.add(String.valueOf(totalReboundCount));// 总篮板 
			dataList.add(String.valueOf(assistCount));// 助攻 
			dataList.add(String.valueOf(stealCount));// 抢断 
			dataList.add(String.valueOf(blockedShotsCount));// 封盖 
			dataList.add(String.valueOf(fouledCount));// 被犯规 
			dataList.add(String.valueOf(foulCount));// 犯规 
			dataList.add(String.valueOf(missCount));// 失误 
		} else {

			dataList.add("0");// 总得分 
			dataList.add("0");// 总出手命中次数（不含罚球）
			dataList.add("0");// 总出手次数（不含罚球） 
			dataList.add("0%");// 总命中率（总命中率中不含罚球命中率） 
			dataList.add("0");// 2分球命中次数 
			dataList.add("0");// 2分球出手次数 
			dataList.add("0%");// 2分球命中率 
			dataList.add("0");// 3分球命中次数 
			dataList.add("0");// 3分球出手次数 
			dataList.add("0%");// 3分球命中率 
			dataList.add("0");// 罚球命中次数 
			dataList.add("0");// 罚球出手次数 
			dataList.add("0%");// 罚球命中率 
			dataList.add("0");// 前场篮板 
			dataList.add("0");// 后场篮板 
			dataList.add("0");// 总篮板 
			dataList.add("0");// 助攻 
			dataList.add("0");// 抢断 
			dataList.add("0");// 封盖 
			dataList.add("0");// 被犯规 
			dataList.add("0");// 犯规 
			dataList.add("0");// 失误 
		}
		
		DataStat innovateDataStatContent = new DataStat();
		innovateDataStatContent.type = DataStatDialog.TYPE_CONTENT;
		innovateDataStatContent.dataList = dataList;
		list.add(innovateDataStatContent);
	}
	
	private void addMemberDataStatContent(Map<Long, Map<Integer, Integer>> mMemberActionMap, List<DataStat> list, List<Member> memberList, Group group) {
		
		PlayingTimeDb db = new PlayingTimeDb(this);
		List<PlayingTime> playingTimeList = db.getGroupMemberPlayingTime(mGame, group);
		Map<Long, Long> playingTimeMap = new HashMap<Long, Long>();
		for (PlayingTime playingTime : playingTimeList) {
			String endTime = playingTime.endTime;
			if (TextUtils.isEmpty(endTime)) {
				endTime = String.valueOf(System.currentTimeMillis());
			}
			
			String startTime = playingTime.startTime;
			if (TextUtils.isEmpty(startTime)) {
				startTime = String.valueOf(System.currentTimeMillis());
			}
			
			Long period = Long.parseLong(endTime) - Long.parseLong(startTime);
			
			Long time = playingTimeMap.get(playingTime.memberId);
			time = time == null ? (time = period) : (time = time + period);
			playingTimeMap.put(playingTime.memberId, time);
		}
		
		// 某队球员创新数据列值
		for (Member member : memberList) {
			List<String> dataList = new ArrayList<String>();
			// 某队队名
			dataList.add(group.groupName);
			dataList.add(member.name);
			
			String playingTime = GameUtils.formatPlayingTime(playingTimeMap.get(member.memberId));// 上场时间
			
			// 总得分 总出手命中次数（不含罚球） 总出手次数（不含罚球） 总命中率（总命中率中不含罚球命中率） 2分球命中次数 2分球出手次数 2分球命中率 3分球命中次数 3分球出手次数 3分球命中率 罚球命中次数 罚球出手次数 罚球命中率 前场篮板 后场篮板 总篮板 助攻 抢断 封盖 被犯规 犯规 失误 上场时间
			Map<Integer, Integer> actionCountMap = mMemberActionMap.get(member.memberId);
			if (actionCountMap != null) {
				
				int dichotomyHitCount = actionCountMap.containsKey(1) ? actionCountMap.get(1) : 0;// 2分球命中次数
				int dichotomyMissCount = actionCountMap.containsKey(2) ? actionCountMap.get(2) : 0;// 2分球不中次数
				int dichotomyShootCount = dichotomyHitCount + dichotomyMissCount;// 2分球出手次数
				float dichotomyHitRate = ((float) dichotomyHitCount / (float) dichotomyShootCount) * 100;// 2分球命中率 
				int dichotomyScore = dichotomyHitCount * mActionMap.get(1).score;// 2分球总得分
				
				int trisectionHitCount = actionCountMap.containsKey(3) ? actionCountMap.get(3) : 0;// 3分球命中次数
				int trisectionMissCount = actionCountMap.containsKey(4) ? actionCountMap.get(4) : 0;// 3分球不中次数
				int trisectionShootCount = trisectionHitCount + trisectionMissCount;// 3分球出手次数
				float trisectionHitRate = ((float) trisectionHitCount / (float) trisectionShootCount) * 100;// 3分球命中率 
				int trisectionScore = trisectionHitCount * mActionMap.get(3).score;// 3分球总得分
				
				int penaltyHitCount = actionCountMap.containsKey(5) ? actionCountMap.get(5) : 0;// 罚球命中次数
				int penaltyMissCount = actionCountMap.containsKey(6) ? actionCountMap.get(6) : 0;// 罚球命中次数
				int penaltyShootCount = penaltyHitCount + penaltyMissCount;// 罚球出手次数
				float penaltyHitRate = ((float) penaltyHitCount / (float) penaltyShootCount) * 100;// 罚球命中率 
				int penaltyScore = penaltyHitCount * mActionMap.get(5).score;// 罚球总得分
				
				int totalScore = dichotomyScore + trisectionScore + penaltyScore;// 总得分
				int totalHitCount = dichotomyHitCount + trisectionHitCount;// 总出手命中次数（不含罚球）
				int totalShootCount = dichotomyShootCount + trisectionShootCount;// 总出手次数（不含罚球） 
				float totalHitRate = ((float) totalHitCount / (float) totalShootCount) * 100;// 总命中率（总命中率中不含罚球命中率）  
				
				int offensiveReboundCount = actionCountMap.containsKey(9) ? actionCountMap.get(9) : 0;// 前场篮板
				int defensiveReboundCount = actionCountMap.containsKey(10) ? actionCountMap.get(10) : 0;// 后场篮板 
				int totalReboundCount = offensiveReboundCount + defensiveReboundCount;// 总篮板
				int assistCount = actionCountMap.containsKey(8) ? actionCountMap.get(8) : 0;// 助攻 
				int stealCount = actionCountMap.containsKey(7) ? actionCountMap.get(7) : 0;// 抢断 
				int blockedShotsCount = actionCountMap.containsKey(12) ? actionCountMap.get(12) : 0;// 封盖 
				int fouledCount = actionCountMap.containsKey(14) ? actionCountMap.get(14) : 0;// 被犯规 
				int foulCount = actionCountMap.containsKey(13) ? actionCountMap.get(13) : 0;// 犯规 
				int missCount = actionCountMap.containsKey(11) ? actionCountMap.get(11) : 0;// 失误 
				
				dataList.add(String.valueOf(totalScore));// 总得分 
				dataList.add(String.valueOf(totalHitCount));// 总出手命中次数（不含罚球）
				dataList.add(String.valueOf(totalShootCount));// 总出手次数（不含罚球） 
				dataList.add(GameUtils.formatHitRatePercent(totalHitRate));// 总命中率（总命中率中不含罚球命中率） 
				dataList.add(String.valueOf(dichotomyHitCount));// 2分球命中次数 
				dataList.add(String.valueOf(dichotomyShootCount));// 2分球出手次数 
				dataList.add(GameUtils.formatHitRatePercent(dichotomyHitRate));// 2分球命中率 
				dataList.add(String.valueOf(trisectionHitCount));// 3分球命中次数 
				dataList.add(String.valueOf(trisectionShootCount));// 3分球出手次数 
				dataList.add(GameUtils.formatHitRatePercent(trisectionHitRate));// 3分球命中率 
				dataList.add(String.valueOf(penaltyHitCount));// 罚球命中次数 
				dataList.add(String.valueOf(penaltyShootCount));// 罚球出手次数 
				dataList.add(GameUtils.formatHitRatePercent(penaltyHitRate));// 罚球命中率 
				dataList.add(String.valueOf(offensiveReboundCount));// 前场篮板 
				dataList.add(String.valueOf(defensiveReboundCount));// 后场篮板 
				dataList.add(String.valueOf(totalReboundCount));// 总篮板 
				dataList.add(String.valueOf(assistCount));// 助攻 
				dataList.add(String.valueOf(stealCount));// 抢断 
				dataList.add(String.valueOf(blockedShotsCount));// 封盖 
				dataList.add(String.valueOf(fouledCount));// 被犯规 
				dataList.add(String.valueOf(foulCount));// 犯规 
				dataList.add(String.valueOf(missCount));// 失误 
			} else {
				dataList.add("0");// 总得分 
				dataList.add("0");// 总出手命中次数（不含罚球）
				dataList.add("0");// 总出手次数（不含罚球） 
				dataList.add("0%");// 总命中率（总命中率中不含罚球命中率） 
				dataList.add("0");// 2分球命中次数 
				dataList.add("0");// 2分球出手次数 
				dataList.add("0%");// 2分球命中率 
				dataList.add("0");// 3分球命中次数 
				dataList.add("0");// 3分球出手次数 
				dataList.add("0%");// 3分球命中率 
				dataList.add("0");// 罚球命中次数 
				dataList.add("0");// 罚球出手次数 
				dataList.add("0%");// 罚球命中率 
				dataList.add("0");// 前场篮板 
				dataList.add("0");// 后场篮板 
				dataList.add("0");// 总篮板 
				dataList.add("0");// 助攻 
				dataList.add("0");// 抢断 
				dataList.add("0");// 封盖 
				dataList.add("0");// 被犯规 
				dataList.add("0");// 犯规 
				dataList.add("0");// 失误 
			}
			
			dataList.add(playingTime);// 上场时间
			
			DataStat memberDataStatContent = new DataStat();
			memberDataStatContent.type = DataStatDialog.TYPE_CONTENT;
			memberDataStatContent.dataList = dataList;
			list.add(memberDataStatContent);
		}
	}

	private void addInnovateDataStatContent(
			Map<Long, Map<Integer, Integer>> mMemberActionMap, List<DataStat> list, List<Member> memberList, Group group) {
		// A队球员创新数据列值
		for (Member member : memberList) {
			List<String> dataList = new ArrayList<String>();
			// A队队名
			dataList.add(group.groupName);
			dataList.add(member.name);
			
			Map<Integer, Integer> actionCountMap = mMemberActionMap.get(member.memberId);
			if (actionCountMap != null) {
				dataList.add(actionCountMap.get(16) != null ? actionCountMap.get(16).toString() : "0");// 一条龙 
				dataList.add(actionCountMap.get(17) != null ? actionCountMap.get(17).toString() : "0");// 超远三分
				dataList.add(actionCountMap.get(18) != null ? actionCountMap.get(18).toString() : "0");// 绝杀
				dataList.add(actionCountMap.get(19) != null ? actionCountMap.get(19).toString() : "0");// 最后三秒得分
				dataList.add(actionCountMap.get(20) != null ? actionCountMap.get(20).toString() : "0");// 晃倒
				dataList.add(actionCountMap.get(21) != null ? actionCountMap.get(21).toString() : "0");// 2+1
				dataList.add(actionCountMap.get(22) != null ? actionCountMap.get(22).toString() : "0");// 3+1
				dataList.add(actionCountMap.get(23) != null ? actionCountMap.get(23).toString() : "0");// 扣篮
				dataList.add(actionCountMap.get(24) != null ? actionCountMap.get(24).toString() : "0");// 快攻
				dataList.add(actionCountMap.get(25) != null ? actionCountMap.get(25).toString() : "0");// 2罚不中
				dataList.add(actionCountMap.get(26) != null ? actionCountMap.get(26).toString() : "0");// 三罚不中
				dataList.add(actionCountMap.get(27) != null ? actionCountMap.get(27).toString() : "0");// 被晃倒
			} else {
				dataList.add("0");// 一条龙 
				dataList.add("0");// 超远三分
				dataList.add("0");// 绝杀
				dataList.add("0");// 最后三秒得分
				dataList.add("0");// 晃倒
				dataList.add("0");// 2+1
				dataList.add("0");// 3+1
				dataList.add("0");// 扣篮
				dataList.add("0");// 快攻
				dataList.add("0");// 2罚不中
				dataList.add("0");// 三罚不中
				dataList.add("0");// 被晃倒
			}
			
			DataStat innovateDataStatContent = new DataStat();
			innovateDataStatContent.type = DataStatDialog.TYPE_CONTENT;
			innovateDataStatContent.dataList = dataList;
			list.add(innovateDataStatContent);
		}
	}
	
	private void requestNewGameData() {
		// 请求新的比赛数据
//		if (running || pausing) {
//			showToastShort("比赛正在进行，不允许获取新的比赛数据");
//		} else {
			requestGameData();
//		}
	}

	private void logout() {
		// 退出登录
		ConfirmDialog dialog = new ConfirmDialog(this, "是否退出登录，数据将被清除？", new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				doLogout();
			}
		});
		dialog.show();
	}

	private void doLogout() {
		// 清除数据
		isRequiredRecord = false;
		
		mSpUtil.logout();
		
		if (mCountDown != null) {
			mCountDown.stop();
		}
		
		GameDb gameDb = new GameDb(this);
		gameDb.clearAllData();
		
		GroupDb groupDb = new GroupDb(this);
		groupDb.clearAllData();
		
		MemberDb memberDb = new MemberDb(this);
		memberDb.clearAllData();
		
		RecordDb recordDb = new RecordDb(this);
		recordDb.clearAllData();
		
		PlayingTimeDb playingTimeDb = new PlayingTimeDb(this);
		playingTimeDb.clearAllData();
		
		GameTimeDb gameTimeDb = new GameTimeDb(this);
		gameTimeDb.clearAllData();
		
		IApplication.hasStart = false;
//		finish();
		System.exit(0);
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
			showToastShort("无比赛数据");
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
		
		// 记录A队数据
		RoleRecord groupARecord = getRoleRecord(mGroupA, 1);
		records.add(groupARecord);
			
		// 记录B队数据
		RoleRecord groupBRecord = getRoleRecord(mGroupB, 1);
		records.add(groupBRecord);
		
		if (mRoles.contains(3)) {// 记录创新数据
			RoleRecord groupANewRecord = getRoleRecord(mGroupA, 2);
			records.add(groupANewRecord);
			RoleRecord groupBNewRecord = getRoleRecord(mGroupB, 2);
			records.add(groupBNewRecord);
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
	private RoleRecord getRoleRecord(Group group, int recordType) {
		RoleRecord roleRecord = new RoleRecord();
		roleRecord.game_id = mGame.gId;
		roleRecord.group_id = group.groupId;
		roleRecord.recordType = recordType;
		
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
	public void onCountDownTimeout() {
		// 比赛完成
		showToastLong("比赛结束");
		running = false;
		pausing = false;
		
		if (mCountDown == null) {
			mCountDown.reset();
		}

		if (isRequiredRecord ) {
			mSpUtil.setGameStateRunning(running);
			mSpUtil.setGameStatePausing(pausing);
			mSpUtil.setLastGameTime(0);
		}
	}

	@Override
	public void onCountDownIntervalReach(int last) {
		mRunningTime  = last;
		if (isRequiredRecord) {
			mSpUtil.setLastGameTime(last);
		}
		mTvGameTime.setText(GameUtils.formGameTime(last));

		mCurrentQuarter = getQuarter();
		if (isQuarterTime()) {
			if (isGameOver()) {
				onCountDownTimeout();
			} else {// 进入下一节，先暂停
				doPauseGame();
			}
		}
		mTvGameFirstHalf.setText("第" + mCurrentQuarter + "节");
		
		mTvGameFirstHalf.setVisibility(View.VISIBLE);
		mTvGameSecondHalf.setVisibility(View.GONE);
//		if (last > 0 && last <= 20 * 60 * 1000) {
//			mTvGameFirstHalf.setTextColor(getResources().getColor(R.color.game_progress));
//			mTvGameSecondHalf.setTextColor(getResources().getColor(R.color.white));
//		} else if (last > 20 * 60 * 1000) {
//			mTvGameFirstHalf.setTextColor(getResources().getColor(R.color.white));
//			mTvGameSecondHalf.setTextColor(getResources().getColor(R.color.game_progress));
//		} else {
//			mTvGameFirstHalf.setTextColor(getResources().getColor(R.color.white));
//			mTvGameSecondHalf.setTextColor(getResources().getColor(R.color.white));
//		}
	}

	public void updateGroupAScore(int score) {
		if (mGroupAScore + score >= 0) {
			mGroupAScore += score;
		}
		mTvGroupAScore.setText(String.valueOf(mGroupAScore));
	}

	public void updateGroupBScore(int score) {
		if (mGroupBScore + score >= 0) {
			mGroupBScore += score;
		}
		mTvGroupBScore.setText(String.valueOf(mGroupBScore));
	}

	public void setCurrentRecordCoordinate(Action action, String coordinate) {
		String[] split = coordinate.split(",");
		
		if (split.length > 1) {
			int position = Integer.parseInt(split[0]) + Integer.parseInt(split[1]) * 32;
			
			mCourtPositions.set(position, action.type);
			mCourtAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.tv_group_a_score:
		case R.id.tv_group_b_score:
			showModifyGameScoreWindow(v);
			break;
		case R.id.tv_game_time:
			showModifyGameTimeWindow(v);
			break;
		}
		return false;
	}

	private void showModifyGameScoreWindow(final View view) {
		final QuickAction window = new QuickAction(view);
		
		View addTimeView = getLayoutInflater().inflate(R.layout.popup_item, null);
		TextView addTimeText = (TextView) addTimeView.findViewById(R.id.item);
		addTimeText.setText("加一分");
		addTimeText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 加一分
				switch (view.getId()) {
				case R.id.tv_group_a_score:
					updateGroupAScore(1);
					break;
				case R.id.tv_group_b_score:
					updateGroupBScore(1);
					break;
				}
			}
		});
		window.addItem(addTimeView);

		View overTimeView = getLayoutInflater().inflate(R.layout.popup_item, null);
		TextView overTimeText = (TextView) overTimeView.findViewById(R.id.item);
		overTimeText.setText("减一分");
		overTimeText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 减一分
				switch (view.getId()) {
				case R.id.tv_group_a_score:
					updateGroupAScore(-1);
					break;
				case R.id.tv_group_b_score:
					updateGroupBScore(-1);
					break;
				}
			}
		});
		window.addItem(overTimeView);
		
		window.show();
	}

	private void showModifyGameTimeWindow(View v) {
		final QuickAction window = new QuickAction(v);
		
		final boolean enabled = isQuarterTime();
		
		View addTimeView = getLayoutInflater().inflate(R.layout.popup_item, null);
		TextView addTimeText = (TextView) addTimeView.findViewById(R.id.item);
		addTimeText.setText("补足时间");
		addTimeText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				window.dismiss();
				// 补足时间
				
				if (!enabled) {// 允许补足时间
					mQuarterTimeList.set(mCurrentQuarter - 1, mQuarterTimeList.get(mCurrentQuarter - 1) + 1);
					
					restartCountDown();
					
					Toast.makeText(MainActivity.this, "补时成功", Toast.LENGTH_SHORT).show();
				}
			}
		});
		addTimeText.setEnabled(!enabled);
		window.addItem(addTimeView);
		if (enabled) {
			window.onItemSelected(addTimeText);
		}

		boolean overTimeEnabled = isGameOver();
		
		View overTimeView = getLayoutInflater().inflate(R.layout.popup_item, null);
		TextView overTimeText = (TextView) overTimeView.findViewById(R.id.item);
		overTimeText.setText("加时赛");
		overTimeText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				window.dismiss();
				// 加时赛
				mQuarterTimeList.set(mCurrentQuarter - 1, mQuarterTimeList.get(mCurrentQuarter - 1) + 5);
				
				restartCountDown();
				
				Toast.makeText(MainActivity.this, "进入加时赛", Toast.LENGTH_SHORT).show();
			}
		});
		window.addItem(overTimeView);
		overTimeText.setEnabled(overTimeEnabled);
		if (!overTimeEnabled) {
			window.onItemSelected(overTimeText);
		}
		
		window.show();
	}
	
	protected void restartCountDown() {		
		mSpUtil.setQuarterTimeList(mQuarterTimeList);
		
		int timeout = getTimeout();
		mCountDown = new CountDown(mRunningTime, timeout, 1000);
		mCountDown.start();// 计时开始
	}
	
	private int getTimeout() {	
		int timeout = 0;
		for (Integer time : mQuarterTimeList) {
			timeout = timeout + time;
		}
		timeout = timeout * 60 * 1000;// 计算比赛时间
		return timeout;
	}

	private boolean isQuarterTime() {
		boolean isQuartTime = false;
		for (Integer time : mQuarterTimeList) {
			if (mRunningTime == time * 60 * 1000) {
				isQuartTime = true;
				break;
			}
		}
		return isQuartTime;
	}
	
	private boolean isGameOver() {
		int total = 0;
		for (Integer time : mQuarterTimeList) {
			total = total + time;
		}
		return mRunningTime == (total * 60 * 1000);
	}
	
	private int getQuarter() {
		int total = 0;
		for (int i = 0; i < mQuarterTimeList.size(); i++) {
			int time = mQuarterTimeList.get(i) * 60 * 1000;
			total = total + time;
			if (mRunningTime <= total) {
				return i + 1;
			}
		}
		return 1;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		boolean allowRecordEvent = running;
		if (allowRecordEvent) {
			showRecordEventDialog(position);
		} else {
			showToast("仅比赛进行时，才允许记录球员事件");
		}
	}

	private void showRecordEventDialog(int position) {
		// 显示记录球员事件的对话框
		
		// TODO only for test
		if (mGroupAPlayingMemberList.isEmpty()) {
			mGroupAPlayingMemberList = filterMember(mGroupAMemberList);
		}
		if (mGroupBPlayingMemberList.isEmpty()) {
			mGroupBPlayingMemberList = filterMember(mGroupBMemberList);
		}
		
		mGameTime = System.currentTimeMillis();
		
		String coordinate = parseCoordinate(position);
		
		List<Action> actionList = new ArrayList<Action>();
		for (Action action : mActionList) {
			if (action.id != 13 && action.id != 14) {
				actionList.add(action);
			}
		}

		RecordEventDialog dialog = new RecordEventDialog(this, actionList);
		dialog.show();
		dialog.fillGameData(mGame, mRoles, mGameTime, coordinate);
		dialog.fillGroupData(mGroupA, mGroupB);
		dialog.fillPlayersData(mGroupAPlayingMemberList, mGroupBPlayingMemberList);
	}

	private List<Member> filterMember(List<Member> groupMemberList) {
		List<Member> groupPlayingMemberList = new ArrayList<Member>();
		if (groupMemberList != null) {
			for (Member member : groupMemberList) {
				if (member.isLeader <= 1) {//3:教练,2:领队,1:队长,0:队员 
					groupPlayingMemberList.add(member);
				}
			}
		}
		return groupPlayingMemberList;
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
	
	public void doPauseGame() {
		showToastShort("比赛暂停");
		
		running = false;
		pausing = true;
		if (isRequiredRecord) {
			mSpUtil.setGameStateRunning(running);
			mSpUtil.setGameStatePausing(pausing);
		}
		
		mIvPauseLeft.setImageResource(R.drawable.btn_continue);
		mIvPauseRight.setImageResource(R.drawable.btn_continue);
		
		if (mCountDown != null) {
			mCountDown.setPauseWork(true);
		}
		
		mGameTime = System.currentTimeMillis();
		
		// 记录比赛开始时间
		GameTimeDb gameTimeDb = new GameTimeDb(getActivity());
		
		// 记录首发球员上场时间
		PlayingTimeDb plaingTimeDb = new PlayingTimeDb(getActivity());
		
		if (mRoles.contains(1)) {// 记录A队数据
			gameTimeDb.pauseOrEndGame(mGame, mGroupA, mGameTime);
			plaingTimeDb.pauseOrEndGame(mGame, mGroupA, mGroupAPlayingMemberList, mGameTime);
		} 
		if (mRoles.contains(2)) {// 记录B队数据
			gameTimeDb.pauseOrEndGame(mGame, mGroupB, mGameTime);
			plaingTimeDb.pauseOrEndGame(mGame, mGroupB, mGroupBPlayingMemberList, mGameTime);
		}
		if (mRoles.contains(3)) {// 记录创新数据
			// 无需处理
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mWakeLock.acquire();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mWakeLock.release();
	}
	
	@Override
	public void onBackPressed() {
//		super.onBackPressed();
		moveAppToBack();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
//		IApplication.hasStart = false;
		mHomeWatcher.stopWatch();
		
		if (isRequiredRecord) {
			mSpUtil.setGameStateRunning(running);
			mSpUtil.setGameStatePausing(pausing);
		}
	}

	private void moveAppToBack() {
		if (pausing) {// 暂停比赛状态下直接切换到后台运行
			moveTaskToBack(false);
		} else if (running) {// 正在比赛状态下，暂停比赛并切换到后台运行
			doPauseGame();
			moveTaskToBack(false);
		} else {// 直接切换到后台运行
			moveTaskToBack(false);
		}
	}

	private void onPostStartPlayers() {
		showToastShort("比赛开始");
		doStartGame();
		
		// 记录比赛开始时间
		GameTimeDb gameTimeDb = new GameTimeDb(getActivity());
		gameTimeDb.startOrContinueGame(mGame, null, mGameTime);
		
		// 记录首发球员上场时间
		PlayingTimeDb db = new PlayingTimeDb(getActivity());
		if (mRoles.contains(1)) {// 记录A队数据
			db.startOrContinueGame(mGame, mGroupA, mGroupAPlayingMemberList, mGameTime);
		} 
		if (mRoles.contains(2)) {// 记录B队数据
			db.startOrContinueGame(mGame, mGroupB, mGroupBPlayingMemberList, mGameTime);
		} 
		if (mRoles.contains(3)) {// 记录创新数据
			// 不处理
		}
		
		onSavePlayingPlayers();// 记录上场球员，在某种特殊条件下，因发生异常导致应用退出，用于重启应用恢复比赛现场
	}

}
