package com.example.basketballsupervisor.model;


public class GameRecord {
	public long id;// [long][not null][比赛记录的ID]
	public long game_id;// [long][not null][所属比赛ID]
	public long group_id;// [long][not null][所属球队ID]
	public long member_id;// [long][not null][球员ID]
	public long action_id;// [long][not null][动作ID，代表技术动作，如二分投篮命中等]
	public String coordinate;// [String][not null][坐标,如：13,12，13代表第13列，12代表12行，中间用”,”分割]
	public int recordType;// [int][not null][记录类型，1：球队记录(一般性记录),2:创新记录]
	public String action_time;// [Date][not null][行为发生时间]
}
