package finalize.tetris.controller;

import java.util.Timer;
import java.util.TimerTask;

import finalize.tetris.view.GameView;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

/**
 * 游戏控制器
 * 
 * @author junjie
 * 
 */
public class GameController {

	public GameView gameView;// 控制界面绘制
	private MusicController gameMusic;// 控制音乐播放

	private Timer timer = new Timer();// 控制记录游戏时间
	private TimerTask refreshTask;// 控制刷新界面的任务
	private TimerTask timeTask;// 控制刷新游戏时间的任务

	private Handler handler;// 控制在非UI线程中弹出对话框
	private Vibrator vibrator;// 控制振动器

	private Context context;// 记录activity的context

	public boolean isPause = false;// 游戏是否暂停
	private long speed;// 游戏的速度

	// 手指触碰屏幕的方向
	private enum TouchDirection {
		UP, DOWN, RIGHT, LEFT, CHANGE, PRESENT
	};

	/**
	 * 构造函数，初始化各对象
	 * 
	 * @param context
	 *            由activity传来
	 * @param density
	 *            用于获取屏幕大小等数据
	 */
	@SuppressLint({ "ClickableViewAccessibility", "HandlerLeak" })
	public GameController(Context context, DisplayMetrics displayMetrics) {
		this.context = context;
		gameMusic = new MusicController(context);

		gameView = new GameView(context, displayMetrics);
		gameView.setLongClickable(true);
		gameView.setOnTouchListener(new GestureController(context));

		// 初始化速度
		speed = gameView.speedArray[gameView.level];

		refreshTask = new RefreshTaskCotroller();
		timeTask = new TimeTaskCotroller();

		vibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);

		// 当接收到非UI线程传过来的消息时，在UI线程弹出失败对话框
		handler = new Handler() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				// super.handleMessage(msg);
				GameController.this.showFailedDialog();
			}

		};
	}

	/**
	 * 开始游戏 开启计时器，音乐，绘图
	 */
	public void startGame() {
		// TODO Auto-generated method stub
		gameView.initView();
		gameMusic.playBackMusic();
		timer.schedule(refreshTask, speed, speed);
		timer.schedule(timeTask, 1000, 1000);
	}

	/**
	 * 暂停游戏
	 * 
	 * @param flag
	 *            flag为真则弹出暂停对话框
	 */
	public void pauseGame(boolean flag) {
		// TODO Auto-generated method stub
		isPause = !isPause;
		if (isPause) {// 暂停
			gameMusic.pauseBackMusic();// 暂停背景音乐

			refreshTask.cancel();// 取消任务
			timeTask.cancel();
		} else {// 恢复
			gameMusic.restartBackMusic();// 恢复背景音乐

			// 新建同样的任务
			refreshTask = new RefreshTaskCotroller();
			timer.schedule(refreshTask, speed, speed);

			timeTask = new TimeTaskCotroller();
			timer.schedule(timeTask, 1000, 1000);
		}

		// 是否弹出暂停对话框
		if (flag == true) {
			showPauseDialog();
		}
	}

	/**
	 * 停止游戏
	 */
	public void stopGame() {
		// TODO Auto-generated method stub
		gameMusic.stopBackMusic(); // 关闭音乐
		timer.cancel();// 取消计时器
	}

	/**
	 * 游戏结束时开始新游戏 初始化各项数据
	 */
	public void newGame() {
		// TODO Auto-generated method stub
		isPause = false;

		gameView.initView();
		gameMusic.newBackMusic();

		// 删除任务
		timeTask.cancel();
		refreshTask.cancel();

		// 新建同样的任务
		timeTask = new TimeTaskCotroller();
		refreshTask = new RefreshTaskCotroller();

		timer.schedule(refreshTask, speed, speed);
		timer.schedule(timeTask, 1000, 1000);

		// 重绘
		gameView.invalidate();
	}

	/**
	 * 右移
	 */
	public void moveRight() {
		if (canMove(TouchDirection.RIGHT)) {
			gameView.nowCoord.x++;
			gameView.postInvalidate();// 重绘
		}
	}

	/**
	 * 左移
	 */
	public void moveLeft() {
		if (canMove(TouchDirection.LEFT)) {
			gameView.nowCoord.x--;
			gameView.postInvalidate();
		}
	}

	/**
	 * 下落 不能下落则将方块存进游戏池， 判断是否能消行，是否增加游戏等级
	 */
	public void moveDown() {
		// TODO Auto-generated method stub
		if (canMove(TouchDirection.DOWN)) {
			gameView.nowCoord.y++;
		} else {
			storeInPool();// 存进游戏池
			int lineDeleted = deleteLine();// 消行

			// 更新分数、消行数
			gameView.lineDelete += lineDeleted;
			gameView.score += lineDeleted * gameView.level;

			// 增加等级，更改下落速度
			if (gameView.lineDelete >= gameView.level * 8) {
				gameView.level++;
				speed = gameView.speedArray[gameView.level];

				refreshTask.cancel();
				refreshTask = new RefreshTaskCotroller();
				timer.schedule(refreshTask, speed, speed);
			}

			// 消行则调用振动器，播放消行音乐，否则播放下落音乐
			if (lineDeleted != 0) {
				vibrator.vibrate(50);
				gameMusic.playDeleteSound();

			} else
				gameMusic.playBottomSound();

			gameView.refreshView();// 刷新界面

			// 要是刷新后不能再下落则表示游戏结束，暂停游戏，发送消息，弹出失败对话框
			if (!canMove(TouchDirection.PRESENT)) {
				pauseGame(false);
				handler.sendEmptyMessage(0);
				// showFailedDialog();
			}
		}
	}

	/**
	 * 加速下落
	 */
	public void moveBottom() {
		moveDown();
		gameView.postInvalidate();
		moveDown();
		gameView.postInvalidate();
	}

	/**
	 * 旋转，能旋转则播放旋转音乐
	 */
	public void change() {
		// TODO Auto-generated method stub

		/* 先旋转 */
		int[][] arrAftChange = new int[4][4]; // 存储旋转后数组
		int tpCol = 0;
		int tpRow = 0;

		for (int row = 0; row < 4; row++) { // 从上到下遍历所有行
			// 从右到左遍历行中元素,从上到下放入目标数组每行中
			for (int column = 3; column >= 0; column--) {
				arrAftChange[tpRow][tpCol] = gameView.now[row][column];
				tpRow++;
			}
			tpCol++;
			tpRow = 0;
		}

		// 去掉旋转后方块上方的空白，得到新数组,保证nowPosition不变

		// 找出非空白行
		int newBegainRow = 0;// 去除空白行后的新顶行
		boolean flg = false;
		for (int row = 0; row < 4; row++) {
			for (int column = 0; column < 4; column++) {
				if (arrAftChange[row][column] > 0) {
					newBegainRow = row;
					flg = true;
					break;
				}
			}
			if (flg)
				break;
		}

		// 获得去掉空白的新数组
		int[][] arrWithoutBlank = new int[4][4];
		for (int row = 0; row < 4; row++) {
			for (int column = 0; column < 4; column++) {
				arrWithoutBlank[row][column] = newBegainRow < 4 ? arrAftChange[newBegainRow][column]
						: 0; // 从非空白行开始复制
			}
			newBegainRow++;
		}

		int[][] nowPtr = gameView.now;// 临时储存now的引用
		gameView.now = arrWithoutBlank;// now指向arrWithoutBlank，用于判断能否旋转

		// 判断是否能旋转
		if (!canMove(TouchDirection.CHANGE)) {
			gameView.now = nowPtr;
			return;
		}

		gameView.now = nowPtr;
		for (int row = 0; row < 4; row++) { // 能转则更新now
			for (int column = 0; column < 4; column++) {
				gameView.now[row][column] = arrWithoutBlank[row][column]; // 从非空白行开始复制
			}
			newBegainRow++;
		}

		gameMusic.playChangeSound();
	}

	/**
	 * 消行
	 * 
	 * @return 消行数
	 */
	private int deleteLine() {
		// TODO Auto-generated method stub
		int lineDeleted = 0;

		boolean[] canDelete = new boolean[gameView.poolHeight]; // 记录可消行

		// 找出可消行
		for (int YCoord = gameView.poolHeight - 1; YCoord >= 0; YCoord--) {
			boolean flg = true; // 计数器
			for (int XCoord = 0; XCoord < gameView.poolWidth; XCoord++) {
				if (gameView.pool[XCoord][YCoord] == 0) {
					flg = false;
					break;
				}
			}
			if (flg == true) {
				canDelete[YCoord] = true;
				lineDeleted++;
			}
		}

		// 复制pool,删除可消行，pool清零
		int[][] copyPool = new int[gameView.poolWidth][gameView.poolHeight];
		for (int YCoord = gameView.poolHeight - 1; YCoord >= 0; YCoord--) {
			for (int XCoord = 0; XCoord < gameView.poolWidth; XCoord++) {
				if (canDelete[YCoord] == true) {
					gameView.pool[XCoord][YCoord] = 0; // 删行
				}
				copyPool[XCoord][YCoord] = gameView.pool[XCoord][YCoord]; // 复制
				gameView.pool[XCoord][YCoord] = 0; // 清零
			}
		}

		// 遍历所有行，将副本复制给pool，只复制非空行
		int YIndex = gameView.poolHeight - 1;
		for (int YCoord = gameView.poolHeight - 1; YCoord >= 0; YCoord--) {
			if (canDelete[YCoord] == false) { // 非空
				for (int XCoord = 0; XCoord < gameView.poolWidth; XCoord++) {
					gameView.pool[XCoord][YIndex] = copyPool[XCoord][YCoord];
				}
				YIndex--;
			}
		}
		return lineDeleted;
	}

	/**
	 * 方块到底之后将方块存进游戏池
	 */
	private void storeInPool() {
		// TODO Auto-generated method stub
		int XNow = gameView.nowCoord.x;
		int YNow = gameView.nowCoord.y;

		for (int XCoord = 0; XCoord < 4; XCoord++) {
			for (int YCoord = 0; YCoord < 4; YCoord++) {
				if (gameView.now[XCoord][YCoord] > 0)
					gameView.pool[XNow + XCoord][YNow + YCoord] = gameView.now[XCoord][YCoord];
			}
		}
	}

	/**
	 * 判断能否向某个方向移动
	 * 
	 * @param direction
	 *            移动的方向
	 * @return 能否移动
	 */
	private boolean canMove(TouchDirection direction) {
		// TODO Auto-generated method stub
		int XNow = gameView.nowCoord.x;
		int YNow = gameView.nowCoord.y;

		// 根据移动方向判断坐标增加值
		int XPlus = 0;
		int YPlus = 0;

		switch (direction) {
		case LEFT:
			XPlus = -1;
			break;
		case RIGHT:
			XPlus = 1;
			break;
		case DOWN:
			YPlus = 1;
			break;
		default:
			break;
		}

		// 判断是否相遇
		for (int XCoord = 0; XCoord < 4; XCoord++) {
			for (int YCoord = 0; YCoord < 4; YCoord++) {
				int XNext = XCoord + XNow + XPlus;
				int YNext = YCoord + YNow + YPlus;
				if (gameView.now[XCoord][YCoord] != 0) {
					if ((XNext > gameView.poolWidth - 1) || (XNext < 0)) // 遇到左右边界
						return false;
					if (YNext > gameView.poolHeight - 1 || YNext < 0) // 遇到上下边界
						return false;
					// 遇到其他方块，在后面判断，要是在左边界，XNext=-1,会角标越界，因此要先对边界进行判断
					if (gameView.pool[XNext][YNext] > 0)
						return false;
				}
			}
		}
		return true;
	}

	/**
	 * 弹出暂停对话框
	 */
	public void showPauseDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder quitDialog = new AlertDialog.Builder(context)
				.setTitle("Are you sure to qiut?");

		quitDialog.setCancelable(false);
		quitDialog.setPositiveButton("Quit",
				new DialogInterface.OnClickListener() {

					// 退出则结束游戏
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						stopGame();
						System.exit(0);
					}
				});
		quitDialog.setNegativeButton("Return",
				new DialogInterface.OnClickListener() {

					// 返回则继续游戏
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						pauseGame(false);
					}
				});
		quitDialog.show();
	}

	/**
	 * 弹出失败对话框
	 */
	private void showFailedDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder quitDialog = new AlertDialog.Builder(context)
				.setTitle("You are failed, try again?");

		quitDialog.setCancelable(false);
		quitDialog.setPositiveButton("Quit",
				new DialogInterface.OnClickListener() {

					// 退出则结束游戏
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						stopGame();
						System.exit(0);
					}
				});
		quitDialog.setNegativeButton("Again",
				new DialogInterface.OnClickListener() {

					// 再来则开始新游戏
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						GameController.this.newGame();
					}
				});
		quitDialog.show();
	}

	/**
	 * 刷新界面任务，即隔一段时间下落一次
	 * 
	 * @author junjie
	 * 
	 */
	public class RefreshTaskCotroller extends TimerTask {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			moveDown();
			gameView.postInvalidate();
		}
	}

	/**
	 * 计时任务，即隔一段时间计时变量自增
	 * 
	 * @author junjie
	 * 
	 */
	public class TimeTaskCotroller extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			gameView.time++;
			gameView.drawTime();
			gameView.postInvalidate();
		}
	}

	/**
	 * 响应触控屏幕
	 * 
	 * @author junjie
	 * 
	 */
	private class GestureController extends GestureListener {

		public GestureController(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean moveUp() {
			// TODO Auto-generated method stub
			GameController.this.pauseGame(true);
			return true;
		}

		@Override
		public boolean moveDown() {
			// TODO Auto-generated method stub
			if (!isPause)
				GameController.this.moveBottom();
			return true;
		}

		@Override
		public boolean moveRight() {
			// TODO Auto-generated method stub
			if (!isPause)
				GameController.this.moveRight();
			return true;
		}

		@Override
		public boolean moveLeft() {
			// TODO Auto-generated method stub
			if (!isPause)
				GameController.this.moveLeft();
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			GameController.this.change();
			return true;
		}
	}

}
