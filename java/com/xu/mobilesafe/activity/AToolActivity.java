package com.xu.mobilesafe.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.View.OnClickListener;

import com.xu.mobilesafe.R;
import com.xu.mobilesafe.engine.SmsBackUp;

import java.io.File;

public class AToolActivity extends AppCompatActivity {
    private TextView tv_query_phone_address,tv_sms_backup;
    private ProgressBar pb_bar;
    private TextView tv_commonnumber_query;
    private TextView tv_app_lock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atool);

        //电话归属地查询方法
        initPhoneAddress();
        //短信备份方法
        initSmsBackUp();
        //常用号码查询
        initCommonNumberQuery();
        //程序锁
        initAppLock();
    }

    //程序锁
    private void initAppLock() {
        tv_app_lock = (TextView) findViewById(R.id.tv_app_lock);
        tv_app_lock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AppLockActivity.class));
            }
        });
    }

    //常用号码查询
    private void initCommonNumberQuery() {
        tv_commonnumber_query = (TextView) findViewById(R.id.tv_commonnumber_query);
        //设置一个点击事件
        tv_commonnumber_query.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //开启一个新界面
                startActivity(new Intent(getApplicationContext(), CommonNumberQueryActivity.class));
            }
        });
    }


    //短信备份方法
    private void initSmsBackUp() {
        //找出控件（对话框的形式）
        tv_sms_backup = (TextView) findViewById(R.id.tv_sms_backup);
        //进度条的形式
        pb_bar = (ProgressBar) findViewById(R.id.pb_bar);
        //点击备份的方法
        tv_sms_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出备份短信的界面
                showSmsBackUpDialog();
            }
        });
    }

    //弹出备份短信的界面
    protected void showSmsBackUpDialog() {
        //1,创建一个带进度条的对话框
        final ProgressDialog progressDialog = new ProgressDialog(this);
        //给进度条设置一个图标
        progressDialog.setIcon(R.mipmap.ic_launcher);
        //给进度条设置一个标题
        progressDialog.setTitle("短信备份");
        //2,指定进度条的样式为水平（水平的）
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //3,展示进度条
        progressDialog.show();

        //4,直接调用备份短信方法即可
        new Thread(){
            @Override
            public void run() {
                //需要一个文件路径，外部设备的文件夹路径加上斜杠在加上文件名
                String path = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"sms74.xml";
                //最后接收的参数可以是对话框，但是现在用进度条
                //用到了观察者设计模式，就是回调方法，自己写的回调方法
                SmsBackUp.backup(getApplicationContext(), path, new SmsBackUp.CallBack() {
                    @Override
                    public void setProgress(int index) {
                        //如果说备份短信是由别人开发的，为了方便维护，进度条和对话框的表达形式
                        //由开发者自己决定,使用对话框还是进度条
                        progressDialog.setProgress(index);
                        pb_bar.setProgress(index);
                    }

                    @Override
                    public void setMax(int max) {
                        //由开发者自己决定,使用对话框还是进度条
                        progressDialog.setMax(max);
                        pb_bar.setMax(max);
                    }
                });
                //按理来说也是不行的，子线程更新UI，但是特殊，progressDialog可以在子线程里运行和结束
                progressDialog.dismiss();
            }
        }.start();
    }



    //电话归属地查询方法
    private void initPhoneAddress() {
        tv_query_phone_address = (TextView) findViewById(R.id.tv_query_phone_address);
        tv_query_phone_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), QueryAddressActivity.class));
            }
        });
    }
}
