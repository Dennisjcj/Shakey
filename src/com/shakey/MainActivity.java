//This is Shakey version Animated Video instead of picture; 
//From now on, I am going to do all the development as forks from this Parent on GitHub.
//So rather than rename the app everytime I change the code significantly, I will create a fork, and call it Shakey 1.8 and so on
//Dennistest
//testcommentistest
//branch commit test
package com.shakey;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
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
	private int axisChooser = 1;
	private int autoChooser = 0;
		
	private double MINIMUM = 0.3; 
	private int militime = 5000;   
	
	long start = 0;
	long end = 0;
	long duration = 0;
	
	boolean videostart = true;
	int videoposition = 0;
	int videoplay = 0;
	
	Intent musiccommand = new Intent("com.android.music.musicservicecommand");
	@SuppressWarnings("deprecation")
	Intent openmusic = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
	double volume = 0.5;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        
		setContentView(R.layout.activity_main); 
		final Button enter = (Button) findViewById(R.id.buttonenter); 
		final Button music = (Button) findViewById(R.id.buttonmusic); 
		final Button play = (Button) findViewById(R.id.buttonplay); 
		final Button pause = (Button) findViewById(R.id.buttonpause); 
		

		final CompoundButton ax = (CompoundButton) findViewById(R.id.radioy); 
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

		EditText min = (EditText)findViewById(R.id.editTextmin); 
		EditText mili = (EditText)findViewById(R.id.editTextmili);
		min.setText("0.3");
		mili.setText("5000");
		
		
		enter.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				EditText min = (EditText)findViewById(R.id.editTextmin);
				EditText mili = (EditText)findViewById(R.id.editTextmili);
        	
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
				videoplay = 1;
				
				musiccommand.putExtra("command", "play");
				MainActivity.this.sendBroadcast(musiccommand);
			}
		});
		pause.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				iv.suspend();
				iv.setVisibility(View.INVISIBLE); 
				videoplay = 2;
				
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


}
