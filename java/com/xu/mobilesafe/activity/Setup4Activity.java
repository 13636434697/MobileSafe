package com.xu.mobilesafe.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.xu.mobilesafe.R;
import com.xu.mobilesafe.utils.ConstantValue;
import com.xu.mobilesafe.utils.SpUtil;
import com.xu.mobilesafe.utils.ToastUtil;

public class Setup4Activity extends BaseSetupActivity {
	private CheckBox cb_box;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup4);
		
		initUI();
	}

	//父类方法的下一页
	@Override
	public void showNextPage() {

		//拿到sp中拿到是否开启防盗总闸的变量
		boolean open_security = SpUtil.getBoolean(this, ConstantValue.OPEN_SECURITY, false);

		if(open_security){
			Intent intent = new Intent(getApplicationContext(), SetupOverActivity.class);
			startActivity(intent);

			finish();
			//下一页跳转完成之后要存储更新一下是否设置完成
			SpUtil.putBoolean(this, ConstantValue.SETUP_OVER, true);

			//开启平移动画。参数：下一页进来的动画，下一页出去的动画
			overridePendingTransition(R.anim.next_in_anim, R.anim.next_out_anim);
		}else{
			ToastUtil.show(getApplicationContext(), "请开启防盗保护");
		}

	}

	//父类方法的上一页
	@Override
	public void showPrePage() {
		Intent intent = new Intent(getApplicationContext(), Setup3Activity.class);
		startActivity(intent);

		finish();

		//开启平移动画。参数：上一页进来的动画，上一页出去的动画
		overridePendingTransition(R.anim.pre_in_anim, R.anim.pre_out_anim);
	}

	private void initUI() {
		cb_box = (CheckBox) findViewById(R.id.cb_box);
		//1,是否选中状态的回显，打开安全设置，初次是false
		boolean open_security = SpUtil.getBoolean(this, ConstantValue.OPEN_SECURITY, false);
		//2,根据状态,修改checkbox后续的文字显示
		cb_box.setChecked(open_security);
		//原有的状态取反
		if(open_security){
			cb_box.setText("安全设置已开启");
		}else{
			cb_box.setText("安全设置已关闭");
		}
		
//		cb_box.setChecked(!cb_box.isChecked());
		//3,点击过程中,监听选中状态发生改变过程,
		cb_box.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//4,isChecked点击后的状态,存储点击后状态，这个状态就是取反的状态，不用手动取反了
				SpUtil.putBoolean(getApplicationContext(), ConstantValue.OPEN_SECURITY, isChecked);
				//5,根据开启关闭状态,去修改显示的文字
				if(isChecked){
					cb_box.setText("安全设置已开启");
				}else{
					cb_box.setText("安全设置已关闭");
				}
			}
		});

	}

}
