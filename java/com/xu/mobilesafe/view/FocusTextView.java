package com.xu.mobilesafe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 能够获取焦点的自定义TextView，来达到自己滚动起来的目的
 *
 * 之所以能够滚动，是因为它能获取焦点，所以重写TextView方法，一直可以获取焦点
 */
public class FocusTextView extends TextView {
	//使用在通过java代码创建控件
	public FocusTextView(Context context) {
		super(context);
	}
	
	//由系统调用(带属性+上下文环境构造方法)
	public FocusTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	//由系统调用(带属性+上下文环境构造方法+布局文件中定义样式文件构造方法)
	public FocusTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	//重写获取焦点的方法,由系统调用,调用的时候默认就能获取焦点
	@Override
	public boolean isFocused() {
		return true;
	}
}
