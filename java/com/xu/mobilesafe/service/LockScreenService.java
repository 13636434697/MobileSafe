package com.xu.mobilesafe.service;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.xu.mobilesafe.engine.ProcessInfoProvider;

public class LockScreenService extends Service {
	private IntentFilter intentFilter;
	private InnerReceiver innerReceiver;

	//开启服务的时候，这样就会接收到一个广告
	@Override
	public void onCreate() {
		//锁屏action，广播接收者的过滤条件
		intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		innerReceiver = new InnerReceiver();
		//注册广播接受者
		registerReceiver(innerReceiver, intentFilter);
		
		super.onCreate();
	}
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	//服务停止之后
	@Override
	public void onDestroy() {
		//广播接收者也取消监听
		if(innerReceiver!=null){
			unregisterReceiver(innerReceiver);
		}
		super.onDestroy();
	}

	//广播接受者
	class InnerReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			//清理手机正在运行的进程
			ProcessInfoProvider.killAll(context);
		}
	}
}
