package com.example.basketballsupervisor.model;

import java.io.Serializable;

public class Action implements Serializable {

	public long id;
	public long nextActionId;
	public long name;
	public long score;
	public int cancelable;
	
}
