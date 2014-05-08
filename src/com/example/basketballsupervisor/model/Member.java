package com.example.basketballsupervisor.model;

import java.io.Serializable;

public class Member implements Serializable {
	public long memberId;// [long][not null][队员ID]
	public String name;// [String][not null][队员名字]
	public String number;// [String][not null][号码]
	public String site;// [String][not null][位置]
	public int isLeader;// [int][not null][是否为队长，1:队长,0:队员]
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		
		if (!(o instanceof Member)) {
			return false;
		}
		
		Member other = (Member) o;
		return memberId == other.memberId;
	}
}
