package com.example.basketballsupervisor.http;

import java.util.List;

import com.example.basketballsupervisor.config.Config;
import com.example.basketballsupervisor.model.RoleRecord;

public class ReportGameRecordRequest extends
		BaseRequest<ReportGameRecordResponse> {
	
	public List<RoleRecord> records;// [List<RoleRecord>][not null][分角色比赛记录数据]

	public ReportGameRecordRequest(List<RoleRecord> records) {
		super("400003");
		this.records = records;
	}

	@Override
	protected ReportGameRecordResponse getNewInstance() {
		return new ReportGameRecordResponse();
	}

	@Override
	protected ReportGameRecordResponse fromJson(String json) throws Throwable {
		return Config.mGson.fromJson(json, ReportGameRecordResponse.class);
	}

}
