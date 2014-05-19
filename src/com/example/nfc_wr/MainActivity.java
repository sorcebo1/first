package com.example.nfc_wr;

import java.io.IOException;
import java.util.ArrayList;

import com.example.nfc_wr.update.UpdateManager;

import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author 瑞泽_Zerui 感应NFC标签后弹出的页面
 *         注:谷歌推荐的数据格式为NDEF,所以本次DEMO的读写都是采用该格式.当然了,你还可以尝试其它格式的开发.
 */
public class MainActivity extends Activity implements CreateNdefMessageCallback {
	private TextView ifo_NFC;
	private ViewPager viewPager;
	private NfcAdapter nfcAdapter;
	private String readResult = "";
	private PendingIntent pendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;
	private boolean isFirst = true;
	private Button toWBtn;
	private IntentFilter ndef;
	private MCBlock[] mcblock = new MCBlock[3];

	// 定义ViewPager适配器
	private ViewPagerAdapter vpAdapter;
	// 定义一个ArrayList来存放View
	private ArrayList<View> views;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 该方法完成接收到Intent时的初始化工作
		init();
		initData();
	}

	/**
	 * 检测工作,判断设备的NFC支持情况
	 * 
	 * @return
	 */
	private Boolean ifNFCUse() {
		// TODO Auto-generated method stub
		if (nfcAdapter == null) {
			ifo_NFC.setText("设备不支持NFC！");
			finish();
			return false;
		}
		if (nfcAdapter != null && !nfcAdapter.isEnabled()) {
			ifo_NFC.setText("请在系统设置中先启用NFC功能！");
			finish();
			return false;
		}
		return true;
	}

	/**
	 * 初始化过程
	 */
	private void init() {
		// TODO Auto-generated method stub
		toWBtn = (Button) findViewById(R.id.toWBtn);

		// 实例化ArrayList对象
		views = new ArrayList<View>();
		// 实例化ViewPager适配器
		vpAdapter = new ViewPagerAdapter(views);
		// 实例化ViewPager
		viewPager = (ViewPager) findViewById(R.id.viewpager);

		toWBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, Write2Nfc.class);
				startActivity(intent);
			}
		});
		ifo_NFC = (TextView) findViewById(R.id.ifo_NFC);
		// NFC适配器，所有的关于NFC的操作从该适配器进行
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (!ifNFCUse()) {
			return;
		}
		// 将被调用的Intent，用于重复被Intent触发后将要执行的跳转
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		// 设定要过滤的标签动作，这里只接收ACTION_NDEF_DISCOVERED类型
		ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		ndef.addCategory("*/*");
		mFilters = new IntentFilter[] { ndef };// 过滤器
		mTechLists = new String[][] { new String[] { NfcA.class.getName() },
				new String[] { NfcF.class.getName() },
				new String[] { NfcB.class.getName() },
				new String[] { MifareClassic.class.getName() },
				new String[] { NfcV.class.getName() } };// 允许扫描的标签类型

		if (isFirst) {
			if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent()
					.getAction())) {
				System.out.println(getIntent().getAction());
				if (readFromTag(getIntent())) {
					// ifo_NFC.setText(readResult);
					System.out.println("1.5...");
				} else {
					ifo_NFC.setText("标签数据为空");
				}
			}
			isFirst = false;
		}
		System.out.println("onCreate...");
	}

	public void initData() {
		LayoutInflater inflater = getLayoutInflater();

		for (int i = 0; i < 3; i++) {
			View v = inflater.inflate(R.layout.block, null);
			TextView tv = (TextView) v.findViewById(R.id.block);

			views.add(tv);
		}

		// 设置数据
		viewPager.setAdapter(vpAdapter);
		// 设置监听
		viewPager.setOnPageChangeListener(new pageListener());

	}

	private class pageListener implements OnPageChangeListener {

		/**
		 * 当滑动状态改变时调用
		 */
		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}

		/**
		 * 当当前页面被滑动时调用
		 */
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			viewPager.setAnimation(AnimationUtils.loadAnimation(
					getApplicationContext(), R.anim.push_left_in));
			viewPager.setAnimation(AnimationUtils.loadAnimation(
					getApplicationContext(), R.anim.push_left_out));
		}

		/**
		 * 当新的页面被选中时调用
		 */
		@Override
		public void onPageSelected(int position) {
			// 设置底部小点选中状态
			System.out.println("setCurDot==" + position);

		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		nfcAdapter.disableForegroundDispatch(this);
		System.out.println("onPause...");
	}

	/*
	 * 重写onResume回调函数的意义在于处理多次读取NFC标签时的情况 (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 前台分发系统,这里的作用在于第二次检测NFC标签时该应用有最高的捕获优先权.
		nfcAdapter.enableForegroundDispatch(this, pendingIntent, mFilters,
				mTechLists);

		System.out.println("onResume...");
	}

	/*
	 * 有必要要了解onNewIntent回调函数的调用时机,请自行上网查询 (non-Javadoc)
	 * 
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		System.out.println("onNewIntent1...");
		System.out.println(intent.getAction());

		if ((NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TECH_DISCOVERED
				.equals(intent.getAction()))) {
			System.out.println("onNewIntent2...");
			if (readFromTag(intent)) {
				// ifo_NFC.setText(readResult);
				System.out.println("onNewIntent3...");
			} else {
				ifo_NFC.setText("标签数据为空");
			}
		}

	}

	/**
	 * 读取NFC标签数据的操作
	 * 
	 * @param intent
	 * @return
	 */
	private boolean readFromTag(Intent intent) {

		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		// 对M1卡的读操作
		MifareClassic mfc = MifareClassic.get(tag);
		if (mfc != null) {
			System.out.println("开始连接mfc");

			try {

				mfc.connect();
				int blockcount = mfc.getSectorCount();
				System.out.println("mfc连接成功");

				boolean auth = mfc.authenticateSectorWithKeyA(3,
						MifareClassic.KEY_DEFAULT);

				if (auth) {
					System.out.println("mfc连接扇区成功");
					int blockIndex = mfc.sectorToBlock(3);
					// 界面加载

					for (int i = 0; i < 3; i++) {
						System.out.println("blockIndex==" + blockIndex);
						byte[] data = mfc.readBlock(blockIndex);

						readResult = new String(data, "UTF-8");
						Typeface typeface = Typeface.create("Comic Sans MS",
								Typeface.BOLD_ITALIC);
						TextView tv = (TextView) views.get(i).findViewById(
								R.id.block);
						tv.setTypeface(typeface);
						tv.setText(readResult);
						System.out.println("readResult==" + readResult);
						blockIndex++;
					}

				} else {
					System.out.println("mfc连接扇区13失败");
					return false;
				}

			}

			catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("mfc连接失败");
				e.printStackTrace();
				return false;
			} finally {
				try {
					mfc.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block

					e.printStackTrace();
					return false;
				}
			}

		} else {
			System.out.println("mfc为null");
			return false;

		}
		return true;
	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		// TODO Auto-generated method stub
		return null;
	}

	// 字符序列转换为16进制字符串
	private String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("0x");
		if (src == null || src.length <= 0) {
			return null;
		}
		char[] buffer = new char[2];
		for (int i = 0; i < src.length; i++) {
			buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
			buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
			System.out.println(buffer);
			stringBuilder.append(buffer);
		}
		return stringBuilder.toString();
	}

	private final Handler mHandler = new Handler() {
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {

			}

		}
	};

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_update:
			UpdateManager manager = new UpdateManager(MainActivity.this);
			// 检查软件更新
			manager.checkUpdate();
			return true;
		case R.id.menu_exit:
			finish();
		}
		return false;
	}

}
