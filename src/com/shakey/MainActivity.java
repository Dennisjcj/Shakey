//This is Shakey version Animated Video instead of picture; 
//From now on, I am going to do all the development as forks from this Parent on GitHub.
//So rather than rename the app everytime I change the code significantly, I will create a fork, and call it Shakey 1.8 and so on

package com.shakey;

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
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.media.AudioManager;


public class MainActivity extends Activity implements SensorEventListener, OnSeekBarChangeListener{

	private float mLastY;
	private boolean mInitialized;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private int axisChooser = 0;
	private static int autoChooser = 0;
		
	private double MINIMUM = 0.3; 
	private int militime = 500;   
	
	//bluetooth stuff
	private BluetoothAdapter mBluetoothAdapter;	
	private boolean hasBluetooth; 
	private final int REQUEST_ENABLE_BT = 333; //Bluetooth enable request ID
	private ComponentName mRemoteControlResponder;
	private static int isPlaying = 0;
	private IntentFilter btFilter;
	private RemoteControlRoutedReceiver btReceiver; 
	//end bluetooth stuff
	
	long start = 0;
	long end = 0;
	long duration = 0;
	
	private boolean displaysettings = false;
	AudioManager am;

	
	Intent musiccommand = new Intent("com.android.music.musicservicecommand");
	@SuppressWarnings("deprecation")
	Intent openmusic = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
	double volume = 0.5;
	
	//buttons
	private Button enter; 
	private Button music; 
	private Button play; 
	private Button pause; 
	private Button menu; 
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main); 

		Button enter = (Button) findViewById(R.id.buttonenter); 
		Button music = (Button) findViewById(R.id.buttonmusic); 
		Button play = (Button) findViewById(R.id.buttonplay); 
		Button pause = (Button) findViewById(R.id.buttonpause); 
		Button menu = (Button) findViewById(R.id.buttonmenu); 
		final LinearLayout settings = (LinearLayout)findViewById(R.id.settings);
		settings.setVisibility(View.INVISIBLE);

		final CompoundButton ax = (CompoundButton) findViewById(R.id.radiox); 
		ax.setChecked(true);
		
		final CompoundButton aut = (CompoundButton) findViewById(R.id.radioauto); 
		aut.setChecked(true);
		
		SeekBar sb = (SeekBar)findViewById(R.id.seekBar1);
		sb.setMax(100);
		sb.setProgress(50);
		sb.setOnSeekBarChangeListener((OnSeekBarChangeListener) this);
	
		mInitialized = false;
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);  
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);  
		
		final EditText min = (EditText)findViewById(R.id.editTextmin); 
		final EditText mili = (EditText)findViewById(R.id.editTextmili);
		min.setText("0.3");
		mili.setText("500");
		final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mili.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(min.getWindowToken(), 0);
		
		//BluetoothSetup
		setUpBlueTooth();
		mRemoteControlResponder = new ComponentName(getPackageName(), RemoteControlReceiver.class.getName());
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		am.registerMediaButtonEventReceiver(mRemoteControlResponder);
		btReceiver = new RemoteControlRoutedReceiver();
		btFilter = new IntentFilter();
		btFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
		this.registerReceiver(btReceiver, btFilter);
		//endBluetoothstuff
		
		menu.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				
				if(displaysettings == false){
					settings.setVisibility(View.VISIBLE);
					displaysettings = true;
				}
				else{
					settings.setVisibility(View.INVISIBLE);
					displaysettings = false;
				}
			}
		});
		enter.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				imm.hideSoftInputFromWindow(mili.getWindowToken(), 0);
				imm.hideSoftInputFromWindow(min.getWindowToken(), 0);

				String minstring = min.getText().toString();
				double mindouble = Double.parseDouble(minstring);
				if(mindouble > 0){
					MINIMUM = mindouble;
				}
				else{
					MINIMUM = 0;
				}
				String milistring = mili.getText().toString();
				int milint = Integer.parseInt(milistring);
				if(milint > 1){
					militime = milint;
				}
				else{
					militime = 1;
				}        
			}
		});
		music.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				startActivity(openmusic);
			}
		});
		
		final VideoView iv = (VideoView)findViewById(R.id.video);
		String uri = "android.resource://" + getPackageName() + "/" + R.raw.quickwho;
		iv.setVideoURI(Uri.parse(uri));	    //iv.setVisibility(View.VISIBLE); 

		play.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				iv.start();
			    iv.setVisibility(View.VISIBLE); 

				musiccommand.putExtra("command", "play");
				MainActivity.this.sendBroadcast(musiccommand);
				isPlaying = 1;
			}
		});
		pause.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				iv.suspend();
				iv.setVisibility(View.INVISIBLE); 
				
				musiccommand.putExtra("command", "pause");
				MainActivity.this.sendBroadcast(musiccommand);
				isPlaying = 0;
			}
		});
		
	}
	
	public void onRadioButtonClicked(View view) {
	    boolean checked = ((RadioButton) view).isChecked();
	    
	    switch(view.getId()) {
	        case R.id.radiox:
	            if (checked) axisChooser = 0;
	            break;
	        case R.id.radioy:
	            if (checked) axisChooser = 1;
	            break;
	        case R.id.radioz:
	            if (checked) axisChooser = 2;
	            break;
	    }
	}
	
	public void onAutochoose(View view) {
	    boolean checked = ((RadioButton) view).isChecked();
	    switch(view.getId()) {
	        case R.id.radioauto:
	            if (checked) autoChooser =  0;
	            break;
	        case R.id.radiomanual:
	            if (checked) autoChooser = 1;
	            break;
	    }
	}
	
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		am.registerMediaButtonEventReceiver(mRemoteControlResponder);
	}
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
		am.unregisterMediaButtonEventReceiver(mRemoteControlResponder);
		//this.unregisterReceiver(btReceiver);
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	
	@Override
	public void onProgressChanged(SeekBar v, int progress, boolean isUser) {
		volume = (double)(progress)/100.0;
		
		int maxv = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		am.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(volume*(double)(maxv)), 0);  
	}

	@Override
	public void onSensorChanged(SensorEvent event) {		
		if(autoChooser == 0){
			TextView tvY= (TextView)findViewById(R.id.y_axis);
			final VideoView iv = (VideoView)findViewById(R.id.video);
			
			float y = event.values[axisChooser]; 
			if (!mInitialized) {
				mLastY = y;
				String uri = "android.resource://" + getPackageName() + "/" + R.raw.quickwho;
				iv.setVideoURI(Uri.parse(uri));	    //iv.setVisibility(View.VISIBLE); 
				tvY.setText("0.0");
				mInitialized = true;
			} 
			else {
				float deltaY = Math.abs(mLastY - y);

				if (deltaY < MINIMUM) deltaY = (float)0.0;
				mLastY = y;
				tvY.setText(Float.toString(deltaY)); 
				if (deltaY > 0) {
					iv.start();
					iv.setVisibility(View.VISIBLE); 
					
					musiccommand.putExtra("command", "play");
					MainActivity.this.sendBroadcast(musiccommand);
					start = System.nanoTime();
				}
				else {	
					end = System.nanoTime(); 
					duration = end - start;		
					if(duration/1000000 > militime){ 
						iv.stopPlayback();
						iv.setVisibility(View.INVISIBLE);

						musiccommand.putExtra("command", "pause");
						MainActivity.this.sendBroadcast(musiccommand);
					}
				}
			}
		}
	}
	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}
	private void setUpBlueTooth(){
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null){
			hasBluetooth = false;
			//Device does not support Bluetooth
			Toast.makeText(this, "This device does not support Bluetooth. Running teethless", Toast.LENGTH_SHORT).show();
			//^ shows quick message
		}
		else{
			hasBluetooth = true;
		}
		if(!mBluetoothAdapter.isEnabled()){//if the bluetooth is not enabled
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);//sets up an intent for bluetooth enabling request
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);//sends the intent to OS
		}
	}
	public static int getAutoChooser(){
		return autoChooser;
	}
	public static int getisPlaying(){
		return isPlaying;
	}
	public void clickPause(){
		pause.performClick();
	}
	public void clickPlay(){
		play.performClick();
	}
	
	
	public class RemoteControlRoutedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())){
				KeyEvent Xevent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
				int keyType = Xevent.getKeyCode();
				String msg ="";
				switch(keyType)
				{
					case KeyEvent.KEYCODE_MEDIA_CLOSE:	msg = "CLOSE";
						break;
					case KeyEvent.KEYCODE_MEDIA_EJECT:	msg = "EJECT";
						break;
					case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:	msg = "FAST FORWARD";
						break;
					case KeyEvent.KEYCODE_MEDIA_NEXT:	msg = "NEXT";
						break;
					case KeyEvent.KEYCODE_MEDIA_PAUSE:	msg = "PAUSE";
						break;
					case KeyEvent.KEYCODE_MEDIA_PLAY:	msg = "PLAY";
						break;
					case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:	msg = "PLAY/PAUSE";
						Toast.makeText(context, "Got to Play/Pause", Toast.LENGTH_SHORT).show();
						if(autoChooser == 0){
							clickPause();
						}
						else{
							if(isPlaying == 0){
								clickPlay();
							}
							else{
								clickPause();
							}
						}
						break;
					case KeyEvent.KEYCODE_MEDIA_PREVIOUS:	msg = "PREVIOUS";
						break;
					case KeyEvent.KEYCODE_MEDIA_RECORD:	msg = "RECORD";
						break;
					case KeyEvent.KEYCODE_MEDIA_REWIND:	msg = "REWIND";
						break;
					case KeyEvent.KEYCODE_MEDIA_STOP:	msg = "STOP";
						break;
					default: msg = "Unknown Key";
				}
				
				
			}			
		}
		
	}
}


