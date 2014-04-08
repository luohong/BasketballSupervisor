package com.example.basketballsupervisor.model;

import java.io.Serializable;

public class Action implements Serializable {
	
	public int id;
	public int nextActionId;
	public String name;
	public int score;
	public int cancelable;
	public int type;// 1 表示得分, 0表示失分

	public Action() {
		
	}
	
	public Action(int id, int nextActionId, String name, int score, int cancelable) {
		this.id = id;
		this.nextActionId = nextActionId;
		this.name = name;
		this.score = score;
		this.cancelable = cancelable;
	}
	
}
