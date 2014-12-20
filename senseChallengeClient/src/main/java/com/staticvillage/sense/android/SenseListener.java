package com.staticvillage.sense.android;

public interface SenseListener {
	public void onConnect(String appId, String sessionId);
	public void onDisconnect();
	public void onConnectError(String Message);
	public void onDataUploaded(String sessionId);
}
