package com.example.basketballsupervisor.util;

public class GameUtils {

	public static String formGameTime(int count) {
		StringBuffer gameTime = new StringBuffer();
		
		int time = count / 1000;
		
		int minutes = time / 60;
		if (minutes < 10) {
			gameTime.append("0");
		}
		gameTime.append(minutes);

		gameTime.append(":");
		
		int second = time % 60;
		if (second < 10) {
			gameTime.append("0");
		}
		gameTime.append(second);
		
		gameTime.append(" | ");
		int milisecends = (count % 1000) / 100;
		if (milisecends < 10) {
			gameTime.append("0");
		}
		gameTime.append(milisecends);
		
		return gameTime.toString();
	}
	
	public static String formatHitRatePercent(float rate) {
		String percent = Float.toString(rate);
		if ("NaN".equals(percent)) {
			percent = "0";
		} else if (percent.startsWith("100.")) {
			percent = "100";
		} else if (percent.length() > 4) {
			percent = percent.substring(0, 4);
		}
		return percent + "%";
	}

	public static String formatPlayingTime(Long time) {
		StringBuffer playingTime = new StringBuffer();
		if (time != null) {
			time = time / 1000;
			
			long minutes = time / 60;
			if (minutes < 10) {
				playingTime.append("0");
			}
			playingTime.append(minutes);

			playingTime.append(":");
			
			long second = time % 60;
			if (second < 10) {
				playingTime.append("0");
			}
			playingTime.append(second);
		} else {
			playingTime.append("00:00");
		}
		return playingTime.toString();
	}
}
