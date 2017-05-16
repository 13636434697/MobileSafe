package com.xu.mobilesafe.activity;


import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

import com.xu.mobilesafe.engine.CommonnumDao.Group;

import com.xu.mobilesafe.R;
import com.xu.mobilesafe.engine.CommonnumDao;

/*
* 查询号码的界面
* */
public class CommonNumberQueryActivity extends Activity {
	private ExpandableListView elv_common_number;
	private List<Group> mGroup;
	private MyAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_common_number);
		//初始化UI
		initUI();
		//给可扩展ListView准备数据,并且填充
		initData();
	}

	/**
	 * 给可扩展ListView准备数据,并且填充
	 */
	private void initData() {
		CommonnumDao commonnumDao = new CommonnumDao();
		mGroup = commonnumDao.getGroup();

		//填充数据
		mAdapter = new MyAdapter();
		elv_common_number.setAdapter(mAdapter);
		//给可扩展listview注册点击事件
		elv_common_number.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				//开启一个打电话
				startCall(mAdapter.getChild(groupPosition, childPosition).number);
				//在adapter返回点击事件是否实现，相对的，这里true的话自己管理事件，false是拓展listView来管理
				return false;
			}
		});
	}

	//开启一个打电话
	protected void startCall(String number) {
		//开启系统的打电话界面
		Intent intent = new Intent(Intent.ACTION_CALL);
		intent.setData(Uri.parse("tel:" + number));
		//自动添加的代码，可能是判断权限
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		startActivity(intent);
	}

	//初始化UI
	private void initUI() {
		elv_common_number = (ExpandableListView) findViewById(R.id.elv_common_number);
	}

	//继承的是扩展listView的adapter
	class MyAdapter extends BaseExpandableListAdapter{
		//获取对应的主中间的孩子节点的对象
		@Override
		public CommonnumDao.Child getChild(int groupPosition, int childPosition) {
			return mGroup.get(groupPosition).childList.get(childPosition);
		}

		//孩子节点的id
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

//		/生成孩子节点的view对象
		@Override
		public View getChildView(int groupPosition, int childPosition,boolean isLastChild, View convertView, ViewGroup parent) {
			View view = View.inflate(getApplicationContext(), R.layout.elv_child_item, null);
			TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
			TextView tv_number = (TextView) view.findViewById(R.id.tv_number);

			//赋值，getchild方法已经操作好了
			tv_name.setText(getChild(groupPosition, childPosition).name);
			tv_number.setText(getChild(groupPosition, childPosition).number);
			
			return view;
		}

		//获取孩子节点的总数，传组的索引
		@Override
		public int getChildrenCount(int groupPosition) {
			return mGroup.get(groupPosition).childList.size();
		}

		//组所对应的对象
		@Override
		public Group getGroup(int groupPosition) {
			return mGroup.get(groupPosition);
		}

		// 组集合的大小
		@Override
		public int getGroupCount() {
			return mGroup.size();
		}

		//组的id
		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}


		//组的相应的view，需要维护每一个组所对应的布局
		//dip = dp
		//dpi == ppi	像素密度(每一个英寸上分布的像素点的个数)
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,View convertView, ViewGroup parent) {
			TextView textView = new TextView(getApplicationContext());
			//文本从组所对应的集合中来，怎么拿组中间的对象，就是组的索引来（getgroup已经做好了，根据索引拿组的网络对象）
			textView.setText("			"+getGroup(groupPosition).name);
			textView.setTextColor(Color.RED);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
			return textView;
		}

		//是否将group组对应集合的id是否作为适配器的id
		@Override
		public boolean hasStableIds() {
			return false;
		}

		//孩子节点是否响应事件
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}
}
