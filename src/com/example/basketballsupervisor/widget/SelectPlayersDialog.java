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
import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.model.Group;
import com.example.basketballsupervisor.model.Member;

public class SelectPlayersDialog extends BaseDialog {
	
	private TextView mTvGroupAName, mTvGroupBName;
	
	private ListView mLvPlaying, mLvBench;
	
	private TextView mTvTrainers;

	private PlayerAdapter mPlayingAdapter;

	private PlayerAdapter mAllAdapter;

	private List<Member> mPlayingMembers;

	private List<Member> mAllMembers;

	private ArrayList<Member> mBenchMembers;

	public SelectPlayersDialog(Context context) {
		super(context);
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
		mPlayingAdapter = new PlayerAdapter(getContext());
		mLvPlaying.setAdapter(mPlayingAdapter);

		mAllAdapter = new PlayerAdapter(getContext());
		mLvBench.setAdapter(mAllAdapter);
		
		mLvPlaying.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Member member = mPlayingMembers.remove(position);
				mBenchMembers.add(member);
				
				mPlayingAdapter.setData(mPlayingMembers);
				mAllAdapter.setData(mBenchMembers);
			}
		});
		mLvBench.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mPlayingMembers.size() <= 5) {
					Member member = mBenchMembers.remove(position);
					mPlayingMembers.add(member);
					
					mPlayingAdapter.setData(mPlayingMembers);
					mAllAdapter.setData(mBenchMembers);
				} else {
					Toast.makeText(context, "比赛规则：场上仅允许五名上场球员", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
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
		
		private Context context;
		private List<Member> members;
		
		private LayoutInflater mInflater;

		public PlayerAdapter(Context context) {
			this.context = context;			
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
