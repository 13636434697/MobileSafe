package com.xu.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.xu.mobilesafe.R;
import com.xu.mobilesafe.utils.ConstantValue;
import com.xu.mobilesafe.utils.SpUtil;
import com.xu.mobilesafe.utils.ToastUtil;

public class Setup3Activity extends BaseSetupActivity {
	private EditText et_phone_number;
	private Button bt_select_number;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup3);
		
		initUI();
	}

	//父类方法的下一页
	@Override
	public void showNextPage() {
		//点击按钮以后,需要获取输入框中的联系人,再做下一页操作
		String phone = et_phone_number.getText().toString();

		//在sp存储了相关联系人以后才可以跳转到下一个界面
//		String contact_phone = SpUtil.getString(getApplicationContext(), ConstantValue.CONTACT_PHONE, "");
		if(!TextUtils.isEmpty(phone)){
			Intent intent = new Intent(getApplicationContext(), Setup4Activity.class);
			startActivity(intent);

			finish();

			//如果现在是输入电话号码,则需要去保存
			SpUtil.putString(getApplicationContext(), ConstantValue.CONTACT_PHONE, phone);

			//开启平移动画。参数：下一页进来的动画，下一页出去的动画
			overridePendingTransition(R.anim.next_in_anim, R.anim.next_out_anim);
		}else{
			ToastUtil.show(this,"请输入电话号码");
		}
	}

	//父类方法的上一页
	@Override
	public void showPrePage() {
		Intent intent = new Intent(getApplicationContext(), Setup2Activity.class);
		startActivity(intent);

		finish();

		//开启平移动画。参数：上一页进来的动画，上一页出去的动画
		overridePendingTransition(R.anim.pre_in_anim, R.anim.pre_out_anim);
	}

	private void initUI() {
		//显示电话号码的输入框
		et_phone_number = (EditText) findViewById(R.id.et_phone_number);

		//获取联系人电话号码回显过程
		String phone = SpUtil.getString(this, ConstantValue.CONTACT_PHONE, "");
		//取到号码后直接设置到edittext上
		et_phone_number.setText(phone);
		
		//点击选择联系人的对话框
		bt_select_number = (Button) findViewById(R.id.bt_select_number);
		//选择联系人的点击事件
		bt_select_number.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ContactListActivity.class);
				//开启一个activity，还要activity返回结果
				startActivityForResult(intent, 0);
			}
		});
	}
	//开启一个activity，还要activity返回结果
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//带结果，data，要做容错处理，因为可能会直接返回
		if(data!=null){
			//1,返回到当前界面的时候,接受结果的方法
			String phone = data.getStringExtra("phone");
			//2,将特殊字符过滤(中划线转换成空字符串)
			phone = phone.replace("-", "").replace(" ", "").trim();
			et_phone_number.setText(phone);
			
			//3,存储联系人至sp中
			SpUtil.putString(getApplicationContext(), ConstantValue.CONTACT_PHONE, phone);
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
}
