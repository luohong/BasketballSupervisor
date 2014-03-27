package com.example.basketballsupervisor.util;

import com.example.basketballsupervisor.config.Config;
import com.example.basketballsupervisor.model.User;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 
 * sharePreference 存储工具类 <功能详细描述>
 * 
 * @author Administrator
 * @version [版本号, 2013-12-16]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class SpUtil {
	private static final String NAME = "preferences";

	public static SpUtil instance = null;

	private Context context;

	private SpUtil(Context context) {
		this.context = context;
	}

	public static SpUtil getInstance(Context context) {
		Context applicationContext = context.getApplicationContext();
		if (null == instance || instance.context != applicationContext) {
			instance = new SpUtil(context);
		}
		return instance;
	}

	private SharedPreferences sp;

	public SharedPreferences getSp() {
		if (sp == null) {
			sp = context.getSharedPreferences(getSpFileName(),
					Context.MODE_PRIVATE);
		}
		return sp;
	}

	public Editor getEdit() {
		return getSp().edit();
	}

	private String getSpFileName() {
		return NAME;
	}

	public void logout(){
		
	}
	
	public void setUser(User user) {
		String json = Config.mGson.toJson(user);
		getEdit().putString("user", json).commit();
	}
	
	public User getUser() {
		String json = getSp().getString("user", "");
		User user = Config.mGson.fromJson(json, User.class);
		if (user == null) {
			user = new User();
		}
		return user;
	}
	
}
