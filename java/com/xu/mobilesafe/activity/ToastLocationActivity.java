package com.xu.mobilesafe.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import android.view.View.OnTouchListener;

import com.xu.mobilesafe.R;
import com.xu.mobilesafe.utils.ConstantValue;
import com.xu.mobilesafe.utils.SpUtil;

import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

public class ToastLocationActivity extends Activity {
    private ImageView iv_drag;
    private Button bt_top,bt_bottom;
    private WindowManager mWM;
    private int mScreenHeight;
    private int mScreenWidth;

    private long startTime = 0;
    //多次点击的时间戳的长度
    private long[] mHits = new long[5];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toast_location);

        initUI();
    }

//   初始化UI，这个控件可以拖拽拖拽归属地显示窗口的位置
    private void initUI() {
        //可拖拽双击居中的图片控件
        iv_drag = (ImageView) findViewById(R.id.iv_drag);
        bt_top = (Button) findViewById(R.id.bt_top);
        bt_bottom = (Button) findViewById(R.id.bt_bottom);



        //屏幕的宽高值
        mWM = (WindowManager) getSystemService(WINDOW_SERVICE);
        mScreenHeight = mWM.getDefaultDisplay().getHeight();
        mScreenWidth = mWM.getDefaultDisplay().getWidth();

        //一打开界面就需要读取窗口位置并显示，默认是左上角是0
        int locationX = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_X, 0);
        int locationY = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_Y, 0);

        //左上角坐标作用在iv_drag上
        //iv_drag在相对布局中,所以其所在位置的规则需要由相对布局提供

        //指定宽高都为WRAP_CONTENT
        LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        //将左上角的坐标作用在iv_drag对应规则参数上
        layoutParams.leftMargin = locationX;
        layoutParams.topMargin = locationY;
        //将以上规则作用在iv_drag上
        iv_drag.setLayoutParams(layoutParams);

        //初始化的时候，也要设置一下显示和隐藏
        if(locationY>mScreenHeight/2){
            bt_bottom.setVisibility(View.INVISIBLE);
            bt_top.setVisibility(View.VISIBLE);
        }else{
            bt_bottom.setVisibility(View.VISIBLE);
            bt_top.setVisibility(View.INVISIBLE);
        }

        //双击弹窗居中的点击事件
        iv_drag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);
                mHits[mHits.length-1] = SystemClock.uptimeMillis();
                if(mHits[mHits.length-1]-mHits[0]<500){

                    //满足双击事件后,调用代码
                    //屏幕宽度的一半减去控件的一半
                    int left = mScreenWidth/2 - iv_drag.getWidth()/2;
                    int top = mScreenHeight/2 - iv_drag.getHeight()/2;
                    int right = mScreenWidth/2+iv_drag.getWidth()/2;
                    int bottom = mScreenHeight/2+iv_drag.getHeight()/2;

                    //控件按以上规则显示
                    iv_drag.layout(left, top, right, bottom);

                    //存储最终位置
                    SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_X, iv_drag.getLeft());
                    SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_Y, iv_drag.getTop());
                }
            }
        });


        //监听图片的拖拽效果，现在是作用在某一个控件的事件监听，所有要调用
        //监听某一个控件的拖拽过程(按下(1),移动(多次),抬起(1))
        //设置在触摸过程中的监听，监听拖拽的控件，实现了接口所对应类的对象（要创建实现类的接口对象，一定实现未实现的方法）
        iv_drag.setOnTouchListener(new OnTouchListener() {
            private int startX;
            private int startY;

            //对不同的事件做不同的逻辑处理。在MotionEvent可以拿到按下移动抬起的动作
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

                        //1,当前控件所在屏幕的(左,上)角的位置
                        //获取控件左上右下的坐标在加上偏移量
                        int left = iv_drag.getLeft()+disX;//左侧坐标
                        int top = iv_drag.getTop()+disY;//顶端坐标
                        int right = iv_drag.getRight()+disX;//右侧坐标
                        int bottom = iv_drag.getBottom()+disY;//底部坐标

                        //容错处理(iv_drag不能拖拽出手机屏幕)
                        //左边缘不能超出屏幕
                        if(left<0){
                            return true;
                        }

                        //右边边缘不能超出屏幕
                        if(right>mScreenWidth){
                            return true;
                        }

                        //上边缘不能超出屏幕可现实区域
                        if(top<0){
                            return true;
                        }

                        //下边缘(屏幕的高度-22 = 底边缘显示最大值)
                        if(bottom>mScreenHeight - 22){
                            return true;
                        }

                        //如果控件上下移动超过屏幕的一般，就设置控件的隐藏和显示
                        if(top>mScreenHeight/2){
                            bt_bottom.setVisibility(View.INVISIBLE);
                            bt_top.setVisibility(View.VISIBLE);
                        }else{
                            bt_bottom.setVisibility(View.VISIBLE);
                            bt_top.setVisibility(View.INVISIBLE);
                        }

                        //2,告知移动的控件,按计算出来的坐标去做展示
                        iv_drag.layout(left, top, right, bottom);

                        //3,重置一次其实坐标
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();

                        break;
                    case MotionEvent.ACTION_UP:
                        //4,存储移动到的位置，因为下次进来，还要在移动后的位置
                        SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_X, iv_drag.getLeft());
                        SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_Y, iv_drag.getTop());
                        break;
                }

                //在当前的情况下返回false不响应事件,返回true才会响应事件
                //如果要实现setOnClick的方法，需要返回false
                //现在既要相应点击事件，又要相应拖拽过程，需要返回false
                return true;
            }
        });

//        //双击事件的处理
//        iv_drag.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //两次点击在一定的时间间隔内,才是双击
//                if(startTime!=0){
//                    //获取时间戳
//                    long endTime = System.currentTimeMillis();
//                    //时间戳减去开始的时间
//                    if(endTime-startTime<500){
//                        Toast.makeText(getApplicationContext(), "是男人", Toast.LENGTH_SHORT).show();
//                    }
//                }
//                startTime = System.currentTimeMillis();
//            }
//        });
//
//        //多击事件处理
//        iv_drag.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //了解
//                //1,原数组(要被拷贝的数组)
//                //2,原数组的拷贝起始位置索引值
//                //3,目标数组(原数组的数据---拷贝-->目标数组)
//                //4,目标数组接受值的起始索引位置
//                //5,拷贝的长度
//                System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);
//                //进入每一次点击的时间戳
//                mHits[mHits.length-1] = SystemClock.uptimeMillis();
//                //最后一个时间戳减掉第0个时间戳
//                if(mHits[mHits.length-1]-mHits[0]<500){
//                    //响应了一个三击事件
//                    Toast.makeText(getApplicationContext(), "超级赛亚人!!!!", 0).show();
//                }
//            }
//        });
    }
}

