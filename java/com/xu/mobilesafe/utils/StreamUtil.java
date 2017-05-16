package com.xu.mobilesafe.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {
	/**
	 * 流转换成字符串
	 * @param is	流对象
	 * @return		流转换成的字符串	返回null代表异常
	 */
	public static String streamToString(InputStream is) {
		//1,在读取的过程中,将读取的内容存储值缓存中,然后一次性的转换成字符串返回
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		//2,读流操作,读到没有为止(循环)，每次1024
		byte[] buffer = new byte[1024];
		//3,记录读取内容的临时变量
		int temp = -1;
		try {
			//调用read读取的方法，每次读1024字节，如果能读到就返回temp，如果temp不等于-1，就说明还有内容，等于-1就跳出循环
			while((temp = is.read(buffer))!=-1){
				//把读到的内容写到里面，最终存在了bos里面
				bos.write(buffer, 0, temp);
			}
			//返回读取数据
			return bos.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				//关闭各种流
				is.close();
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
