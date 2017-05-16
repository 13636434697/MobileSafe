package com.xu.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.xu.mobilesafe.R;


/*
* 这里所有的方法都抽到基类里面去做了
*还有所有的设置页面都继承基类，所以都简化了
* */
public class Setup1Activity extends BaseSetupActivity {

	private GestureDetector mGestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup1);
	}


	//下一页的方法，要传一个view，不传的view话，就是定义的一个成员方法
	@Override
	public void showNextPage() {
		//跳转到第二页
		Intent intent = new Intent(getApplicationContext(), Setup2Activity.class);
		startActivity(intent);

		finish();

		//开启平移动画。参数：下一页进来的动画，下一页出去的动画
		overridePendingTransition(R.anim.next_in_anim, R.anim.next_out_anim);
	}

	//第一个界面没有上一页的，所以没有业务逻辑，空实现
	@Override
	public void showPrePage() {

	}


//	//下一页的方法，要传一个view，不传的view话，就是定义的一个成员方法
//	public void nextPage(View view){
//		//跳转到第二页
//		Intent intent = new Intent(getApplicationContext(), Setup2Activity.class);
//		startActivity(intent);
//
//		finish();
//
//		//开启平移动画。参数：下一页进来的动画，下一页出去的动画
//		overridePendingTransition(R.anim.next_in_anim, R.anim.next_out_anim);
//
//		//3，创建手势识别器
//		//监听手势在移动过程中，起始点和结束点的位置
//		mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
//			//4，重写手势识别器中，包含下点和拾起点在移动过程中的方法
//			@Override
//			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//				//e1 起始点
//				//e2 抬起点
//				if (e1.getRawX()-e2.getRawX()>100){
//					//下一页，右向左滑动
//					Intent intent = new Intent(getApplicationContext(), Setup2Activity.class);
//					startActivity(intent);
//
//					finish();
//				}
//				if (e2.getRawX()-e1.getRawX()>100){
//					//上一页，左向右滑动
//					//因为没有上一页所以给空实现
//				}
//				return super.onFling(e1, e2, velocityX, velocityY);
//			}
//		});
//	}


//	//1，监听当前activity上的触摸事件（按下（1次），滑动（多次），抬起（1次））
//	//本来是由activity来处理的onTouchEvent方法，现在交给手势识别器处理
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//
//		//2，通过手势识别器识别不同的事件类型，做逻辑
//		mGestureDetector.onTouchEvent(event);
//		return super.onTouchEvent(event);
//	}
}
