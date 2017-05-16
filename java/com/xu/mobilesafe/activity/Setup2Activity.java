package com.xu.mobilesafe.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.xu.mobilesafe.R;
import com.xu.mobilesafe.utils.ConstantValue;
import com.xu.mobilesafe.utils.SpUtil;
import com.xu.mobilesafe.utils.ToastUtil;
import com.xu.mobilesafe.view.SettingItemView;

public class Setup2Activity extends BaseSetupActivity {
	private SettingItemView siv_sim_bound;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup2);
		
		initUI();
	}

	//父类方法的下一页
	@Override
	public void showNextPage() {
		//取出序列号
		String serialNumber = SpUtil.getString(this, ConstantValue.SIM_NUMBER, "");
		//如果序列号为空
		if(!TextUtils.isEmpty(serialNumber)){
			//才能跳到下一页
			Intent intent = new Intent(getApplicationContext(), Setup3Activity.class);
			startActivity(intent);

			finish();

			//开启平移动画。参数：下一页进来的动画，下一页出去的动画
			overridePendingTransition(R.anim.next_in_anim, R.anim.next_out_anim);
		}else{
			ToastUtil.show(this,"请绑定sim卡");
		}
	}

	//父类方法的上一页
	@Override
	public void showPrePage() {
		Intent intent = new Intent(getApplicationContext(), Setup1Activity.class);
		startActivity(intent);

		finish();

		//开启平移动画。参数：上一页进来的动画，上一页出去的动画
		overridePendingTransition(R.anim.pre_in_anim, R.anim.pre_out_anim);
	}

	//初始化布局
	private void initUI() {
		siv_sim_bound = (SettingItemView) findViewById(R.id.siv_sim_bound);
		//1,回显(读取已有的绑定状态,用作显示,sp中是否存储了sim卡的序列号)，如果没有返回空字符串
		String sim_number = SpUtil.getString(this, ConstantValue.SIM_NUMBER, "");
		//2,判断是否序列卡号为""
		if(TextUtils.isEmpty(sim_number)){
			//序列卡号没有存储过，肯定是false
			siv_sim_bound.setCheck(false);
		}else{
			siv_sim_bound.setCheck(true);
		}

		//checkbox的点击事件
		siv_sim_bound.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//3,获取原有的状态
				boolean isCheck = siv_sim_bound.isCheck();
				//4,将原有状态取反
				//5,状态设置给当前条目
				siv_sim_bound.setCheck(!isCheck);

				//先在要存储状态，绑定的时候存储
				if(!isCheck){
					//6,存储(序列卡号)
						//6.1获取sim卡序列号TelephoneManager
						TelephonyManager manager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
						//6.2获取sim卡的序列卡号
						String simSerialNumber = manager.getSimSerialNumber();
						//6.3存储
						SpUtil.putString(getApplicationContext(), ConstantValue.SIM_NUMBER, simSerialNumber);
				}else{
					//7,将存储序列卡号的节点,从sp中删除掉
					SpUtil.remove(getApplicationContext(), ConstantValue.SIM_NUMBER);
				}
			}
		});
	}
}
