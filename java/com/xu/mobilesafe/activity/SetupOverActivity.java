package com.xu.mobilesafe.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.xu.mobilesafe.R;
import com.xu.mobilesafe.utils.ConstantValue;
import com.xu.mobilesafe.utils.SpUtil;

/*
* 设置防盗开启完成
* */
public class SetupOverActivity extends Activity {
	private TextView tv_phone;
	private TextView tv_reset_setup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//是否开启防盗
		boolean setup_over = SpUtil.getBoolean(this, ConstantValue.SETUP_OVER, false);
		if(setup_over){
			//密码输入成功,并且四个导航界面设置完成----->停留在设置完成功能列表界面
			setContentView(R.layout.activity_setup_over);
		}else{
			//密码输入成功,四个导航界面没有设置完成----->跳转到导航界面第1个
			Intent intent = new Intent(this, Setup1Activity.class);
			startActivity(intent);
			
			//开启了一个新的界面以后,关闭功能列表界面
			finish();
		}
	}

	//初始化UI
	private void initUI() {
		tv_phone = (TextView) findViewById(R.id.tv_phone);
		//获取指定联系人号码
		String phone = SpUtil.getString(this,ConstantValue.CONTACT_PHONE, "");
		//把获取到的电话号码设置给textView
		tv_phone.setText(phone);

		//重新设置条目被点击
		//让TextView具备可点击的操作，设置一个点击事件（TextView，ImageView默认没有点击事件）（Button默认具备点击事件）
		tv_reset_setup = (TextView) findViewById(R.id.tv_reset_setup);
		//给textView设置点击事件
		tv_reset_setup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), Setup1Activity.class);
				startActivity(intent);
				finish();
			}
		});
	}
}
