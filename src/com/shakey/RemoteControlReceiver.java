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
			
					
			
			Intent i = new Intent();
		    i.setAction("com.MainActivity.Shakey.MEDIA_BUTTON");
		    i.putExtra("keyType", keyType);
			context.sendBroadcast(i);
			Toast.makeText(context, String.valueOf(MainActivity.mult), Toast.LENGTH_SHORT).show();
			MainActivity.mult++;
			abortBroadcast();
		
		}
		
	}
}
