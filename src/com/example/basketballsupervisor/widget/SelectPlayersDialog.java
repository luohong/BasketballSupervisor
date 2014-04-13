package com.example.basketballsupervisor.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.framework.core.widget.BaseDialog;
import com.android.framework.core.widget.ConfirmDialog;
import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.model.Group;
import com.example.basketballsupervisor.model.Member;

public class SelectPlayersDialog extends BaseDialog {
	
	public static final int MODE_SELECT_STARTS = 0;
	public static final int MODE_SUBSTITUTE = 1;
	public static final int MODE_RECORD_EVENT = 2;
	
	private TextView mTvGroupAName, mTvGroupBName;
	
	private ListView mLvPlaying, mLvBench;
	
	private TextView mTvTrainers;

	private PlayerAdapter mPlayingAdapter;

	private PlayerAdapter mAllAdapter;

	private List<Member> mPlayingMembers;

	private List<Member> mAllMembers;

	private ArrayList<Member> mBenchMembers;
	
	private int mMode;
	
	private int mSelectedPlayingPos = -1;
	private int mSelectedBenchPos = -1;

	public SelectPlayersDialog(Context context, int mode) {
		super(context);
		mMode = mode;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.dialog_select_players;
	}

	@Override
	protected void initDialogViews() {
		
		mTvGroupAName = (TextView) findViewById(R.id.tv_group_a_name);
		mTvGroupBName = (TextView) findViewById(R.id.tv_group_b_name);
		
		mLvPlaying = (ListView) findViewById(R.id.lv_playing);
		mLvBench = (ListView) findViewById(R.id.lv_bench);
		
		mTvTrainers = (TextView) findViewById(R.id.tv_trainers);

	}

	@Override
	protected void afterDialogViews() {
		
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
				}
			}
		});
		mLvBench.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (mMode) {
				case MODE_SELECT_STARTS:
					if (mPlayingMembers.size() <= 5) {
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
				}
			}
		});
		
//		mTvTrainers.setVisibility(View.GONE);
		
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

	public void fillGroupData(Group groupA, Group groupB) {
		mTvGroupAName.setText(groupA.groupName);
		mTvGroupBName.setText(groupB.groupName);
	}
	
	public void fillPlayersData(List<Member> playingMembers, List<Member> allMembers) {
		mPlayingMembers = playingMembers;
		mAllMembers = allMembers;
		
		mPlayingAdapter.setData(mPlayingMembers);
		
		mBenchMembers = new ArrayList<Member>();
		if (playingMembers != null && playingMembers.size() > 0) {
			for (Member member : allMembers) {
					if (!playingMembers.contains(member)) {
						mBenchMembers.add(member);
					}
			}
		} else {
			mBenchMembers.addAll(allMembers);
		}
		mAllAdapter.setData(mBenchMembers);
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
			holder.tvNum.setText(member.number);
			
			switch (mMode) {
			case MODE_SUBSTITUTE:
				boolean selected = (mTeam == TEAM_A && mSelectedPlayingPos >= 0 && mSelectedPlayingPos == position) 
					|| (mTeam == TEAM_B && mSelectedBenchPos >= 0 && mSelectedBenchPos == position); 
				holder.ivPoint.setSelected(selected);
				holder.tvTitle.setSelected(selected);
				holder.tvNum.setSelected(selected);
				break;
			}
			
			return convertView;
		}
		
	}
	
	private static class ViewHolder {

		private ImageView ivPoint;
		private TextView tvTitle;
		private TextView tvNum;

		public ViewHolder(View convertView) {
			ivPoint = (ImageView) convertView.findViewById(R.id.iv_point);
			tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
			tvNum = (TextView) convertView.findViewById(R.id.tv_num);
		}
		
	}

}
