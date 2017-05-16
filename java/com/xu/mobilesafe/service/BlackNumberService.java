package com.xu.mobilesafe.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;
import com.xu.mobilesafe.db.dao.BlackNumberDao;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;

public class BlackNumberService extends Service {
	private InnerSmsReceiver mInnerSmsReceiver;
	private BlackNumberDao mDao;
	private TelephonyManager mTM;
	private MyPhoneStateListener mPhoneStateListener;
	private MyContentObserver mContentObserver;

	//创建
	@Override
	public void onCreate() {
		//会空指针异常，因为在调用的时候，还没有赋值，必须在刚开始就赋值，不能放在短信接收到后在赋值
		mDao = BlackNumberDao.getInstance(getApplicationContext());
		
		//拦截短信
		//新建过滤器
		IntentFilter intentFilter = new IntentFilter();
		//添加动作，在清单文件里面抄袭
		intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		//设置优先级
		intentFilter.setPriority(1000);

		//注册广播接受者
		mInnerSmsReceiver = new InnerSmsReceiver();
		registerReceiver(mInnerSmsReceiver, intentFilter);
		
		//监听电话的状态
		//1,电话管理者对象
		mTM = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		//2,监听电话状态
		mPhoneStateListener = new MyPhoneStateListener();
		mTM.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		
		super.onCreate();
	}

	//监听电话的状态
	class MyPhoneStateListener extends PhoneStateListener{
		//3,手动重写,电话状态发生改变会触发的方法
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				//挂断电话 	aidl文件中去了
//				mTM.endCall();
				endCall(incomingNumber);
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	}

	//注册一个内部的广播接受者
	class InnerSmsReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			//获取短信内容,获取发送短信电话号码,如果此电话号码在黑名单中,并且拦截模式也为1(短信)或者3(所有),拦截短信
			//1,获取短信内容
			Object[] objects = (Object[]) intent.getExtras().get("pdus");
			//2,循环遍历短信过程
			for (Object object : objects) {
				//3,获取短信对象
				SmsMessage sms = SmsMessage.createFromPdu((byte[])object);
				//4,获取短信对象的基本信息
				String originatingAddress = sms.getOriginatingAddress();
				String messageBody = sms.getMessageBody();

				//根据电话号码找拦截模式
				int mode = mDao.getMode(originatingAddress);
				//给mode值做判断
				if(mode == 1 || mode == 3){
					//拦截短信(android 4.4版本失效	短信数据库,删除)
					// 中断广播接受者
					abortBroadcast();
				}
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	//挂断电话的方法，这里不能直接调用系统的类，需要用到反射，aidl文件
	public void endCall(String phone) {
		int mode = mDao.getMode(phone);
		
		if(mode == 2 || mode == 3){
//			ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
			//ServiceManager此类android对开发者隐藏,所以不能去直接调用其方法,需要反射调用
			try {
				//1,获取ServiceManager字节码文件，这里需要完成的类名
				Class<?> clazz = Class.forName("android.os.ServiceManager");
				//2,获取上面类的方法，调用哪个方法就传哪个名称，最后可变参数要的是参数所对应的字节码，Context.TELEPHONY_SERVICE是string类型的
				Method method = clazz.getMethod("getService", String.class);
				//3,反射调用此方法（因为系统级别的类，开发者是不能直接调用的）
				// 但是需要这个方法，知道类名，方法名，方法要的参数就可以用反射
				//ITelephony.Stub.asInterface(SerivceManager.getService(Context.TELECOM_SERVICE));
				//getService是一个静态方法，类名调用方法是静态方法，静态方法不需要用对象调用，拿类名调用就可以了
				//不需要对象，就传null，传个对象进去调用也可以，这个可变参数就是固定的字符串
				//源码里面IBinder，所以强转成IBinder
				IBinder iBinder = (IBinder) method.invoke(null, Context.TELEPHONY_SERVICE);
				//4,调用获取aidl文件对象方法
				ITelephony iTelephony = ITelephony.Stub.asInterface(iBinder);
				//5,调用在aidl中隐藏的endCall方法（挂电话）
				iTelephony.endCall();
			} catch (Exception e) {
				e.printStackTrace();
			}

			//6,在内容解析器上,去注册内容观察者,通过内容观察者,观察数据库(Uri决定那张表那个库)的变化
			mContentObserver = new MyContentObserver(new Handler(),phone);
			//删除被拦截电话号码的通信记录，calls是模糊匹配，true能匹配上就true
			// 在指定的内容解析器上去注册一个内容观察者所对应的对象，去观察这个uri指向的数据表的数据的改变
			getContentResolver().registerContentObserver(Uri.parse("content://call_log/calls"), true, mContentObserver);
		}
	}

	//内容观察者的对象
	class MyContentObserver extends ContentObserver {
		private String phone;
		//构造方法
		public MyContentObserver(Handler handler, String phone) {
			super(handler);
			this.phone = phone;
		}
		//数据库中指定calls表发生改变的时候会去调用方法
		@Override
		public void onChange(boolean selfChange) {
			//插入一条数据后,再进行删除
			getContentResolver().delete(Uri.parse("content://call_log/calls"), "number = ?", new String[]{phone});
			super.onChange(selfChange);
		}
	}

	//销毁方法都在这里
	@Override
	public void onDestroy() {
		//取消广播接受者
		if(mInnerSmsReceiver!=null){
			unregisterReceiver(mInnerSmsReceiver);
		}
		//注销内容观察者
		if(mContentObserver!=null){
			getContentResolver().unregisterContentObserver(mContentObserver);
		}

		//取消对电话状态的监听
		if(mPhoneStateListener!=null){
			mTM.listen(mPhoneStateListener,PhoneStateListener.LISTEN_NONE);
		}
		super.onDestroy();
	}
}
