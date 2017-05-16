package com.xu.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnTouchListener;

import com.xu.mobilesafe.R;
import com.xu.mobilesafe.engine.AddressDao;
import com.xu.mobilesafe.utils.ConstantValue;
import com.xu.mobilesafe.utils.SpUtil;

public class AddressService extends Service {
	public static final String tag = "AddressService";
	private TelephonyManager mTM;
	private MyPhoneStateListener mPhoneStateListener;
	//测量的父控件
	private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
	private View mViewToast;
	private WindowManager mWM;
	private String mAddress;
	private TextView tv_toast;
	private int[] mDrawableIds;

	private int mScreenHeight;
	private int mScreenWidth;

	private InnerOutCallReceiver mInnerOutCallReceiver;
	//使用了消息机制
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			//收到空消息之后，就知道了号码，就可以直接设置界面了，是设置到悬浮窗口上
			tv_toast.setText(mAddress);
		};
	};

	
	@Override
	public void onCreate() {
		//第一次开启服务以后,就需要去管理吐司的显示
		//电话状态的监听(服务开启的时候,需要去做监听,关闭的时候电话状态就不需要监听)
		//1,电话管理者对象
		mTM = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		//2,监听电话状态
		mPhoneStateListener = new MyPhoneStateListener();
		mTM.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		//获取窗体对象
		mWM = (WindowManager) getSystemService(WINDOW_SERVICE);

		//获取屏幕的宽和高
		mScreenHeight = mWM.getDefaultDisplay().getHeight();
		mScreenWidth = mWM.getDefaultDisplay().getWidth();



		//监听播出电话的广播过滤条件(权限)
		//过滤器在清单文件中可以配置，静态注册。这里用代码注册
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		//创建广播接受者
		mInnerOutCallReceiver = new InnerOutCallReceiver();
		//注册广播接受者
		registerReceiver(mInnerOutCallReceiver, intentFilter);
		super.onCreate();
	}

	class InnerOutCallReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			//接收到此广播后,需要显示自定义的吐司,显示播出归属地号码
			//获取播出电话号码的字符串
			String phone = getResultData();
			showToast(phone);
		}
	}

	//2,监听电话状态,重写方法
	class MyPhoneStateListener extends PhoneStateListener{
		//3,手动重写,电话状态发生改变会触发的方法
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				//空闲状态,没有任何活动(移除吐司)
				Log.i(tag, "挂断电话,空闲了.......................");
				//挂断电话的时候窗体需要移除吐司
				if(mWM!=null && mViewToast!=null){
					mWM.removeView(mViewToast);
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				//摘机状态，至少有个电话活动。该活动或是拨打（dialing）或是通话
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				//响铃(展示吐司)
				Log.i(tag, "响铃了.......................");
				showToast(incomingNumber);
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	public void showToast(String incomingNumber) {
//		源码里面复制过来的，宽高等等的一些参数定义
	    final WindowManager.LayoutParams params = mParams;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE	默认能够被触摸
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.format = PixelFormat.TRANSLUCENT;
        //在响铃的时候显示吐司,和电话类型一致
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.setTitle("Toast");
        
        //指定吐司的所在位置(将吐司指定在左上角)
        params.gravity = Gravity.LEFT+Gravity.TOP;
        
        //吐司显示效果(吐司布局文件),xml-->view(吐司),将吐司挂在到windowManager窗体上
        mViewToast = View.inflate(this, R.layout.toast_view, null);
        tv_toast = (TextView) mViewToast.findViewById(R.id.tv_toast);

		//来电时候的悬浮窗位置拖拽
		mViewToast.setOnTouchListener(new OnTouchListener() {
			private int startX;
			private int startY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						//（getX是点相对于自己控件的距离，getRawX是相对于屏幕的的距离）
						//记录下按下的坐标
						startX = (int) event.getRawX();
						startY = (int) event.getRawY();
						break;
					case MotionEvent.ACTION_MOVE:
						//移动之后的坐标也记下
						int moveX = (int) event.getRawX();
						int moveY = (int) event.getRawY();

						//然后移动的距离减去按下的距离，偏移量
						int disX = moveX-startX;
						int disY = moveY-startY;

						//移动的X和Y都要作用到窗口上去
						params.x = params.x+disX;
						params.y = params.y+disY;

						//容错处理
						//左
						if(params.x<0){
							params.x = 0;
						}
						//上
						if(params.y<0){
							params.y=0;
						}
						//右
						if(params.x>mScreenWidth-mViewToast.getWidth()){
							params.x = mScreenWidth-mViewToast.getWidth();
						}
						//下
						if(params.y>mScreenHeight-mViewToast.getHeight()-22){
							params.y = mScreenHeight-mViewToast.getHeight()-22;
						}


						//告知窗体吐司需要按照手势的移动,去做位置的更新
						mWM.updateViewLayout(mViewToast, params);

						startX = (int) event.getRawX();
						startY = (int) event.getRawY();

						break;
					case MotionEvent.ACTION_UP:
						//抬起的时候，要记录一下窗口的值，要更新设置窗口的值。这样设置和显示就同步窗口位置了
						SpUtil.putInt(getApplicationContext(),ConstantValue.LOCATION_X, params.x);
						SpUtil.putInt(getApplicationContext(),ConstantValue.LOCATION_Y, params.y);
						break;
				}
				//true 响应拖拽触发的事件
				return true;
			}
		});

		//读取sp中存储吐司位置的x,y坐标值
		// params.x为吐司左上角的x的坐标
		params.x = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_X, 0);
		// params.y为吐司左上角的y的坐标
		params.y = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_Y, 0);

        //从sp中获取色值文字的索引,匹配图片,用作展示
        mDrawableIds = new int[]{
        		R.drawable.call_locate_white,
        		R.drawable.call_locate_orange,
        		R.drawable.call_locate_blue,
        		R.drawable.call_locate_gray,
        		R.drawable.call_locate_green};
		//存储的什么颜色文字，就设置什么颜色的图片，所以先获取存储的颜色
        int toastStyleIndex = SpUtil.getInt(getApplicationContext(), ConstantValue.TOAST_STYLE, 0);
		//设置颜色的图片
        tv_toast.setBackgroundResource(mDrawableIds[toastStyleIndex]);
        
        //在窗体上挂在一个view(权限)
        mWM.addView(mViewToast, params);
        
        //获取到了来电号码以后,需要做来电号码查询
        query(incomingNumber);
	}

	//电话号码的查询
	private void query(final String incomingNumber) {
		//耗时操作，需要线程
		new Thread(){
			public void run() {
				//给电话号码就返回结果
				mAddress = AddressDao.getAddress(incomingNumber);
				//查到之后就发送空消息
				mHandler.sendEmptyMessage(0);
			};
		}.start();
	}

	@Override
	public void onDestroy() {
		//服务销毁的时候 ，电话还会继续监听，所以要在这里取消一下
		//取消对电话状态的监听(开启服务的时候监听电话的对象)
		if(mTM!=null && mPhoneStateListener!=null){
			mTM.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
		}
		if(mInnerOutCallReceiver!=null){
			//去电广播接受者的注销过程
			unregisterReceiver(mInnerOutCallReceiver);
		}
		super.onDestroy();
	}
}
