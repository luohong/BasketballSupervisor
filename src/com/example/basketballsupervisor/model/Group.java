package com.example.basketballsupervisor.model;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {
	public long gruopId;// [long][not null][队ID]
	public String groupName;// [String][not null][队名]
	public String Slogan;// [String][null able][导语]
	public List<Member> memberList;// [List<Member>][not null][成员数据]
}
