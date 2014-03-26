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
	public String remark;
	
}
