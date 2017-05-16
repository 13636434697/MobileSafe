package com.xu.mobilesafe.activity;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.xu.mobilesafe.R;
import com.xu.mobilesafe.utils.ToastUtil;

public class EnterPsdActivity extends Activity {
	private String packagename;
	private TextView tv_app_name;
	private ImageView iv_app_icon;
	private EditText et_psd;
	private Button bt_submit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//获取包名
		packagename = getIntent().getStringExtra("packagename");
		setContentView(R.layout.activity_enter_psd);
		initUI();
		initData();
	}

	//初始化数据
	private void initData() {
		//通过传递过来的包名获取拦截应用的图标以及名称
		PackageManager pm = getPackageManager();
		try {
			ApplicationInfo applicationInfo = pm.getApplicationInfo(packagename,0);
			Drawable icon = applicationInfo.loadIcon(pm);
			iv_app_icon.setBackgroundDrawable(icon);
			tv_app_name.setText(applicationInfo.loadLabel(pm).toString());
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		//确认拦截按钮的点击事件
		bt_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String psd = et_psd.getText().toString();
				if(!TextUtils.isEmpty(psd)){
					if(psd.equals("123")){
						//解锁,进入应用,告知看门口不要再去监听以及解锁的应用,发送广播
						//自定义的一个action，去看门狗的服务里面去匹配
						Intent intent = new Intent("android.intent.action.SKIP");
						intent.putExtra("packagename",packagename);
						sendBroadcast(intent);
						//结束掉就拦截了
						finish();
					}else{
						ToastUtil.show(getApplicationContext(), "密码错误");
					}
				}else{
					ToastUtil.show(getApplicationContext(), "请输入密码");
				}
			}
		});
	}

	private void initUI() {
		tv_app_name = (TextView) findViewById(R.id.tv_app_name);
		iv_app_icon = (ImageView) findViewById(R.id.iv_app_icon);
		
		et_psd = (EditText) findViewById(R.id.et_psd);
		bt_submit = (Button) findViewById(R.id.bt_submit);
	}

	//按回退按钮的时候，看门狗服务，还会继续拦截，反复进入应用，所以换思路，重写这个事件，直接跳到桌面
	@Override
	public void onBackPressed() {
		//通过隐式意图,跳转到桌面（桌面就是一个应用而已，所以直接打开桌面的activity）
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
		super.onBackPressed();
	}
}
