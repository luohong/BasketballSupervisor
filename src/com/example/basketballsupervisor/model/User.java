package com.example.basketballsupervisor.model;

import android.text.TextUtils;

public class User {
	public String token;
	
	public boolean isLogin() {
		return !TextUtils.isEmpty(token);
	}
}
