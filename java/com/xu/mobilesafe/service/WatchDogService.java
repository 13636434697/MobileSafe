package com.xu.mobilesafe.service;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;

import com.xu.mobilesafe.activity.EnterPsdActivity;
import com.xu.mobilesafe.db.dao.AppLockDao;

public class WatchDogService extends Service {
	private boolean isWatch;
	private AppLockDao mDao;
	private List<String> mPacknameList;
	private InnerReceiver mInnerReceiver;
	private String mSkipPackagename;
	private MyContentObserver mContentObserver;
	@Override
	public void onCreate() {

		mDao = AppLockDao.getInstance(this);
		isWatch = true;
		//维护一个看门狗的死循环,让其时刻监测现在开启的应用,是否为程序锁中要去拦截的应用
		watch();

		//创建一个广播接收者，去匹配自己写的发送的广播
		IntentFilter intentFilter = new IntentFilter();	
		intentFilter.addAction("android.intent.action.SKIP");
		
		mInnerReceiver = new InnerReceiver();
		//注册一个广播接受者
		registerReceiver(mInnerReceiver, intentFilter);
		

		//如果程序拦截已经开启，再去添加程序锁，并不会起作用，因为刚插入的数据库的程序没有办法获取到，需要在刷新一次数据库，才能拦截
		//注册一个内容观察者,观察数据库的变化,一旦数据有删除或者添加,则需要让mPacknameList重新获取一次数据
		mContentObserver = new MyContentObserver(new Handler());
		//匹配内容观察者的一致的uri所指向的uri，后面天false的话，uri是一摸一样的，true的话是模糊匹配
		getContentResolver().registerContentObserver(Uri.parse("content://applock/change"), true, mContentObserver);
		super.onCreate();
	}

	//注册一个内容观察者
	class MyContentObserver extends ContentObserver{

		public MyContentObserver(Handler handler) {
			super(handler);
		}
		
		//一旦数据库发生改变时候调用方法,重新获取包名所在集合的数据
		@Override
		public void onChange(boolean selfChange) {
			new Thread(){
				public void run() {
					//重新查询一下数据库
					mPacknameList = mDao.findAll();
				};
			}.start();
			super.onChange(selfChange);
		}
	}

	//注册一个广播接受者
	class InnerReceiver extends BroadcastReceiver{
		//接收到广播之后的操作
		@Override
		public void onReceive(Context context, Intent intent) {
			//获取发送广播过程中传递过来的包名,跳过次包名检测过程
			mSkipPackagename = intent.getStringExtra("packagename");
		}
	}

	//维护一个看门狗的死循环,让其时刻监测现在开启的应用,是否为程序锁中要去拦截的应用
	private void watch() {
		//不能直接while，而是一个可以控制的死循环
		//1,子线程中,开启一个可控死循环
		new Thread(){
			public void run() {
				//加锁的集合没有必要循环一次查一次
				mPacknameList = mDao.findAll();
				//可以控制的死循环
				while(isWatch){
					//2.监测现在正在开启的应用,任务栈
					//3.获取activity管理者对象
					ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
					//4.获取正在开启应用的任务栈（任务站的总数要一个就可以了，最前面的）
					List<RunningTaskInfo> runningTasks = am.getRunningTasks(1);
					//最后一个开启的任务站的应用
					RunningTaskInfo runningTaskInfo = runningTasks.get(0);
					//5.获取栈顶的activity,然后在获取此activity所在应用的包名
					String packagename = runningTaskInfo.topActivity.getPackageName();
					
					//如果任务栈指向应用有切换,将mSkipPackagename空字符串
					
					//6.拿此包名在已加锁的包名集合中去做比对,如果包含次包名,则需要弹出拦截界面
					if(mPacknameList.contains(packagename)){
						//如果现在检测的程序,已经解锁了,则不需要去弹出拦截界面
						//在循环的过程中忽略已经解锁的程序
						if(!packagename.equals(mSkipPackagename)){
							//7,弹出拦截界面
							Intent intent = new Intent(getApplicationContext(),EnterPsdActivity.class);
							//在服务里面开activity，并且还没有任务站，还需要开一个任务站
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							//包名传进去
							intent.putExtra("packagename", packagename);
							startActivity(intent);
						}
					}
					//程序没有必要每一毫秒都循环的
					//睡眠一下,时间片轮转
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
	
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	@Override
	public void onDestroy() {
		//停止看门狗循环
		isWatch = false;
		//注销广播接受者
		if(mInnerReceiver!=null){
			unregisterReceiver(mInnerReceiver);
		}
		//注销内容观察者
		if(mContentObserver!=null){
			getContentResolver().unregisterContentObserver(mContentObserver);
		}
		super.onDestroy();
	}
}
