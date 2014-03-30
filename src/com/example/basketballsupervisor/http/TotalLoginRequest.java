package com.example.basketballsupervisor.http;

import com.example.basketballsupervisor.config.Config;

public class TotalLoginRequest extends BaseRequest<TotalLoginResponse> {
	
	public String username;// [String][not null][手机号码]
	public String pass_word;// [String][not null][密码]
	
	public TotalLoginRequest(String username, String password) {
		super("400001");
		this.username = username;
		this.pass_word = password;
	}

	@Override
	protected TotalLoginResponse getNewInstance() {
		return new TotalLoginResponse();
	}
	
	@Override
	protected TotalLoginResponse fromJson(String json) throws Throwable {
		return Config.mGson.fromJson(json, TotalLoginResponse.class);
	}
}
