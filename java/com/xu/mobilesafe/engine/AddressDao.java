package com.xu.mobilesafe.engine;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AddressDao {
	private static final String tag = "AddressDao";
	//1,指定访问数据库的路径
	public static String path = "data/data/com.xu.mobilesafe/files/address.db";
	private static String mAddress = "未知号码";
	/**传递一个电话号码,开启数据库连接,进行访问,返回一个归属地
	 * @param phone	查询电话号码
	 *
	 * 有返回值结果给其他类去使用
	 */
	public static String getAddress(String phone){
		mAddress = "未知号码";
		//正则表达式,匹配手机号码
		//手机号码的正则表达式
		String regularExpression = "^1[3-8]\\d{9}";
		//2,开启数据库连接(只读的形式打开)
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
		//如果传进来的号码没有七位要崩溃的，所以要判断一下，匹配正则表达式
		if(phone.matches(regularExpression)){
			phone = phone.substring(0,7);
			//3,数据库查询，参数：先查data1，查处outkey，然后在查data2表、参数1：数据表，参数2：字段，参数3，查询条件
			Cursor cursor = db.query("data1", new String[]{"outkey"}, "id = ?", new String[]{phone}, null, null, null);
			//4,查到即可，所以是if就可以了
			if(cursor.moveToNext()){
				//这个就是外键outkey
				String outkey = cursor.getString(0);
				Log.i(tag, "outkey = "+outkey);
				//5,通过data1查询到的结果,作为外键查询data2。参数1：数据表，参数2：字段，参数3，查询条件
				Cursor indexCursor = db.query("data2", new String[]{"location"}, "id = ?", new String[]{outkey}, null, null, null);
				//4,查到即可，所以是if就可以了
				if(indexCursor.moveToNext()){
					//6,获取查询到的电话归属地
					mAddress = indexCursor.getString(0);
					Log.i(tag, "address = "+mAddress);
				}
			}else{
				mAddress = "未知号码";
			}
			//如果不匹配正则表达式的话，还有其他情况
		}else{
			int length = phone.length();
			switch (length) {
			case 3://119 110 120 114
				mAddress = "报警电话";
				break;
			case 4://119 110 120 114
				mAddress = "模拟器";
				break;
			case 5://10086 99555
				mAddress = "服务电话";
				break;
			case 7:
				mAddress = "本地电话";
				break;
			case 8:
				mAddress = "本地电话";
				break;
			case 11:
				//(3+8) 区号+座机号码(外地),查询data2
				//把传进来的号码截取一下，第一位到第三位
				String area = phone.substring(1, 3);
				//5,通过data1查询到的结果,作为外键查询data2。参数1：数据表，参数2：字段，参数3，查询条件
				Cursor cursor = db.query("data2", new String[]{"location"}, "area = ?", new String[]{area}, null, null, null);
				if(cursor.moveToNext()){
					mAddress = cursor.getString(0);
				}else{
					mAddress = "未知号码";
				}
				break;
			case 12:
				//(4+8) 区号(0791(江西南昌))+座机号码(外地),查询data2
				//把传进来的号码截取一下，第一位到第四位
				String area1 = phone.substring(1, 4);
				//5,通过data1查询到的结果,作为外键查询data2。参数1：数据表，参数2：字段，参数3，查询条件
				Cursor cursor1 = db.query("data2", new String[]{"location"}, "area = ?", new String[]{area1}, null, null, null);
				if(cursor1.moveToNext()){
					mAddress = cursor1.getString(0);
				}else{
					mAddress = "未知号码";
				}
				break;
			}
		}
		//有返回值结果给其他类去使用
		return mAddress;
	}
}
