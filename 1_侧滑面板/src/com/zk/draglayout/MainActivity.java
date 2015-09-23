package com.zk.draglayout;

import java.util.Random;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.zk.draglayout.view.DragLayout;
import com.zk.draglayout.view.DragLayout.OnDragUpdateListener;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private ListView lv_left;
	private ListView lv_main;
	private DragLayout dl;
	private ImageView iv_header;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		dl = (DragLayout) findViewById(R.id.dl);
		iv_header = (ImageView) findViewById(R.id.iv_header);
		iv_header.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dl.open(true);
			}
		});
		//设置拖拽监听
		dl.setOnDragUpdateListener(new OnDragUpdateListener(){

			@Override
			public void onOpen() {
				Random random = new Random();
				lv_left.smoothScrollToPosition(random.nextInt(50));
			}

			@Override
			public void onClose() {
				//当关闭时,小头像执行左右晃动的动画
				ObjectAnimator animator = ObjectAnimator.ofFloat(iv_header, "translationX", 15f);
				animator.setInterpolator(new CycleInterpolator(4));
				animator.setDuration(500);
				animator.start();
			}

			@Override
			public void onDraging(float percent) {
				// 拖拽过程中不断修改小头像的透明度
				ViewHelper.setAlpha(iv_header, 1-percent);
			}
			
		});
		
		lv_left = (ListView) findViewById(R.id.lv_left);
		lv_left.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Cheeses.sCheeseStrings){
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				View view = super.getView(position, convertView, parent);
				((TextView)view).setTextColor(Color.WHITE);
				return view;
			}
		});
		
		lv_main = (ListView) findViewById(R.id.lv_main);
		lv_main.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, Cheeses.NAMES));
	}


}
