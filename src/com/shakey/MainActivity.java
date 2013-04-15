//This is Shakey version Animated Video instead of picture; 
//From now on, I am going to do all the development as forks from this Parent on GitHub.
//So rather than rename the app everytime I change the code significantly, I will create a fork, and call it Shakey 1.8 and so on

package com.shakey;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.VideoView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;


public class MainActivity extends Activity implements SensorEventListener, OnSeekBarChangeListener{

	private float mLastY;
	private boolean mInitialized;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private int axisChooser = 0;
	private int vidChooser = 1;
	private boolean vidchanged = false;
	private int autoChooser = 1;
	
		
	private double MINIMUM = 0.3; 
	private int militime = 500;   
	
	long start = 0;
	long end = 0;
	long duration = 0;
	
	private boolean displaysettings = false;
	
	Intent musiccommand = new Intent("com.android.music.musicservicecommand");
	@SuppressWarnings("deprecation")
	Intent openmusic = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
	double volume = 0.5;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main); 

		final Button enter = (Button) findViewById(R.id.buttonenter); 
		final Button music = (Button) findViewById(R.id.buttonmusic); 
		final Button play = (Button) findViewById(R.id.buttonplay); 
		final Button pause = (Button) findViewById(R.id.buttonpause); 
		final Button menu = (Button) findViewById(R.id.buttonmenu); 
		final LinearLayout settings = (LinearLayout)findViewById(R.id.settings);
		settings.setVisibility(View.INVISIBLE);

		final CompoundButton ax = (CompoundButton) findViewById(R.id.radiox); 
		ax.setChecked(true);
		
		final CompoundButton man = (CompoundButton) findViewById(R.id.radiomanual); 
		man.setChecked(true);
		
		final CompoundButton fir = (CompoundButton) findViewById(R.id.radiofireworks); 
		fir.setChecked(true);
		
		
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
		
		iv.setOnCompletionListener(new OnCompletionListener(){
			public void onCompletion(MediaPlayer arg0) {
				if(vidChooser == 0){
					String banana_uri = "android.resource://" + getPackageName() + "/" + R.raw.banana;
					iv.setVideoURI(Uri.parse(banana_uri));	   
				}
				else if(vidChooser == 1){
					String fireworks_uri = "android.resource://" + getPackageName() + "/" + R.raw.fireworks;
					iv.setVideoURI(Uri.parse(fireworks_uri));					
				}
				else{
					String bubbles_uri = "android.resource://" + getPackageName() + "/" + R.raw.bubbles;
					iv.setVideoURI(Uri.parse(bubbles_uri));					
				}
				iv.start();
			}
		});
		
		play.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				VideoView iv = (VideoView)findViewById(R.id.video);
				if(vidChooser == 0){
					String banana_uri = "android.resource://" + getPackageName() + "/" + R.raw.banana;
					iv.setVideoURI(Uri.parse(banana_uri));	   
				}
				else if(vidChooser == 1){
					String fireworks_uri = "android.resource://" + getPackageName() + "/" + R.raw.fireworks;
					iv.setVideoURI(Uri.parse(fireworks_uri));					
				}
				else{
					String bubbles_uri = "android.resource://" + getPackageName() + "/" + R.raw.bubbles;
					iv.setVideoURI(Uri.parse(bubbles_uri));					
				}
				iv.start();
				iv.setVisibility(View.VISIBLE); 
			    
				musiccommand.putExtra("command", "play");
				MainActivity.this.sendBroadcast(musiccommand);
			}
		});
		
		pause.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				VideoView iv = (VideoView)findViewById(R.id.video);
				iv.pause();
			    iv.setVisibility(View.INVISIBLE); 

				musiccommand.putExtra("command", "pause");
				MainActivity.this.sendBroadcast(musiccommand);
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
	
	public void onVidChoose(View view) {
	    boolean checked = ((RadioButton) view).isChecked();
	    vidchanged = true;
	    switch(view.getId()) {
	        case R.id.radiobanana:
	            if (checked) vidChooser = 0;
	            break;
	        case R.id.radiofireworks:
	            if (checked) vidChooser = 1;
	            break;
	        case R.id.radiobubbles:
	            if (checked) vidChooser = 2;
	            break;
	    }
	}
	
	public void onAutochoose(View view) {
		mInitialized = false;
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
	}
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	
	@Override
	public void onProgressChanged(SeekBar v, int progress, boolean isUser) {
		volume = (double)(progress)/100.0;
		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		int maxv = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		am.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(volume*(double)(maxv)), 0);  
	}

	@Override
	public void onSensorChanged(SensorEvent event) {	
		if(autoChooser == 0){
			TextView tvY= (TextView)findViewById(R.id.y_axis);
			VideoView iv = (VideoView)findViewById(R.id.video);
			if(vidchanged == true){
				vidchanged = false;
				if(vidChooser == 0){
					String banana_uri = "android.resource://" + getPackageName() + "/" + R.raw.banana;
					iv.setVideoURI(Uri.parse(banana_uri));	   
				}
				else if(vidChooser == 1){
					String fireworks_uri = "android.resource://" + getPackageName() + "/" + R.raw.fireworks;
					iv.setVideoURI(Uri.parse(fireworks_uri));					
				}
				else{
					String bubbles_uri = "android.resource://" + getPackageName() + "/" + R.raw.bubbles;
					iv.setVideoURI(Uri.parse(bubbles_uri));					
				}
			}
			float y = event.values[axisChooser]; 
			if (!mInitialized) {
				mLastY = y;
				if(vidChooser == 0){
					String banana_uri = "android.resource://" + getPackageName() + "/" + R.raw.banana;
					iv.setVideoURI(Uri.parse(banana_uri));	   
				}
				else if(vidChooser == 1){
					String fireworks_uri = "android.resource://" + getPackageName() + "/" + R.raw.fireworks;
					iv.setVideoURI(Uri.parse(fireworks_uri));					
				}
				else{
					String bubbles_uri = "android.resource://" + getPackageName() + "/" + R.raw.bubbles;
					iv.setVideoURI(Uri.parse(bubbles_uri));					
				}
				iv.start();
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
						iv.pause();
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
}
