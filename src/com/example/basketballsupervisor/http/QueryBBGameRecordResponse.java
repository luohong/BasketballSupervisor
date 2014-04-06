package com.example.basketballsupervisor.http;

import java.util.List;

import com.example.basketballsupervisor.model.GameRecord;

public class QueryBBGameRecordResponse extends BaseResponse {
	public int is_complete;//[int][not null][1:代表数据已经完整，0：代表数据尚未完整,等待其他组成部分上报完成后再次获取]
	public List<GameRecord> game_record_list;//[List<GameRecord>][not null][当is_complete返回为1时，会携带这部分记录数据]
}
