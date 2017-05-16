package com.xu.mobilesafe.receiver;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.xu.mobilesafe.service.UpdateWidgetService;

/*
* 主屏幕的窗口小部件，不能直接继承广播接受者，需要继承专用的窗口小部件的广播接受者
* */
@SuppressLint("NewApi")
public class MyAppWidgetProvider extends AppWidgetProvider {
	private static final String tag = "MyAppWidgetProvider";
	//接收到广播的方法
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(tag, "onReceive............");
		super.onReceive(context, intent);
	}

	//三个方法都是在创建窗体小部件的方法
	@Override
	public void onEnabled(Context context) {
		//创建第一个窗体小部件的方法
		Log.i(tag, "onEnabled 创建第一个窗体小部件调用方法");
		//开启服务(onCreate)
		context.startService(new Intent(context, UpdateWidgetService.class));
		super.onEnabled(context);
	}
	//更新窗体小部件的方法
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.i(tag, "onUpdate 创建多一个窗体小部件调用方法");
		//开启服务
		context.startService(new Intent(context, UpdateWidgetService.class));
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
	@Override
	public void onAppWidgetOptionsChanged(Context context,AppWidgetManager appWidgetManager, int appWidgetId,Bundle newOptions) {
		//当窗体小部件宽高发生改变的时候调用方法,创建小部件的时候,也调用此方法
		//开启服务
		context.startService(new Intent(context, UpdateWidgetService.class));
		Log.i(tag, "onAppWidgetOptionsChanged 创建多一个窗体小部件调用方法");
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
				newOptions);
	}

	//下面2个方法是在删除窗口小部件的方法
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.i(tag, "onDeleted 删除一个窗体小部件调用方法");
		super.onDeleted(context, appWidgetIds);
	}
	
	@Override
	public void onDisabled(Context context) {
		Log.i(tag, "onDisabled 删除最后一个窗体小部件调用方法");
		//关闭服务
		context.stopService(new Intent(context, UpdateWidgetService.class));
		super.onDisabled(context);
	}
}
