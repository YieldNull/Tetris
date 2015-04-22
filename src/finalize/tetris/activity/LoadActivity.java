package finalize.tetris.activity;

import finalize.tetris.view.LoadView;
import project.tetris.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * @author junjie 游戏启动界面
 */
public class LoadActivity extends Activity {

	// 启动图像的显示时间
	private final int LOAD_DISPLAY_TIME = 2000;
	private LoadView loadView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// 加载view
		loadView = new LoadView(this);
		getWindow().setBackgroundDrawableResource(R.drawable.load); // 设置背景图片
		setContentView(loadView);

		// 设置LOAD_DISPLAY_TIME后调用主游戏界面
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent mainIntent = new Intent(LoadActivity.this,
						MainActivity.class);
				LoadActivity.this.startActivity(mainIntent);
				LoadActivity.this.finish();
			}
		}, LOAD_DISPLAY_TIME);
	}

}
