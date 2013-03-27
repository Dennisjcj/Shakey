package com.shakey;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


public class RemoteControlReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())){
			Toast.makeText(context, "Received Intent.ACTION_MEDIA_BUTTON", Toast.LENGTH_SHORT).show();
		}
		
	}
}
