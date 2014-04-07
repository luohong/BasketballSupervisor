package com.example.basketballsupervisor;

import android.app.Application;

import com.android.framework.core.util.CrashHandler;
import com.example.basketballsupervisor.config.Config;
import com.example.basketballsupervisor.db.DbHelper;
/**
 * application
 * 
 * @author pengjianbo
 *
 */
public class IApplication extends Application {
	
//	private ACache mCache;
	
	public static boolean hasStart = false;

	@Override
	public void onCreate() {
		super.onCreate();
		
//		mCache = ACache.get(this, Config.PFILE_NAME);

		new Config(this);
		
		DbHelper.getInstance(this);
		
        // 异常捕获处理
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
	}
	
//	/**
//	 * 获取缓存实例
//	 * 
//	 * @return
//	 */
//	public ACache getACache() {
//		return mCache;
//	}
	
}
