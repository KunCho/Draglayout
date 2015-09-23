package com.zk.draglayout.view;

import com.nineoldandroids.view.ViewHelper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class DragLayout extends FrameLayout {
	private ViewDragHelper dragHelper;
	private int mHeight;
	private int mWidth;
	private int mRange;
	private View mLeftContent;//左面板
	private View mMainContent;//主面板
	private Status status = Status.Close;//默认为close状态
	private OnDragUpdateListener onDragUpdateListener;
	
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	//定义一个三种状态
	public static enum Status{
		Close,Draging,Open
	}
	
	//定义接口
	public interface OnDragUpdateListener{
		void onOpen();
		void onClose();
		void onDraging(float percent);
	}
	
	public OnDragUpdateListener getOnDragUpdateListener() {
		return onDragUpdateListener;
	}
	public void setOnDragUpdateListener(OnDragUpdateListener onDragUpdateListener){
		this.onDragUpdateListener = onDragUpdateListener;
	}
	public DragLayout(Context context) {
		this(context,null);
	}
	public DragLayout(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}
	public DragLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//float sensitivity 敏感度
		dragHelper = ViewDragHelper.create(this, 1.0f, callback);
	}
	//重写回调监听
	ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
		
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			//返回值决定child是否可拖拽.
			return true;
		}
		
		//1.获取视图水平方向拖拽范围
		@Override
		public int getViewHorizontalDragRange(View child) {
			
			return mRange;
		}
		//2.修正视图水平方向位置,返回值决定了将要移动的水平位置
		@Override
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			if (child == mMainContent) {
				//拖拽的主面板
				left = fixLeft(left);
			}
			return left;
		}
		//3.当视图位置变化时调用,处理伴随动画,更新状态,执行监听
		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
//			super.onViewPositionChanged(changedView, left, top, dx, dy);
			if (changedView == mLeftContent) {
				//让左面板位置不动
				mLeftContent.layout(0, 0, mWidth, mHeight);
				//移动主面板
				int newLeft = mMainContent.getLeft();
				//限定范围
				newLeft = fixLeft(newLeft);
				mMainContent.layout(newLeft, 0, newLeft+mWidth, 0+mHeight);
				
			}
			dispathDragEvent();
			invalidate();//为了兼容低版本,手动刷新
		}
		//4.当视图被释放时调用
		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
//			super.onViewReleased(releasedChild, xvel, yvel);
			//判断所有打开的情况
			if (xvel == 0 && mMainContent.getLeft()> mRange*0.5f) {
				open();
			} else if (xvel>0) {
				open();
			} else {
				close();
			}
		}
	};
	private int fixLeft(int left) {
		if (left<0) {
			return 0;
		} else if (left>mRange) {
			return mRange;
		} 
		return left;
	}
	//执行伴随动画,更新状态,执行监听
	protected void dispathDragEvent() {
		//时间轴
		float percent = mMainContent.getLeft()*1.0f/mRange;
//		Log.i("LOG", "percent:"+percent);
		
		//左面板:缩放动画,平移动画,透明度
		animViews(percent);
		//执行监听
		if (onDragUpdateListener != null) {
			onDragUpdateListener.onDraging(percent);
		}
		//记录上一次的状态
		Status laststatus = status;
		//获取最新的状态
		status = updateStatus(percent);
		//状态发生变化的时候执行监听,并且进行状态的对比
		if (laststatus != status) {
			if (status == Status.Open) {
				onDragUpdateListener.onOpen();
			} else if (status == Status.Close) {
				onDragUpdateListener.onClose();
			}
		}
		
	}
	//获取最新状态
	private Status updateStatus(float percent) {
		if (percent == 0) {
			return Status.Close;
		} else if (percent == 1.0f) {
			return Status.Open;
		}
		return Status.Draging;
	}
	private void animViews(float percent) {
		//左面板:缩放动画,平移动画,透明度
//		mLeftContent.setScaleX(percent*0.5f+0.5f);
//		mLeftContent.setScaleY(percent*0.5f+0.5f);
		
		ViewHelper.setScaleX(mLeftContent, evaluate(percent, 0.5f, 1.0f));
		ViewHelper.setScaleY(mLeftContent, evaluate(percent, 0.5f, 1.0f));
		//平移动画
		ViewHelper.setTranslationX(mLeftContent, evaluate(percent, -mWidth*0.5f, 0));
		//透明度
		ViewHelper.setAlpha(mLeftContent, evaluate(percent, 0.2f, 1.0f));
		//主面板缩放动画
		ViewHelper.setScaleX(mMainContent, evaluate(percent, 1.0f, 0.9f));
		ViewHelper.setScaleY(mMainContent, evaluate(percent, 1.0f, 0.9f));
		//背景变化  黑色--->透明色
		getBackground().setColorFilter((Integer)evaluateColor(percent, Color.BLACK, Color.TRANSPARENT), Mode.SRC_OVER);
	}
	/**
	 * 颜色过渡器
	 * @param fraction
	 * @param startValue
	 * @param endValue
	 * @return
	 */
	 public Object evaluateColor(float fraction, Object startValue, Object endValue) {
	        int startInt = (Integer) startValue;
	        int startA = (startInt >> 24) & 0xff;
	        int startR = (startInt >> 16) & 0xff;
	        int startG = (startInt >> 8) & 0xff;
	        int startB = startInt & 0xff;

	        int endInt = (Integer) endValue;
	        int endA = (endInt >> 24) & 0xff;
	        int endR = (endInt >> 16) & 0xff;
	        int endG = (endInt >> 8) & 0xff;
	        int endB = endInt & 0xff;

	        return (int)((startA + (int)(fraction * (endA - startA))) << 24) |
	                (int)((startR + (int)(fraction * (endR - startR))) << 16) |
	                (int)((startG + (int)(fraction * (endG - startG))) << 8) |
	                (int)((startB + (int)(fraction * (endB - startB))));
	    }
	/**
	 * TypeEvaluator 类型估值器
	 * @param fraction
	 * @param startValue
	 * @param endValue
	 * @return
	 */
	public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }
	protected void close() {
		close(true);
	}
	private void close(boolean isSmooth) {
		int finalLeft = 0;
		if (isSmooth) {
			if (dragHelper.smoothSlideViewTo(mMainContent, finalLeft, 0)) {
				ViewCompat.postInvalidateOnAnimation(this);
			}
		} else {
			mMainContent.layout(finalLeft, 0, finalLeft+mWidth, 0+mHeight);
		}
	}
	protected void open() {
		open(true);
		
	}
	public void open(boolean isSmooth) {
		int finalLeft = mRange;
		if (isSmooth) {
			//触发一个平滑动画
			if (dragHelper.smoothSlideViewTo(mMainContent, finalLeft, 0)) {
				ViewCompat.postInvalidateOnAnimation(this);
			}
		} else {
			mMainContent.layout(finalLeft, 0, finalLeft+mWidth, 0+mHeight);
		}
	}
	//维持平滑动画的继续
	@Override
	public void computeScroll() {
		super.computeScroll();
		if (dragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}
	//拦截事件判断,触摸事件
	public boolean onInterceptTouchEvent(android.view.MotionEvent ev) {
		return dragHelper.shouldInterceptTouchEvent(ev);
	};
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		try {
			dragHelper.processTouchEvent(event);
		} catch (Exception e) {
		}
		return true;//当前ViewGroup可以处理event事件
	};
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mLeftContent = getChildAt(0);
		mMainContent = getChildAt(1);
	}
	//获取当前控件的宽高
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mHeight = getMeasuredHeight();
		mWidth = getMeasuredWidth();
		mRange = (int) (mWidth*0.6f);
	}

	
	

}
