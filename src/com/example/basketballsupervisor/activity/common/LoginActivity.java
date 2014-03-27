package com.example.basketballsupervisor.activity.common;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.activity.BaseActivity;

public class LoginActivity extends BaseActivity {

	private TextView tvTitle;
	private Button btnRegister;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}
	
	@Override
	public void onInit() {

	}

	@Override
	public void onFindViews() {
		tvTitle = (TextView) findViewById(R.id.tv_title);
		btnRegister = (Button) findViewById(R.id.btn_right);
	}

	@Override
	public void onInitViewData() {
		tvTitle.setText("登录");
		btnRegister.setText("注册");
		btnRegister.setVisibility(View.VISIBLE);
		Drawable rightArrow = getResources().getDrawable(R.drawable.ic_right_arrow);
		btnRegister.setCompoundDrawablesWithIntrinsicBounds(null, null, rightArrow, null);
	}

	@Override
	public void onBindListener() {

	}

}
