package com.example.basketballsupervisor.config;

import android.app.Application;
import android.content.Context;

import com.example.basketballsupervisor.db.DbHelper;
import com.example.basketballsupervisor.model.User;
import com.google.gson.Gson;

public class Config {
	
	private static final String TAG = Config.class.getSimpleName();

	public static final String SERVER = "http://android1.putao.so/PT_SERVER/interface.s";
	public static final String ACTION_HTTP_REQUEST = "so.putao.community.httprequest";

	public static final String PREFERENCES = "preferences";
	public static final String APP_PREFERENCES = "app_preferences";
	public static final String KEY = "233&*Adc^%$$per";
	public static long HEART_BEAT_DELAY = 1 * 60 * 1000; // 心跳周期
	public static Gson mGson = new Gson();

	public static Context mContext;
	
	public Config(Application application) {
		mContext = application.getApplicationContext();
	}
	
	public static Context getContext() {
		return mContext;
	}

	public static User getUser() {
		return null;
	}
	
}
