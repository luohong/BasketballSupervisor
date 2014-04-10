package com.example.basketballsupervisor.widget;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.framework.core.widget.BaseDialog;
import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.model.DataStat;

public class DataStatDialog extends BaseDialog {

	public static final int TYPE_TITLE = 0;
	public static final int TYPE_COLUMN = 1;
	public static final int TYPE_CONTENT = 2;

	private HorizontalListView mListView;
	private List<DataStat> list;

	public DataStatDialog(Context context, List<DataStat> list) {
		super(context);
		this.list = list;
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
		
		private LayoutInflater mInflater;

		public DataStatAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return list != null ? list.size() : 0;
		}
		
		@Override
		public int getViewTypeCount() {
			return 3;
		}
		
		@Override
		public int getItemViewType(int position) {
			DataStat dataStat = (DataStat) getItem(position);
			return dataStat.type;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			DataStat dataStat = (DataStat) getItem(position);
			if (dataStat == null) {
				return convertView;
			}
			List<String> dataList = dataStat.dataList;
			if (dataList == null || dataList.size() == 0) {
				return convertView;
			}
			
			int viewType = getItemViewType(position);
			switch (viewType) {
			case TYPE_TITLE:
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.item_data_stat_title, null);
				}
				
				((TextView) convertView.findViewById(R.id.tv_title)).setText(dataList.get(0));
				break;
			case TYPE_COLUMN:
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.item_data_stat, null);
				}

				// 总得分 总出手命中次数（不含罚球） 总出手次数（不含罚球） 总命中率（总命中率中不含罚球命中率） 2分球命中次数 2分球出手次数 2分球命中率 3分球命中次数 3分球出手次数 3分球命中率 罚球命中次数 罚球出手次数 罚球命中率 前场篮板 后场篮板 总篮板 助攻 抢断 封盖 被犯规 犯规 失误 上场时间
				for (int i = 0; i < dataList.size(); i++) {
					((TextView) convertView.findViewById(R.id.tv_column2 + i)).setText(dataList.get(i));
				}
				break;
			case TYPE_CONTENT:
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.item_data_stat, null);
				}

				// 一条龙，超远三分，绝杀，最后三秒得分，晃倒，2+1,3+1，扣篮，快攻，2罚不中，三罚不中，被晃倒
				for (int i = 0; i < dataList.size(); i++) {
					((TextView) convertView.findViewById(R.id.tv_column2 + i)).setText(dataList.get(i));
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
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}

}
