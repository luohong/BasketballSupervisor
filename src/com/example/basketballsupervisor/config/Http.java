package com.example.basketballsupervisor.config;

import android.content.Context;

import com.example.basketballsupervisor.config.http.IgnitedHttp;

public class Http extends IgnitedHttp {

	public Http(Context context) {
		super(context);
		init();
	}

	private void init() {
		enableResponseCache(25, 1440, 8);
		setDefaultHeader("Accept-Encoding", "gzip");
	}

}
