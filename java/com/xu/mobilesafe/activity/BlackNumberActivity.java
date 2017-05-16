package com.xu.mobilesafe.activity;

import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.xu.mobilesafe.R;
import com.xu.mobilesafe.db.dao.BlackNumberDao;
import com.xu.mobilesafe.db.domain.BlackNumberInfo;
import com.xu.mobilesafe.utils.ToastUtil;

//1,复用convertView
//2,对findViewById次数的优化,使用ViewHolder
//3,将ViewHolder定义成静态,不会去创建多个对象
//4,listView如果有多个条目的时候,可以做分页算法,每一次加载20条,逆序返回

public class BlackNumberActivity extends Activity {
	private Button bt_add;
	private ListView lv_blacknumber;
	private BlackNumberDao mDao;
	private List<BlackNumberInfo> mBlackNumberList;
	private MyAdapter mAdapter;
	//拦截模式的默认选择
	private int mode = 1;
	//条件三：是否listView在加载中
	private boolean mIsLoad = false;
	private int mCount;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			//4,告知listView可以去设置数据适配器
			if(mAdapter == null){
				mAdapter = new MyAdapter();
				lv_blacknumber.setAdapter(mAdapter);
			}else{
				//不是第一次请求加载，刷新一下就可以了
				mAdapter.notifyDataSetChanged();
			}
		};
	};

	//数据适配器
	class MyAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			return mBlackNumberList.size();
		}

		@Override
		public Object getItem(int position) {
			return mBlackNumberList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		//position索引的值每加一就走一边这个逻辑，有多少数据就走多少遍，而且listView在调用的时候要调用多遍，所有要复用
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
//			View view = null;
/*			if(convertView == null){
				view = View.inflate(getApplicationContext(), R.layout.listview_blacknumber_item, null);
			}else{
				view = convertView;
			}*/
			
			//1,复用convertView
			
			//复用viewHolder步骤一
			ViewHolder holder = null;
			//convertView之前已经有展示过这个对象，复用就可以了，简单的修改下内容，
			//convertView如果空的话就代表是第一次构建，就需要查找控件，后续条目做复用，
			//创建一个viewHolder对象，去存储第一步找到的所有的控件，当前的viewholder对象，又由convertView去做存储setTag（viewholder）
			//在复用convertView的条目展示的时候，找到之前设置过的tag，
			if(convertView == null){
				//为空就是没有复用过,直接复用convertView
				convertView = View.inflate(getApplicationContext(), R.layout.listview_blacknumber_item, null);
				//2,减少findViewById()次数
				//复用viewHolder步骤三
				holder = new ViewHolder();
				//复用viewHolder步骤四
				//在复用的情况下不需要在走一边了，找到控件之后存储在viewholder里面，viewholder在放到convertView
				holder.tv_phone = (TextView) convertView.findViewById(R.id.tv_phone);
				holder.tv_mode = (TextView)convertView.findViewById(R.id.tv_mode);
				holder.iv_delete = (ImageView)convertView.findViewById(R.id.iv_delete);
				//复用viewHolder步骤五，存储viewholer对象
				convertView.setTag(holder);
			}else{
				//获取viewholer对象
				holder = (ViewHolder) convertView.getTag();
			}

			//在这里做删除操作
			holder.iv_delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//删除的时候也要同步
					//1,数据库删除，拿到position这个相应的对象
					mDao.delete(mBlackNumberList.get(position).phone);
					//2,集合中的删除
					mBlackNumberList.remove(position);
					//3,通知数据适配器刷新
					if(mAdapter!=null){
						mAdapter.notifyDataSetChanged();
					}
				}
			});
			
			holder.tv_phone.setText(mBlackNumberList.get(position).phone);
			int mode = Integer.parseInt(mBlackNumberList.get(position).mode);
			switch (mode) {
			case 1:
				holder.tv_mode.setText("拦截短信");
				break;
			case 2:
				holder.tv_mode.setText("拦截电话");
				break;
			case 3:
				holder.tv_mode.setText("拦截所有");
				break;
			}
			return convertView;
		}
	}
	//复用viewHolder步骤二
	//这个类包含的控件的字段
	static class ViewHolder{
		TextView tv_phone;
		TextView tv_mode;
		ImageView iv_delete;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blacknumber);

		//初始化UI
		initUI();
		//准备数据填充UI
		initData();
	}

	private void initData() {
		//获取数据库中所有电话号码
		new Thread(){
			public void run() {
				//1,获取操作黑名单数据库的对象
				mDao = BlackNumberDao.getInstance(getApplicationContext());
				//2,查询部分数据，第一次就传0，倒叙后20条数据
				mBlackNumberList = mDao.find(0);
				//获取数据库中的数量
				mCount = mDao.getCount();
				
				//3,通过消息机制告知主线程可以去使用包含数据的集合
				mHandler.sendEmptyMessage(0);
			}
		}.start();
	}

	private void initUI() {
		bt_add = (Button) findViewById(R.id.bt_add);
		lv_blacknumber = (ListView) findViewById(R.id.lv_blacknumber);
		
		bt_add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//添加按钮的操作
				showDialog();
			}
		});
		
		//监听listView的其滚动状态
		lv_blacknumber.setOnScrollListener(new OnScrollListener() {
			//滚动过程中,状态发生改变调用方法()
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
//				OnScrollListener.SCROLL_STATE_FLING	飞速滚动
//				OnScrollListener.SCROLL_STATE_IDLE	 空闲状态
//				OnScrollListener.SCROLL_STATE_TOUCH_SCROLL	拿手触摸着去滚动状态
				//如果集合不等于空
				if(mBlackNumberList!=null){
					//条件一:滚动到停止状态
					//条件二:最后一个条目可见(最后一个条目的索引值>=数据适配器中集合的总条目个数-1)
					//条件三：是否listView在加载中，因为查询是耗时操作，可能会反复加载，所以类型加锁
					if(scrollState == OnScrollListener.SCROLL_STATE_IDLE
							&& lv_blacknumber.getLastVisiblePosition()>=mBlackNumberList.size()-1
							&& !mIsLoad){
						/*mIsLoad防止重复加载的变量
						如果当前正在加载mIsLoad就会为true,本次加载完毕后,再将mIsLoad改为false
						如果下一次加载需要去做执行的时候,会判断上诉mIsLoad变量,是否为false,如果为true,就需要等待上一次加载完成,将其值
						改为false后再去加载*/
						
						//如果条目总数大于集合大小的时,才可以去继续加载更多
						if(mCount>mBlackNumberList.size()){
							//加载下一页数据
							new Thread(){
								public void run() {
									//1,获取操作黑名单数据库的对象
									mDao = BlackNumberDao.getInstance(getApplicationContext());
									//2,查询部分数据，更多的数据
									List<BlackNumberInfo> moreData = mDao.find(mBlackNumberList.size());
									//3,添加下一页数据的过程
									mBlackNumberList.addAll(moreData);
									//4,通知数据适配器刷新
									mHandler.sendEmptyMessage(0);
								}
							}.start();
						}
					}
				}
				
			}
			
			//滚动过程中调用方法
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				
			}
		});
	}

	//添加按钮的操作
	protected void showDialog() {
		//新建一个弹窗
		Builder builder = new Builder(this);
		//自定义所有create
		final AlertDialog dialog = builder.create();
		View view = View.inflate(getApplicationContext(), R.layout.dialog_add_blacknumber, null);
		dialog.setView(view, 0, 0, 0, 0);
		
		final EditText et_phone = (EditText) view.findViewById(R.id.et_phone);
		RadioGroup rg_group = (RadioGroup) view.findViewById(R.id.rg_group);
		
		Button bt_submit = (Button) view.findViewById(R.id.bt_submit);
		Button bt_cancel = (Button)view.findViewById(R.id.bt_cancel);
		
		//监听其选中条目的切换过程
		rg_group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.rb_sms:
					//拦截短信
					mode = 1;
					break;
				case R.id.rb_phone:
					//拦截电话
					mode = 2;
					break;
				case R.id.rb_all:
					//拦截所有
					mode = 3;
					break;
				}
			}
		});
		//提交按钮的点击事件
		bt_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//1,获取输入框中的电话号码
				String phone = et_phone.getText().toString();
				//如果号码不为空的话
				if(!TextUtils.isEmpty(phone)){
					//2,数据库插入当前输入的拦截电话号码
					mDao.insert(phone, mode+"");
					//已经插入数据库，但是还没有插入集合，所有需要2边一起插入才能同步，这里用了第二种，插入集合
					//3,让数据库和集合保持同步(1.数据库中数据重新读一遍,2.手动向集合中添加一个对象(插入数据构建的对象))
					BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
					blackNumberInfo.phone = phone;
					//模式是前面维护好的，这里因为需要字符串所以加了个空字符串
					blackNumberInfo.mode = mode+"";
					//4,将对象插入到集合的最顶部
					mBlackNumberList.add(0, blackNumberInfo);
					//5,通知数据适配器刷新(数据适配器中的数据有改变了)
					if(mAdapter!=null){
						mAdapter.notifyDataSetChanged();
					}
					//6,隐藏对话框
					dialog.dismiss();
				}else{
					ToastUtil.show(getApplicationContext(), "请输入拦截号码");
				}
			}
		});

		//取消按钮
		bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		dialog.show();
	}
}	
