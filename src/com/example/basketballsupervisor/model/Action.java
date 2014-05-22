package com.example.basketballsupervisor.model;

import java.io.Serializable;

public class Action implements Serializable, Cloneable {
	
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
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public static Action newFoulAction() {
		return new Action(13,14,"犯规",0,0);
	}

	public static Action newFouledAction() {
		return new Action(14,-12,"被犯规",0,0);
	}
	
}
