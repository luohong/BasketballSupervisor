package com.example.basketballsupervisor.activity.common;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.activity.BaseActivity;

public class LoginActivity extends BaseActivity implements OnClickListener {

	private TextView tvTitle;
	private Button btnRegister;
	private Button btnCommit;
	private EditText etUsername;
	private EditText etPassword;
	private Button btnForgetPassword;

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
		etUsername = (EditText) findViewById(R.id.et_username);
		etPassword = (EditText) findViewById(R.id.et_password);
		btnCommit = (Button) findViewById(R.id.btn_commit);
		btnForgetPassword = (Button) findViewById(R.id.btn_forget_password);
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
		btnCommit.setOnClickListener(this);
		btnForgetPassword.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_commit:
			if (checkUsername() && checkPassword()) {
				requestLogin();
			}
			break;
		case R.id.btn_forget_password:
			break;
		}
	}

	private boolean checkUsername() {
		String username = getUsername();
		
		boolean check = false;
		if (TextUtils.isEmpty(username)) {
			showToast("请填写注册的手机号码");
		} else if (username.contains(" ")) {
			showToast("手机号码不能包含空格");
		} else if (username.length() != 11) {
			showToast("手机号码长度不对");
		} else {
			check = true;
		}
		
		return check;
	}
	
	public String getUsername() {
		return etUsername.getText().toString();
	}
	
	private boolean checkPassword() {
		String username = getUsername();
		return !TextUtils.isEmpty(username);
	}
	
	public String getPassword() {
		return etPassword.getText().toString();
	}
	
	private void requestLogin() {
		
	}

}
