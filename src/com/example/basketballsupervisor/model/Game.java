package com.example.basketballsupervisor.model;

import java.io.Serializable;
import java.util.List;

public class Game implements Serializable {
	public long gId;// [long][not null][gameId]
	public long pId;// [long][not null][平台ID]
	public String gameName;// [String][not null][比赛名字]
	public String gameRemark;// [String][null able][比赛描述]
	public List<Group> groupList;// [List<Group>][not null][比赛队伍,size=2]
	public List<Integer> role;// [List<Integer>][not
								// null][记录权限列表：1：记录groupList中1队数据，2：groupList中2对数据,3：记录创新数据]
	public String time;// [String][not null][比赛开始时间 yyyy-MM-dd hh:mm:ss]
	public String location;// [String][not null][比赛地址]
}
