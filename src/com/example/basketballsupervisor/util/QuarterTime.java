package com.example.basketballsupervisor.util;

import java.util.ArrayList;
import java.util.List;

public class QuarterTime {
	
	private int mCurrentQuarter = 1;
	private int mQuarterCount = 1;
	private int mQuarterTime = 10;
	private List<Integer> mQuarterTimeList = new ArrayList<Integer>();
	
	public int getTimeout() {	
		int timeout = 0;
		for (Integer time : mQuarterTimeList) {
			timeout = timeout + time;
		}
		timeout = timeout * 60 * 1000;// 计算比赛时间
		return timeout;
	}

	public boolean isQuarterTime(int mRunningTime) {
		boolean isQuartTime = false;
		for (Integer time : mQuarterTimeList) {
			if (mRunningTime == time * 60 * 1000) {
				isQuartTime = true;
				break;
			}
		}
		return isQuartTime;
	}
	
	public boolean isGameOver(int mRunningTime) {
		int total = 0;
		for (Integer time : mQuarterTimeList) {
			total = total + time;
		}
		return mRunningTime == (total * 60 * 1000);
	}
	
	public int getQuarter(int mRunningTime) {
		int total = 0;
		for (int i = 0; i < mQuarterTimeList.size(); i++) {
			int time = mQuarterTimeList.get(i) * 60 * 1000;
			total = total + time;
			if (mRunningTime <= total) {
				return i + 1;
			}
		}
		return 1;
	}
}
