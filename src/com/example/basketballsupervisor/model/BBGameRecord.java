package com.example.basketballsupervisor.model;

public class BBGameRecord {
	public long member_id;// [long][not null][球员ID]
	public long group_id;// [long][not null][球队ID]
	public long action_id;// [long][not null][动作ID，代表技术动作，如二分投篮命中等]
	public String coordinate;// [String][not null][坐标,如：13,12，13代表第13列，12代表12行，中间用”,”分割]
	public long action_time;// [long][not null][动作发生时间，毫秒Date.getTime]
}
