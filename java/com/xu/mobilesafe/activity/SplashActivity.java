package com.xu.mobilesafe.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.xu.mobilesafe.R;
import com.xu.mobilesafe.utils.StreamUtil;
import com.xu.mobilesafe.utils.ToastUtil;
import com.xu.mobilesafe.utils.ConstantValue;
import com.xu.mobilesafe.utils.SpUtil;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import net.youmi.android.AdManager;

public class SplashActivity extends Activity {
    protected static final String tag = "SplashActivity";
	/**
	 * 更新新版本的状态码
	 */
	protected static final int UPDATE_VERSION = 100;
	/**
	 * 进入应用程序主界面状态码
	 */
	protected static final int ENTER_HOME = 101;
	
	/**
	 * url地址出错状态码
	 */
	protected static final int URL_ERROR = 102;
	protected static final int IO_ERROR = 103;
	protected static final int JSON_ERROR = 104;
    
	private TextView tv_version_name;
	private int mLocalVersionCode;
	private String mVersionDes;
	private String mDownloadUrl;

	private RelativeLayout rl_root;


	//提示用户更新,弹出对话框(UI),消息机制(就是handler的message)
	//成员变量
	private Handler mHandler = new Handler(){
		@Override
		//alt+ctrl+向下箭头,向下拷贝相同代码
		public void handleMessage(Message msg) {
			//判断发送回来的状态，分别做出判断
			switch (msg.what) {
			case UPDATE_VERSION:
				//弹出对话框,提示用户更新
				showUpdateDialog();
				break;
			case ENTER_HOME:
				//进入应用程序主界面,activity跳转过程
				enterHome();
				break;
			case URL_ERROR:
				ToastUtil.show(getApplicationContext(), "url异常");
				enterHome();
				break;
			case IO_ERROR:
				ToastUtil.show(getApplicationContext(), "读取异常");
				enterHome();
				break;
			case JSON_ERROR:
				ToastUtil.show(getApplicationContext(), "json解析异常");
				enterHome();
				break;
			}
		}
	};
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除掉当前activity头title
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);


		AdManager.getInstance(this).init("2f2c31cc7b078bd0", "bbb521418b4757dd", true);

		//初始化UI
        initUI();
        //初始化数据
        initData();
		//初始化动画
		initAnimation();
		//初始化数据库
		initDB();
		//如果已经生成快捷方法就不用生成了
		if(!SpUtil.getBoolean(this, ConstantValue.HAS_SHORTCUT, false)){
			//生成快捷方式
			initShortCut();
		}
	}

	/**
	 * 生成快捷方式
	 */
	private void initShortCut() {
		//1,给intent维护图标,名称，广播如果要接收，需要匹配这个action，广播接受者才能收到这个广播
		Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		//维护图标（维护数据的名称快捷方式的图标，设置一个资源图片）
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON,BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
		//名称（维护数据的名称快捷方式的名称）
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "黑马卫士");
		//2,点击快捷方式后跳转到的activity
		//2.1维护开启的意图对象（隐士意图）
		//因为通过这个去开启一个应用（隐士意图）
		Intent shortCutIntent = new Intent("android.intent.action.HOME");
		shortCutIntent.addCategory("android.intent.category.DEFAULT");
		//点完之后的跳转意图（需要配置文件配置隐士意图）
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortCutIntent);
		//3,发送广播
		sendBroadcast(intent);
		//4,告知sp已经生成快捷方式
		SpUtil.putBoolean(this, ConstantValue.HAS_SHORTCUT, true);
	}

/*
* 初始化数据库
* */
	private void initDB() {
		//1,归属地数据拷贝过程
		initAddressDB("address.db");
		//2,常用号码数据库拷贝过程
		initAddressDB("commonnum.db");
		//3,拷贝病毒数据库
		initAddressDB("antivirus.db");
	}


	/**
	 * 拷贝数据库值files文件夹下
	 * @param dbName	数据库名称
	 */
	private void initAddressDB(String dbName) {
		//1,在files文件夹下创建同名dbName数据库文件过程
		//获取文件夹路径
		File files = getFilesDir();
		//放文件名称
		File file = new File(files, dbName);
		//如果文件存在，那就返回出去，后面代码就不用走了
		//如果不存在文件，就执行以下代码
		if(file.exists()){
			return;
		}

		InputStream stream = null;
		FileOutputStream fos = null;
		//2,输入流读取第三方资产目录下的文件
		try {
			stream = getAssets().open(dbName);
			//3,将读取的内容写入到指定文件夹的文件中去
			fos = new FileOutputStream(file);
			//4,每次的读取内容大小
			byte[] bs = new byte[1024];
			//零时变量
			int temp = -1;
			//循环读流，如果结果不等于负一的话，还有内容，那就继续，直到-1为止
			while( (temp = stream.read(bs))!=-1){
				//写流
				fos.write(bs, 0, temp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			//都会执行的代码块，关闭流，之前都要做非空的判断
			if(stream!=null && fos!=null){
				try {
					stream.close();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}


	/**
	 * 广告页跳主页的动画效果-添加淡入动画效果
	 */
	private void initAnimation() {
		//从看不见到看的见的动画
		AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
		alphaAnimation.setDuration(3000);
		//让当前界面执行动画，之前需要找到view中的控件
		rl_root.startAnimation(alphaAnimation);
	}

	/**
	 * 弹出对话框,提示用户更新
	 */
	protected void showUpdateDialog() {
		//对话框,是依赖于activity存在的
		Builder builder = new Builder(this);
		//设置左上角图标
		builder.setIcon(R.mipmap.ic_launcher);
		builder.setTitle("版本更新");
		//设置描述内容
		builder.setMessage(mVersionDes);
		
		//积极按钮,立即更新
		builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//下载apk,apk链接地址,downloadUrl
				downloadApk();
			}
		});
		
		builder.setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//取消对话框,进入主界面
				enterHome();
			}
		});
		
		//点击取消事件监听
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				//即使用户点击取消,也需要让其进入应用程序主界面
				enterHome();
				dialog.dismiss();
			}
		});
		
		builder.show();
	}

	//下载的方法
	protected void downloadApk() {
		//apk下载链接地址,放置apk的所在路径
		
		//1,判断sd卡是否可用,是否挂在上
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			//2,获取sd路径
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+"mobilesafe74.apk";
			//3,发送请求,获取apk,并且放置到指定路径
			HttpUtils httpUtils = new HttpUtils();

			//httpUtils.send();这个方法是用来请求链接，列入json
			//4,发送请求,传递参数(下载地址,下载应用放置位置)
			httpUtils.download(mDownloadUrl, path, new RequestCallBack<File>() {
				@Override
				public void onSuccess(ResponseInfo<File> responseInfo) {
					//下载成功(下载过后的放置在sd卡中apk)
					Log.i(tag, "下载成功");
					File file = responseInfo.result;
					//提示用户安装
					installApk(file);
				}
				@Override
				public void onFailure(HttpException arg0, String arg1) {
					Log.i(tag, "下载失败");
					//下载失败
				}
				//刚刚开始下载方法
				@Override
				public void onStart() {
					Log.i(tag, "刚刚开始下载");
					super.onStart();
				}
				
				//下载过程中的方法(下载apk总大小,当前的下载位置,是否正在下载)
				@Override
				public void onLoading(long total, long current,boolean isUploading) {
					Log.i(tag, "下载中........");
					Log.i(tag, "total = "+total);
					Log.i(tag, "current = "+current);
					super.onLoading(total, current, isUploading);
				}
			});
			
		}
	}

	/**
	 * 安装对应apk
	 * @param file	安装文件
	 */
	protected void installApk(File file) {
		//系统应用界面,源码,安装apk入口
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		/*//文件作为数据源
		intent.setData(Uri.fromFile(file));
		//设置安装的类型
		intent.setType("application/vnd.android.package-archive");*/
		intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
//		startActivity(intent);
		//跳回引导页后，在跳主页，开启一个activity等结果
		startActivityForResult(intent, 0);
	}

	//上面有结果了，之后返回结果，在调用这个方法
	//开启一个activity后,返回结果调用的方法
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		enterHome();
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 进入应用程序主界面
	 */
	protected void enterHome() {
		Intent intent = new Intent(this,HomeActivity.class);
		startActivity(intent);
		//在开启一个新的界面后,将导航界面关闭(导航界面只可见一次)
		finish();
	}

	/**
	 * 获取数据方法
	 */
	private void initData() {
		//1,应用版本名称
		tv_version_name.setText("版本名称:"+getVersionName());
		//检测(本地版本号和服务器版本号比对)是否有更新,如果有更新,提示用户下载(member)
		//2,获取本地版本号
		mLocalVersionCode = getVersionCode();
//3,获取服务器版本号(客户端发请求,服务端给响应,(json,xml))
		//http://www.oxxx.com/update74.json?key=value  返回200 请求成功,流的方式将数据读取下来
		//json中内容包含:
		/* 更新版本的版本名称
		 * 新版本的描述信息
		 * 服务器版本号
		 * 新版本apk下载地址*/
		if(SpUtil.getBoolean(this, ConstantValue.OPEN_UPDATE, false)){
			checkVersion();
		}else{
			//直接进入应用程序主界面
//			enterHome();
			//消息机制，不能直接睡，不能堵塞主线程
//			mHandler.sendMessageDelayed(msg, 4000);
			//在发送消息4秒后去处理,ENTER_HOME状态码指向的消息
			//延时的意思是发完消息以后，延迟多久再去处理消息(传递消息的状态码，4秒)
			mHandler.sendEmptyMessageDelayed(ENTER_HOME, 4000);
		}
	}

	/**
	 * 检测版本号
	 *
	 * 服务器版的json：
	 * {
	 *     versionName:"2.0",
	 *     versionDes:"日方刚刚发生大幅度的斯蒂芬",
	 *     versionCode:"2",
	 *     versionUrl:"http://www.baidu.com/baidu.apk"
	 * }
	 *
	 */
	private void checkVersion() {
		//耗时操作在子线程里
		new Thread(){
			public void run() {
				//发送请求获取数据,参数则为请求json的链接地址
				//http://192.168.13.99:8080/update74.json	测试阶段不是最优
				//仅限于模拟器访问电脑tomcat
				//创建方法message的对象
				Message msg = Message.obtain();
				//记录一下开始请求网络的时间
				long startTime = System.currentTimeMillis();
				try {
					//1,封装url地址
					URL url = new URL("http://10.0.2.2:8080/update74.json");
					//2,开启一个链接
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();


					//3,设置常见请求参数(请求头)
					//请求超时（从来跟服务器没有连上过）
					connection.setConnectTimeout(2000);
					//读取超时（已经连上，但是断掉了）
					connection.setReadTimeout(2000);
					
					//默认就是get请求方式,
//					connection.setRequestMethod("POST");
					
					//4,获取请求成功响应码
					if(connection.getResponseCode() == 200){
						//5,以流的形式,将数据获取下来
						InputStream is = connection.getInputStream();
						//6,将流转换成字符串(工具类封装)
						String json = StreamUtil.streamToString(is);
						Log.i(tag, json);
						//7,json解析
						JSONObject jsonObject = new JSONObject(json);
						
						//debug调试,解决问题
						String versionName = jsonObject.getString("versionName");
						mVersionDes = jsonObject.getString("versionDes");
						String versionCode = jsonObject.getString("versionCode");
						mDownloadUrl = jsonObject.getString("downloadUrl");

						//日志打印	
						Log.i(tag, versionName);
						Log.i(tag, mVersionDes);
						Log.i(tag, versionCode);
						Log.i(tag, mDownloadUrl);
						
						//8,比对版本号(服务器版本号>本地版本号,提示用户更新)（强转成整型）
						if(mLocalVersionCode<Integer.parseInt(versionCode)){
							//提示用户更新,弹出对话框(UI),消息机制(就是handler的message)
							//状态码维护成静态常量
							msg.what = UPDATE_VERSION;
						}else{
							//进入应用程序主界面
							//状态码维护成静态常量
							msg.what = ENTER_HOME;
						}
					}
				}catch(MalformedURLException e) {
					e.printStackTrace();
					//异常情况也可以告诉主界面
					msg.what = URL_ERROR;
				}catch (IOException e) {
					e.printStackTrace();
					//异常情况也可以告诉主界面
					msg.what = IO_ERROR;
				} catch (JSONException e) {
					e.printStackTrace();
					//异常情况也可以告诉主界面
					msg.what = JSON_ERROR;
				}finally{
					//这里的代码一定发送出去
					//指定睡眠时间,请求网络的时长超过4秒则不做处理
					//请求网络的时长小于4秒,强制让其睡眠满4秒钟
					long endTime = System.currentTimeMillis();
					if(endTime-startTime<4000){
						try {
							Thread.sleep(4000-(endTime-startTime));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					mHandler.sendMessage(msg);
				}
			};
		}.start();
		
		/*new Thread(new Runnable() {
			@Override
			public void run() {
				
			}
		});*/
	}

	/**
	 * 返回版本号
	 * @return	
	 * 			非0 则代表获取成功
	 */
	private int getVersionCode() {
		//1,包管理者对象packageManager
		PackageManager pm = getPackageManager();
		//2,从包的管理者对象中,获取指定包名的基本信息(版本名称,版本号),传0代表获取基本信息
		try {
			PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
			//3,获取版本名称
			return packageInfo.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 获取版本名称:清单文件中
	 * @return	应用版本名称	返回null代表异常
	 */
	private String getVersionName() {
		//1,包管理者对象packageManager
		PackageManager pm = getPackageManager();
		//2,从包的管理者对象中,获取指定包名的基本信息(版本名称,版本号),传0代表获取基本信息
		try {
			PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
			//3,获取版本名称
			return packageInfo.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 初始化UI方法	alt+shift+j
	 */
	private void initUI() {
		tv_version_name = (TextView) findViewById(R.id.tv_version_name);
		rl_root = (RelativeLayout) findViewById(R.id.rl_root);
	}
}
