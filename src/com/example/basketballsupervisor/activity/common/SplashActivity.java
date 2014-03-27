package com.example.basketballsupervisor.activity.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.activity.BaseActivity;
import com.example.basketballsupervisor.activity.MainActivity;
import com.example.basketballsupervisor.config.Config;
import com.example.basketballsupervisor.model.User;

public class SplashActivity extends BaseActivity {

	private View llSplash;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		startSplash();
	}

	@Override
	public void onInit() {

	}

	@Override
	public void onFindViews() {
		llSplash = findViewById(R.id.ll_splash);
	}

	@Override
	public void onInitViewData() {

	}

	@Override
	public void onBindListener() {

	}

	private void startSplash() {
		llSplash.startAnimation(getAlphaInAnimation(2000));
	}

	private Animation getAlphaInAnimation(int time) {
		Animation ani = new AlphaAnimation(0.1f, 1.0f);
		ani.setDuration(time);
		ani.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				User user = Config.getUser();
				Intent intent = null;
				if (user != null) {
					intent = new Intent(SplashActivity.this, MainActivity.class);
				} else {
					intent = new Intent(SplashActivity.this, LoginActivity.class);
				}
				startActivity(intent);
				
		        finish();
			}
		});
		return ani;
	}
}
