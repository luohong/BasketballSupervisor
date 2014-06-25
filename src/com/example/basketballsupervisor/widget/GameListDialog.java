package com.example.basketballsupervisor.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
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

public class GameListDialog extends BaseDialog {
	
	private ListView mLvGame;

	private PlayerAdapter mGameAdapter;

	private List<Game> mGameList;

	private MainActivity mMainActivity;
	
	public GameListDialog(Context context, List<Game> gameList) {
		super(context);
		mMainActivity = (MainActivity) context;
		mGameList = gameList;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.dialog_game_list;
	}

	@Override
	protected void onFindViews() {
		mLvGame = (ListView) findViewById(R.id.lv_game);
	}

	@Override
	protected void onInitViewData() {
		
		mGameAdapter = new PlayerAdapter(getContext(), mGameList);
		mLvGame.setAdapter(mGameAdapter);
		
		mLvGame.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				dismiss();
				
				Game game = mGameList.get(position);
				mMainActivity.onGameItemClick(game);
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
//	    int height = (int) (dm.heightPixels * 0.8);

		WindowManager.LayoutParams p = win.getAttributes();
		p.width = width;
//		p.height = height;
		
	    win.setAttributes(p);
	}
	
	private class PlayerAdapter extends BaseAdapter {
		
		private LayoutInflater mInflater;

		private List<Game> gameList;

		public PlayerAdapter(Context context, List<Game> gameList) {
			mInflater = LayoutInflater.from(context);
			this.gameList = gameList;
		}
		
		@Override
		public int getCount() {
			return gameList != null ? gameList.size() : 0;
		}

		@Override
		public Object getItem(int arg0) {
			return gameList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder holder = null;
			
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_game_list, parent, false);
				holder = new ViewHolder(convertView);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			Game game = (Game) getItem(position);
			
			holder.tvTitle.setText(game.gameName);
			holder.tvTime.setText("比赛时间: " + game.time);
			holder.tvSection.setText("共" + game.section + "节 " + game.section_time + "分钟/节");
			
			String role = "";
			if (game.role != null && game.role.size() > 0) {
				if (game.role.contains(1) || game.role.contains(2)) {
					role = "统计角色";
				} 
				if (game.role.contains(3)) {
					if (!TextUtils.isEmpty(role)) {
						role += "、";
					}
					role += "创新角色";
				}
			}
			holder.tvRole.setText(role);
			holder.tvAddress.setText(game.location);
			holder.tvRemark.setText(game.gameRemark);
			
			return convertView;
		}
		
	}
	
	private static class ViewHolder {

		private TextView tvTitle;
		private TextView tvTime;
		private TextView tvRole;
		private TextView tvSection;
		private TextView tvAddress;
		private TextView tvRemark;

		public ViewHolder(View convertView) {
			tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
			tvTime = (TextView) convertView.findViewById(R.id.tv_time);
			tvRole = (TextView) convertView.findViewById(R.id.tv_role);
			tvSection = (TextView) convertView.findViewById(R.id.tv_section);
			tvAddress = (TextView) convertView.findViewById(R.id.tv_address);
			tvRemark = (TextView) convertView.findViewById(R.id.tv_remark);
		}
		
	}

}
