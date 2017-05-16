package com.xu.mobilesafe.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Md5Util {

	//这是java代码
//	public static void main(String[] args) {
//		//加盐
//		String psd = "123"+"abc";
//		encoder(psd);
//	}

	/**
	 * 给指定字符串按照md5算法去加密
	 * @param psd	需要加密的密码	加盐处理
	 * @return		md5后的字符串
	 */

	/**给指定字符串按照md5算法去加密
	 * @param psd	需要加密的密码
	 */

	//返回的类型是字符串
	public static String encoder(String psd) {
		try {
			//在这里先做加盐处理，然后在赋给值
			//加盐处理
			psd = psd+"mobilesafe";
			//1,指定加密算法类型（因为算法有很多）
			//单例模式，拿对象（类名）调用静态方法获取对象，一定是一个单例模式getInstance
			MessageDigest digest = MessageDigest.getInstance("MD5");
			//2,将需要加密的字符串中转换成byte类型的数组,然后进行随机哈希过程
			byte[] bs = digest.digest(psd.getBytes());
//			System.out.println(bs.length);

			//现在是16位的，但是加密之后要变成32位，是2倍的关系。数组中每一个元素转成长度为2的字符串，就能构成32位字符串了
			//3,循环遍历bs,然后让其生成32位字符串,固定写法
			//4,拼接字符串过程
			StringBuffer stringBuffer = new StringBuffer();
			//加强for循环，拿到bs中每一个索引位置上的byte，
			for (byte b : bs) {
				//然后给byte做与的操作，八位与八位，就是16位，然后赋给i，四个字节32位
				int i = b & 0xff;
				//将i转换成2个字符串，循环一次就2个字符串，循环16次，就是32位了
				//int类型的i需要转换成16机制字符
				String hexString = Integer.toHexString(i);
//				System.out.println(hexString);
				//转换之后的字符串，因为有的一组是1位数，有的2位数，1位的话需要补0的操作
				if(hexString.length()<2){
					hexString = "0"+hexString;
				}
				//这里就是拼接字符串的过程
				stringBuffer.append(hexString);
			}
			//5,打印测试
			System.out.println(stringBuffer.toString());

			//返回字符串
			return stringBuffer.toString();
			//抛出没有这个算法的异常
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		//出现异常的就返回空字符串
		return null;
	}
}
