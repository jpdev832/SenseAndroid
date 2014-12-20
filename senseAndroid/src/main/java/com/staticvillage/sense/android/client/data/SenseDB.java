package com.staticvillage.sense.android.client.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SenseDB extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION 		= 2;
    public static final String DATABASE_NAME 		= "sense.db";
    public static final String HISTORY_TABLE_NAME   = "sense_history";
    
    public static final String KEY_NAME 			= "name";
    public static final String KEY_TAG 				= "tag";
    public static final String KEY_SESSION 			= "session";
    public static final String KEY_TIMESTAMP 		= "timestamp";
    public static final String KEY_UPLOADED 		= "uploaded";
    
    private static final String TEXT_TYPE 			= " TEXT";
    private static final String INTEGER_TYPE 		= " INTEGER";
    private static final String COMMA_SEP 			= ",";
    
    private static final String SQL_CREATE_ENTRIES 	=
        "CREATE TABLE " + HISTORY_TABLE_NAME + " (" +
        "ID INTEGER PRIMARY KEY" + COMMA_SEP +
        KEY_SESSION + TEXT_TYPE + COMMA_SEP +
        KEY_NAME + TEXT_TYPE + COMMA_SEP +
        KEY_TAG + TEXT_TYPE + COMMA_SEP +
        KEY_TIMESTAMP + INTEGER_TYPE + COMMA_SEP +
        KEY_UPLOADED + INTEGER_TYPE + " )";
    private static final String SQL_SELECT_ENTRIES 	=
            "SELECT * FROM "+HISTORY_TABLE_NAME;

    private static final String SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS " + HISTORY_TABLE_NAME;

    private SQLiteDatabase db;
    
    public SenseDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public void openRW(){
    	db = this.getWritableDatabase();
    }
    
    public void openR(){
    	db = this.getReadableDatabase();
    }
    
    public void close(){
    	if(db != null)
    		db.close();
    }
    
    public void onCreate(SQLiteDatabase db) {
    	Log.d("sense", SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    
    public long addEntry(String name, String tag, String session, long timestamp, boolean uploaded){
    	if(db == null || !db.isOpen())
    		openRW();
    	
    	ContentValues values = new ContentValues();
    	values.put(KEY_NAME, name);
    	values.put(KEY_TAG, tag);
    	values.put(KEY_SESSION, session);
    	values.put(KEY_TIMESTAMP, timestamp);
    	values.put(KEY_UPLOADED, uploaded ? 1:0);
    	
    	return db.insert(HISTORY_TABLE_NAME, null, values);
    }
    
    public Cursor getEntries() {
    	if(db == null || !db.isOpen())
    		openR();
    	
    	return db.query(HISTORY_TABLE_NAME, 
    			new String[]{
    				"rowid _id",
    				KEY_NAME,
    				KEY_TAG,
    				KEY_SESSION,
    				KEY_TIMESTAMP,
    				KEY_UPLOADED
    			}, 
    			null, 
    			null, 
    			null, 
    			null, 
    			KEY_TIMESTAMP+" DESC");	
	}
    
    public int updateEntry(String session, boolean uploaded){
    	if(db == null || !db.isOpen())
    		openRW();
    	
    	ContentValues values = new ContentValues();
    	values.put(KEY_UPLOADED, uploaded ? 1:0);
    	
    	String selection = KEY_SESSION + " = ?";
    	String[] selectionArgs = { String.valueOf(session) };
    	
    	return db.update(HISTORY_TABLE_NAME, values, selection, selectionArgs);
    }
}