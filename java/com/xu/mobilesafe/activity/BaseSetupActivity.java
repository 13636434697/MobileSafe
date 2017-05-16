package com.xu.mobilesafe.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public abstract class BaseSetupActivity extends AppCompatActivity {
    private GestureDetector mGestureDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //2,创建手势管理的对象,用作管理在onTouchEvent(event)传递过来的手势动作
		//监听手势在移动过程中，起始点和结束点的位置
		mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
			//4，重写手势识别器中，包含下点和拾起点在移动过程中的方法
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				//e1 起始点
				//e2 抬起点
                //监听手势的移动
				if (e1.getRawX()-e2.getRawX()>100){
					//下一页，右向左滑动
                    //调用子类的下一页方法,抽象方法

                    //在第一个界面上的时候,跳转到第二个界面
                    //在第二个界面上的时候,跳转到第三个界面
                    //.......
                    showNextPage();

				}
				if (e2.getRawX()-e1.getRawX()>100){
					//上一页，左向右滑动
                    //调用子类的上一页方法
                    //在第一个界面上的时候,无响应,空实现
                    //在第二个界面上的时候,跳转到第1个界面
                    //.......
					showPrePage();
				}
				return super.onFling(e1, e2, velocityX, velocityY);
			}
		});
    }


    //1，监听当前activity上的触摸事件（按下（1次），滑动（多次），抬起（1次））
	//本来是由activity来处理的onTouchEvent方法，现在交给手势识别器处理
    //统一处理activity上的事件
    //让手势响应activity事件，公有的方法会继承到子类
	@Override
	public boolean onTouchEvent(MotionEvent event) {

        //3,通过手势处理类,接收多种类型的事件,用作处理
		mGestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

    //下一页的抽象方法,由子类决定具体跳转到那个界面
    //抽象方法，定义跳到下一页的方法,必须是抽象类
    public abstract void showNextPage();

    //上一页的抽象方法,由子类决定具体跳转到那个界面
    //抽象方法，定义跳到上一页的方法,必须是抽象类
    public abstract void showPrePage();



    //统一处理每一个界面中的上一页下一页
    //点击下一页按钮的时候,根据子类的showNextPage方法做相应跳转
    public void nextPage(View view){
        //不能实现，需要交给子类来实现
        showNextPage();
    }

    //点击上一页按钮的时候,根据子类的showPrePage方法做相应跳转
    public void prePage(View view){
        //不能实现，需要交给子类来实现
        showPrePage();
    }
}
