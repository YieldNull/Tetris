package finalize.tetris.view;

import java.util.Random;
import project.tetris.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * 绘制游戏界面 通过在指定坐标绘制emojis[block[i][j]]链接的emoji图像
 * block[][]=0则表示该位置没有图像，同样emojis[0]表示没有与之链接的bitmap
 * 
 * @author junjie
 * @time 2014.10.01
 * @version V1.1
 */
public class GameView extends View {

	/* 声明、初始化各项数据，及其坐标、画笔颜色、字体大小 */
	private float density;
	private int screenWidth;
	private int screenHeight;

	public Point nowCoord = new Point(3, 0); // 方块现在位置的笛卡尔坐标,初始化为游戏池上中部
	private final Point originNowCoord = new Point(3, 0);
	private final Point originPoolCoord = new Point(0, 0);
	private PointF originPoint = new PointF(84, 160); // 原点像素坐标
	private PointF followPoint = new PointF(394, 236); // 预显示区 follow像素坐标
	private PointF secondPoint = new PointF(394, 434); // 预显示区second像素坐标

	public int level = 1; // 游戏等级
	private int levelSize = 50;
	private final int levelARGB = Color.argb(255, 255, 255, 0);// 画等级
	private final PointF levelPoint = new PointF(430, 130);

	public int score = 0; // 分数
	private int scoreSize = levelSize;
	private final int scoreARGB = levelARGB; // 画分数
	private PointF scorePoint = new PointF(214, 130);

	public int time = 0; // 时间
	private int minuteSpan = 0; // 分
	private int secondSpan = 0; // 秒
	private int timeSize = 40;
	private final int timeARGB = Color.argb(255, 255, 185, 15); // 画分数
	private final PointF minPoint = new PointF(380, 670);
	private final PointF secPoint = new PointF(434, 670);

	private int blockCountSize = 40;
	private final int blockCountARGB = Color.argb(255, 110, 139, 61);
	private PointF[] blockCountPoint = new PointF[7]; // 画每个方块数量
	private int[] blockCount = new int[7]; // 各个方块出现在游戏池中的个数

	public int lineDelete = 0; // 消去的总行数
	private int lineSize = 40;
	private final int lineARGB = Color.argb(255, 238, 180, 34);
	private final PointF linePoint = new PointF(18, 734); // 画总消去行数

	private int sum = 0; // 消去的总方块数
	private int sumSize = 40;
	private final int sumARGB = Color.argb(255, 125, 38, 205);
	private final PointF sumPoint = new PointF(24, 240);// 总方块数

	public int[] speedArray = new int[9];// 速度

	/* 初始化游戏池、方块数组 */

	public int poolWidth = 9; // 像素大小
	public int poolHeight = 19;
	public int[][] pool;

	public int[][] now = new int[4][4];
	private int[][] follow = new int[4][4];
	private int[][] second = new int[4][4];
	private double blockSize = 31; // 方块像素大小

	// 方块形状。用于记录各个形状出现在游戏池中的次数
	private int secondIndex;
	private int followIndex;
	private int nowIndex;

	/* 链接emoji与bitmap */
	private Random random = new Random();// 随机数生成器

	// emoji图片的id,emojis[0]表示当前位置没有方块
	int[] emojisId = { 0, R.drawable.emoji1, R.drawable.emoji2,
			R.drawable.emoji3, R.drawable.emoji4, R.drawable.emoji5,
			R.drawable.emoji6, R.drawable.emoji7, R.drawable.emoji8,
			R.drawable.emoji9, R.drawable.emoji10, R.drawable.emoji11,
			R.drawable.emoji12, R.drawable.emoji13, R.drawable.emoji14,
			R.drawable.emoji15, R.drawable.emoji16, R.drawable.emoji17,
			R.drawable.emoji18, R.drawable.emoji19, R.drawable.emoji20,
			R.drawable.emoji21, R.drawable.emoji22, R.drawable.emoji23,
			R.drawable.emoji24, R.drawable.emoji25, R.drawable.emoji26,
			R.drawable.emoji27, R.drawable.emoji28, R.drawable.emoji29,
			R.drawable.emoji30 };

	private Resources res = getResources();// 获取资源文件
	private Bitmap[] bitmaps = new Bitmap[emojisId.length];// 存储emoji的Bitmap

	private Paint paint = new Paint();// 画笔
	private Canvas canvas;// 画布

	/**
	 * 构造函数，初始化数据
	 * */
	public GameView(Context context, DisplayMetrics displayMetrics) {
		super(context);
		// TODO Auto-generated constructor stub
		this.screenWidth = displayMetrics.widthPixels;
		this.screenHeight = displayMetrics.heightPixels;
		this.density = displayMetrics.density;

		initMembers();// 初始化各参数
	}

	@SuppressLint("DrawAllocation")
	@Override
	/**
	 * 循环调用，重绘
	 * */
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		// super.onDraw(canvas);
		// testPool(canvas);
		this.canvas = canvas;

		// 画方块
		paintByCoord(nowCoord, now);
		paintByPixel(followPoint, follow);
		paintByPixel(secondPoint, second);

		// 画游戏池
		paintByCoord(originPoolCoord, pool);

		drawScore();// 画分数
		drawTime(); // 画时间
	}

	/**
	 * 初始化各项数据
	 * */
	private void initMembers() {
		// TODO Auto-generated method stub

		suitScreen(); // 匹配不同屏幕，调整字体大小、像素坐标、游戏池大小等

		// 使用调整后的游戏池大小
		pool = new int[poolWidth][poolHeight];

		// 初始化速度(毫秒值),等级越高，下落速度越快
		for (int i = 0; i < 9; i++) {
			speedArray[i] = 800 - 80 * i;
		}

		// 初始化每个方块的像素坐标
		for (int k = 0; k < blockCountPoint.length; k++) {
			blockCountPoint[k] = new PointF(35, 290 + 58 * k);
			suitPoint(blockCountPoint[k]);
		}

		// Bitmap与emoji 图片链接
		bitmaps[0] = null;
		for (int i = 1; i < emojisId.length; i++) {
			bitmaps[i] = BitmapFactory.decodeResource(res, emojisId[i]);
		}
	}

	/**
	 * 初始化view 画初始的分数、等级等 预显示区显示方块
	 * */
	public void initView() {
		sum = 0;
		score = 0;
		lineDelete = 0;
		level = 1;
		time = 0;

		for (int i = 0; i < blockCount.length; i++) {
			blockCount[i] = 0;
		}

		for (int i = 0; i < 4; i++) {
			for (int k = 0; k < 4; k++) {
				now[i][k] = 0;
				second[i][k] = 0;
				follow[i][k] = 0;
			}
		}

		for (int i = 0; i < poolWidth; i++) {
			for (int k = 0; k < poolHeight; k++) {
				pool[i][k] = 0;
			}
		}

		// 在内存中获取方块
		secondIndex = getBlock(second);
		followIndex = getBlock(follow);

		nowIndex = getBlock(now);

		// 计数
		blockCount[nowIndex]++;
		sum++;
	}

	/**
	 * 方块落到底部之后，刷新view，更新游戏池与预显示区，更新分数等级
	 * */
	public void refreshView() {

		// 预显示区的方块进入游戏池
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				now[i][j] = follow[i][j];
			}
		}

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				follow[i][j] = second[i][j];
			}
		}

		// 同样要调整方块的形状
		nowIndex = followIndex;
		followIndex = secondIndex;

		// 在内存中生成新的方块
		secondIndex = getBlock(second);

		// 计数
		blockCount[nowIndex]++;
		sum++;

		// 初始化nowPoint
		nowCoord.x = originNowCoord.x;
		nowCoord.y = originNowCoord.y;
	}

	/**
	 * 在内存中得到方块，也就是为方块数组赋值，之后再paint到屏幕上
	 * 
	 * @param block
	 *            方块数组
	 * @return 得到的方块形状
	 */
	private int getBlock(int[][] block) {
		// 先清空数组
		for (int i = 0; i < 4; i++) {
			for (int k = 0; k < 4; k++) {
				block[i][k] = 0;
			}
		}

		// 随机选择将要出现的方块形状，以及组成它的emoji
		// 1<=emojiX<=emojisId.length-1
		int index = Math.abs(random.nextInt()) % 7;
		int emoji1 = Math.abs(random.nextInt()) % (emojisId.length - 1) + 1;
		int emoji2 = Math.abs(random.nextInt()) % (emojisId.length - 1) + 1;
		int emoji3 = Math.abs(random.nextInt()) % (emojisId.length - 1) + 1;
		int emoji4 = Math.abs(random.nextInt()) % (emojisId.length - 1) + 1;

		switch (index) {
		case 0: // I
			block[0][0] = emoji1;
			block[0][1] = emoji2;
			block[0][2] = emoji3;
			block[0][3] = emoji4;
			break;
		case 1: // T
			block[0][0] = emoji1;
			block[0][1] = emoji2;
			block[0][2] = emoji3;
			block[1][1] = emoji4;
			break;
		case 2: // L
			block[0][0] = emoji1;
			block[1][0] = emoji2;
			block[1][1] = emoji3;
			block[1][2] = emoji4;
			break;
		case 3: // J
			block[0][0] = emoji1;
			block[0][1] = emoji2;
			block[0][2] = emoji3;
			block[1][0] = emoji4;
			break;
		case 4: // Z
			block[0][1] = emoji1;
			block[0][2] = emoji2;
			block[1][0] = emoji3;
			block[1][1] = emoji4;
			break;
		case 5: // S
			block[0][0] = emoji1;
			block[0][1] = emoji2;
			block[1][1] = emoji3;
			block[1][2] = emoji4;
			break;
		case 6: // O
			block[0][0] = emoji1;
			block[0][1] = emoji2;
			block[1][0] = emoji3;
			block[1][1] = emoji4;
			break;
		default:
			break;
		}

		return index;// 得到的方块形状
	}

	/**
	 * 调用drawNumber画各项分数
	 * */
	private void drawScore() {
		// TODO Auto-generated method stub

		drawNumber(Integer.toString(level), levelPoint, levelSize, levelARGB);
		drawNumber(Integer.toString(score), scorePoint, scoreSize, scoreARGB);
		drawNumber(Integer.toString(lineDelete), linePoint, lineSize, lineARGB);
		drawNumber(Integer.toString(sum), sumPoint, sumSize, sumARGB);
		for (int i = 0; i < blockCount.length; i++) {
			drawNumber(Integer.toString(blockCount[i]), blockCountPoint[i],
					blockCountSize, blockCountARGB);
		}
	}

	/**
	 * 调用drawNumber画时间
	 */
	public void drawTime() {
		// TODO Auto-generated method stub
		minuteSpan = time / 60;
		secondSpan = time % 60;

		drawNumber(Integer.toString(minuteSpan), minPoint, timeSize, timeARGB);
		drawNumber(Integer.toString(secondSpan), secPoint, timeSize, timeARGB);
	}

	/**
	 * 根据坐标、ARGB、字体大小在相应地方画数字
	 * 
	 * @param text
	 *            要写的字
	 * @param point
	 *            坐标
	 * @param textSize
	 *            字体大小
	 * @param color
	 *            ARGB，透明度+颜色
	 */
	private void drawNumber(String text, PointF point, int textSize, int color) {
		// TODO Auto-generated method stub
		paint.setColor(color);
		paint.setTextSize(textSize);
		canvas.drawText(text, point.x, point.y, paint);
	}

	/**
	 * 按笛卡尔坐标画方块(调用paintByPixel)
	 * 
	 * @param point
	 *            坐标
	 * @param block
	 *            方块
	 */
	private void paintByCoord(Point point, int[][] block) {
		// TODO Auto-generated method stub
		int XCoord = point.x;
		int YCoord = point.y;

		// 转化为像素坐标
		PointF realPoint = new PointF();
		realPoint.x = (float) (XCoord * blockSize + originPoint.x);
		realPoint.y = (float) (YCoord * blockSize + originPoint.y);
		paintByPixel(realPoint, block);
	}

	/**
	 * 按像素坐标画方块
	 * 
	 * @param point
	 *            坐标
	 * @param block
	 *            方块
	 */
	private void paintByPixel(PointF point, int[][] block) {
		// TODO Auto-generated method stub
		float XPixel = point.x;
		float YPixel = point.y;

		float x, y;
		// 在指定点画出方块
		for (int i = 0; i < block.length; i++) {
			for (int j = 0; j < block[0].length; j++) {
				if (block[i][j] != 0) {
					x = (float) (XPixel + blockSize * i);
					y = (float) (YPixel + blockSize * j);
					canvas.drawBitmap(bitmaps[block[i][j]], x, y, paint);
				}
			}
		}
	}

	/**
	 * 为了适应不同屏幕，根据像素点在调试机器屏幕上的百分位置
	 * 调整坐标、字体、游戏池大小
	 */
	private void suitScreen() {
		// TODO Auto-generated method stub
		suitPool();

		suitPoint(originPoint);
		suitPoint(followPoint);
		suitPoint(secondPoint);
		suitPoint(levelPoint);
		suitPoint(scorePoint);
		suitPoint(minPoint);
		suitPoint(secPoint);
		suitPoint(linePoint);
		suitPoint(sumPoint);

		suitFont();

	}

	/**
	 * 调整游戏池大小
	 */
	private void suitPool() {
		// TODO Auto-generated method stub

		// 算出在任何屏幕上面的pool左右坐标
		double leftXPixel = originPoint.x;
		double rightXPixel = originPoint.x + poolWidth * blockSize;
		leftXPixel = leftXPixel / 480 * screenWidth;
		rightXPixel = rightXPixel / 480 * screenWidth;

		// 算出poolWidth
		blockSize = 31 * (density / 1.5);
		poolWidth = (int) ((rightXPixel - leftXPixel) / blockSize);

		// 算出poolHeight
		double topYPixel = originPoint.y;
		topYPixel = topYPixel / 800 * screenHeight;
		poolHeight = (int) ((screenHeight - topYPixel) / blockSize) - 1;
	}

	/**
	 * 调整字体
	 */
	private void suitFont() {
		// TODO Auto-generated method stub
		double rate = density / 1.5;

		sumSize *= rate;
		levelSize *= rate;
		lineSize *= rate;
		blockCountSize *= rate;
		timeSize *= rate;
		scoreSize *= rate;

	}

	/**
	 * 调整坐标
	 * 
	 * @param point
	 *            原始坐标
	 */
	private void suitPoint(PointF point) {
		// TODO Auto-generated method stub
		point.x = (float) (point.x / 480 * screenWidth);
		point.y = (float) (point.y / 800 * screenHeight);
	}
}
