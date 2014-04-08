package com.example.basketballsupervisor.http;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.util.Log;

import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.config.Config;
import com.example.basketballsupervisor.util.SpUtil;
import com.example.basketballsupervisor.util.SystemUtil;

public abstract class BaseRequest<T extends BaseResponse> {

	public String action_code;// [string][not null][指令码]
	public String token;// [string][null able][用户令牌,云端生成]
	public long timestamp;// [long][not null][时间戳：毫秒数]
	public UaInfo ua;// [UaInfo][null able][Ua系统]
	public VersionInfo version;// [VersionInfo][not null][版本结构]
	public String secret_key;// [string][not null][交互密钥]

	public BaseRequest(String actionCode) {
		Context context = Config.getContext();
		
		action_code = actionCode;
		token = SpUtil.getInstance(context).getUser().token;
		timestamp = System.currentTimeMillis();
		ua = new UaInfo();
		version = new VersionInfo(SystemUtil.getAppVersion(context) + "", null, null);
		secret_key = Md5Util.md5(actionCode + version.app_version + timestamp
				+ Config.KEY);
	}

	public HttpEntity getData() {
	    String content = Config.mGson.toJson(this);
		Log.d("HTTP", "Http Request: " + content);
		
		StringEntity entity = null;
		try {
			entity = new StringEntity(content, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
		    e.printStackTrace();
        }
		return entity;
	}

	public T getObject(String json) {
		T data;
		try {
			data = fromJson(json);
		} catch (Throwable e) {
			e.printStackTrace();
			data = getNewInstance();
			data.ret_code = "9999";
			data.error_remark = Config.getContext().getResources().getString(R.string.no_network_connection_toast);//"网络链接不可用";// Server error.
		}
		try {
			//真滴所有接口响应做心跳间隔变化处理
			if(data.active_m_s > 0){
				Config.HEART_BEAT_DELAY = data.active_m_s;
			}
			//针对所有接口响应做TOKEN过期处理
			if ("1002".equals(data.ret_code)) {// 用户Token过期
//                if(Config.getUser().isLogin()){
//                	Config.getUser().setToken("");
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Config.logout();
//                        }
//                    }).start();
//                }
			}
//			Config.getUser().setUserId(data.user_id);
		} catch (Exception e) {
		    Log.e("BaseRequest", "data:" + data + "json:" + json);
			e.printStackTrace();
		}

		return data;
	}

	protected abstract T getNewInstance();

	protected abstract T fromJson(String json) throws Throwable;

}
