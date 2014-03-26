package com.example.basketballsupervisor.http;

import java.io.Serializable;
import java.util.List;

public class BaseResponse implements Serializable {
	public String ret_code;// [string][not null][响应码]
	public String error_remark;// [string][null able][错误描述]
	public List<String> next_code;// [list][null able][操作码指示序列]
	public int active_m_s;// [int][null able][心跳包时间间隔配置]
	public String current_action_code;// [String][not null][对应请求的操作码]
	public long user_id;// 服务端生成的用户ID

	public boolean isSuccess() {
		return "0000".equals(ret_code);
	}

	public class PublicSnsInfo {
		public String version;// [String][not null][当前版本]
		public List<PublicSnsAccessToken> psa_list;// [List<PublicSnsAccessToken>][not
													// null][公用账号列表]

	}

	public class PublicSnsAccessToken {
		public int sns_id;// [int][not null][1:新浪,2:QQ,3:人人网]
		public String s_id;// [String][not null][SNS域用户标识]
		public String access_token;// [String][not null][SNS域授权信息]
		// 注：当sns_id为2时，access_token字段格式为access_token&access_token_secret
	}

}
