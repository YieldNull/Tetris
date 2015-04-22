package finalize.tetris.activity;

import finalize.tetris.controller.GameController;
import project.tetris.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;

/**
 * 主游戏界面
 * 
 * @author junjie
 * 
 */
public class MainActivity extends Activity {

	private GameController gameController;// 游戏控制者

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);// 删除title bar
		getWindow().setBackgroundDrawableResource(R.drawable.pool); // 设置背景图片

		// 用于获取屏幕大小等值
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		gameController = new GameController(this, displayMetrics);
		setContentView(gameController.gameView);// 设置view
	}

	/**
	 * 展示游戏规则
	 */
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		showRuleDialog();
	}

	/**
	 * 当界面不可见时，退出游戏
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		System.exit(0);
		super.onPause();
	}

	/**
	 * 
	 */
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		gameController.stopGame();
		super.onStop();
	}

	/**
	 * 按返回键时，暂停游戏，弹出退出对话框
	 */
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		gameController.pauseGame(false);
		gameController.showPauseDialog();
	}

	/**
	 * 规则对话框
	 */
	private void showRuleDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);
		quitDialog.setTitle("~Help~");
		quitDialog.setMessage("Slide ← → to move\nSlide ↓ to accelerate\n"
				+ "Slide ↑ to pause\nTap screen to rotate");
		quitDialog.setCancelable(false);
		quitDialog.setNegativeButton("Got it!",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						gameController.startGame();
					}
				});
		quitDialog.show();
	}
}
