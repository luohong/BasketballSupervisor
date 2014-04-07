package com.example.basketballsupervisor.activity.common;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.basketballsupervisor.IApplication;
import com.example.basketballsupervisor.R;
import com.example.basketballsupervisor.activity.BaseActivity;
import com.example.basketballsupervisor.activity.MainActivity;
import com.example.basketballsupervisor.config.Config;
import com.example.basketballsupervisor.config.Config.CallBack;
import com.example.basketballsupervisor.http.TotalLoginRequest;
import com.example.basketballsupervisor.http.TotalLoginResponse;
import com.example.basketballsupervisor.model.User;
import com.example.basketballsupervisor.util.SpUtil;

public class LoginActivity extends BaseActivity implements OnClickListener {

	private TextView tvTitle;
	private Button btnRegister;
	private Button btnCommit;
	private EditText etUsername;
	private EditText etPassword;
	private TextView btnForgetPassword;

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
		btnForgetPassword = (TextView) findViewById(R.id.btn_forget_password);
	}

	@Override
	public void onInitViewData() {
		tvTitle.setText("登录");
//		btnRegister.setText("注册");
		btnRegister.setVisibility(View.GONE);
//		Drawable rightArrow = getResources().getDrawable(R.drawable.ic_right_arrow);
//		btnRegister.setCompoundDrawablesWithIntrinsicBounds(null, null, rightArrow, null);
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
			showToast("请输入用户名");
		} else if (username.contains(" ")) {
			showToast("用户名不能包含空格");
//		} else if (username.length() != 11) {
//			showToast("手机号码长度不对");
		} else {
			check = true;
		}
		
		return check;
	}
	
	public String getUsername() {
		return etUsername.getText().toString();
	}
	
	private boolean checkPassword() {
		String password = getPassword();
		
		boolean check = false;
		if (TextUtils.isEmpty(password)) {
			showToast("请输入密码");
		} else if (password.contains(" ")) {
			showToast("密码不能包含空格");
		} else if (password.length() < 6 || password.length() > 30) {
			showToast("密码由6~30位字母，数字和下划线组成");
		} else {
			check = true;
		}
		
		return check;
	}
	
	public String getPassword() {
		return etPassword.getText().toString();
	}
	
	private void requestLogin() {
		String username = getUsername();
		String password = getPassword();
		
		final TotalLoginRequest request = new TotalLoginRequest(username, password);
		Config.asynPost(this, "正在登录", request.getData(), new CallBack() {
			
			@Override
			public void onSuccess(String o) {
				TotalLoginResponse response = request.getObject(o);
				if (response != null) {
					if (response.isSuccess()) {
						showToastShort("登录成功");
						
						SpUtil sp = SpUtil.getInstance(getActivity());
						User user = sp.getUser();
						user.token = response.token;
						sp.setUser(user);
						
						gotoMain();
					} else {
						onFail(response.error_remark);
					}
				} else {
					onFail(null);
				}
			}
			
			@Override
			public void onFinish(Object obj) {
			}
			
			@Override
			public void onFail(String msg) {
				if (TextUtils.isEmpty(msg)) {
					msg = "用户名或密码错误";
				}
				showToastShort(msg);
			}
		});
	}

	protected void gotoMain() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		
		finish();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		IApplication.hasStart = false;
	}

}
