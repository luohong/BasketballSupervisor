package com.example.basketballsupervisor.model;

import java.io.Serializable;

public class Record implements Serializable {
	
	public long id;
	public long gameId;
	public long groupId;
	public long memberId;
	
	public long actionId;
	public String showTime;
	public String createTime;
	public String coordinate;

	public Record() {
	}

	public Record(GameRecord gameRecord) {
//		id = gameRecord.id;
		gameId = gameRecord.game_id;
		groupId = gameRecord.group_id;
		memberId = gameRecord.member_id;
		actionId = gameRecord.action_id;
		showTime = gameRecord.action_time;
		createTime = gameRecord.action_time;
		coordinate = gameRecord.coordinate;
	}
	
}
