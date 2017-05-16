package com.xu.mobilesafe.db.dao;


import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.xu.mobilesafe.db.AppLockOpenHelper;


public class AppLockDao {
	private AppLockOpenHelper appLockOpenHelper;
	private Context context;
	//BlackNumberDao单例模式
	//1,私有化构造方法
	private AppLockDao(Context context){
		//需要传一个上下文
		this.context = context;
		//创建数据库已经其表机构
		appLockOpenHelper = new AppLockOpenHelper(context);
	}
	//2,声明一个当前类的对象
	private static AppLockDao appLockDao = null;
	//3,提供一个静态方法,如果当前类的对象为空,创建一个新的
	public static AppLockDao getInstance(Context context){
		if(appLockDao == null){
			appLockDao = new AppLockDao(context);
		}
		return appLockDao;
	}
	
	//插入方法
	public void insert(String packagename){
		SQLiteDatabase db = appLockOpenHelper.getWritableDatabase();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put("packagename", packagename);
		
		db.insert("applock", null, contentValues);
		db.close();

		//如果程序拦截已经开启，再去添加程序锁，并不会起作用，因为刚插入的数据库的程序没有办法获取到，需要在刷新一次数据库，才能拦截
		//注册一个内容观察者,观察数据库的变化,一旦数据有删除或者添加,则需要让mPacknameList重新获取一次数据
		//通知内容观察者，数据有改变的方法，uri自己定义，匹配上字符串就可以了，但是需要匹配规则
		context.getContentResolver().notifyChange(Uri.parse("content://applock/change"), null);
	}
	//删除方法
	public void delete(String packagename){
		SQLiteDatabase db = appLockOpenHelper.getWritableDatabase();
		
		ContentValues contentValues = new ContentValues();
		contentValues.put("packagename", packagename);
		
		db.delete("applock", "packagename = ?", new String[]{packagename});
		
		db.close();

		//如果程序拦截已经开启，再去添加程序锁，并不会起作用，因为刚插入的数据库的程序没有办法获取到，需要在刷新一次数据库，才能拦截
		//注册一个内容观察者,观察数据库的变化,一旦数据有删除或者添加,则需要让mPacknameList重新获取一次数据
		//通知内容观察者，数据有改变的方法，uri自己定义，匹配上字符串就可以了，但是需要匹配规则
		context.getContentResolver().notifyChange(Uri.parse("content://applock/change"), null);
	}
	//查询所有
	public List<String> findAll(){
		SQLiteDatabase db = appLockOpenHelper.getWritableDatabase();
		Cursor cursor = db.query("applock", new String[]{"packagename"}, null, null, null, null, null);
		List<String> lockPackageList = new ArrayList<String>();
		while(cursor.moveToNext()){
			//拿到这个字段之后在添加到集合
			lockPackageList.add(cursor.getString(0));
		}
		cursor.close();
		db.close();
		return lockPackageList;
	}
}
