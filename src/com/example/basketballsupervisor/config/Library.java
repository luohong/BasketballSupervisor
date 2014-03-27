package com.example.basketballsupervisor.config;

/**
 * @author William.cheng
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.os.Environment;

import com.example.basketballsupervisor.config.Library.OnLowMemoryListener;
import com.example.basketballsupervisor.config.http.IgnitedHttp;

public abstract class Library {
	private static Library mInstance;

	/**
	 * Used for receiving low memory system notification. You should definitely
	 * use it in order to clear caches and not important data every time the
	 * system needs memory.
	 * 
	 * @author Cyril Mottier
	 * @see MLibrary#registerOnLowMemoryListener(OnLowMemoryListener)
	 * @see MLibrary#unregisterOnLowMemoryListener(OnLowMemoryListener)
	 */
	public static interface OnLowMemoryListener {

		/**
		 * Callback to be invoked when the system needs memory.
		 */
		public void onLowMemoryReceived();
	}

	/**
	 * 启动应用分组获取动态线程数至少有三个，一般有6,7个，因此增开到10个线程数，执行其他任务，防止线程堵车，请求等待过长时间
	 */
	private static final int CORE_POOL_SIZE = 10;

	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		@Override
        public Thread newThread(Runnable r) {
			return new Thread(r, "MDroid thread #" + mCount.getAndIncrement());
		}
	};

	private Application mApplication;
	private ExecutorService mExecutorService;
	private ExecutorService mBackExecutorService;
	private ExecutorService weiboExecutorService;
	// private ImageCache mImageCache;
	private IgnitedHttp mHttp;
	private ArrayList<WeakReference<OnLowMemoryListener>> mLowMemoryListeners;

	public static Library Instance() {
		return mInstance;
	}

	public Library(Application application) {
		if (mInstance != null) {
			throw new Error("Must be only one Library instance.");
		}
		mLowMemoryListeners = new ArrayList<WeakReference<OnLowMemoryListener>>();
		mApplication = application;
		mInstance = this;
	}

	/**
	 * Return an ExecutorService (global to the entire application) that may be
	 * used by clients when running long tasks in the background.
	 * 
	 * @return An ExecutorService to used when processing long running tasks
	 */
	public synchronized ExecutorService getExecutor() {
		if (mExecutorService == null) {
			mExecutorService = Executors.newFixedThreadPool(CORE_POOL_SIZE,sThreadFactory);
		}
		return mExecutorService;
	}

	public synchronized ExecutorService getBackExecutor() {
		if (mBackExecutorService == null) {
			mBackExecutorService = Executors.newFixedThreadPool(CORE_POOL_SIZE,
					sThreadFactory);
		}
		return mBackExecutorService;
	}

	public synchronized ExecutorService getWeiboExecutorService() {
		if (weiboExecutorService == null) {
			weiboExecutorService = Executors.newFixedThreadPool(CORE_POOL_SIZE,
					sThreadFactory);
		}
		return weiboExecutorService;
	}

	/**
	 * Add a new listener to registered {@link OnLowMemoryListener}.
	 * 
	 * @param listener
	 *            The listener to unregister
	 * @see OnLowMemoryListener
	 */
	public synchronized void registerOnLowMemoryListener(
			OnLowMemoryListener listener) {
		mLowMemoryListeners
				.add(new WeakReference<OnLowMemoryListener>(listener));
	}

	/**
	 * Remove a previously registered listener
	 * 
	 * @param listener
	 *            The listener to unregister
	 * @see OnLowMemoryListener
	 */
	public synchronized void unregisterOnLowMemoryListener(
			OnLowMemoryListener listener) {
		int i = 0;
		while (i < mLowMemoryListeners.size()) {
			final OnLowMemoryListener l = mLowMemoryListeners.get(i).get();
			if (l == null || l == listener) {
				mLowMemoryListeners.remove(i);
			} else {
				i++;
			}
		}
	}

	public void onLowMemery() {
		int i = 0;
		while (i < mLowMemoryListeners.size()) {
			final OnLowMemoryListener listener = mLowMemoryListeners.get(i)
					.get();
			if (listener == null) {
				mLowMemoryListeners.remove(i);
			} else {
				listener.onLowMemoryReceived();
				i++;
			}
		}
	}

	public Application getApplication() {
		return mApplication;
	}

	public IgnitedHttp getHttp() {
		if (mHttp == null) {
			mHttp = new IgnitedHttp(mApplication);
		}
		return mHttp;
	}

	public String getUserAgent() {
		return "Android/mDroid";
	}

	public InputStream getAssetsStream(String fileName) throws IOException {
		return mApplication.getAssets().open(fileName);
	}

	/**
	 * Checks if the application is in the background (i.e behind another
	 * application's Activity).
	 * 
	 * @return true if another application is above this one.
	 */
	public boolean isApplicationBroughtToBackground() {
		ActivityManager am = (ActivityManager) mApplication
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (!topActivity.getPackageName().equals(
					mApplication.getPackageName())) {
				return true;
			}
		}

		return false;
	}

	public String getRootCache() {
		String rootDir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			// SD-card available
			rootDir = Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ "/Android/data/"
					+ mApplication.getPackageName();
		}
		return rootDir;
	}

	public String getNetCache() {
		String root = getRootCache();
		if (root == null) {
			return null;
		}
		String rootPath = root + "/net";
		File rootFile = new File(rootPath);
		if (rootFile.isDirectory() || rootFile.mkdirs()) {
			return rootPath;
		}
		return null;
	}

	public String getNetImagesCache() {
		String root = getRootCache();
		if (root == null) {
			return null;
		}
		String rootPath = root + "/net/images";
		File rootFile = new File(rootPath);
		if (rootFile.isDirectory() || rootFile.mkdirs()) {
			return rootPath;
		}
		return null;
	}

	public String getLocalCache() {
		String root = getRootCache();
		if (root == null) {
			return null;
		}
		String rootPath = root + "/local";
		File rootFile = new File(rootPath);
		if (rootFile.isDirectory() || rootFile.mkdirs()) {
			return rootPath;
		}
		return null;
	}

	public String getModelCache() {
		String root = getRootCache();
		if (root == null) {
			return null;
		}
		String rootPath = root + "/local/model";
		File rootFile = new File(rootPath);
		if (rootFile.isDirectory() || rootFile.mkdirs()) {
			return rootPath;
		}
		return null;
	}
}