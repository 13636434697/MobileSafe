package com.xu.mobilesafe.engine;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/*
* 查询数据库号码的类
* */
public class CommonnumDao {
	//1,指定访问数据库的路径
	public String path = "data/data/com.itheima.mobilesafe74/files/commonnum.db";
	//2,开启数据(数据库里面的数据是以组存在的)
	//对第一张表的操作
	public List<Group> getGroup(){
		//操作数据库，开启一个数据库（路径，游标工厂，只读权限）
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
		//拿到db去查询（组所在的表，查询字段的名称）
		Cursor cursor = db.query("classlist", new String[]{"name","idx"}, null, null, null,null,null, null);
		//组的集合
		List<Group> groupList = new ArrayList<Group>();
		while (cursor.moveToNext()) {
			//这个对象循环一次创建一次，在添加到集合里面去
			Group group = new Group();
			//第一个是名称，添加到集合处理
			group.name = cursor.getString(0);
			//第二个是索引值，添加到集合处理
			group.idx = cursor.getString(1);
			//查询2表，要调用2次，比较麻烦，所以在循环的时候直接调用这里就可以了
			group.childList = getChild(group.idx);
//			添加到集合里面去循环一次添加一次
			groupList.add(group);
		}
		//关闭游标和数据库
		cursor.close();
		db.close();
		return groupList;
	}
	//获取每一个组中孩子节点的数据，需要传那张表的参数index
	public List<Child> getChild(String idx){
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
//		Cursor cursor = db.query("table"+idx, new String[]{"name","idx"}, null, null, null,null,null, null);
		//使用原生的sql语句，后面一个参数null是如果有问号的话，这里可以填充
		Cursor cursor = db.rawQuery("select * from table"+idx+";", null);
		List<Child> childList = new ArrayList<Child>();
		while (cursor.moveToNext()) {
			//生成对象
			Child child = new Child();
			//一共三个字段
			child._id = cursor.getString(0);
			child.number = cursor.getString(1);
			child.name = cursor.getString(2);
			//添加到集合
			childList.add(child);
		}
		//关闭游标
		cursor.close();
		db.close();
		return childList;
	}

	//集合所对应的泛型
	public class Group{
		public String name;
		public String idx;
		public List<Child> childList;
	}
	
	public class Child{
		public String _id;
		public String number;
		public String name;
	}
}
