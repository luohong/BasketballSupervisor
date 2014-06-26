package com.example.basketballsupervisor.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.framework.core.widget.BaseDialog;
import com.android.framework.core.widget.ConfirmDialog;
import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.activity.MainActivity;
import com.example.basketballsupervisor.db.RecordDb;
import com.example.basketballsupervisor.model.Action;
import com.example.basketballsupervisor.model.Game;
import com.example.basketballsupervisor.model.Group;
import com.example.basketballsupervisor.model.Member;

public class RecordEventDialog extends BaseDialog {

	private MainActivity mMainActivity;

	private List<Action> mActionList;

	protected ViewFlipper vFlipper;
	
	private TextView mTvGroupAName, mTvGroupBName;
	private TextView mTvGroupATitle, mTvGroupBTitle;
	private ListView mLvPlaying, mLvBench;

	private View mDivider;
	
	private PlayerAdapter mGroupAPlayingAdapter;
	private PlayerAdapter mGroupBPlayingAdapter;

	private List<Member> mGroupAPlayingMembers;
	private List<Member> mGroupBPlayingMembers;

	protected Member mSelectedMember;
	private int mSelectedGroupAPlayingPos = -1;
	private int mSelectedGroupBPlayingPos = -1;
	
	private TextView mTvPage2Title;
	private GridView mGvPage2Event;
	
	private TextView mTvPage3Title;
	private GridView mGvPage3Event;
	
	private TextView mTvPage4Title;
	private TextView mTvPage4GroupAName, mTvPage4GroupBName;
	private TextView mTvPage4GroupATitle, mTvPage4GroupBTitle;
	private ListView mLvPage4Playing, mLvPage4Bench;
	
	private PlayerAdapter mPage4GroupAPlayingAdapter;
	private PlayerAdapter mPage4GroupBPlayingAdapter;

	protected Action mNextAction;

	private Game mGame;
	private  List<Integer> mRoles;
	private long mGameTime;
	private String mCoordinate;

	private Group mGroupA;
	private Group mGroupB;
	private Group mPreviousSelectedGroup;
	private Group mSelectedGroup;

	private boolean isSetRecordCoordinate;

	private List<Action> step2ActionList;

	public RecordEventDialog(Context context, List<Action> actionList) {
		super(context);
		mMainActivity = (MainActivity) context;
		mActionList = filterAction(actionList);
	}

	private List<Action> filterAction(List<Action> actionList) {
		List<Action> list = new ArrayList<Action>();
		for (Action action : actionList) {
			try {
				Action a = (Action)action.clone();
				// 过滤被犯规，前场篮板
				if (action.id == 9) {
					a.name = "篮板";
				}
				list.add(a);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.dialog_record_event;
	}

	@Override
	protected void onFindViews() {
		
		vFlipper = (ViewFlipper) findViewById(R.id.vf_events);
		
		mTvGroupAName = (TextView) findViewById(R.id.tv_group_a_name);
		mTvGroupBName = (TextView) findViewById(R.id.tv_group_b_name);
		
		mTvGroupATitle = (TextView) findViewById(R.id.tv_group_a_title);
		mTvGroupBTitle = (TextView) findViewById(R.id.tv_group_b_title);
		
		mDivider = findViewById(R.id.divider);
		
		mLvPlaying = (ListView) findViewById(R.id.lv_playing);
		mLvBench = (ListView) findViewById(R.id.lv_bench);
		
		mTvPage2Title = (TextView) findViewById(R.id.tv_page2_title);
		mGvPage2Event = (GridView) findViewById(R.id.gv_page2_event);
		
		mTvPage3Title = (TextView) findViewById(R.id.tv_page3_title);
		mGvPage3Event = (GridView) findViewById(R.id.gv_page3_event);

		mTvPage4Title = (TextView) findViewById(R.id.tv_page4_title);
		
		mTvPage4GroupAName = (TextView) findViewById(R.id.tv_page4_group_a_name);
		mTvPage4GroupBName = (TextView) findViewById(R.id.tv_page4_group_b_name);
		
		mTvPage4GroupATitle = (TextView) findViewById(R.id.tv_page4_group_a_title);
		mTvPage4GroupBTitle = (TextView) findViewById(R.id.tv_page4_group_b_title);
		
		mLvPage4Playing = (ListView) findViewById(R.id.lv_page4_playing);
		mLvPage4Bench = (ListView) findViewById(R.id.lv_page4_bench);
		
	}

	@Override
	protected void onInitViewData() {
		
		// page1		
		mGroupAPlayingAdapter = new PlayerAdapter(getContext(), PlayerAdapter.TEAM_A);
		mLvPlaying.setAdapter(mGroupAPlayingAdapter);

		mGroupBPlayingAdapter = new PlayerAdapter(getContext(), PlayerAdapter.TEAM_B);
		mLvBench.setAdapter(mGroupBPlayingAdapter);
		
		mLvPlaying.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mRoles.contains(1) || mRoles.contains(3)) {// 记录A队数据或创新数据
					mSelectedGroupAPlayingPos = position;
					mGroupAPlayingAdapter.notifyDataSetChanged();
					
					mSelectedGroup = mGroupA;
					
					mSelectedMember = (Member) parent.getItemAtPosition(position);
					String title = mSelectedMember.number + " " + mSelectedMember.name + " 技术统计";
					mTvPage2Title.setText(title);
					
					if (step2ActionList.size() == 1) {// 仅一个选项时直接跳到第三步
						showNext();
						doAction(mActionList.get(0));
					} else {
						showNext();
					}
				} else {
					Toast.makeText(context, "您没有权限记录" + mGroupA.groupName + "的技术数据", Toast.LENGTH_SHORT).show();
				}
			}
		});
		mLvBench.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mRoles.contains(2) || mRoles.contains(3)) {// 记录B队数据或创新数据

					mSelectedGroupBPlayingPos = position;
					mGroupBPlayingAdapter.notifyDataSetChanged();

					mSelectedGroup = mGroupB;
					
					mSelectedMember = (Member) parent.getItemAtPosition(position);
					String title = mSelectedMember.number + " " + mSelectedMember.name + " 技术统计";
					mTvPage2Title.setText(title);
					
					if (step2ActionList.size() == 1) {// 仅一个选项时直接跳到第三步
						showNext();
						doAction(mActionList.get(0));
					} else {
						showNext();
					}
				} else {
					Toast.makeText(context, "您没有权限记录" + mGroupB.groupName + "的技术数据", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		// page2
		step2ActionList = filterActionListByStep(2);
		EventAdapter adapter = new EventAdapter(getContext(), 2, step2ActionList);
		mGvPage2Event.setAdapter(adapter);
		mGvPage2Event.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Action action = (Action) parent.getItemAtPosition(position);
				doAction(action);
			}
		});
		
		// page4		
		mPage4GroupAPlayingAdapter = new PlayerAdapter(getContext(), PlayerAdapter.TEAM_A);
		mLvPage4Playing.setAdapter(mPage4GroupAPlayingAdapter);

		mPage4GroupBPlayingAdapter = new PlayerAdapter(getContext(), PlayerAdapter.TEAM_B);
		mLvPage4Bench.setAdapter(mPage4GroupBPlayingAdapter);
		
		mLvPage4Playing.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				dismiss();
				
				mSelectedGroup = mGroupA;

				mSelectedMember = (Member) parent.getItemAtPosition(position);
				saveRecordEvent(mNextAction);
			}
		});
		mLvPage4Bench.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				dismiss();
				
				mSelectedGroup = mGroupB;
				
				mSelectedMember = (Member) parent.getItemAtPosition(position);
				saveRecordEvent(mNextAction);
			}
		});
	}
	
	@Override
	public void initWindowLayoutParams() {
		Window win = getWindow();
	    WindowManager m = win.getWindowManager();
		DisplayMetrics  dm = new DisplayMetrics();    
	    m.getDefaultDisplay().getMetrics(dm);    
		
	    int width = (int) (dm.widthPixels * 0.6);
	    int height = (int) (dm.heightPixels * 0.8);

		WindowManager.LayoutParams p = win.getAttributes();
		p.width = width;
		p.height = height;
		
	    win.setAttributes(p);
	}

	private void saveRecordEvent(Action action) {
		saveRecordEvent(action, true);
	}
	
	private void saveRecordEvent(Action action, boolean toast) {
		// 记录动作行为
		
		if (mPreviousSelectedGroup != null && mPreviousSelectedGroup != mSelectedGroup && action.id == 9) {
			action.id = 10;// 所有投篮未中的后续篮板选项中应该是两队球员，如果选择的是投篮一方的球员，则记录前场篮板，如果选择的是防守一方的球员，则记录后场篮板。
		}
		
		RecordDb db = new RecordDb(getContext());
		if (mRoles.contains(1) && mSelectedGroup == mGroupA) {// 记录A队数据
			mMainActivity.updateGroupAScore(action.score);
//			db.saveRecord(mGame, mGroupA, mSelectedMember, action, mGameTime, mCoordinate);
		} 
		if (mRoles.contains(2) && mSelectedGroup == mGroupB) {// 记录B队数据
			mMainActivity.updateGroupBScore(action.score);
//			db.saveRecord(mGame, mGroupB, mSelectedMember, action, mGameTime, mCoordinate);
		} 
//		if (mRoles.contains(3) && action.nextActionId == -2) {// 记录创新数据
//			db.saveRecord(mGame, mSelectedGroup, mSelectedMember, action, mGameTime, mCoordinate);
//		}
		db.saveRecord(mGame, mSelectedGroup, mSelectedMember, action, mGameTime, mCoordinate);
		
		if (toast) {
			Toast.makeText(context, "记录成功", Toast.LENGTH_SHORT).show();
		}
		
		// 仅记录首次操作的状态显示在球场上，而非
		if (!isSetRecordCoordinate) {
			isSetRecordCoordinate = true;
			mMainActivity.setCurrentRecordCoordinate(action, mCoordinate);
		}
		
//		if (action.id == 13) {// 犯规行为产生，即停表
//			mMainActivity.doPauseGame();
//		}
	}

	protected void showNextStat() {
		saveRecordEvent(mNextAction, false);
		
		Action action = null;
		for (Action temp : mActionList) {
			if (temp.id == mNextAction.nextActionId) {
				action = temp;
				break;
			}
		}
		mNextAction = action;
		mPreviousSelectedGroup = mSelectedGroup;
		
		mTvPage4Title.setText(mNextAction.name + "球员选项");
		
		mTvPage4GroupAName.setText(mGroupA.groupName);
		mTvPage4GroupBName.setText(mGroupB.groupName);
		
		mPage4GroupAPlayingAdapter.setData(mGroupAPlayingMembers);
		mPage4GroupBPlayingAdapter.setData(mGroupBPlayingMembers);
		
		if (mNextAction.id != 14 && mNextAction.id != 9 && mNextAction.id != 10) {// 非前场篮板，后场篮板，被犯规选择本队球员
			if (mSelectedGroup == mGroupA && mRoles.contains(1)) {// 记录A队数据
				mTvPage4GroupAName.setVisibility(View.VISIBLE);
				mTvPage4GroupATitle.setVisibility(View.VISIBLE);
				
				mTvPage4GroupBName.setVisibility(View.GONE);
				mTvPage4GroupBTitle.setVisibility(View.GONE);
				
				mLvPage4Playing.setVisibility(View.VISIBLE);
				mLvPage4Bench.setVisibility(View.GONE);
			} else if (mSelectedGroup == mGroupB && mRoles.contains(2)) {// 记录B队数据
				mTvPage4GroupAName.setVisibility(View.GONE);
				mTvPage4GroupATitle.setVisibility(View.GONE);
				
				mTvPage4GroupBName.setVisibility(View.VISIBLE);
				mTvPage4GroupBTitle.setVisibility(View.VISIBLE);
				
				mLvPage4Playing.setVisibility(View.GONE);
				mLvPage4Bench.setVisibility(View.VISIBLE);
			}
		} else if (mNextAction.id == 14) {// 仅被犯规选择对方球员
			if (mSelectedGroup == mGroupA && mRoles.contains(1)) {// 记录A队数据
				mTvPage4GroupAName.setVisibility(View.GONE);
				mTvPage4GroupATitle.setVisibility(View.GONE);
				
				mTvPage4GroupBName.setVisibility(View.VISIBLE);
				mTvPage4GroupBTitle.setVisibility(View.VISIBLE);
				
				mLvPage4Playing.setVisibility(View.GONE);
				mLvPage4Bench.setVisibility(View.VISIBLE);
			} else if (mSelectedGroup == mGroupB && mRoles.contains(2)) {// 记录B队数据
				mTvPage4GroupAName.setVisibility(View.VISIBLE);
				mTvPage4GroupATitle.setVisibility(View.VISIBLE);
				
				mTvPage4GroupBName.setVisibility(View.GONE);
				mTvPage4GroupBTitle.setVisibility(View.GONE);
				
				mLvPage4Playing.setVisibility(View.VISIBLE);
				mLvPage4Bench.setVisibility(View.GONE);
			}
		} else {// 仅前场篮板，后场篮板选择双方球员
			mTvPage4GroupBName.setVisibility(View.VISIBLE);
			mTvPage4GroupBTitle.setVisibility(View.VISIBLE);
			
			mLvPage4Playing.setVisibility(View.VISIBLE);
			mLvPage4Bench.setVisibility(View.VISIBLE);
			
			mTvPage4GroupAName.setText(mGroupA.groupName);
			mTvPage4GroupBName.setText(mGroupB.groupName);
			
			mPage4GroupAPlayingAdapter.setData(mGroupAPlayingMembers);
			mPage4GroupBPlayingAdapter.setData(mGroupBPlayingMembers);
			
//			if (mSelectedGroup == mGroupA && mRoles.contains(1)) {// 记录A队数据
//				mTvPage4GroupAName.setText(mGroupB.groupName);
//				mPage4GroupAPlayingAdapter.setData(mGroupBPlayingMembers);
//			} else if (mSelectedGroup == mGroupB && mRoles.contains(2)) {// 记录B队数据
//				mTvPage4GroupAName.setText(mGroupA.groupName);
//				mPage4GroupAPlayingAdapter.setData(mGroupAPlayingMembers);
//			}
		}
		
		mSelectedGroupAPlayingPos = -1;
		mSelectedGroupBPlayingPos = -1;
		
		showNext();
		showNext();
	}

	private void showNewStat() {
		// page3
		List<Action> step3ActionList = filterActionListByStep(3);
		EventAdapter page3Adapter = new EventAdapter(getContext(), 3, step3ActionList);
		mGvPage3Event.setAdapter(page3Adapter);
		mGvPage3Event.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				dismiss();
				
				// 记录动作行为
				Action action = (Action) parent.getItemAtPosition(position);
				saveRecordEvent(action);
			}
		});
		
		String title = mSelectedMember.number + " " + mSelectedMember.name + " 创新统计";
		mTvPage3Title.setText(title);
		
		showNext();
	}
	
	private List<Action> filterActionListByStep(int step) {
		
		List<Action> stepActionList = new ArrayList<Action>();
		if (step == 2) {
			for (Action action : mActionList) {
				if (action.nextActionId >= -1) {
					stepActionList.add(action);
				}
			}
		} else {
			for (Action action : mActionList) {
				if (action.nextActionId < mNextAction.nextActionId && action.nextActionId > -10 && mNextAction.nextActionId == -1) {
					stepActionList.add(action);
				}
			}
		}
		
		return stepActionList;
	}

	protected void showConfirmSubstitudeDialog() {
		// 检查是否符合换人的条件
		
		if (mSelectedGroupAPlayingPos >= 0 && mSelectedGroupBPlayingPos >= 0) {
			ConfirmDialog dialog = new ConfirmDialog(context, "确认换人？", true, "取消", "确认", new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					dismiss();
					// 记录球员上场时间
				}
			}, new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					dismiss();
					
					mSelectedGroupAPlayingPos = -1;
					mSelectedGroupBPlayingPos = -1;

					mGroupAPlayingAdapter.notifyDataSetChanged();
					mGroupBPlayingAdapter.notifyDataSetChanged();
				}
			});
			
			dialog.show();
		}
	}
	
	public void fillGameData(Game game, List<Integer> roles, long time, String coordinate) {
		mGame = game;
		mRoles = roles;
		mGameTime = time;
		mCoordinate = coordinate;
	}

	public void fillGroupData(Group groupA, Group groupB) {
		mGroupA = groupA;
		mGroupB = groupB;
			
		if (mRoles.contains(3)) {// 记录创新数据
			mTvGroupAName.setVisibility(View.VISIBLE);
			mTvGroupATitle.setVisibility(View.VISIBLE);
			mDivider.setVisibility(View.VISIBLE);
			mTvGroupBName.setVisibility(View.VISIBLE);
			mTvGroupBTitle.setVisibility(View.VISIBLE);
		} else {
			if (mRoles.contains(1)) {// 记录A队数据
				mTvGroupAName.setVisibility(View.VISIBLE);
				mTvGroupATitle.setVisibility(View.VISIBLE);
			} else {
				mTvGroupAName.setVisibility(View.GONE);
				mTvGroupATitle.setVisibility(View.GONE);
			}
			
			if (mRoles.contains(2)) {// 记录B队数据
				mTvGroupBName.setVisibility(View.VISIBLE);
				mTvGroupBTitle.setVisibility(View.VISIBLE);
			} else {
				mTvGroupBName.setVisibility(View.GONE);
				mTvGroupBTitle.setVisibility(View.GONE);
			}
			
			if ((mRoles.contains(1) && mRoles.contains(2))) {
				mDivider.setVisibility(View.VISIBLE);
			} else {
				mDivider.setVisibility(View.GONE);
			}
		}
		
		mTvGroupAName.setText(mGroupA.groupName);
		mTvGroupBName.setText(mGroupB.groupName);
	}
	
	public void fillPlayersData(List<Member> groupAPlayingMembers, List<Member> groupBPlayingMembers) {
		mGroupAPlayingMembers = groupAPlayingMembers;
		mGroupBPlayingMembers = groupBPlayingMembers;

		if (mRoles.contains(3)) {// 记录创新数据
			mLvPlaying.setVisibility(View.VISIBLE);
			mLvBench.setVisibility(View.VISIBLE);
			mGroupAPlayingAdapter.setData(mGroupAPlayingMembers);
			mGroupBPlayingAdapter.setData(mGroupBPlayingMembers);
		} else {
			if (mRoles.contains(1)) {// 记录A队数据
				mLvPlaying.setVisibility(View.VISIBLE);
				mGroupAPlayingAdapter.setData(mGroupAPlayingMembers);
			} else {
				mLvPlaying.setVisibility(View.GONE);
			}
			
			if (mRoles.contains(2)) {// 记录B队数据
				mLvBench.setVisibility(View.VISIBLE);
				mGroupBPlayingAdapter.setData(mGroupBPlayingMembers);
			} else {
				mLvBench.setVisibility(View.GONE);
			}
		}
	}

	protected void showPrevious() {
		vFlipper.setInAnimation(getContext(), R.anim.left_in);
		vFlipper.setOutAnimation(getContext(), R.anim.right_out);
		vFlipper.showPrevious();
	}

	protected void showNext() {
		vFlipper.setInAnimation(getContext(), R.anim.right_in);
		vFlipper.setOutAnimation(getContext(), R.anim.left_out);
		vFlipper.showNext();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		int displayChild = vFlipper.getDisplayedChild();
		if (displayChild == 0) {
			dismiss();
		} else if (displayChild == 1) {
			showPrevious();
		}
	}
	
	private void doAction(Action action) {
		if (action.nextActionId == 0) {
			if (!mRoles.contains(1) && !mRoles.contains(2)) {// 仅记录创新数据
				Toast.makeText(context, "您没有权限记录技术数据", Toast.LENGTH_SHORT).show();
			} else {
				dismiss();
				
				saveRecordEvent(action);
			}
		} else {
			mNextAction = action;
			
			if (mNextAction.nextActionId == -1) {
				if (mRoles.contains(3)) {// 记录创新数据
					showNewStat();
				} else {
					Toast.makeText(context, "您没有权限记录创新数据", Toast.LENGTH_SHORT).show();
				}
			} else if (mNextAction.nextActionId > 0) {
				if (!mRoles.contains(1) && !mRoles.contains(2)) {// 记录创新数据
					Toast.makeText(context, "您没有权限记录技术数据", Toast.LENGTH_SHORT).show();
				} else {
					showNextStat();
				}
			}
		}
	}

	private class PlayerAdapter extends BaseAdapter {
		
		public static final int TEAM_A = 0;
		public static final int TEAM_B = 1;
		
		private LayoutInflater mInflater;

		private List<Member> members;
		private int mTeam;

		public PlayerAdapter(Context context, int team) {
			this.mTeam = team;
			mInflater = LayoutInflater.from(context);
		}
		
		public void setData(List<Member> members) {
			this.members = members;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return members != null ? members.size() : 0;
		}

		@Override
		public Object getItem(int arg0) {
			return members.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder holder = null;
			
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_select_players, parent, false);
				holder = new ViewHolder(convertView);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			Member member = (Member) getItem(position);
			
			holder.ivPoint.setVisibility(View.VISIBLE);
			holder.tvTitle.setText(member.name);
			holder.tvNum.setText(member.number);
			holder.tvTCount.setText("1");
			
			boolean selected = (mTeam == TEAM_A && mSelectedGroupAPlayingPos >= 0 && mSelectedGroupAPlayingPos == position) 
				|| (mTeam == TEAM_B && mSelectedGroupBPlayingPos >= 0 && mSelectedGroupBPlayingPos == position); 
			holder.ivPoint.setSelected(selected);
			holder.tvTitle.setSelected(selected);
			holder.tvNum.setSelected(selected);
			holder.tvTCount.setSelected(selected);
			
			return convertView;
		}
		
	}
	
	private static class ViewHolder {

		private ImageView ivPoint;
		private TextView tvTitle;
		private TextView tvNum;
		private TextView tvTCount;

		public ViewHolder(View convertView) {
			ivPoint = (ImageView) convertView.findViewById(R.id.iv_point);
			tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
			tvNum = (TextView) convertView.findViewById(R.id.tv_num);
			tvTCount = (TextView) convertView.findViewById(R.id.tv_t_count);
		}
		
	}
	
	private class EventAdapter extends BaseAdapter {

		private Resources mResources;
		private LayoutInflater mInflater;

		private List<Action> actionList;

		private int step;
		
		public EventAdapter(Context context, int step, List<Action> actionList) {
			mResources = context.getResources();
			mInflater = LayoutInflater.from(context);
			this.step = step;
			this.actionList = actionList;
		}

		@Override
		public int getCount() {
			return actionList != null ? actionList.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			return actionList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			convertView = mInflater.inflate(R.layout.item_event, parent, false);
			
			Action action = (Action) getItem(position);
			
			TextView tvEvent = (TextView) convertView.findViewById(R.id.tv_event);
			tvEvent.setText(action.name);
			
			boolean newStat = (step == 2 && action.nextActionId == -1);
			tvEvent.setBackgroundResource(!newStat ? R.drawable.tan_bg03 : R.drawable.tanfont_redbg);
			tvEvent.setTextColor(!newStat ? mResources.getColor(R.color.black) : mResources.getColor(R.color.white));
			
			return convertView;
		}
		
	}
}
