//This is Shakey.
//This all the main code for this app is saved in this activity.
//This application plays music and video when the accelerometer feels motion.
//The user can designate which axis.
//The "music" button will take the user to the native music application to choose which music to play.
//The user can choose between 4 different videos to play.
//The user can choose between manual mode and automatic mode.
//The minimum force required to trigger the music and video are customizable.
//The duration of automatic play is customizable.
//Volume can be controlled with the onscreen slider.
//A bluetooth remote can also control the application.

package com.shakey;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.media.AudioManager;
import android.media.MediaPlayer;

public class MainActivity extends Activity implements SensorEventListener, OnSeekBarChangeListener {

	// accelerometer sensor variables
	private float mLastY;
	private boolean mInitialized;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private int axisChooser = 2; // axis choice variable
	private int vidChooser = 1; // video choice variable
	private int autoChooser = 1; // manual vs. auto mode choice variable

	private double MINIMUM = 0.3; // minimum threshold force variable
	private int militime = 4000; // time auto mode will play music in
									// miliseconds

	// bluetooth stuff
	private BluetoothAdapter mBluetoothAdapter;
	private boolean hasBluetooth;
	private final int REQUEST_ENABLE_BT = 333; // Bluetooth enable request ID
	private ComponentName mRemoteControlResponder;
	private int isPlaying = 0;
	private IntentFilter btFilter;
	private RemoteControlRoutedReceiver btReceiver;
	// end bluetooth stuff

	private CompoundButton man;
	private CompoundButton ax;
	private CompoundButton auto;
	private RadioGroup manAuto;

	// timer variables
	long start = 0;
	long end = 0;
	long duration = 0;

	private boolean displaysettings = false;

	AudioManager am; // controls the music app pause and play as well as device
						// volume

	Intent musiccommand = new Intent("com.android.music.musicservicecommand");
	@SuppressWarnings("deprecation")
	Intent openmusic = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
	double volume = 0.5;

	// onscreen buttons
	private Button enter;
	private Button music;
	private Button play;
	private Button pause;
	private Button menu;

	// videos
	private String banana_uri;
	private String fireworks_uri;
	private String bubbles_uri;
	private VideoView iv;

	@Override
	public void onCreate(Bundle savedInstanceState) { // occurs when the app is
														// first started
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);

		// set the view activity_main.xml saved in the res/layout folder
		setContentView(R.layout.activity_main);

		// give the buttons from the layout names
		enter = (Button) findViewById(R.id.buttonenter);
		music = (Button) findViewById(R.id.buttonmusic);
		play = (Button) findViewById(R.id.buttonplay);
		pause = (Button) findViewById(R.id.buttonpause);
		menu = (Button) findViewById(R.id.buttonmenu);
		final LinearLayout settings = (LinearLayout) findViewById(R.id.settings);
		settings.setVisibility(View.INVISIBLE);

		// set the default checks for the radio buttons
		ax = (CompoundButton) findViewById(R.id.radioz);
		ax.setChecked(true);

		man = (CompoundButton) findViewById(R.id.radiomanual);
		man.setChecked(true);

		final CompoundButton fir = (CompoundButton) findViewById(R.id.radiofireworks);
		fir.setChecked(true);

		auto = (CompoundButton) findViewById(R.id.radioauto);

		manAuto = (RadioGroup) findViewById(R.id.manAuto);

		// volume seekbar
		SeekBar sb = (SeekBar) findViewById(R.id.seekBar1);
		sb.setMax(100);
		sb.setProgress(50);
		sb.setOnSeekBarChangeListener((OnSeekBarChangeListener) this);

		mInitialized = false;
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
		
		//Initializing config
		loadPref();
		// set textbox display texts
		final EditText min = (EditText) findViewById(R.id.editTextmin);
		final EditText mili = (EditText) findViewById(R.id.editTextmili);
		min.setText(Double.toString(MINIMUM));
		mili.setText(Integer.toString(militime));
		final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mili.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(min.getWindowToken(), 0);

		// BluetoothSetup
		setUpBlueTooth();
		mRemoteControlResponder = new ComponentName(getPackageName(),
				RemoteControlReceiver.class.getName());
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		btReceiver = new RemoteControlRoutedReceiver();
		btFilter = new IntentFilter();
		btFilter.addAction("com.MainActivity.Shakey.MEDIA_BUTTON");
		// endBluetoothstuff

		// videostuffz
		banana_uri = "android.resource://" + getPackageName() + "/"
				+ R.raw.banana;
		fireworks_uri = "android.resource://" + getPackageName() + "/"
				+ R.raw.fireworks;
		bubbles_uri = "android.resource://" + getPackageName() + "/"
				+ R.raw.bubbles;
		iv = (VideoView) findViewById(R.id.video);

		iv.setVideoURI(Uri.parse(fireworks_uri));
		//
		
		
		
		
		// the following set what happens when a button is clicked
		menu.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (displaysettings == false) {
					settings.setVisibility(View.VISIBLE);
					displaysettings = true;
				} else {
					settings.setVisibility(View.INVISIBLE);
					displaysettings = false;
				}
			}
		});
		enter.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				imm.hideSoftInputFromWindow(mili.getWindowToken(), 0);
				imm.hideSoftInputFromWindow(min.getWindowToken(), 0);
				String minstring = min.getText().toString();
				double mindouble = Double.parseDouble(minstring);
				if (mindouble > 0) {
					MINIMUM = mindouble;
				} else {
					MINIMUM = 0;
				}
				String milistring = mili.getText().toString();
				int milint = Integer.parseInt(milistring);
				if (milint > 1) {
					militime = milint;
				} else {
					militime = 1;
				}
				
				savePref();
			}
		});
		music.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(openmusic);
			}
		});

		/*
		 * iv.setOnCompletionListener(new OnCompletionListener(){ public void
		 * onCompletion(MediaPlayer arg0) { playVideo(); }
		 * 
		 * });
		 */
		iv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.setLooping(true);
				// mp.setPause
			}
		});

		play.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				playVideo();
				playMusic();
			}
		});

		pause.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				pauseVideo();

				pauseMusic();
			}
		});
	}

	public void onRadioButtonClicked(View view) {
		boolean checked = ((RadioButton) view).isChecked();
		switch (view.getId()) {
		case R.id.radiox:
			if (checked)
				axisChooser = 0;
			break;
		case R.id.radioy:
			if (checked)
				axisChooser = 1;
			break;
		case R.id.radioz:
			if (checked)
				axisChooser = 2;
			break;
		}
	}

	public void onVidChoose(View view) {
		boolean checked = ((RadioButton) view).isChecked();
		iv.setVisibility(view.VISIBLE);
		switch (view.getId()) {
		case R.id.radiobanana:
			if (checked)
				vidChooser = 0;
			iv.setVideoURI(Uri.parse(banana_uri));
			iv.start();
			break;
		case R.id.radiofireworks:
			if (checked)
				vidChooser = 1;
			iv.setVideoURI(Uri.parse(fireworks_uri));
			iv.start();
			break;
		case R.id.radiobubbles:
			if (checked)
				vidChooser = 2;
			iv.setVideoURI(Uri.parse(bubbles_uri));
			iv.start();
			break;
		case R.id.radionone:
			if (checked)
				vidChooser = 3;
			iv.setVisibility(View.INVISIBLE);
			iv.start();
			break;
		}
		if (isPlaying == 1) {
			playMusic();
		}
	}

	public void onAutochoose(View view) {
		mInitialized = false;
		boolean checked = ((RadioButton) view).isChecked();
		switch (view.getId()) {
		case R.id.radioauto:
			if (checked)
				autoChooser = 0;
			break;
		case R.id.radiomanual:
			if (checked)
				autoChooser = 1;
			break;
		}
	}

	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		// bluetooth
		am.registerMediaButtonEventReceiver(mRemoteControlResponder);
		registerReceiver(btReceiver, btFilter);
		// bluetooth
	}

	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
		// bluetooth
		unregisterReceiver(btReceiver);
		am.unregisterMediaButtonEventReceiver(mRemoteControlResponder);
		// bluetooth
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onProgressChanged(SeekBar v, int progress, boolean isUser) {
		volume = (double) (progress) / 100.0;

		int maxv = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		am.setStreamVolume(AudioManager.STREAM_MUSIC,
				(int) (volume * (double) (maxv)), 0);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (autoChooser == 0) {
			TextView tvY = (TextView) findViewById(R.id.y_axis);
			/*
			 * if(vidchanged == true){ vidchanged = false; if(vidChooser == 0){
			 * iv.setVideoURI(Uri.parse(banana_uri)); } else if(vidChooser ==
			 * 1){ iv.setVideoURI(Uri.parse(fireworks_uri)); } else
			 * if(vidChooser == 2){ iv.setVideoURI(Uri.parse(bubbles_uri)); }
			 * else{ iv.setVisibility(View.INVISIBLE); }
			 * 
			 * }
			 */
			float y = event.values[axisChooser];

			if (!mInitialized) {
				mLastY = y;
				/*
				 * if(vidChooser == 0){ iv.setVideoURI(Uri.parse(banana_uri)); }
				 * else if(vidChooser == 1){
				 * iv.setVideoURI(Uri.parse(fireworks_uri)); } else
				 * if(vidChooser == 2){ iv.setVideoURI(Uri.parse(bubbles_uri));
				 * }
				 */
				// iv.start();
				tvY.setText("0.0");
				mInitialized = true;
			} else {
				float deltaY = Math.abs(mLastY - y);

				if (deltaY < MINIMUM)
					deltaY = (float) 0.0;
				mLastY = y;
				tvY.setText(Float.toString(deltaY));
				if (deltaY > 0) {
					playVideo();
					playMusic();
					start = System.nanoTime();
				} else {
					end = System.nanoTime();
					duration = end - start;
					if (duration / 1000000 > militime) {
						pauseVideo();
						iv.setVisibility(View.INVISIBLE);

						pauseMusic();
					}
				}
			}
		}
		am.registerMediaButtonEventReceiver(mRemoteControlResponder);
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	private void setUpBlueTooth() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			hasBluetooth = false;
			// Device does not support Bluetooth
			Toast.makeText(
					this,
					"This device does not support Bluetooth. Running teethless",
					Toast.LENGTH_SHORT).show();
			// ^ shows quick message
		} else {
			hasBluetooth = true;
		}
		if (!mBluetoothAdapter.isEnabled()) {// if the bluetooth is not enabled
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);// sets up an intent
															// for bluetooth
															// enabling request
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);// sends
																		// the
																		// intent
																		// to OS
		}
	}

	public void clickPause() {
		pause.performClick();
	}

	public void clickPlay() {
		play.performClick();
	}

	public void setManual() {
		if (autoChooser == 0) {
			manAuto.check(man.getId());
			man.performClick();
		}
	}

	public void setAuto() {
		if (autoChooser == 1) {
			manAuto.check(auto.getId());
			auto.performClick();
		}
	}

	private void playMusic() {

		musiccommand.putExtra("command", "play");
		MainActivity.this.sendBroadcast(musiccommand);
		isPlaying = 1;
		am.registerMediaButtonEventReceiver(mRemoteControlResponder);

	}

	private void playVideo() {
		if (vidChooser != 3) {
			if (isPlaying == 0) {
				iv.setVisibility(View.VISIBLE);
				iv.start();
				isPlaying = 1;
			}
		}
	}

	private void pauseVideo() {
		if (isPlaying == 1) {
			iv.pause();
			iv.setVisibility(View.INVISIBLE);
			isPlaying = 0;
		}
	}

	private void pauseMusic() {
		musiccommand.putExtra("command", "pause");
		MainActivity.this.sendBroadcast(musiccommand);
		isPlaying = 0;
		am.registerMediaButtonEventReceiver(mRemoteControlResponder);
	}

	private void savePref() {
		try {
			FileOutputStream fos = openFileOutput("shakey.cfg",	Context.MODE_PRIVATE);
			String config = new String();
			config = Double.toString(MINIMUM) + " "	+ Integer.toString(militime) +" ";
			fos.write(config.getBytes(Charset.defaultCharset()));
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void loadPref() {
		Log.d("Shakey", "runningLoadPref");
		try {
			byte[] config = new byte[30];
			FileInputStream fis = openFileInput("shakey.cfg");
			fis.read(config, 0, 30);
			String configS = new String(config, Charset.defaultCharset());
			String[] blah = configS.split(" ");
			MINIMUM = Double.valueOf(blah[0]);
			militime = Integer.valueOf(blah[1]);
			fis.close();
			
		} catch (FileNotFoundException e) {
			Log.d("Shakey", "FileNotFound");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public class RemoteControlRoutedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if ("com.MainActivity.Shakey.MEDIA_BUTTON".equals(intent
					.getAction())) {
				Bundle extras = intent.getExtras();
				int keyType = extras.getInt("keyType");
				String msg = "";

				switch (keyType) {
				case KeyEvent.KEYCODE_MEDIA_CLOSE:
					msg = "CLOSE";
					break;
				case KeyEvent.KEYCODE_MEDIA_EJECT:
					msg = "EJECT";
					break;
				case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
					msg = "FAST FORWARD";
					break;
				case KeyEvent.KEYCODE_MEDIA_NEXT:
					msg = "NEXT";
					setAuto();
					break;
				case KeyEvent.KEYCODE_MEDIA_PAUSE:
					msg = "PAUSE";
					break;
				case KeyEvent.KEYCODE_MEDIA_PLAY:
					msg = "PLAY";
					if (autoChooser == 0) {
						clickPause();
					} else {
						if (isPlaying == 0) {
							clickPlay();
						} else {
							clickPause();
						}
					}
					break;
				case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
					msg = "PLAY/PAUSE";
					if (autoChooser == 0) {
						clickPause();
					} else {
						if (isPlaying == 0) {
							clickPlay();
						} else {
							clickPause();
						}
					}
					break;
				case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
					msg = "PREVIOUS";
					setManual();
					break;
				case KeyEvent.KEYCODE_MEDIA_RECORD:
					msg = "RECORD";
					break;
				case KeyEvent.KEYCODE_MEDIA_REWIND:
					msg = "REWIND";
					break;
				case KeyEvent.KEYCODE_MEDIA_STOP:
					msg = "STOP";
					break;
				default:
					msg = "Unknown Key";
				}

			}

			am.registerMediaButtonEventReceiver(mRemoteControlResponder);
			abortBroadcast();
		}
	}
}
