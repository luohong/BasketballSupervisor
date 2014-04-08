package com.example.basketballsupervisor.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.framework.core.widget.BaseDialog;
import com.example.basketballsupervisor.R;

public class DataStatDialog extends BaseDialog {

	private HorizontalListView mListView;

	public DataStatDialog(Context context) {
		super(context);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.dialog_data_stat;
	}

	@Override
	protected void initDialogViews() {
		mListView = (HorizontalListView) findViewById(android.R.id.list);
		mListView.setAdapter(new DataStatAdapter(getContext()));
	}

	@Override
	protected void afterDialogViews() {

	}

	private class DataStatAdapter extends BaseAdapter {

		private static final int TYPE_TITLE = 0;
		private static final int TYPE_COLUMN = 1;
		private static final int TYPE_CONTENT = 2;
		
		private LayoutInflater mInflater;

		public DataStatAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return 6;
		}
		
		@Override
		public int getViewTypeCount() {
			return 3;
		}
		
		@Override
		public int getItemViewType(int position) {
			return position % 2 == 0 ? TYPE_TITLE : TYPE_COLUMN;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			int viewType = getItemViewType(position);
			switch (viewType) {
			case TYPE_TITLE:
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.item_data_stat_title, null);
				}
				
				String title = "";
				switch (position) {
				case 1:
					title = "球队统计";
					break;
				case 3:
					title = "球员统计";
					break;
				case 5:
					title = "创新数据";
					break;
				}
				
				((TextView) convertView.findViewById(R.id.tv_title)).setText(title);
				break;
			case TYPE_COLUMN:
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.item_data_stat, null);
				}

				// 总得分 总出手命中次数（不含罚球） 总出手次数（不含罚球） 总命中率（总命中率中不含罚球命中率） 2分球命中次数 2分球出手次数 2分球命中率 3分球命中次数 3分球出手次数 3分球命中率 罚球命中次数 罚球出手次数 罚球命中率 前场篮板 后场篮板 总篮板 助攻 抢断 封盖 被犯规 犯规 失误 上场时间
				// 一条龙，超远三分，绝杀，最后三秒得分，晃倒，2+1,3+1，扣篮，快攻，2罚不中，三罚不中，被晃倒
				for (int i = 0; i < 8; i++) {
					((TextView) convertView.findViewById(R.id.tv_column2 + i)).setText("数据" + position + "行" + (i + 2) + "列");
					((TextView) convertView.findViewById(R.id.tv_content2 + i)).setText("数据" + position + "行" + (i + 2) + "列");
				}
				break;
			}

			//校正（处理同时上下和左右滚动出现错位情况）
			View child = ((ViewGroup) convertView).getChildAt(1);
			int head = mListView.getHeadScrollX();
			if (child.getScrollX() != head) {
				child.scrollTo(mListView.getHeadScrollX(), 0);
			}
			return convertView;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
	}

}
