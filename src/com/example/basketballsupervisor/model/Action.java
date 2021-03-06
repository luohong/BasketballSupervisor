package com.example.basketballsupervisor.model;

import java.io.Serializable;

import com.example.basketballsupervisor.util.Constants;

public class Action implements Serializable, Cloneable {
	
	public int id;
	public int nextActionId;
	public String name;
	public int score;
	public int type;// 0 表示默认，1 表示得分，2表示失分

	public Action() {
		
	}
	
	public Action(int id, int nextActionId, String name, int score, int type) {
		this.id = id;
		this.nextActionId = nextActionId;
		this.name = name;
		this.score = score;
		this.type = type;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public static Action newFoulAction() {
		return new Action(13,14,"犯规",0,Constants.CourtShowType.NORMAL);
	}

	public static Action newFouledAction() {
		return new Action(14,-12,"被犯规",0,Constants.CourtShowType.NORMAL);
	}

	public static Action newAddOnePoint() {
		return new Action(29,-100,"加一分",1,Constants.CourtShowType.NORMAL);
	}

	public static Action newMinusOnePoint() {
		return new Action(30,-100,"减一分",-1,Constants.CourtShowType.NORMAL);
	}
	
}
