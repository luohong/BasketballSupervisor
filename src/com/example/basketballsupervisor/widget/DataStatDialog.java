package com.example.basketballsupervisor.widget;

import java.util.List;

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
import android.widget.ListView;
import android.widget.TextView;

import com.android.framework.core.widget.BaseDialog;
import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.model.DataStat;

public class DataStatDialog extends BaseDialog {

	public static final int TYPE_TITLE = 0;
	public static final int TYPE_COLUMN = 1;
	public static final int TYPE_CONTENT = 2;

	private ListView mListView;
	private List<DataStat> list;
	private int selectedItem = -1;
	private DataStatAdapter mAdapter;

	public DataStatDialog(Context context, List<DataStat> list) {
		super(context);
		this.list = list;
	}
	
	@Override
	protected int getLayoutId() {
		return R.layout.dialog_data_stat;
	}

	@Override
	protected void onFindViews() {
		mListView = (ListView) findViewById(android.R.id.list);
		mAdapter = new DataStatAdapter(getContext());
		mListView.setAdapter(mAdapter);
	}

	@Override
	protected void onInitViewData() {
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (selectedItem == position) {
					selectedItem = -1;
				} else {
					selectedItem = position;
				}
				mAdapter.notifyDataSetChanged();
			}
		});
	}
	
	@Override
	public void initWindowLayoutParams() {
		Window win = getWindow();
	    WindowManager m = win.getWindowManager();
		DisplayMetrics  dm = new DisplayMetrics();    
	    m.getDefaultDisplay().getMetrics(dm);    

	    int width = (int) (dm.widthPixels * 0.85);
	    int height = (int) (dm.heightPixels * 0.8);

		WindowManager.LayoutParams p = win.getAttributes();
		p.width = width;
		p.height = height;
		
	    win.setAttributes(p);
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
			
			int size = dataList.size();
			
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
				for (int i = 0; i < 25; i++) {
					((TextView) convertView.findViewById(R.id.tv_column1 + i)).setText(i < size ? dataList.get(i) : "");
				}
				break;
			case TYPE_CONTENT:
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.item_data_stat, null);
				}

				// 一条龙，超远三分，绝杀，最后三秒得分，晃倒，2+1,3+1，扣篮，快攻，2罚不中，三罚不中，被晃倒
				for (int i = 0; i < 25; i++) {
					((TextView) convertView.findViewById(R.id.tv_column1 + i)).setText(i < size ? dataList.get(i) : "");
				}
				break;
			}
			
			View view = convertView.findViewById(R.id.ll_column);
			view.setSelected(selectedItem == position);

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
