package com.staticvillage.sense.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import android.os.Message;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.util.JSON;
import com.staticvillage.sense.android.data.PayLoad;
import com.staticvillage.sense.android.data.SensorData;
import com.staticvillage.sense.android.exception.RunnerInitializationException;
import com.staticvillage.sense.android.list.SensorBuffer;
import com.staticvillage.sense.android.thread.SenseHandler;

public class SenseClient {
	public static final int RUNNER_CONNECT = 999999;
	public static final int RUNNER_DISCONNECT = 999998;
	public static final int RUNNER_CONNECT_ERROR = 999997;
	public static final int RUNNER_DATA_UPLOADED = 999996;
	public static final int RUNNER_PAYLOAD = 999995;
	public static final int RUNNER_PAYLOAD_END = 999994;
	
	public static final String MONGODB_PROTOCOL = "mongodb://";
	
	private int mBufferSize;
	private String mAppId;
	private String mSessionId;
	private SensorBuffer mSensorBuffer;
	private SenseRunner mRunner;
	private boolean mRunnerInitialzed;
	private SenseListener mListener;
	
	private static SenseHandler mHandler;
	
	/**
	 * Build a MongoDB connection string
	 * 
	 * @param username MongoDB Username
	 * @param pass MongoDB Password
	 * @param db MongoDB Database
	 * @param host MongoDB Hostname
	 * @param port MongoDB Port
	 * @return MongoDB connection string
	 */
	public static String buildConectionString(String username, String pass, String db, String host, String port){
		String connStr = null;
		
		if(username.isEmpty() || pass.isEmpty() || username == null || pass == null)
			connStr = MONGODB_PROTOCOL+host+":"+port+"/"+db;
		else
			connStr = MONGODB_PROTOCOL+username+":"+pass+"@"+host+":"+port+"/"+db;
		
		return connStr;
	}
	
	/**
	 * Verify MongoDB account settings
	 * 
	 * @param connectionString MongoDB connection string
	 * @param db MongoDB database
	 * @return verified
	 */
	public static boolean verifyAccount(String connectionString, String db){
		MongoClient client;
		MongoClientURI uri = new MongoClientURI(connectionString);
		
		try {
			client = new MongoClient(uri);
			DB mdb = client.getDB(db);
			mdb.getCollectionNames();
			client.close();
			
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (Exception exception){
			exception.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Retrieve Sensor data from file
	 * 
	 * @param file sensor data file
	 * @param collection collection name
	 * @return array of sensor data
	 * @throws FileNotFoundException 
	 */
	public static SensorData[] retrieveFromFile(File file, String collection) throws FileNotFoundException{
		if(!file.exists())
			throw new FileNotFoundException(String.format("No file is found: {%s}", file.getAbsolutePath()));

		BufferedReader reader = new BufferedReader(new FileReader(file));
		ArrayList<SensorData> list = new ArrayList<SensorData>();
		
		String read = null;
		try {
			while((read = reader.readLine()) != null){
				SensorData data = SensorData.initFromDBObject((DBObject) JSON.parse(read));
				
				if(data.name.equals(collection))
					list.add(data);
			}
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return list.toArray(new SensorData[]{});
	}
	
	/**
	 * Constructor
	 * 
	 * @param listener SenseListener
	 * @param appId Application Identifier
	 */
	public SenseClient(SenseListener listener, String appId){
		this(listener, appId, 10);
	}
	public SenseClient(SenseListener listener, String appId, int bufferSize){
		if(bufferSize < 1)
			bufferSize = 1;
		
		mAppId = appId;
		mSessionId = UUID.randomUUID().toString();
		mBufferSize = bufferSize;
		mHandler = new SenseHandler(this);
		mListener = listener;
		
		mSensorBuffer = new SensorBuffer(mBufferSize);
	}
	
	/**
	 * Application Identifier
	 * 
	 * @return Application Id
	 */
	public String getAppId(){
		return mAppId;
	}
	
	/**
	 * Get the current session ID that was created
	 * 
	 * @return Session ID
	 */
	public String getSessionId(){
		if(mSessionId == null)
			mSessionId = UUID.randomUUID().toString();
		
		return mSessionId;
	}
	
	/**
	 * Client connection and initialization state
	 * 
	 * @return connection/initialization state
	 */
	public boolean isConnected(){
		return mRunnerInitialzed;
	}
	
	/**
	 * Connect to a MongoDB instance to store captured sensor data
	 * 
	 * @param connectionString MongoDB connection string
	 * @param db MongoDB database
	 * @return Session ID
	 */
	public String connect(String connectionString, String db){
		if(mRunner == null){
			mSessionId = getSessionId();
			mRunner = new SenseRunner(mHandler, connectionString, db);
			mRunner.start();
			
			return mSessionId;
		}
		
		return null;
	}
	
	/**
	 * Connect locally to store captured sensor data in a file
	 *
	 * @return Session ID
	 */
	public String connect(File file){
		if(mRunner == null){
			mSessionId = getSessionId();
			mRunner = new SenseRunner(mHandler, file);
			mRunner.start();
			
			return mSessionId;
		}
		
		return null;
	}
	
	/**
	 * Send sensor payload to background persistence thread
	 * 
	 * @param payload payload of sensor information
	 */
	protected void sendPayloadToRunner(PayLoad payload){
		Message msg = new Message();
		msg.what = RUNNER_PAYLOAD;
		msg.obj = payload;
		
		mRunner.mHandler.sendMessage(msg);
	}
	
	/**
	 * Report captured sensor data
	 * 
	 * @param data sensor data to record
	 * @param tag session tag
	 * @throws RunnerInitializationException Background connection thread not initialized
	 */
	public void report(SensorData data, String tag) throws RunnerInitializationException {
		if(!mRunnerInitialzed)
			throw new RunnerInitializationException("Background network thread has not yet initialized");
		
		data.name = data.getName();
		data.tag = tag;
		data.app_id = mAppId;
		data.session_id = mSessionId;
		
		if(data.timestamp <= 0)
			data.timestamp = new Date().getTime();
		
		SensorData[] out = mSensorBuffer.add(data);
		
		if(out != null){
			PayLoad payload = new PayLoad();
			payload.setCollection(out[0].name);
			payload.setData(out);

			sendPayloadToRunner(payload);
		}
	}
	
	/**
	 * Flush any data remaining in buffer
	 */
	public void flush(){
		for(SensorData[] data : mSensorBuffer.flush())
		{
			PayLoad payload = new PayLoad();
			payload.setCollection(data[0].name);
			payload.setData(data);

			sendPayloadToRunner(payload);
		}
	}
	
	/**
	 * Close client connection
	 * 
	 * @throws IOException
	 */
	public void close(){
		if(mRunner != null){
			flush();
			mRunner.mHandler.getLooper().quit();
			mRunner = null;
			mRunnerInitialzed = false;
			mSessionId = null;
		}
	}
	
	/**
	 * Upload captured sensor information from a file to a MongoDB instance
	 *
	 * @throws IOException 
	 */
	public void uploadFromFile(File file) throws IOException{
		if(!file.exists())
			throw new FileNotFoundException(String.format("No file is found for session {%s}", file.getAbsolutePath()));

		BufferedReader reader = new BufferedReader(new FileReader(file));
		HashMap<String, ArrayList<SensorData>> map = new HashMap<String, ArrayList<SensorData>>();
		
		String read = null;
		while((read = reader.readLine()) != null){
			SensorData data = SensorData.initFromDBObject((DBObject) JSON.parse(read));
			
			if(!map.containsKey(data.name))
				map.put(data.name, new ArrayList<SensorData>());
			
			map.get(data.name).add(data);
		}
		reader.close();
		
		int i = 0;
		int last = map.values().size() - 1;
		for(ArrayList<SensorData> sData : map.values()){
			String name = sData.get(0).name;
			
			PayLoad payload = new PayLoad();
			payload.setCollection(name);
			payload.setData(sData.toArray(new SensorData[]{}));

			Message msg = new Message();
			msg.what = (i == last) ? RUNNER_PAYLOAD_END : RUNNER_PAYLOAD;
			msg.obj = payload;
			
			mRunner.mHandler.sendMessage(msg);
            i++;
		}
	}
	
	/**
	 * Message handler for recorder thread
	 * 
	 * @param msg Runner Thread Message
	 */
	public void handleMessage(Message msg){
		switch(msg.what){
			case RUNNER_CONNECT:
				mRunnerInitialzed = true;
				mListener.onConnect(mAppId, mSessionId);
				break;
			case RUNNER_DISCONNECT:
				mRunnerInitialzed = false;
				mListener.onDisconnect();
				break;
			case RUNNER_CONNECT_ERROR:
				mRunnerInitialzed = false;
				mListener.onConnectError((String)msg.obj);
				break;
			case RUNNER_DATA_UPLOADED:
				String sessionId = (String)msg.obj;
				mListener.onDataUploaded(sessionId);
				break;
			default:
				break;
		}
	}
}
