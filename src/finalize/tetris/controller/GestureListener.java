package finalize.tetris.controller;

/**
 * @author junjie
 * @time 2014.10.01
 * @version 1.1
 * 监听滑动事件：
 * 当滑动时，系统调用OnTouchListener的onTouch方法
 * 在onTouch方法中，调用GestureDetector.onTouchEvent(event);
 * onTouchEventh方法会根据event来调用对应的方法如OnFiling
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * 监听屏幕滑动事件 SimpleOnGestureListener implements
 * GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener
 * */
public class GestureListener extends SimpleOnGestureListener implements
		OnTouchListener {
	/** 滑动的最短距离 */
	private int distance = 80;
	/** 滑动的最大速度 */
	private int velocity = 150;

	private GestureDetector gestureDetector;

	public GestureListener(Context context) {
		super();
		gestureDetector = new GestureDetector(context, this);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return gestureDetector.onTouchEvent(event);
	}

	/**
	 * @param e1
	 *            第1个ACTION_DOWN MotionEvent
	 * @param e2
	 *            最后一个ACTION_MOVE MotionEvent
	 * @param velocityX
	 *            X轴上的移动速度（像素/秒）
	 * @param velocityY
	 *            Y轴上的移动速度（像素/秒）
	 * */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub

		// 向左滑
		if (e1.getX() - e2.getX() > distance && Math.abs(velocityX) > velocity) {
			moveLeft();
			return true;
		}

		// 向右滑
		if (e2.getX() - e1.getX() > distance && Math.abs(velocityX) > velocity) {
			moveRight();
			return true;
		}

		// 向上滑
		if (e1.getY() - e2.getY() > distance && Math.abs(velocityX) > velocity) {
			moveUp();
			return true;
		}

		// 向下滑
		if (e2.getY() - e1.getY() > distance && Math.abs(velocityY) > velocity) {
			moveDown();
			return true;
		}

		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return onFling(e1, e2, distanceX, distanceY);
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean moveDown() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean moveUp() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean moveRight() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean moveLeft() {
		// TODO Auto-generated method stub
		return false;
	}
}
