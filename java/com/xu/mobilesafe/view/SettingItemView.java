package com.xu.mobilesafe.view;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xu.mobilesafe.R;

/*
* 设置界面所对应的条目的类
*
* 设置一个布局，并加载，抽取成一个类来管理
*
* 自定义命名空间，需要在attrs里面写一下
* */
public class SettingItemView extends RelativeLayout {

	private static final String NAMESPACE = "http://schemas.android.com/apk/res/com.xu.mobilesafe";
	private static final String tag = "SettingItemView";
	private CheckBox cb_box;
	private TextView tv_des;
	private String mDestitle;
	private String mDesoff;
	private String mDeson;

	//代码new出来用的方法
	public SettingItemView(Context context) {
		//当前类所对应的方法，this指向的是带2个参数的方法
		//如果调用了new了这个方法，就调用第二个方法
		this(context,null);
	}

	//属性用这个方法
	public SettingItemView(Context context, AttributeSet attrs) {
		//当前类所对应的方法，
		//如果调用第二个方法，就调用了第三个方法
		this(context, attrs,0);
	}

	//上面无论调用哪个方法，都会调用到这个方法
	//不管调用什么构造方法，都调用这个方法
	public SettingItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//xml--->view	将设置界面的一个条目转换成view对象,直接添加到了当前SettingItemView对应的view中
		//走到这里来，就把布局转换成对象，this参数的含义是否要挂载到父控件上是需要挂载的，因为这个类要变成一个标签挂载到布局中
		//指定某一部分view布局，转换成对象，然后在放到布局里面
		View.inflate(context, R.layout.setting_item_view, this);

		//等同于以下两行代码
		/*View view = View.inflate(context, R.layout.setting_item_view, null);
		this.addView(view);*/
		
		//自定义组合控件中的标题描述
		TextView tv_title = (TextView) findViewById(R.id.tv_title);
		tv_des = (TextView) findViewById(R.id.tv_des);
		cb_box = (CheckBox) findViewById(R.id.cb_box);

		//写到最终调用的构造方法里，系统在构建这个源码的时候，就将属性转换成了集合，在构造方面里面传递过来了
		//获取自定义以及原生属性的操作,写在此处,AttributeSet attrs对象中获取
		initAttrs(attrs);
		//获取布局文件中定义的字符串,赋值给自定义组合控件的标题
		//这里就用到了自定义命名空间的值
		tv_title.setText(mDestitle);
	}
	
	/**
	 * 返回属性集合中自定义属性属性值
	 * @param attrs	构造方法中维护好的属性集合
	 */
	private void initAttrs(AttributeSet attrs) {
		/*//获取属性的总个数
		Log.i(tag, "attrs.getAttributeCount() = "+attrs.getAttributeCount());
		//获取属性名称以及属性值
		for(int i=0;i<attrs.getAttributeCount();i++){
			Log.i(tag, "name = "+attrs.getAttributeName(i));
			Log.i(tag, "value = "+attrs.getAttributeValue(i));
			Log.i(tag, "分割线 ================================= ");
		}*/
		
		//通过名空间+属性名称获取属性值
		
		mDestitle = attrs.getAttributeValue(NAMESPACE, "destitle");
		mDesoff = attrs.getAttributeValue(NAMESPACE, "desoff");
		mDeson = attrs.getAttributeValue(NAMESPACE, "deson");
		
		Log.i(tag, mDestitle);
		Log.i(tag, mDesoff);
		Log.i(tag, mDeson);
	}

	/**
	 * CheckBox判断是否开启的方法
	 * @return	返回当前SettingItemView是否选中状态	true开启(checkBox返回true)	false关闭(checkBox返回true)
	 */
	public boolean isCheck(){
		//由checkBox的选中结果,决定当前条目是否开启
		return cb_box.isChecked();
	}

	/**
	 * CheckBox
	 * @param isCheck	作为是否开启的变量,由点击过程中去做传递
	 */
	public void setCheck(boolean isCheck){
		//当前条目在选择的过程中,cb_box选中状态也在跟随(isCheck)变化
		cb_box.setChecked(isCheck);
		if(isCheck){
			//开启
			//这里就用到了自定义命名空间的值
			tv_des.setText(mDeson);
		}else{
			//关闭
			//这里就用到了自定义命名空间的值
			tv_des.setText(mDesoff);
		}
	}
	
}
