package com.example.basketballsupervisor.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.util.Constants;

public class CourtAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<Integer> positions;

	private int columnWidth;
	private int columnHeight;

	public CourtAdapter(Context context, List<Integer> positions) {
		mInflater = LayoutInflater.from(context);
		this.positions = positions;
	}

	public void setColumn(int columnWidth, int columnHeight) {
		this.columnWidth = columnWidth;
		this.columnHeight = columnHeight;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return positions.size();
	}

	@Override
	public Object getItem(int position) {
		return positions.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_event_coordinate, parent, false);
		}

		if (columnHeight > 0) {
			AbsListView.LayoutParams params = new AbsListView.LayoutParams(columnWidth, columnHeight);
			convertView.setLayoutParams(params);
		}

		ImageView image = (ImageView) convertView.findViewById(R.id.iv_coordinate);

		Integer type = (Integer) getItem(position);
		if (type == null) {
			// image.setImageResource(R.drawable.basketball_square);
			image.setImageResource(0);
		} else {
			switch (type) {
			case Constants.CourtShowType.NORMAL:
				image.setImageResource(0);
				break;
			case Constants.CourtShowType.HIT:
				image.setImageResource(R.drawable.position_03);
				break;
			case Constants.CourtShowType.MISS:
				image.setImageResource(R.drawable.position_07);
				break;
			default:
				image.setImageResource(0);
				break;
			}
		}

		return convertView;
	}

}