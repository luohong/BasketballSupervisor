package com.example.basketballsupervisor.model;

import java.util.List;

public class RoleRecord {
	public long group_id;//[long id][not null][比赛队伍ID，如果记录是创新数据的话，则填0]
	public long game_id;//[long ][not null][比赛ID]
	public int recordType;//[int][not null][记录类型，1：球队记录(一般性记录),2:创新记录]
	public List<BBGameRecord> bb_game_record;//[List<BBGameRecord>][not null][比赛具体记录]
}
