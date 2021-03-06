package com.example.basketballsupervisor.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.framework.core.widget.BaseDialog;
import com.android.framework.core.widget.ConfirmDialog;
import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.activity.MainActivity;
import com.example.basketballsupervisor.db.RecordDb;
import com.example.basketballsupervisor.model.Action;
import com.example.basketballsupervisor.model.Game;
import com.example.basketballsupervisor.model.Group;
import com.example.basketballsupervisor.model.Member;

public class SelectPlayersDialog extends BaseDialog {
	
	public static final int MODE_SELECT_STARTS = 0;
	public static final int MODE_SUBSTITUTE = 1;
	public static final int MODE_TECHNICAL_FOULS = 2;
	
	private TextView mTvGroupAName, mTvGroupBName;
	private TextView mTvGroupATitle, mTvGroupBTitle;
	
	private ListView mLvPlaying, mLvBench;

	private LinearLayout mLlTrainers;
	private TextView mTvTrainers;

	private PlayerAdapter mPlayingAdapter;

	private PlayerAdapter mAllAdapter;

	private List<Member> mPlayingMembers;

	private List<Member> mAllMembers;

	private ArrayList<Member> mBenchMembers;
	
	private int mMode;
	
	private int mSelectedPlayingPos = -1;
	private int mSelectedBenchPos = -1;
	
	private Group mGroup;
	private Game mGame;
	private List<Integer> mRoles;
	private long mGameTime;
	private String mCoordinate;
	private Group mGroupA;
	private Group mGroupB;
	private Map<Long, Integer> mMemberActionMap;
	private MainActivity mMainActivity;

	public SelectPlayersDialog(Context context, int mode) {
		super(context);
		mMainActivity = (MainActivity) context;
		mMode = mode;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.dialog_select_players;
	}

	@Override
	protected void onFindViews() {
		
		mTvGroupAName = (TextView) findViewById(R.id.tv_group_a_name);
		mTvGroupBName = (TextView) findViewById(R.id.tv_group_b_name);
		
		mTvGroupATitle = (TextView) findViewById(R.id.tv_group_a_title);
		mTvGroupBTitle = (TextView) findViewById(R.id.tv_group_b_title);
		
		mLvPlaying = (ListView) findViewById(R.id.lv_playing);
		mLvBench = (ListView) findViewById(R.id.lv_bench);
		
		mLlTrainers = (LinearLayout) findViewById(R.id.ll_trainers);
		mTvTrainers = (TextView) findViewById(R.id.tv_trainers);

	}

	@Override
	protected void onInitViewData() {
		
		mPlayingAdapter = new PlayerAdapter(getContext(), PlayerAdapter.TEAM_A);
		mLvPlaying.setAdapter(mPlayingAdapter);

		mAllAdapter = new PlayerAdapter(getContext(), PlayerAdapter.TEAM_B);
		mLvBench.setAdapter(mAllAdapter);
		
		mLvPlaying.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (mMode) {
				case MODE_SELECT_STARTS:
					Member member = mPlayingMembers.remove(position);
					mBenchMembers.add(member);
					
					mPlayingAdapter.setData(mPlayingMembers);
					mAllAdapter.setData(mBenchMembers);
					break;
				case MODE_SUBSTITUTE:
					if (mSelectedPlayingPos != position) {
						mSelectedPlayingPos = position;
					} else {
						mSelectedPlayingPos = -1;
					}
					mPlayingAdapter.notifyDataSetChanged();
					
					showConfirmSubstitudeDialog();
					break;
				case MODE_TECHNICAL_FOULS:
					// 不处理
					break;
				}
			}
		});
		mLvBench.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (mMode) {
				case MODE_SELECT_STARTS:
					if (mPlayingMembers.size() < 5) {
						Member member = mBenchMembers.remove(position);
						mPlayingMembers.add(member);
						
						mPlayingAdapter.setData(mPlayingMembers);
						mAllAdapter.setData(mBenchMembers);
					} else {
						Toast.makeText(context, "比赛规则：场上仅允许五名上场球员", Toast.LENGTH_SHORT).show();
					}
					break;
				case MODE_SUBSTITUTE:
					if (mSelectedBenchPos != position) {
						mSelectedBenchPos = position;
					} else {
						mSelectedBenchPos = -1;
					}
					mAllAdapter.notifyDataSetChanged();
					
					showConfirmSubstitudeDialog();
					break;
				case MODE_TECHNICAL_FOULS:
					dismiss();
					
					mSelectedBenchPos = position;
					saveTechnicalFoulsEvent();
					break;
				}
			}
		});
		
//		mTvTrainers.setVisibility(View.GONE);
		
	}
	
	private void saveTechnicalFoulsEvent() {
		// 记录动作行为
		Member member = mBenchMembers.get(mSelectedBenchPos);
		
		Integer count = mMemberActionMap.get(member.memberId);
		if (count != null && count > 2) {
			Toast.makeText(context, "累计技术犯规次数超过限制", Toast.LENGTH_SHORT).show();
			return;
		}
		
		Action action = new Action(28,-10,"技术犯规",0,0);
		
		RecordDb db = new RecordDb(getContext());
		if (mRoles.contains(1) && mGroupA == mGroup) {// 记录A队数据
			db.saveRecord(mGame, mGroup, member, action, mGameTime, mCoordinate);
		} 
		if (mRoles.contains(2) && mGroupB == mGroup) {// 记录B队数据
			db.saveRecord(mGame, mGroup, member, action, mGameTime, mCoordinate);
		} 
		if (mRoles.contains(3)) {// 记录创新数据
			// 不处理
		}
		
		mMainActivity.setCurrentRecordCoordinate(action, mCoordinate);
		
		Toast.makeText(context, "记录成功", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void initWindowLayoutParams() {
		Window win = getWindow();
	    WindowManager m = win.getWindowManager();
		DisplayMetrics  dm = new DisplayMetrics();    
	    m.getDefaultDisplay().getMetrics(dm);    
		
	    int width = (int) (dm.widthPixels * 0.6);
//	    int height = (int) (dm.heightPixels * 0.8);

		WindowManager.LayoutParams p = win.getAttributes();
		p.width = width;
//		p.height = height;
		
	    win.setAttributes(p);
	}
	
	protected void showConfirmSubstitudeDialog() {
		// 检查是否符合换人的条件
		
		if ((mSelectedPlayingPos >= 0 && mSelectedBenchPos >= 0)
				|| (mSelectedPlayingPos >= 0 && mBenchMembers.size() == 0)) {
			final ConfirmDialog dialog = new ConfirmDialog(context, "确认换人？", true, "取消", "确认", new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					dismiss();
					
					Toast.makeText(context, "换人成功", Toast.LENGTH_SHORT).show();
					Member playingMember = mPlayingMembers.remove(mSelectedPlayingPos);
					
					if (mSelectedBenchPos >= 0) {
						Member benchMember = mBenchMembers.remove(mSelectedBenchPos);
						mPlayingMembers.add(benchMember);
					}

					mBenchMembers.add(playingMember);
				}
			}, new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					cancel();
					
					mSelectedPlayingPos = -1;
					mSelectedBenchPos = -1;

					mPlayingAdapter.notifyDataSetChanged();
					mAllAdapter.notifyDataSetChanged();
				}
			});
			
			dialog.show();
		}
	}
	
	public void fillGroupData(Group group) {
		mGroup = group;
		
		mTvGroupAName.setText(group.groupName);
		mTvGroupBName.setText("");
	}

	public void fillGroupData(Group groupA, Group groupB) {
		mGroupA = groupA;
		mGroupB = groupB;
		
		mTvGroupAName.setText(groupA.groupName);
		mTvGroupBName.setText(groupB.groupName);
	}
	
	public void fillPlayersData(List<Member> allMembers, Map<Long, Integer> memberActionMap) {
		mMemberActionMap = memberActionMap;

		mTvGroupATitle.setVisibility(View.VISIBLE);
		mTvGroupATitle.setText("球员  号码  技术犯规次数");
		mTvGroupBTitle.setVisibility(View.GONE);
		
		mPlayingMembers = null;
		mAllMembers = allMembers;
		
		mPlayingAdapter.setData(mPlayingMembers);
		mLvPlaying.setVisibility(View.GONE);
		
		mBenchMembers = new ArrayList<Member>();
		mBenchMembers.addAll(allMembers);
		mAllAdapter.setData(mBenchMembers);
	}
	
	public void fillPlayersData(List<Member> playingMembers, List<Member> allMembers) {
		mPlayingMembers = playingMembers;
		mAllMembers = allMembers;
		
		mPlayingAdapter.setData(mPlayingMembers);
		
		mBenchMembers = new ArrayList<Member>();
		List<Member> mTrainers = new ArrayList<Member>();
		for (Member member : allMembers) {
				if (!playingMembers.contains(member) && member.isLeader <= 1) {//3:教练,2:领队,1:队长,0:队员 
					mBenchMembers.add(member);
				} else if (member.isLeader > 2) {
					mTrainers.add(member);
				}
		}
		mAllAdapter.setData(mBenchMembers);
		
		if (mTrainers.size() > 0) {
			mLlTrainers.setVisibility(View.VISIBLE);
			mTvTrainers.setVisibility(View.VISIBLE);
			
			String trainers = "";
			for (int i = 0; i < mTrainers.size(); i++) {
				trainers += mTrainers.get(i).name + " ";
			}
			mTvTrainers.setText(trainers);
		} else {
			mLlTrainers.setVisibility(View.GONE);
			mTvTrainers.setVisibility(View.GONE);
		}
	}
	
	public void fillGameData(Game game, List<Integer> roles, long time, String coordinate) {
		mGame = game;
		mRoles = roles;
		mGameTime = time;
		mCoordinate = coordinate;
	}
	
	private class PlayerAdapter extends BaseAdapter {
		
		public static final int TEAM_A = 0;
		public static final int TEAM_B = 1;
		
		private Context context;
		private LayoutInflater mInflater;

		private List<Member> members;
		private int mTeam;

		public PlayerAdapter(Context context, int team) {
			this.context = context;			
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
			switch (member.isLeader) {
			case 0://0:队员 
			case 1://1:队长
				holder.tvNum.setText(member.number);
				break;
			case 2://2:领队
				holder.tvNum.setText("领队");
				break;
			case 3://3:教练
				holder.tvNum.setText("教练");
				break;
			default:
				holder.tvNum.setText(member.number);
				break;
			}
			
			Integer count = mMemberActionMap != null ? mMemberActionMap.get(member.memberId) : null;
			if (count != null && count > 0) {
				holder.tvTCount.setVisibility(View.VISIBLE);
				if (count > 2) count = 2;
				holder.tvTCount.setText(count.toString());
			} else if (mMemberActionMap != null) {
				holder.tvTCount.setText("0");
				holder.tvTCount.setVisibility(View.INVISIBLE);
			} else {
				holder.tvTCount.setVisibility(View.GONE);
			}
			
			switch (mMode) {
			case MODE_SUBSTITUTE:
				boolean selected = (mTeam == TEAM_A && mSelectedPlayingPos >= 0 && mSelectedPlayingPos == position) 
					|| (mTeam == TEAM_B && mSelectedBenchPos >= 0 && mSelectedBenchPos == position); 
				holder.ivPoint.setSelected(selected);
				holder.tvTitle.setSelected(selected);
				holder.tvNum.setSelected(selected);
				holder.tvTCount.setSelected(selected);
				break;
			}
			
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

}
