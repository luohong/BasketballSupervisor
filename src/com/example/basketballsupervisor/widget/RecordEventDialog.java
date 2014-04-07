package com.example.basketballsupervisor.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.android.framework.core.widget.BaseDialog;
import com.android.framework.core.widget.ConfirmDialog;
import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.model.Action;
import com.example.basketballsupervisor.model.Group;
import com.example.basketballsupervisor.model.Member;

public class RecordEventDialog extends BaseDialog {

	private List<Action> mActionList;

	protected ViewFlipper vFlipper;
	
	private TextView mTvGroupAName, mTvGroupBName;
	
	private ListView mLvPlaying, mLvBench;
	
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

	protected int lastNextActionId;

	public RecordEventDialog(Context context, List<Action> actionList) {
		super(context);
		mActionList = actionList;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.dialog_record_event;
	}

	@Override
	protected void initDialogViews() {
		
		vFlipper = (ViewFlipper) findViewById(R.id.vf_events);
		
		mTvGroupAName = (TextView) findViewById(R.id.tv_group_a_name);
		mTvGroupBName = (TextView) findViewById(R.id.tv_group_b_name);
		
		mLvPlaying = (ListView) findViewById(R.id.lv_playing);
		mLvBench = (ListView) findViewById(R.id.lv_bench);
		
		mTvPage2Title = (TextView) findViewById(R.id.tv_page2_title);
		mGvPage2Event = (GridView) findViewById(R.id.gv_page2_event);
		
		mTvPage3Title = (TextView) findViewById(R.id.tv_page3_title);
		mGvPage3Event = (GridView) findViewById(R.id.gv_page3_event);
		
	}

	@Override
	protected void afterDialogViews() {
		
		// page1		
		mGroupAPlayingAdapter = new PlayerAdapter(getContext(), PlayerAdapter.TEAM_A);
		mLvPlaying.setAdapter(mGroupAPlayingAdapter);

		mGroupBPlayingAdapter = new PlayerAdapter(getContext(), PlayerAdapter.TEAM_B);
		mLvBench.setAdapter(mGroupBPlayingAdapter);
		
		mLvPlaying.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mSelectedGroupAPlayingPos = position;
				mGroupAPlayingAdapter.notifyDataSetChanged();
				
				mSelectedMember = (Member) parent.getItemAtPosition(position);
				String title = mSelectedMember.number + " " + mSelectedMember.name + " 技术统计";
				mTvPage2Title.setText(title);
				
//				showConfirmSubstitudeDialog();
				showNext();
			}
		});
		mLvBench.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mSelectedGroupBPlayingPos = position;
				mGroupBPlayingAdapter.notifyDataSetChanged();
				
				mSelectedMember = (Member) parent.getItemAtPosition(position);
				String title = mSelectedMember.number + " " + mSelectedMember.name + " 技术统计";
				mTvPage2Title.setText(title);
				
//				showConfirmSubstitudeDialog();
				showNext();
			}
		});
		
		// page2
		List<Action> step2ActionList = filterActionListByStep(2);
		EventAdapter adapter = new EventAdapter(getContext(), 2, step2ActionList);
		mGvPage2Event.setAdapter(adapter);
		mGvPage2Event.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Action action = (Action) parent.getItemAtPosition(position);
				if (action.nextActionId == 0) {
					dismiss();
					
					// 记录动作行为
				} else {
					String title = mSelectedMember.number + " " + mSelectedMember.name + " 创新统计";
					mTvPage3Title.setText(title);
					
					lastNextActionId = action.nextActionId;
					
					showNext();
				}
			}
		});
		
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
			}
		});
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
				if (action.nextActionId == lastNextActionId) {
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

	public void fillGroupData(Group groupA, Group groupB) {
		mTvGroupAName.setText(groupA.groupName);
		mTvGroupBName.setText(groupB.groupName);
	}
	
	public void fillPlayersData(List<Member> groupAPlayingMembers, List<Member> groupBPlayingMembers) {
		mGroupAPlayingMembers = groupAPlayingMembers;
		mGroupBPlayingMembers = groupBPlayingMembers;
		
		mGroupAPlayingAdapter.setData(mGroupAPlayingMembers);
		mGroupBPlayingAdapter.setData(mGroupBPlayingMembers);
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
			
			boolean selected = (mTeam == TEAM_A && mSelectedGroupAPlayingPos >= 0 && mSelectedGroupAPlayingPos == position) 
				|| (mTeam == TEAM_B && mSelectedGroupBPlayingPos >= 0 && mSelectedGroupBPlayingPos == position); 
			holder.ivPoint.setSelected(selected);
			holder.tvTitle.setSelected(selected);
			holder.tvNum.setSelected(selected);
			
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
