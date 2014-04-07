package com.example.basketballsupervisor.model;

import java.io.Serializable;

public class Action implements Serializable {

	public long id;
	public long nextActionId;
	public String name;
	public int score;
	public int cancelable;
	
}
