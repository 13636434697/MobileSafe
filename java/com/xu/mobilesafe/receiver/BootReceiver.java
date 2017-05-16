package com.xu.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.xu.mobilesafe.utils.ConstantValue;
import com.xu.mobilesafe.utils.SpUtil;

/*
* sim卡更换报警
*
* */
public class BootReceiver extends BroadcastReceiver {

	private static final String tag = "BootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		//一旦监听到开机广播，就需要去发送短信给指定号码

		Log.i(tag, "重启手机成功了,并且监听到了相应的广播......");
		//1,获取开机后手机的sim卡的序列号
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		//为了测试而加的xxx
		String simSerialNumber = tm.getSimSerialNumber()+"xxx";
		//2,sp中存储的序列卡号
		String sim_number = SpUtil.getString(context, ConstantValue.SIM_NUMBER, "");
		//3,比对不一致
		if(!simSerialNumber.equals(sim_number)){
			//4,发送短信给选中联系人号码
			SmsManager sms = SmsManager.getDefault();

			String phone = SpUtil.getString(context, ConstantValue.CONTACT_PHONE, "");
			//参数：1.发送电话，2.目标电话号码。3.发消息（模拟器不支持中文），4，发送完成之后的意图，5，我们接收到之后的意图
			sms.sendTextMessage(phone, null, "sim change!!!", null, null);
		}
	}
}
