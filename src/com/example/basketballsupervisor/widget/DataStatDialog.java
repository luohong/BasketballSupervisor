package com.example.basketballsupervisor.widget;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.net.Uri;
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

import com.android.framework.core.widget.BaseDialog;
import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.activity.MainActivity;
import com.example.basketballsupervisor.model.DataStat;
import com.example.basketballsupervisor.model.Group;
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

		DisplayMetrics dm = new DisplayMetrics();
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
		DisplayMetrics dm = new DisplayMetrics();
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

				// 总得分 总出手命中次数（不含罚球） 总出手次数（不含罚球） 总命中率（总命中率中不含罚球命中率） 2分球命中次数
				// 2分球出手次数 2分球命中率 3分球命中次数 3分球出手次数 3分球命中率 罚球命中次数 罚球出手次数 罚球命中率
				// 前场篮板 后场篮板 总篮板 助攻 抢断 封盖 被犯规 犯规 失误 上场时间
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
			String error = null;
			if (TextUtils.isEmpty(mCourtPointImagePath)) {
				Bitmap tableBm = genTable();
				System.gc();
				
				int width = Math.max(tableBm.getWidth(), mCourtPointBm.getWidth());
	            int height = tableBm.getHeight() + mCourtPointBm.getHeight();
				Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_4444);
				Canvas canvas = new Canvas(result);
				// 填充打点图的背景颜色
				Paint paintColor = new Paint();
				paintColor.setStyle(Style.FILL);
				paintColor.setColor(Color.rgb(235, 241, 221));
				canvas.drawRect(0, 0, width, mCourtPointBm.getHeight(), paintColor);
				canvas.drawBitmap(mCourtPointBm, 0, 0, null);
				canvas.drawBitmap(tableBm, 0, mCourtPointBm.getHeight(), null);
				System.gc();
				
				error = SystemUtil.saveBitmap(result, getFilename());
				System.gc();
				
				if (!TextUtils.isEmpty(error) && error.startsWith("保存成功: ")) {
					mCourtPointImagePath = error.substring("保存成功: ".length());
					share();
//					String ext = "_" + mCourtPointBm.getWidth() + "x" + mCourtPointBm.getHeight();
//					mCourtPointImagePath += ext;
				}
			} else {
				error = "保存成功: " + mCourtPointImagePath;
				share();
			}

//			list.get(0).dataList.set(0, mCourtPointImagePath);// 更新打点图的路径
//			error = JXLUtil.write(getFilename(), list);
			Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
			break;
		}
	}

	private void share() {
		Group groupA = mMainActivity.getGroupA();
		Group groupB = mMainActivity.getGroupB();
		String title = groupA.groupName + "VS" + groupB.groupName + " " + mMainActivity.getGroupAScore() + "-" + mMainActivity.getGroupBScore();
		
		Intent intent = new Intent(Intent.ACTION_SEND);  
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mCourtPointImagePath)));  //传输图片或者文件 采用流的方式  
		intent.putExtra(Intent.EXTRA_TEXT, title);   //附带的说明信息  
		intent.putExtra(Intent.EXTRA_SUBJECT, title);
		intent.setType("image/*");   //分享图片  
		context.startActivity(Intent.createChooser(intent,"分享"));
	}

	private String getFilename() {
		Group groupA = mMainActivity.getGroupA();
		Group groupB = mMainActivity.getGroupB();

		StringBuilder builder = new StringBuilder();
		// builder.append(groupA.groupName);
		// builder.append("VS");
		// builder.append(groupB.groupName);
		// builder.append("_");

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String formatDate = sdf.format(date);
		builder.append(formatDate);

		return builder.toString();
	}

	public Bitmap genTable() {

		int gridHeight = 30;
		int gridWidth = 80;

		int STARTX = 0;
		int STARTY = 0;

		int col = 25;
		int row = list.size() - 1;

		Bitmap bitmap = Bitmap.createBitmap(col * gridWidth, row * gridHeight, Bitmap.Config.ARGB_4444);
		final Canvas canvas = new Canvas(bitmap);

		// 填充表格颜色
		Paint paintColor = new Paint();
		paintColor.setStyle(Style.FILL);
		paintColor.setColor(Color.rgb(235, 241, 221));
		canvas.drawRect(STARTX, STARTY, STARTX + gridWidth * col, STARTY + gridHeight * row, paintColor);
		paintColor.setColor(Color.rgb(219, 238, 243));
		for (int i = 0; i < row; i++) {
			if ((i + 1) % 2 == 1) {
				canvas.drawRect(STARTX, i * gridHeight + STARTY, STARTX + col * gridWidth, STARTY + (i + 1) * gridHeight, paintColor);
			}
		}

		// 画表格最外层边框
		Paint paintRect = new Paint();
		paintRect.setColor(Color.rgb(79, 129, 189));
		paintRect.setStrokeWidth(2);
		paintRect.setStyle(Style.STROKE);
		canvas.drawRect(STARTX, STARTY, STARTX + gridWidth * col, STARTY + gridHeight * row, paintRect);
		// 画表格的行和列,先画行后画列
		paintRect.setStrokeWidth(1);
		for (int i = 0; i < row - 1; i++) {
			canvas.drawLine(STARTX, STARTY + (i + 1) * gridHeight, STARTX + col * gridWidth, STARTY + (i + 1) * gridHeight, paintRect);
		}
		for (int j = 0; j < col - 1; j++) {
			canvas.drawLine(STARTX + (j + 1) * gridWidth, STARTY, STARTX + (j + 1) * gridWidth, STARTY + row * gridHeight, paintRect);
		}

		Paint paint = new Paint();
		paint.setColor(Color.rgb(79, 129, 189));
		paint.setStyle(Style.STROKE);
		paint.setTextAlign(Align.CENTER);
		if (row > 40 || col > 25) {
			paint.setTextSize(8);
		} else if (row > 30 || col > 20) {
			paint.setTextSize(10);
		} else if (row > 20 || col > 15) {
			paint.setTextSize(11);
		} else if (row > 10 || col > 10) {
			paint.setTextSize(12);
		}

		FontMetrics fontMetrics = paint.getFontMetrics();
		float fontHeight = fontMetrics.bottom - fontMetrics.top;

		for (int i = 0; i < row; i++) {
			DataStat dataStat = (DataStat) list.get(i + 1);
			List<String> dataList = dataStat.dataList;
			int size = dataList.size();
			switch (dataStat.type) {
			case TYPE_COURT_POINT:
				// mIvCourt.setMaxWidth(mDialogWidth);
				// mIvCourt.setImageBitmap(mCourtPointBm);
				break;
			case TYPE_TITLE: {
				float mLeft = 0 * gridWidth + STARTX;
				float mTop = i * gridHeight + STARTY;
				float mRight = mLeft + gridWidth;
				float textBaseY = (int) (gridHeight + fontHeight) >> 1;

				String text = dataList.get(0);
				canvas.drawText(text + "", (int) (mLeft + mRight) >> 1, textBaseY + mTop, paint);
			}
				break;
			case TYPE_COLUMN:
				// 总得分 总出手命中次数（不含罚球） 总出手次数（不含罚球） 总命中率（总命中率中不含罚球命中率） 2分球命中次数
				// 2分球出手次数 2分球命中率 3分球命中次数 3分球出手次数 3分球命中率 罚球命中次数 罚球出手次数 罚球命中率
				// 前场篮板 后场篮板 总篮板 助攻 抢断 封盖 被犯规 犯规 失误 上场时间
			case TYPE_CONTENT:
				// 一条龙，超远三分，绝杀，最后三秒得分，晃倒，2+1,3+1，扣篮，快攻，2罚不中，三罚不中，被晃倒
				for (int j = 0; j < col; j++) {
					float mLeft = j * gridWidth + STARTX;
					float mTop = i * gridHeight + STARTY;
					float mRight = mLeft + gridWidth;
					float textBaseY = (int) (gridHeight + fontHeight) >> 1;

					String text = j < size ? dataList.get(j) : "";
					canvas.drawText(text + "", (int) (mLeft + mRight) >> 1, textBaseY + mTop, paint);
				}
				break;
			}
		}

		return bitmap;
	}
	
}
