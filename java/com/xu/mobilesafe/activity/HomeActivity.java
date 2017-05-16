package com.xu.mobilesafe.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.app.AlertDialog.Builder;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xu.mobilesafe.R;
import com.xu.mobilesafe.receiver.DeviceAdmin;
import com.xu.mobilesafe.utils.ConstantValue;
import com.xu.mobilesafe.utils.Md5Util;
import com.xu.mobilesafe.utils.SpUtil;
import com.xu.mobilesafe.utils.ToastUtil;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;

public class HomeActivity extends Activity {
	private GridView gv_home;
	private String[] mTitleStrs;
	private int[] mDrawableIds;

	//开启设备管理器需要的类
	public ComponentName mDeviceAdminSample;
	private DevicePolicyManager mDPM;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

//		//开启设备管理器需要的类（好像只能放在activity，不能放在receiver类里面）
//		//参数：上下文环境，广播接受者所对应的字节码文件
//		mDeviceAdminSample = new ComponentName(this,DeviceAdmin.class);
//		//获取设备的管理者对象
//		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);


		// 实例化广告条
		AdView adView = new AdView(this, AdSize.FIT_SCREEN);

		// 获取要嵌入广告条的布局
		LinearLayout adLayout=(LinearLayout)findViewById(R.id.adLayout);

		// 将广告条加入到布局中
		adLayout.addView(adView);

//		/初始化UI
		initUI();
		//初始化数据的方法
		initData();

	}

	//以下一个方法应该放在smsreceiver类里，但是不行啊！！日后在说
//	//开启设备管理器的点击事件
//	bt_start.setOnlickListener(new OnClickListener(){
//			public void onClick(View v){
//				//开启设备管理器的activity
//				Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
//				intent.putExtra(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN,mDeviceAdminSample);
//				intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"设备管理器");
//				startActivity(intent);
//		}
//	});
//	//一键锁屏
//	bt_lock.setOnClickListener(new OnClickListener(){
//			public void onClick(View v){
//				//设备管理器是否开启的判断
//				if (mDPM.isAdminActive(mDeviceAdminSample)){
//					//在激活的情况下就锁屏
//					mDPM.lockNow();
//					//锁屏同时还设置密码,参数，锁屏密码，没有进入应用的情况下锁屏传0
//					mDPM.resetPassword("123",0);
//				}else{
//					ToastUtil.show(getApplication(),"请先激活设备管理器");
//				}
//		}
//
//	});
//	//清除数据
//	bt_lock.setOnClickListener(new OnClickListener(){
//		public void onClick(View v){
//			//设备管理器是否开启的判断
//			if (mDPM.isAdminActive(mDeviceAdminSample)){
//				//可以传0就是手机内部
//				mDPM.wipeData(0);
//				//清除手机sd卡数据
//				mDPM.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
//
//			}else{
//				ToastUtil.show(getApplication(),"请先激活设备管理器");
//			}
//		}
//	});
	//一键卸载
//	bt_lock.setOnClickListener(new OnClickListener(){
//		public void onClick(View v){
//			//隐示意图，维护一个action
//			Intent intent = new Intent("android.intent.action.DELETE");
//			//类型
//			intent.addCategory("android.intent.category.DEFAULT");
//			//数据，接收的数据类型是包名,要卸载的是自己的包名
//			intent.setData(Uri.parse("package:"+getPackageName()));
//			startActivity(intent);
//		}
//	});




	private void initData() {
		//准备数据(文字(9组),图片(9张))
		mTitleStrs = new String[]{"手机防盗","通信卫士","软件管理","进程管理","流量统计","手机杀毒","缓存清理","高级工具","设置中心"};

		mDrawableIds = new int[]{R.mipmap.home_safe,R.mipmap.home_callmsgsafe,R.mipmap.home_apps,R.mipmap.home_taskmanager,
				R.mipmap.home_netmanager,R.mipmap.home_trojan,R.mipmap.home_sysoptimize,R.mipmap.home_tools,R.mipmap.home_settings};

		//九宫格控件设置数据适配器(等同ListView数据适配器)
		gv_home.setAdapter(new MyAdapter());

		//注册九宫格单个条目点击事件
		gv_home.setOnItemClickListener(new OnItemClickListener() {
			//点中列表条目索引position
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//position是点中的位置
				switch (position) {
					case 0:
						//开启对话框
						showDialog();
						break;
					case 1:
						//跳转到通信卫士模块
						startActivity(new Intent(getApplicationContext(), BlackNumberActivity.class));
						break;
					case 2:
						//跳转到通信卫士模块
						startActivity(new Intent(getApplicationContext(), AppManagerActivity.class));
						break;
					case 3:
						//跳转到进程管理模块
						startActivity(new Intent(getApplicationContext(), ProcessManagerActivity.class));
						break;
					case 4:
						//跳转到通信卫士模块
						startActivity(new Intent(getApplicationContext(), TrafficActivity.class));
						break;
					case 5:
						//跳转到通信卫士模块
						startActivity(new Intent(getApplicationContext(), AnitVirusActivity.class));
						break;
					case 6:
//					startActivity(new Intent(getApplicationContext(), CacheClearActivity.class));
						startActivity(new Intent(getApplicationContext(), BaseCacheClearActivity.class));
						break;
					case 7:
						//跳转到高级工具功能列表界面
						startActivity(new Intent(getApplicationContext(), AToolActivity.class));
						break;
					case 8:
						Intent intent = new Intent(getApplicationContext(),SettingActivity.class);
						startActivity(intent);
						break;
				}
			}
		});
	}

	protected void showDialog() {
		//判断本地是否有存储密码(sp	字符串)，默认是空字符串
		String psd = SpUtil.getString(this, ConstantValue.MOBILE_SAFE_PSD, "");
		//判断字符串是否为空
		if(TextUtils.isEmpty(psd)){
			//1,初始设置密码对话框
			showSetPsdDialog();
		}else{
			//2,确认密码对话框
			showConfirmPsdDialog();
		}
	}

	/**
	 * 确认密码对话框
	 */
	private void showConfirmPsdDialog() {

		//因为需要去自己定义对话框的展示样式,所以需要调用dialog.setView(view);
		//view是由自己编写的xml转换成的view对象xml----->view
		Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dialog = builder.create();

		final View view = View.inflate(this, R.layout.dialog_confirm_psd, null);
		//让对话框显示一个自己定义的对话框界面效果
//		dialog.setView(view);

		//因为2.3.3的背景色需要修改bug，还有内边距也要屏蔽掉也是bug，但是在代码里修改
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();

		Button bt_submit = (Button) view.findViewById(R.id.bt_submit);
		Button bt_cancel = (Button) view.findViewById(R.id.bt_cancel);

		//给按钮注册点击事件
		bt_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//找出窗口的控件
				EditText et_confirm_psd = (EditText)view.findViewById(R.id.et_confirm_psd);

				//拿到edittext的内容
				String confirmPsd = et_confirm_psd.getText().toString();

				//密码不等于空的话
				if(!TextUtils.isEmpty(confirmPsd)){
					//密码正确的话
					//将存储在sp中32位的密码,获取出来,然后将输入的密码同样进行md5,然后与sp中存储密码比对
					String psd = SpUtil.getString(getApplicationContext(), ConstantValue.MOBILE_SAFE_PSD, "");

					//需要将用户输入的密码进行md5加密，然后在于存储的md5密码对比
					if(psd.equals(Md5Util.encoder(confirmPsd))){
						//进入应用手机防盗模块,开启一个新的activity
						Intent intent = new Intent(getApplicationContext(), SetupOverActivity.class);
						startActivity(intent);
						//跳转到新的界面以后需要去隐藏对话框
						dialog.dismiss();
					}else{
						ToastUtil.show(getApplicationContext(),"确认密码错误");
					}
				}else{
					//提示用户密码输入有为空的情况
					ToastUtil.show(getApplicationContext(), "请输入密码");
				}
			}
		});

		//给按钮注册点击事件
		bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//取消窗口
				dialog.dismiss();
			}
		});
	}

	/**
	 * 设置密码对话框
	 */
	private void showSetPsdDialog() {
		//因为需要去自己定义对话框的展示样式,所以需要调用dialog.setView(view);
		//view是由自己编写的xml转换成的view对象xml----->view
		//因为dailog是依赖activity所有要传this
		Builder builder = new AlertDialog.Builder(this);
		//现在对话框不是默认的，需要自己定义，所以要自己创建
		final AlertDialog dialog = builder.create();

		//现在对话框不是默认的，需要自己定义，所以要自己创建
		final View view = View.inflate(this, R.layout.dialog_set_psd, null);
		//让对话框显示一个自己定义的对话框界面效果
//		dialog.setView(view);

		//因为2.3.3的背景色需要修改bug，还有内边距也要屏蔽掉也是bug，但是在代码里修改
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();

		Button bt_submit = (Button) view.findViewById(R.id.bt_submit);
		Button bt_cancel = (Button) view.findViewById(R.id.bt_cancel);

		bt_submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText et_set_psd = (EditText) view.findViewById(R.id.et_set_psd);
				EditText et_confirm_psd = (EditText)view.findViewById(R.id.et_confirm_psd);

				String psd = et_set_psd.getText().toString();
				String confirmPsd = et_confirm_psd.getText().toString();

				if(!TextUtils.isEmpty(psd) && !TextUtils.isEmpty(confirmPsd)){
					if(psd.equals(confirmPsd)){
						//进入应用手机防盗模块,开启一个新的activity
						Intent intent = new Intent(getApplicationContext(), SetupOverActivity.class);
						startActivity(intent);
						//跳转到新的界面以后需要去隐藏对话框
						dialog.dismiss();

						//在存储密码前用md5加密下密码，这里需要的是字符串，需要在工具类中返回出字符串
						SpUtil.putString(getApplicationContext(), ConstantValue.MOBILE_SAFE_PSD, Md5Util.encoder(confirmPsd));
					}else{
						ToastUtil.show(getApplicationContext(),"确认密码错误");
					}
				}else{
					//提示用户密码输入有为空的情况
					ToastUtil.show(getApplicationContext(), "请输入密码");
				}
			}
		});

		bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

	private void initUI() {
		gv_home = (GridView) findViewById(R.id.gv_home);
	}

	//九宫格控件设置数据适配器(等同ListView数据适配器)
	class MyAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			//条目的总数	文字组数 == 图片张数
			return mTitleStrs.length;
		}

		@Override
		public Object getItem(int position) {
			return mTitleStrs[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		//这里暂时不复用了
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//填充布局
			View view = View.inflate(getApplicationContext(), R.layout.gridview_item, null);
			//找到ID
			TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
			ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);

			//设置标题
			tv_title.setText(mTitleStrs[position]);
			//设置图片
			iv_icon.setBackgroundResource(mDrawableIds[position]);

			return view;
		}
	}
}