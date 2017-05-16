package com.xu.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xu.mobilesafe.engine.ProcessInfoProvider;

/*
*
* 窗口部件专用杀死进程的广播
* */
public class KillProcessReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		//杀死进程
		ProcessInfoProvider.killAll(context);
	}
}
