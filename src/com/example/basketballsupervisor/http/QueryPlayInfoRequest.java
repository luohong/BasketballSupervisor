package com.example.basketballsupervisor.http;

import com.example.basketballsupervisor.config.Config;

public class QueryPlayInfoRequest extends BaseRequest<QueryPlayInfoResponse> {

	public int isAll;//[int][not null][0:未读，1:全部]
	
	public QueryPlayInfoRequest(int isAll) {
		super("400002");
		this.isAll = isAll;
	}

	@Override
	protected QueryPlayInfoResponse getNewInstance() {
		return new QueryPlayInfoResponse();
	}

	@Override
	protected QueryPlayInfoResponse fromJson(String json) throws Throwable {
		return Config.mGson.fromJson(json, QueryPlayInfoResponse.class);
	}

}
