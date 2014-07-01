package com.example.basketballsupervisor.util;

public class Constants {

	
	public static String[] GROUP_DATA_STAT_COLUMNS = new String[] { "球队\\统计项",
			"总得分", "总出手命中次数（不含罚球）", "总出手次数（不含罚球）", "总命中率（总命中率中不含罚球命中率）",
			"2分球命中次数", "2分球出手次数", "2分球命中率", "3分球命中次数", "3分球出手次数", "3分球命中率",
			"罚球命中次数", "罚球出手次数", "罚球命中率", "前场篮板", "后场篮板", "总篮板", "助攻", "抢断",
			"封盖", "被犯规", "犯规", "失误" };
	public static String[] MEMBER_DATA_STAT_COLUMNS = new String[] { "球队",
			"球员\\统计项", "总得分", "总出手命中次数（不含罚球）", "总出手次数（不含罚球）",
			"总命中率（总命中率中不含罚球命中率）", "2分球命中次数", "2分球出手次数", "2分球命中率", "3分球命中次数",
			"3分球出手次数", "3分球命中率", "罚球命中次数", "罚球出手次数", "罚球命中率", "前场篮板", "后场篮板",
			"总篮板", "助攻", "抢断", "封盖", "被犯规", "犯规", "失误", "上场时间" };
	public static String[] INNOVATE_DATA_STAT_COLUMNS = new String[] { "球队",
			"球员\\统计项", "一条龙", "超远3分", "绝杀", "最后3秒得分", "晃倒", "2+1", "3+1", "扣篮",
			"快攻", "2罚不中", "3罚不中", "被晃倒" };
	
	public interface CourtShowType {
		
		/**
		 * 常规（仅记录、不显示）
		 */
		public static final int NORMAL = 0;

		/**
		 * 命中（打勾）
		 */
		public static final int HIT = 1;
		
		/**
		 * 未命中（打叉）
		 */
		public static final int MISS = 2;
	}
	
}
