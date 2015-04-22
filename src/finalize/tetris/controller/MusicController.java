package finalize.tetris.controller;

import java.util.HashMap;

import project.tetris.R;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

/**
 * 控制音乐播放
 * 
 * @author junjie
 * 
 */
public class MusicController {
	private MediaPlayer player;// 播放背景音乐
	private SoundPool soundPool;// 播放音效
	private HashMap<String, Integer> soundPoolMap;// 存储各个音效

	// 获取资源
	private Context context;
	private int backMusicId = R.raw.music;
	private int deleteLineId = R.raw.delete;
	private int changeId = R.raw.change;
	private int bottomId = R.raw.bottom;

	/**
	 * @param context
	 */
	public MusicController(Context context) {
		this.context = context;
		initSound();
	}

	/**
	 * 初始化播放器
	 */
	private void initSound() {
		// TODO Auto-generated method stub

		player = MediaPlayer.create(context, backMusicId);
		player.setLooping(true);

		soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);

		soundPoolMap = new HashMap<String, Integer>();
		soundPoolMap.put("change", soundPool.load(context, changeId, 1));
		soundPoolMap
				.put("deleteLine", soundPool.load(context, deleteLineId, 1));
		soundPoolMap.put("bottom", soundPool.load(context, bottomId, 1));
	}

	/**
	 * 播放背景音乐
	 */
	public void playBackMusic() {
		// TODO Auto-generated method stub
		player.start();
	}

	/**
	 * 暂停背景音乐播放
	 */
	public void pauseBackMusic() {
		// TODO Auto-generated method stub
		player.pause();
	}

	/**
	 * 停止背景音乐播放
	 */
	public void stopBackMusic() {
		// TODO Auto-generated method stub
		player.stop();
		player.release();
	}

	/**
	 * 恢复背景音乐播放
	 */
	public void restartBackMusic() {
		// TODO Auto-generated method stub
		player.start();
	}

	/**
	 * 结束当前的播放，重新开启背景音乐
	 */
	public void newBackMusic() {
		// TODO Auto-generated method stub
		stopBackMusic();
		player = MediaPlayer.create(context, backMusicId);
		player.setLooping(true);
		playBackMusic();
	}

	/**
	 * 播放消行音效
	 */
	public void playDeleteSound() {
		// TODO Auto-generated method stub
		soundPool.play(soundPoolMap.get("deleteLine"), 1, 1, 0, 0, 1);
	}

	/**
	 * 播放旋转音效
	 */
	public void playChangeSound() {
		// TODO Auto-generated method stub
		soundPool.play(soundPoolMap.get("change"), 1, 1, 0, 0, 1);
	}

	/**
	 * 播放落到底部音效
	 */
	public void playBottomSound() {
		// TODO Auto-generated method stub
		soundPool.play(soundPoolMap.get("bottom"), 1, 1, 0, 0, 1);
	}

}
