package com.xu.mobilesafe.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.telephony.SmsMessage;

import com.xu.mobilesafe.R;
import com.xu.mobilesafe.service.LocationService;
import com.xu.mobilesafe.utils.ConstantValue;
import com.xu.mobilesafe.utils.SpUtil;


/*
* 防盗短信报警
* */
public class SmsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		//1,判断是否开启了防盗保护
		boolean open_security = SpUtil.getBoolean(context, ConstantValue.OPEN_SECURITY, false);
		//确实开启防盗保护
		if(open_security){
			//2,获取短信内容（因为短信是数组）
			Object[] objects = (Object[]) intent.getExtras().get("pdus");
			//3,循环遍历短信过程
			for (Object object : objects) {
				//4,获取短信对象（对象转成byte数组）
				SmsMessage sms = SmsMessage.createFromPdu((byte[])object);
				//5,获取短信对象的基本信息
//				获取发送短信的号码，获取消息内容
				String originatingAddress = sms.getOriginatingAddress();
				String messageBody = sms.getMessageBody();
				
				//6,判断是否包含播放音乐的关键字
				if(messageBody.contains("#*alarm*#")){
					//7,播放音乐(准备音乐,MediaPlayer)
					MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.ylzs);
					//无限循环
					mediaPlayer.setLooping(true);
					mediaPlayer.start();
				}

				//如果短信接收到获取位置的短信
				if(messageBody.contains("#*location*#")){
					//8,开启获取位置服务
					//因为获取位置是需要后台服务的，所以开启一个服务
					//这是在广播接收者里面调用服务，广播接收者是没有上下文环境的
					context.startService(new Intent(context,LocationService.class));
				}

				//以下2个方法，放在homeactivity里面，但是放在这里类就有问题编译不通过，展示放下
				//一键锁屏
				if(messageBody.contains("#*lockscrenn*#")){
				}

				//一键清除数据
				if(messageBody.contains("#*wipedate*#")){
				}
			}
		}
	}
}
