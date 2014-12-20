package com.staticvillage.sense.android.thread;

import java.lang.ref.WeakReference;

import com.staticvillage.sense.android.SenseClient;

import android.os.Handler;
import android.os.Message;

public class SenseHandler extends Handler {
	private WeakReference<SenseClient> ref;
	
	public SenseHandler(SenseClient client){
		ref = new WeakReference<SenseClient>(client);
	}

	@Override
	public void handleMessage(Message msg) {
		SenseClient client = ref.get();
		
		if(client != null){
			client.handleMessage(msg);
		}
	}

}
