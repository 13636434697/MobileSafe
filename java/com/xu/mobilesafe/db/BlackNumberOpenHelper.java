package com.xu.mobilesafe.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BlackNumberOpenHelper extends SQLiteOpenHelper {
	public BlackNumberOpenHelper(Context context) {
		super(context, "blacknumber.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//创建数据库中表的方法（语法需要在sqlite expert professional软件里面坚持下）
		db.execSQL("create table blacknumber " +
				"(_id integer primary key autoincrement , phone varchar(20), mode varchar(5));");
	}

	//更新数据库的话就调用这个方法，新版本号一定要高
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
