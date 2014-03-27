package com.example.basketballsupervisor.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.android.framework.core.widget.ProgressDialog;
import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.config.http.IgnitedHttpResponse;
import com.example.basketballsupervisor.db.DbHelper;
import com.example.basketballsupervisor.util.SystemUtil;
import com.google.gson.Gson;

public class Config extends Library {

	private static final String TAG = Config.class.getSimpleName();

	public static final String SERVER = "http://android1.putao.so/PT_SERVER/interface.s";
	public static final String ACTION_HTTP_REQUEST = "so.putao.community.httprequest";

	public static final String PREFERENCES = "preferences";
	public static final String APP_PREFERENCES = "app_preferences";
	public static final String KEY = "233&*Adc^%$$per";
	public static long HEART_BEAT_DELAY = 1 * 60 * 1000; // 心跳周期
	public static Gson mGson = new Gson();

	public static Context mContext;
	private static Http mHttp;

	private static ExecutorService mExecutorService;

	public Config(Application application) {
		super(application);
		mContext = application.getApplicationContext();
	}

	public static Context getContext() {
		return mContext;
	}

	// 取得http对象
	public static Http getApiHttp() {
		if (mHttp == null) {
			mHttp = new Http(Instance().getApplication());
		}
		return mHttp;
	}
	
	/**
	 * 主要用于解决 跟服务器交互获取好友时JSON串被截断的问题 HttpURLConnection post方式请求服务器
	 * 
	 * @param urlpath
	 * @param requestData
	 * @return
	 * @throws IOException
	 */
	public static String requestByPost(String urlpath, HttpEntity requestData) throws IOException {
		String str = "";
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}

		URL url = new URL(urlpath);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.setConnectTimeout(60 * 1000);
		conn.setReadTimeout(60 * 1000);
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		// 需要设置 gzip的请求头 才可以获取Content-Encoding响应码
		conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
		conn.connect();
		conn.getOutputStream().write(EntityUtils.toString(requestData).getBytes("utf-8"));
		conn.getOutputStream().flush();
		conn.getOutputStream().close();
		
		// //获取所有响应头字段
		String content_encode = conn.getContentEncoding();

		InputStream in = conn.getInputStream();
		// 如果是gzip的压缩流 进行解压缩处理
		if (null != content_encode && !"".equals(content_encode) && content_encode.equals("gzip")) {
			try {
				in = new GZIPInputStream(conn.getInputStream());
			} catch (Exception e) {
			}
		}

		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		int len;
		byte[] buffer = new byte[1024];
		while ((len = in.read(buffer)) != -1) {
			arrayOutputStream.write(buffer, 0, len);
		}
		in.close();
		arrayOutputStream.close();
		conn.disconnect();
		str = new String(arrayOutputStream.toByteArray(), "utf-8");

		return str;
	}

	/** post提交请求 **/
	public static void asynPost(final HttpEntity entity, final CallBack callBack) {
		asynPost(null, null, entity, callBack);
	}

	/** post提交请求 **/
	public static void asynPost(final Activity context, String msg, final HttpEntity entity, final CallBack callBack) {
		if (context != null && !SystemUtil.contactNet(context)) {
			callBack.onFail(context.getResources().getString(R.string.no_network_connection_toast));
			return;
		}
		ProgressDialog tDialog = null;
		if (context != null) {
			try {
				tDialog = new ProgressDialog(context);
				tDialog.setMessage(msg);
				tDialog.setCancelable(true);
				tDialog.setCanceledOnTouchOutside(false);
			} catch (Exception e) {
			}
		}
		final ProgressDialog dialog = tDialog;
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (dialog != null && dialog.getWindow() != null && dialog.isShowing()) {
					try {
						dialog.cancel();
					} catch (Exception e) {
					}
				}
				if (callBack != null) {
					if (msg.what == 0) {
						callBack.onSuccess((String) msg.obj);
					} else {
						callBack.onFail((String) msg.obj);
					}
				}
			}
		};
		execute(new Runnable() {
			@Override
			public void run() {
				try {
					IgnitedHttpResponse httpResponse = getApiHttp().post(SERVER, entity).send();
					String content = httpResponse.getResponseBodyAsString();
					if (!TextUtils.isEmpty(content)) {
						Message msg = handler.obtainMessage(0);
						msg.obj = content;
						handler.sendMessage(msg);
					} else {
						Message msg = handler.obtainMessage(1);
						msg.obj = mContext.getResources().getString(R.string.no_network_connection_toast);// "网络链接不可用";
						handler.sendMessage(msg);
					}
				} catch (ConnectException e) {
					Message msg = handler.obtainMessage(1);
					msg.obj = mContext.getResources().getString(R.string.no_network_connection_toast);// "网络链接不可用";
					handler.sendMessage(msg);
				} catch (IOException e) {
					Message msg = handler.obtainMessage(2);
					msg.obj = "Read stram error.";
					handler.sendMessage(msg);
				}
			}
		});
	}

	/** get提交请求 **/
	public static void asynGet(Context context, String msg, final String url, final CallBack callBack) {
		asynGet(context, msg, url, false, callBack);
	}

	/** get提交请求 **/
	public static void asynGet(final String url, final boolean cache, final CallBack callBack) {
		asynGet(null, null, url, cache, callBack);
	}

	/** get提交请求 **/
	public static void asynGet(Context context, String msg, final String url, final boolean cache, final CallBack callBack) {
		// ProgressDialog tDialog = null;
		if (context != null) {
			// try {
			// tDialog = ProgressDialog.show(context, msg);
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
		}
		// final ProgressDialog dialog = tDialog;
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// if (dialog != null && dialog.isShowing()) {
				// dialog.dismiss();
				// }
				if (callBack != null) {
					if (msg.what == 0) {
						callBack.onSuccess((String) msg.obj);
					} else {
						callBack.onFail((String) msg.obj);
					}
				}
			}
		};
		execute(new Runnable() {
			@Override
			public void run() {
				try {
					IgnitedHttpResponse httpResponse = getApiHttp().get(url, cache).send();
					String content = httpResponse.getResponseBodyAsString();
					Message msg = handler.obtainMessage(0);
					msg.obj = content;
					handler.sendMessage(msg);
				} catch (ConnectException e) {
					Message msg = handler.obtainMessage(1);
					msg.obj = mContext.getResources().getString(R.string.no_network_connection_toast);// "网络链接不可用";
					handler.sendMessage(msg);
				} catch (IOException e) {
					Message msg = handler.obtainMessage(2);
					msg.obj = "Read stram error.";
					handler.sendMessage(msg);
				}
			}
		});
	}

	/** post提交请求 **/
	public static void asynPost(Context context, String msg, final String url, final HttpEntity entity, final CallBack callBack) {
		// ProgressDialog d = null;
		if (context != null) {
			// d = ProgressDialog.show(context, msg);
		}
		// final ProgressDialog dialog = d;
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// if (dialog != null && dialog.isShowing()) {
				// dialog.dismiss();
				// }
				if (callBack != null) {
					if (msg.what == 0) {
						callBack.onSuccess((String) msg.obj);
					} else {
						callBack.onFail((String) msg.obj);
					}
				}
			}
		};
		execute(new Runnable() {
			@Override
			public void run() {
				try {
					IgnitedHttpResponse httpResponse = getApiHttp().post(url, entity).send();
					String content = httpResponse.getResponseBodyAsString();
					Message msg = handler.obtainMessage(0);
					msg.obj = content;
					handler.sendMessage(msg);
				} catch (ConnectException e) {
					Message msg = handler.obtainMessage(1);
					msg.obj = mContext.getResources().getString(R.string.no_network_connection_toast);// "网络链接不可用";
					handler.sendMessage(msg);
				} catch (IOException e) {
					Message msg = handler.obtainMessage(2);
					msg.obj = "Read stram error.";
					handler.sendMessage(msg);
				}
			}
		});

	}

	/** 回调接口 **/
	public interface CallBack {
		void onSuccess(String o);

		void onFail(String msg);

		void onFinish(Object obj);
	}

	public static void execute(Runnable r) {
		Instance().getExecutor().execute(r);
	}

}
