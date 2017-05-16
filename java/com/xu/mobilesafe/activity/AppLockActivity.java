package com.xu.mobilesafe.activity;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import android.view.View.OnClickListener;
import com.xu.mobilesafe.R;
import com.xu.mobilesafe.db.dao.AppLockDao;
import com.xu.mobilesafe.db.domain.AppInfo;
import com.xu.mobilesafe.receiver.AppInfoProvider;

public class AppLockActivity extends Activity {
	private Button bt_unlock,bt_lock;
	private LinearLayout ll_unlock,ll_lock;
	private TextView tv_unlock,tv_lock;
	private ListView lv_unlock,lv_lock;
	private List<AppInfo> mAppInfoList;
	private List<AppInfo> mLockList;
	private List<AppInfo> mUnLockList;
	private AppLockDao mDao;

	private MyAdapter mLockAdapter;
	private MyAdapter mUnLockAdapter;
	private TranslateAnimation mTranslateAnimation;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			//6.接收到消息,填充已加锁和未加锁的数据适配器
			//已经加锁的数据适配器
			mLockAdapter = new MyAdapter(true);
			lv_lock.setAdapter(mLockAdapter);

			//为经加锁的数据适配器
			mUnLockAdapter = new MyAdapter(false);
			lv_unlock.setAdapter(mUnLockAdapter);
		};
	};

	//设置已加锁和未加锁的数据适配器，通用一个数据适配器
	class MyAdapter extends BaseAdapter {
		private boolean isLock;
		/**
		 * @param isLock	用于区分已加锁和未加锁应用的标示	true已加锁数据适配器	false未加锁数据适配器
		 */
		public MyAdapter(boolean isLock) {
			this.isLock = isLock;
		}
		//返回数据适配器条目的总数
		@Override
		public int getCount() {
			if(isLock){
				//这里需要字符串，如果不加字符串，就会当资源id
				tv_lock.setText("已加锁应用:"+mLockList.size());
				return mLockList.size();
			}else{
				tv_unlock.setText("未加锁应用:"+mUnLockList.size());
				return mUnLockList.size();
			}
		}

		//如果得到的是已加锁的数据适配器，得到的条目是已加锁的数据集合根据索引得到的对象
		@Override
		public AppInfo getItem(int position) {
			if(isLock){
				return mLockList.get(position);
			}else{
				return mUnLockList.get(position);
			}
		}

		//固定的索引值
		@Override
		public long getItemId(int position) {
			return position;
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null){
				convertView = View.inflate(getApplicationContext(), R.layout.listview_islock_item, null);
				holder = new ViewHolder();
				holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.iv_lock = (ImageView) convertView.findViewById(R.id.iv_lock);

				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			//就能拿到已加锁还是为枷锁的对象
			//内部类使用外部变量convertView，需要加final，但是final不能赋值，所以convertView赋给临时变量，在用临时变量执行动画
			final AppInfo appInfo = getItem(position);
			final View animationView = convertView;

			holder.iv_icon.setBackgroundDrawable(appInfo.icon);
			holder.tv_name.setText(appInfo.name);
			if(isLock){
				holder.iv_lock.setBackgroundResource(R.mipmap.lock);
			}else{
				holder.iv_lock.setBackgroundResource(R.mipmap.unlock);
			}
			//锁的点击事件，执行的时候是伴随动画效果
			holder.iv_lock.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//添加动画效果,动画默认是非阻塞的,所以执行动画的同时,动画以下的代码也会执行
					animationView.startAnimation(mTranslateAnimation);//500毫秒
					//对动画执行过程做事件监听,监听到动画执行完成后,再去移除集合中的数据,操作数据库,刷新界面
					mTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
							//动画开始的是调用方法
						}
						@Override
						public void onAnimationRepeat(Animation animation) {
							//动画重复时候调用方法
						}
						//动画执行结束后调用方法
						@Override
						public void onAnimationEnd(Animation animation) {
							//添加条目放在最顶部和最底部的话，需要实现，这里没有实现
							if(isLock){
								//已加锁------>未加锁过程
								//1.已加锁集合删除一个,未加锁集合添加一个,对象就是getItem方法获取的对象
								mLockList.remove(appInfo);
								mUnLockList.add(appInfo);
								//2.从已加锁的数据库中删除一条数据，按包名去删除
								mDao.delete(appInfo.packageName);
								//3.刷新数据适配器
								mLockAdapter.notifyDataSetChanged();
							}else{
								//未加锁------>已加锁过程
								//1.已加锁集合添加一个,未加锁集合移除一个,对象就是getItem方法获取的对象
								mLockList.add(appInfo);
								mUnLockList.remove(appInfo);
								//2.从已加锁的数据库中插入一条数据
								mDao.insert(appInfo.packageName);
								//3.刷新数据适配器
								mUnLockAdapter.notifyDataSetChanged();
							}
						}
					});
				}
			});
			return convertView;
		}
	}

	//控件的id
	static class ViewHolder{
		ImageView iv_icon;
		TextView tv_name;
		ImageView iv_lock;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_lock);
		//初始化控件
		initUI();
		//初始化数据
		initData();
		//初始化平移动画的方法(平移自身的一个宽度大小)
		initAnimation();
	}


	/**
	 * 初始化平移动画的方法(平移自身的一个宽度大小)
	 */
	private void initAnimation() {
		//Y没有变化，X从0到1，依赖于自身
		mTranslateAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 1,
				Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0);
		mTranslateAnimation.setDuration(500);
	}

	/**
	 * 区分已加锁和未加锁应用的集合
	 *
	 * 已加锁和为枷锁等于手机中所有的应用（AppInfoProvider）
	 */
	private void initData() {
		new Thread(){
			public void run() {
				//1.获取所有手机中的应用
				mAppInfoList = AppInfoProvider.getAppInfoList(getApplicationContext());
				//2.区分已加锁应用和未加锁应用
				mLockList = new ArrayList<AppInfo>();
				mUnLockList = new ArrayList<AppInfo>();
				
				//3.获取数据库中已加锁应用包名的的结合
				mDao = AppLockDao.getInstance(getApplicationContext());
				List<String> lockPackageList = mDao.findAll();
				//遍历查出来的集合
				for (AppInfo appInfo : mAppInfoList) {
					//4,如果循环到的应用的包名,在数据库中,则说明是已加锁应用
					if(lockPackageList.contains(appInfo.packageName)){
						mLockList.add(appInfo);
					}else{
						mUnLockList.add(appInfo);
					}
				}
				//5.告知主线程,可以使用维护的数据
				mHandler.sendEmptyMessage(0);

			};
		}.start();
	}

	//初始化控件
	private void initUI() {
		bt_unlock = (Button) findViewById(R.id.bt_unlock);
		bt_lock = (Button) findViewById(R.id.bt_lock);
		
		ll_unlock = (LinearLayout) findViewById(R.id.ll_unlock);
		ll_lock = (LinearLayout) findViewById(R.id.ll_lock);
		
		tv_unlock = (TextView) findViewById(R.id.tv_unlock);
		tv_lock = (TextView) findViewById(R.id.tv_lock);
		
		lv_unlock = (ListView) findViewById(R.id.lv_unlock);
		lv_lock = (ListView) findViewById(R.id.lv_lock);

		//为枷锁列表点击事件
		bt_unlock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//1.已加锁列表隐藏,未加锁列表显示
				ll_lock.setVisibility(View.GONE);
				ll_unlock.setVisibility(View.VISIBLE);
				//2.未加锁变成深色图片,已加锁变成浅色图片
				bt_unlock.setBackgroundResource(R.drawable.tab_left_pressed);
				bt_lock.setBackgroundResource(R.drawable.tab_right_default);
			}
		});

		//已枷锁列表点击事件
		bt_lock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//1.已加锁列表显示,未加锁列表隐藏
				ll_lock.setVisibility(View.VISIBLE);
				ll_unlock.setVisibility(View.GONE);
				//2.未加锁变成浅色图片,已加锁变成深色图片
				bt_unlock.setBackgroundResource(R.drawable.tab_left_default);
				bt_lock.setBackgroundResource(R.drawable.tab_right_pressed);
			}
		});
	}
}
