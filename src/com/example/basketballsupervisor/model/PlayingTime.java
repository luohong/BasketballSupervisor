package com.example.basketballsupervisor.model;

import java.io.Serializable;

public class PlayingTime implements Serializable {

	public long id;
	public long gameId;
	public long groupId;
	public long memberId;
	
	public String startTime;
	public String endTime;
	
}
