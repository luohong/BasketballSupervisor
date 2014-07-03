package com.example.basketballsupervisor.widget;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.framework.core.util.SDcardUtil;
import com.android.framework.core.widget.BaseDialog;
import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.activity.MainActivity;
import com.example.basketballsupervisor.model.DataStat;
import com.example.basketballsupervisor.model.Group;
import com.example.basketballsupervisor.util.JXLUtil;
import com.example.basketballsupervisor.util.ScreenShot;
import com.example.basketballsupervisor.util.SystemUtil;

public class DataStatDialog extends BaseDialog implements android.view.View.OnClickListener {

	public static final int TYPE_COURT_POINT = 0;
	public static final int TYPE_TITLE = 1;
	public static final int TYPE_COLUMN = 2;
	public static final int TYPE_CONTENT = 3;

	private ListView mListView;
	private List<DataStat> list;
	private int selectedItem = -1;
	private DataStatAdapter mAdapter;
	private Button mBtnSave;
	private MainActivity mMainActivity;
	private Bitmap mCourtPointBm;
	private String mCourtPointImagePath;
	private int mDialogWidth;
	
	public DataStatDialog(Context context, List<DataStat> list) {
		super(context);
		this.mMainActivity = (MainActivity) context;
		this.list = list;
	}
	
	@Override
	protected int getLayoutId() {
		return R.layout.dialog_data_stat;
	}

	@Override
	protected void onFindViews() {
		mBtnSave = (Button) findViewById(R.id.btn_save);
		mListView = (ListView) findViewById(android.R.id.list);
	}

	@Override
	protected void onInitViewData() {
		mBtnSave.setOnClickListener(this);
		
		DisplayMetrics  dm = new DisplayMetrics();    
		getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);    

	    mDialogWidth = (int) (dm.widthPixels * 0.85);
		
		View court = mMainActivity.getCourtView();
		mCourtPointBm = SystemUtil.getViewBitmap(court);
		
		mAdapter = new DataStatAdapter(getContext());
		mListView.setAdapter(mAdapter);
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
			return 4;
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
			case TYPE_COURT_POINT:
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.item_data_stat_court_point, null);
				}
				ImageView mIvCourt = ((ImageView) convertView.findViewById(R.id.iv_court_point));
				mIvCourt.setMaxWidth(mDialogWidth);
				mIvCourt.setImageBitmap(mCourtPointBm);
				break;
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

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_save:
//			View v = findViewById(R.id.ll_data_stat);
//			if (v != null) {
//				Bitmap bm = SystemUtil.getViewBitmap(v);
//				Bitmap bm = ScreenShot.getListViewBitmap(mListView);
//				Bitmap bm = ScreenShot.getScrollViewBitmap((ScrollView)v);
//				String error = SystemUtil.saveBitmap(bm, getFilename());
//				Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
//			}
			
			String error = null;
			if (TextUtils.isEmpty(mCourtPointImagePath)) {
				error = SystemUtil.saveBitmap(mCourtPointBm, getFilename());
				if (!TextUtils.isEmpty(error) && error.startsWith("保存成功: "))  {
					mCourtPointImagePath = error.substring("保存成功: ".length());
					String ext = "_" + mCourtPointBm.getWidth() + "x" + mCourtPointBm.getHeight();
					mCourtPointImagePath += ext;
				} else {
					Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
					return ;
				}
			} 
			
			list.get(0).dataList.set(0, mCourtPointImagePath);// 更新打点图的路径
			
			error = JXLUtil.write(getFilename(), list);
			Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
			break;
		}
	}
	
	private String getFilename() {
		Group groupA = mMainActivity.getGroupA();
		Group groupB = mMainActivity.getGroupB();
		
		StringBuilder builder = new StringBuilder();
//		builder.append(groupA.groupName);
//		builder.append("VS");
//		builder.append(groupB.groupName);
//		builder.append("_");
		
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String formatDate = sdf.format(date);
		builder.append(formatDate);
		
		return builder.toString();
	}

}
