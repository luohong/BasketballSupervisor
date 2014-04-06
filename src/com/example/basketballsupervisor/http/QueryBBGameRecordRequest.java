package com.example.basketballsupervisor.http;

import com.example.basketballsupervisor.config.Config;

public class QueryBBGameRecordRequest extends
		BaseRequest<QueryBBGameRecordResponse> {
	
	public long game_id;//[long][not null][要请求的比赛ID]

	public QueryBBGameRecordRequest(long gameId) {
		super("400004");
		game_id = gameId;
	}

	@Override
	protected QueryBBGameRecordResponse getNewInstance() {
		return new QueryBBGameRecordResponse();
	}

	@Override
	protected QueryBBGameRecordResponse fromJson(String json) throws Throwable {
		return Config.mGson.fromJson(json, QueryBBGameRecordResponse.class);
	}

}
