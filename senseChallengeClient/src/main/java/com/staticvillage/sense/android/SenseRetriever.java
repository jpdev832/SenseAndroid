package com.staticvillage.sense.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import android.os.AsyncTask;

import com.mongodb.AggregationOutput;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.util.JSON;
import com.staticvillage.sense.android.data.SensorData;
import com.staticvillage.sense.android.data.RetrieveResult;

public class SenseRetriever extends Observable {
	public static final String TYPE_COLLECTION  = "collection";
	public static final String TYPE_TAG  		= "tag";
	public static final String TYPE_RAW			= "raw";
	
	public static final String KEY_NAME 		= "name";
	public static final String KEY_TAG 			= "tag";
	public static final String KEY_APPID 		= "app_id";
	public static final String KEY_SESSION_ID 	= "session_id";
	public static final String KEY_TIMESTAMP 	= "timestamp";
	
	private MongoClient client;
	private DB db;
	private String connectionString;
	private String dbStr;
	
	public SenseRetriever(String connectionString, String db){
		this.connectionString = connectionString;
		this.dbStr = db;
	}
	
	/**
	 * Initialize collector connect to db
	 * @throws Exception 
	 */
	public void init() throws Exception{
		MongoClientURI uri = new MongoClientURI(connectionString);
		client = new MongoClient(uri);
		db = client.getDB(this.dbStr);
		
		if(db == null)
			throw new Exception("db does not exist");
	}
	
	/**
	 * Close connection to the database and stop collecting data
	 */
	public void close() {
		if(client != null)
			client.close();
	}
	
	/**
	 * Retrieve available collections
	 */
	public void getCollections(){
		new CollectionsAsyncTask().execute(db);
	}
	
	/**
	 * Retrieve available tags for collection
	 * 
	 * @param collection collection to search
	 */
	public void getTags(String collection){
		DistinctAsyncTask a = new DistinctAsyncTask(db, collection, TYPE_TAG);
		a.execute();
	}
	
	/**
	 * Retrieve data from collection/tag/session
	 * 
	 * @param collection collection to retrieve data from
	 * @param object
	 */
	public void retrieve(String collection, DBObject object){
		RetrieveAsyncTask a = new RetrieveAsyncTask(db, collection, object);
		a.execute();
	}
	
	/**
	 * Retrieve aggregated results from a collection and query
	 * 
	 * @param collection collection
	 * @param object DBObject query
	 */
	public void retrieveAggregate(String collection, List<DBObject> object){
		retrieveAggregateAsyncTask a = new retrieveAggregateAsyncTask(db, collection, object);
		a.execute();
	}
	
	/**
	 * Retrieve an array of collection names
	 * 
	 * @author joelparrish
	 */
	protected class CollectionsAsyncTask extends AsyncTask<DB, Void, String[]>{

		@Override
		protected String[] doInBackground(DB... db) {
			if(db == null || db.length < 1)
				return new String[]{};
			
			try{
				Set<String> names = db[0].getCollectionNames();
				String[] sName = new String[names.size() - 1];
				
				int index = 0;
				for(String name : names){
					if(name.equals("system.indexes") || name.equals("system.users") || name == null)
						continue;
					
					sName[index] = name;
					index++;
				}
				
				return sName;
			}catch(Exception e){
				e.printStackTrace();
				return new String[]{};
			}
		}

		@Override
		protected void onPostExecute(String[] result) {
			RetrieveResult ret = new RetrieveResult();
			ret.type = TYPE_COLLECTION;
			ret.data = result;
			
			setChanged();
			notifyObservers(ret);
		}
		
	}
	
	/**
	 * Retrieve Distinct tag names
	 * 
	 * @author joelparrish
	 */
	protected class DistinctAsyncTask extends AsyncTask<Void, Void, String[]>{
		private String collection;
		private String name;
		private DB db;
		
		public DistinctAsyncTask(DB db, String collection, String name){
			this.db = db;
			this.collection = collection;
			this.name = name;
		}
		
		@Override
		protected String[] doInBackground(Void... v) {
			DBCollection coll = db.getCollection(collection);
			List<DBObject> items = coll.distinct(name);
			
			String[] sItems = new String[items.size()];
			
			int index = 0;
			for(Object s : items){
				String str = (String)s;
				sItems[index] = (str.equals("")) ? "unknown":str;
				index++;
			}
			
			return sItems;
		}

		@Override
		protected void onPostExecute(String[] result) {
			RetrieveResult ret = new RetrieveResult();
			ret.type = name;
			ret.data = result;
			
			setChanged();
			notifyObservers(ret);
		}
		
	}

	/**
	 * Retrieve Sensor Data
	 * 
	 * @author joelparrish
	 */
	protected class RetrieveAsyncTask extends AsyncTask<Void, Void, SensorData[]>{
		private String collection;
		private DB db;
		private DBObject query;
		
		public RetrieveAsyncTask(DB db, String collection, DBObject query){
			this.db = db;
			this.collection = collection;
			this.query = query;
		}
		
		@Override
		protected SensorData[] doInBackground(Void... v) {
			DBCollection col = db.getCollection(collection);
			DBCursor cursor = col.find(query);
			
			ArrayList<SensorData> c = new ArrayList<SensorData>(cursor.count());
			try {
			    while (cursor.hasNext()) {
			    	SensorData sensorData = SensorData.initFromDBObject(cursor.next());
			    	
			        c.add(sensorData);
			    }
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
			    cursor.close();
			}
			
			return c.toArray(new SensorData[]{});
		}

		@Override
		protected void onPostExecute(SensorData[] result) {
			RetrieveResult ret = new RetrieveResult();
			ret.type = "sensor_data";
			ret.data = result;
			
			setChanged();
			notifyObservers(ret);
		}
		
	}
	
	/**
	 * Retrieve aggregated data
	 * 
	 * @author joelparrish
	 */
	protected class retrieveAggregateAsyncTask extends AsyncTask<Void, Void, DBObject[]>{
		private String collection;
		private DB db;
		private List<DBObject> query;
		
		public retrieveAggregateAsyncTask(DB db, String collection, List<DBObject> object){
			this.db = db;
			this.collection = collection;
			this.query = object;
		}
		
		@Override
		protected DBObject[] doInBackground(Void... v) {
			DBCollection col = db.getCollection(collection);
			AggregationOutput output = col.aggregate(query);
			
			ArrayList<DBObject> c = new ArrayList<DBObject>();
		    for(DBObject obj : output.results()){
		    	c.add(obj);
		    }
			
			return c.toArray(new DBObject[]{});
		}

		@Override
		protected void onPostExecute(DBObject[] result) {
			RetrieveResult ret = new RetrieveResult();
			ret.type = TYPE_RAW;
			ret.data = result;
			
			setChanged();
			notifyObservers(ret);
		}
		
	}
}
