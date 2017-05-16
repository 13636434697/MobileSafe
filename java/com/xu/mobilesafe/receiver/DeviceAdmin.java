package com.xu.mobilesafe.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DeviceAdmin extends DeviceAdminReceiver {

    public DeviceAdmin() {
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context,intent);
    }
}
