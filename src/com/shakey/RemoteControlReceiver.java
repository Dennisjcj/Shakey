package com.shakey;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.Toast;
import android.app.Activity;


public class RemoteControlReceiver extends BroadcastReceiver {


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
			Intent i = intent;
		    i.setClass(context, MainActivity.class);
		    //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    context.sendBroadcast(i);
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		}
		
	}
}
