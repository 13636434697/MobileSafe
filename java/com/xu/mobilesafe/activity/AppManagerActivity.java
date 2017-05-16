package com.xu.mobilesafe.activity;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import  android.view.View.OnClickListener;

import com.xu.mobilesafe.R;
import com.xu.mobilesafe.db.domain.AppInfo;
import com.xu.mobilesafe.receiver.AppInfoProvider;
import com.xu.mobilesafe.utils.ToastUtil;

public class AppManagerActivity extends Activity implements OnClickListener{
	private List<AppInfo> mAppInfoList;

	private AppInfo mAppInfo;
	private ListView lv_app_list;
	private MyAdapter mAdapter;
	private PopupWindow mPopupWindow;
	
	private List<AppInfo> mSystemList;
	private List<AppInfo> mCustomerList;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			//给listView填充数据
			mAdapter = new MyAdapter();
			lv_app_list.setAdapter(mAdapter);

			//填充完数据之后在写标题，之前要容错处理
			if(tv_des!=null && mCustomerList!=null){
				tv_des.setText("用户应用("+mCustomerList.size()+")");
			}
		};
	};

	private TextView tv_des;

	//给listView填充数据
	class MyAdapter extends BaseAdapter{
		
		//获取数据适配器中条目类型的总数,修改成两种(纯文本,图片+文字)
		@Override
		public int getViewTypeCount() {
			return super.getViewTypeCount()+1;
		}
		
		//指定索引指向的条目类型,条目类型状态码指定(0(复用系统),1)
		@Override
		public int getItemViewType(int position) {
			//如果对象等于零或者对象等于用户应用大小加1
			if(position == 0 || position == mCustomerList.size()+1){
				//返回0,代表纯文本条目的状态码
				return 0;
			}else{
				//返回1,代表图片+文本条目状态码
				return 1;
			}
		}
		
		//listView中添加两个描述条目
		@Override
		public int getCount() {
			//获取用户应用和系统应用，在加上2条title的信息才是集合的总数
			return mCustomerList.size()+mSystemList.size()+2;
		}

		//获取条目.返回的是appinfo集合
		//要求是用户应用要显示在上面，系统应用显示在下面
		@Override
		public AppInfo getItem(int position) {
			//如果对象等于零或者对象等于用户应用大小加1
			if(position == 0 || position == mCustomerList.size()+1){
				return null;
			}else{
				//如果对象小于用户应用大小+1
				if(position<mCustomerList.size()+1){
					//返回用户对象减1
					return mCustomerList.get(position-1);
				}else{
					//返回系统应用对应条目的对象
					return mSystemList.get(position - mCustomerList.size()-2);
				}
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//获取条目的类型
			int type = getItemViewType(position);
			//如果等于0就是纯文字，就是标题
			if(type == 0){
				//展示灰色纯文本条目
				ViewTitleHolder holder = null;
				if(convertView == null){
					convertView = View.inflate(getApplicationContext(), R.layout.listview_app_item_title, null);
					holder = new ViewTitleHolder();
					holder.tv_title = (TextView)convertView.findViewById(R.id.tv_title);
					convertView.setTag(holder);
				}else{
					holder = (ViewTitleHolder) convertView.getTag();
				}
				if(position == 0){
					//设置文字
					holder.tv_title.setText("用户应用("+mCustomerList.size()+")");
				}else{
					//设置文字
					holder.tv_title.setText("系统应用("+mSystemList.size()+")");
				}
				return convertView;
				//如果等于1就是图片文字，就是内容
			}else{
				//展示图片+文字条目
				ViewHolder holder = null;
				if(convertView == null){
					convertView = View.inflate(getApplicationContext(), R.layout.listview_app_item, null);
					holder = new ViewHolder();
					holder.iv_icon = (ImageView)convertView.findViewById(R.id.iv_icon);
					holder.tv_name = (TextView)convertView.findViewById(R.id.tv_name);
					holder.tv_path = (TextView) convertView.findViewById(R.id.tv_path);
					convertView.setTag(holder);
				}else{
					holder = (ViewHolder) convertView.getTag();
				}
				//设置图片
				holder.iv_icon.setBackgroundDrawable(getItem(position).icon);
				//设置文字
				holder.tv_name.setText(getItem(position).name);
				//判断条目对象是否是sd卡
				if(getItem(position).isSdCard){
					holder.tv_path.setText("sd卡应用");
				}else{
					holder.tv_path.setText("手机应用");
				}
				return convertView;
			}
		}
	}

	//图片加文字的holder
	static class ViewHolder{
		ImageView iv_icon;
		TextView tv_name;
		TextView tv_path;
	}
	//纯文字的holder
	static class ViewTitleHolder{
		TextView tv_title;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_manager);

		//初始化标题
		initTitle();
		initList();
	}

	//初始化listView
	private void initList() {
		lv_app_list = (ListView) findViewById(R.id.lv_app_list);
		tv_des = (TextView) findViewById(R.id.tv_des);
		
		new Thread(){
			public void run() {
				//取出包信息的集合
				mAppInfoList = AppInfoProvider.getAppInfoList(getApplicationContext());
				//需要将用户应用和系统应用分开展示
				//新建一个系统的集合
				mSystemList = new ArrayList<AppInfo>();
				//新建一个用户的集合
				mCustomerList = new ArrayList<AppInfo>();
				//遍历包集合
				for (AppInfo appInfo : mAppInfoList) {
					//判断是不是系统应用
					if(appInfo.isSystem){
						//系统应用
						mSystemList.add(appInfo);
					}else{
						//用户应用
						mCustomerList.add(appInfo);
					}
				}
				//循环完成后发送空消息
				mHandler.sendEmptyMessage(0);
			};
		}.start();

		//给listView添加一个滚动监听事件，在listView上面做悬浮条目
		lv_app_list.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				//滚动过程中调用方法
				//AbsListView中view就是listView对象
				//firstVisibleItem第一个可见条目索引值
				//visibleItemCount当前一个屏幕的可见条目数
				//总共条目总数
				if(mCustomerList!=null && mSystemList!=null){
					//第一个可见的条目
					if(firstVisibleItem>=mCustomerList.size()+1){
						//滚动到了系统条目
						tv_des.setText("系统应用("+mSystemList.size()+")");
					}else{
						//滚动到了用户应用条目
						tv_des.setText("用户应用("+mCustomerList.size()+")");
					}
				}
				
			}
		});

		//listView条目的点击事件
		lv_app_list.setOnItemClickListener(new OnItemClickListener() {
			//view点中条目指向的view对象
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//点击到title就不响应（纯文字）
				if(position == 0 || position == mCustomerList.size()+1){
					return;
				}else{
					if(position<mCustomerList.size()+1){
						mAppInfo = mCustomerList.get(position-1);
					}else{
						//返回系统应用对应条目的对象
						mAppInfo = mSystemList.get(position - mCustomerList.size()-2);
					}
					//点击listView条目后要弹出窗口
					showPopupWindow(view);
				}
			}
		});
	}

	//listView条目弹窗的方法
	protected void showPopupWindow(View view) {
		//被点击的条目对象传进来，这个view才是指定位置的view
		View popupView = View.inflate(this, R.layout.popupwindow_layout, null);

		TextView tv_uninstall = (TextView) popupView.findViewById(R.id.tv_uninstall);
		TextView tv_start = (TextView) popupView.findViewById(R.id.tv_start);
		TextView tv_share = (TextView) popupView.findViewById(R.id.tv_share);

		tv_uninstall.setOnClickListener(this);
		tv_start.setOnClickListener(this);
		tv_share.setOnClickListener(this);

		//透明动画(透明--->不透明)
		AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
		alphaAnimation.setDuration(1000);
		alphaAnimation.setFillAfter(true);

		//缩放动画
		ScaleAnimation scaleAnimation = new ScaleAnimation(
				0, 1,
				0, 1,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		scaleAnimation.setDuration(1000);
		alphaAnimation.setFillAfter(true);
		//动画集合Set，共享一个查补器，是否用同一个数学函数，达到运动效果
		AnimationSet animationSet = new AnimationSet(true);
		//添加两个动画
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(scaleAnimation);

		//1,创建窗体对象,指定宽高（让弹窗填满父窗体，如果填满屏幕就marent）
		mPopupWindow = new PopupWindow(popupView,
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT, true);
		//2,设置一个透明背景(new ColorDrawable())因为设置背景，可以响应物理键的回退。
		mPopupWindow.setBackgroundDrawable(new ColorDrawable());
		//3,指定窗体位置，在某个控件的下面，在指定偏移量
		//被点击的条目对象传进来，这个view才是指定位置的view
		//mPopupWindow.showAtLocation();
		mPopupWindow.showAsDropDown(view, 50, -view.getHeight());
		//4,popupView执行动画
		popupView.startAnimation(animationSet);
	}

	//初始化标题
	private void initTitle() {
		//1,获取磁盘(内存,区分于手机运行内存)可用大小,磁盘路径
		String path = Environment.getDataDirectory().getAbsolutePath();
		//2,获取sd卡可用大小,sd卡路径
		String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		//3,获取以上两个路径下文件夹的可用大小（获取出来的单位是bite，需要格式化一下mb）
		String memoryAvailSpace = Formatter.formatFileSize(this, getAvailSpace(path));
		String sdMemoryAvailSpace = Formatter.formatFileSize(this,getAvailSpace(sdPath));
		
		TextView tv_memory = (TextView) findViewById(R.id.tv_memory);
		TextView tv_sd_memory = (TextView) findViewById(R.id.tv_sd_memory);
		
		tv_memory.setText("磁盘可用:"+memoryAvailSpace);
		tv_sd_memory.setText("sd卡可用:"+sdMemoryAvailSpace);
	}
	//弹窗卸载应用之后，没有刷新listView，所以在重写获取焦点的时候在重写获取下数据
	@Override
	protected void onResume() {
		//重新获取数据
		getData();
		super.onResume();
	}

	//int代表多少个G	
	/**
	 * 返回值结果单位为byte = 8bit,最大结果为2147483647 bytes
	 * @param path
	 * @return	返回指定路径可用区域的byte类型值
	 *
	 * 获取以上两个路径下文件夹的可用大小
	 */
	private long getAvailSpace(String path) {
		//获取可用磁盘大小类
		StatFs statFs = new StatFs(path);
		//获取可用区块的个数
		long count = statFs.getAvailableBlocks();
		//获取区块的大小
		long size = statFs.getBlockSize();
		//区块大小*可用区块个数 == 可用空间大小
		return count*size;
//		Integer.MAX_VALUE	代表int类型数据的最大大小
//		0x7FFFFFFF
//		
//		2147483647bytes/1024 =  2096128 KB
//		2096128KB/1024 = 2047	MB
//		2047MB = 2G
	}

	//重新获取数据
	//弹窗卸载应用之后，没有刷新listView，所以在重写获取焦点的时候在重写获取下数据
	private void getData() {
		new Thread(){
			public void run() {
				//通过应用的引擎类，来获取列表的信息
				mAppInfoList = AppInfoProvider.getAppInfoList(getApplicationContext());
				mSystemList = new ArrayList<AppInfo>();
				mCustomerList = new ArrayList<AppInfo>();
				for (AppInfo appInfo : mAppInfoList) {
					if(appInfo.isSystem){
						//系统应用
						mSystemList.add(appInfo);
					}else{
						//用户应用
						mCustomerList.add(appInfo);
					}
				}
				mHandler.sendEmptyMessage(0);
			};
		}.start();
	}

	//listView条目弹窗的点击方法的事件
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tv_uninstall:
				//判断是否是系统应用
				if (mAppInfo.isSystem) {
					ToastUtil.show(getApplicationContext(), "此应用不能卸载");
				} else {
					//卸载程序
					Intent intent = new Intent("android.intent.action.DELETE");
					intent.addCategory("android.intent.category.DEFAULT");
					intent.setData(Uri.parse("package:" + mAppInfo.getPackageName()));
					startActivity(intent);
				}
				break;
			case R.id.tv_start:
				//通过桌面去启动指定包名应用launch
				PackageManager pm = getPackageManager();
				//通过Launch开启制定包名的意图,去开启应用
				Intent launchIntentForPackage = pm.getLaunchIntentForPackage(mAppInfo.getPackageName());
				//非空的判断
				if (launchIntentForPackage != null) {
					startActivity(launchIntentForPackage);
				} else {
					ToastUtil.show(getApplicationContext(), "此应用不能被开启");
				}
				break;
			//分享(第三方(微信,新浪,腾讯)平台),智慧北京
			//拍照-->分享:将图片上传到微信服务器,微信提供接口api,推广
			//查看朋友圈的时候:从服务器上获取数据(你上传的图片)
			case R.id.tv_share:
				//通过短信应用,向外发送短信
				Intent intent = new Intent(Intent.ACTION_SEND);
				//要把文本数据传送给短信的应用，短信的内容
				intent.putExtra(Intent.EXTRA_TEXT, "分享一个应用,应用名称为" + mAppInfo.getName());
				//数据类型
				intent.setType("text/plain");
				startActivity(intent);
				break;
		}

		//点击了窗体后消失窗体
		if (mPopupWindow != null) {
			mPopupWindow.dismiss();
		}
	}
}
