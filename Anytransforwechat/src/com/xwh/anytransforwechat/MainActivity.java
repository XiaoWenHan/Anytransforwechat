package com.xwh.anytransforwechat;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

public class MainActivity extends ActionBarActivity {

	private IWXAPI iwxapi;
	public static final String APP_ID = "wxb41ec03a8c801ab5";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findView();
		init();
	}

	// ��ʼ������
	private void init() {
		iwxapi = WXAPIFactory.createWXAPI(this, null);
		iwxapi.registerApp(APP_ID);
	}

	// ��ʼ���ؼ�
	private void findView() {
		getSupportActionBar().hide();
	}

}
