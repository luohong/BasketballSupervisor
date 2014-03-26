package com.example.basketballsupervisor.model;

import java.io.Serializable;

public class GameTime implements Serializable {

	public long id;
	public long gameId;
	public long groupRequestId;
	
	public String suspendTime;
	public String continueTime;
	
}
