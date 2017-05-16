package com.xu.mobilesafe.test;

import java.util.List;
import java.util.Random;


import android.test.AndroidTestCase;

import com.xu.mobilesafe.db.dao.BlackNumberDao;
import com.xu.mobilesafe.db.domain.BlackNumberInfo;

/*
* junit测试类
* */
public class Test extends AndroidTestCase {
	public void insert(){
		BlackNumberDao dao = BlackNumberDao.getInstance(getContext());
		for(int i=0;i<100;i++){
			if(i<10){
				//插入电话号码和拦截模式
				dao.insert("1860000000"+i, 1+new Random().nextInt(3)+"");
			}else{
				dao.insert("186000000"+i, 1+new Random().nextInt(3)+"");
			}
		}
	}
	
	public void delete(){
		BlackNumberDao dao = BlackNumberDao.getInstance(getContext());
		dao.delete("110");
	}
	
	public void update(){
		BlackNumberDao dao = BlackNumberDao.getInstance(getContext());
		dao.update("110", "2");
	}
	
	public void findAll(){
		BlackNumberDao dao = BlackNumberDao.getInstance(getContext());
		List<BlackNumberInfo> blackNumberInfoList = dao.findAll();
	}
}
