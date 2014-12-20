package com.staticvillage.sense.android;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.staticvillage.sense.android.data.PayLoad;
import com.staticvillage.sense.android.data.SensorData;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class SenseRunner extends Thread {
	public Handler mClientHandler;
	public Handler mHandler;
	
	private String mConnectionString;
	private String mDBString;
	private MongoClient mClient;
	private DB mDB;
	private File file;
	private BufferedWriter mWriter;
	private boolean mOffline;
	
	/**
	 * Constructor
	 * 
	 * @param handler Handler to return messages
	 * @param connectionString MongoDB connection string
	 * @param db MongoDB database
	 */
	public SenseRunner(Handler handler, String connectionString, String db){
		this.mClientHandler = handler;
		this.mDBString = db;
		this.mConnectionString = connectionString;
		this.mOffline = false;
	}
	
	/**
	 * Constructor
	 * 
	 * @param handler Handler to return messages
	 * @param file file to store data
	 */
	public SenseRunner(Handler handler, File file){
		this.mClientHandler = handler;
		this.file = file;
		this.mOffline = true;
	}
	
	@Override
	public void run() {
		Looper.prepare();
		
		if(!mOffline){
			if(!initMongoDb())
				return;
		}else{
			if(!initFile())
				return;
		}
		
		mHandler = new Handler() {
            public void handleMessage(Message msg) {
                PayLoad payload = (PayLoad) msg.obj;
                
                try {
                	if(!mOffline) {
	                	DBCollection col;
	                	if(!mDB.collectionExists(payload.getCollection()))
	                		col = mDB.createCollection(payload.getCollection(), new BasicDBObject("capped", false));
	                	else
	                		col = mDB.getCollection(payload.getCollection());
	                		
						send(col, payload.getData(), msg.what == SenseClient.RUNNER_PAYLOAD_END);
                	}else{
                		send(payload.getData(), msg.what == SenseClient.RUNNER_PAYLOAD_END);
                	}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
            }
        };
        
        notifyHandler(SenseClient.RUNNER_CONNECT, "");
        
		Looper.loop();
		
		if(!mOffline)
			mClient.close();
		else
			try {
				mWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	/**
	 * Initialize a connection to monogDB instance
	 * 
	 * @return status
	 */
	protected boolean initMongoDb(){
		try {
			MongoClientURI uri = new MongoClientURI(mConnectionString);
			mClient = new MongoClient(uri);
			mDB = mClient.getDB(mDBString);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			notifyHandler(SenseClient.RUNNER_CONNECT_ERROR, e1.getMessage());
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Initialize writer for file
	 * 
	 * @return status
	 */
	protected boolean initFile(){
		try {
			mWriter = new BufferedWriter(new FileWriter(file, true));
		} catch (IOException e) {
			e.printStackTrace();
			notifyHandler(SenseClient.RUNNER_CONNECT_ERROR, e.getMessage());
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Send data to mongoDB
	 * 
	 * @param col collection
	 * @param data array of sensor data objects
	 * @param payloadEnd last payload to send
	 * @throws IOException connection error
	 */
	protected void send(DBCollection col, SensorData[] data, boolean payloadEnd) throws IOException{
		BulkWriteOperation builder = col.initializeUnorderedBulkOperation();
		
		for(SensorData a : data){
			builder.insert(a.getDBObject());
		}
		
		builder.execute();
		
		if(payloadEnd){
			notifyHandler(SenseClient.RUNNER_DATA_UPLOADED, data[0].session_id);
		}
	}
	
	/**
	 * Send data to file
	 * 
	 * @param data array of sensor data objects
	 * @param payloadEnd last payload to send
	 * @throws IOException connection error
	 */
	protected void send(SensorData[] data, boolean payloadEnd) throws IOException{
		for(SensorData a : data){
			mWriter.write(a.getDBObject().toString());
			mWriter.newLine();
		}
		
		if(payloadEnd){
			notifyHandler(SenseClient.RUNNER_DATA_UPLOADED, data[0].session_id);
		}
	}
	
	protected void notifyHandler(int what, Object data){
		Message msg = new Message();
		msg.what = what;
		msg.obj = data;
		
		mClientHandler.sendMessage(msg);
	}
}
