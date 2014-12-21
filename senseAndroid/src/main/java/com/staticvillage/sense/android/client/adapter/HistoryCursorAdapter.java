package com.staticvillage.sense.android.client.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.staticvillage.sense.android.R;
import com.staticvillage.sense.android.SenseClient;
import com.staticvillage.sense.android.client.AccountActivity;
import com.staticvillage.sense.android.client.data.HistoryDetail;
import com.staticvillage.sense.android.client.data.SenseDB;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Image;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class HistoryCursorAdapter extends SimpleCursorAdapter {
    private Context context; 
    private Cursor data;
    private SimpleDateFormat format;
    private OnClickListener listener;
    private ArrayList<ImageView> uploadViews;
    private boolean uploadEnabled;
    
    public HistoryCursorAdapter(Context context, OnClickListener listener, Cursor data) {
        super(context, 
        		R.layout.list_item_history, 
        		data, 
        		new String[]{
					SenseDB.KEY_NAME,
					SenseDB.KEY_TAG,
					SenseDB.KEY_SESSION,
					SenseDB.KEY_TIMESTAMP
				}, 
				new int[]{
					R.id.txtHistName,
					R.id.txtHistTag,
					R.id.txtHistSession,
					R.id.txtHistTime
				}, 
        		CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        this.context = context;
        this.listener = listener;
        this.data = data;
        this.uploadEnabled = false;
        
        format = new SimpleDateFormat("EEE, MMM d h:mm:ss a");
        uploadViews = new ArrayList<ImageView>();
    }
    
    public void enableUpload(){
    	uploadEnabled = true;
    	
    	for(View v : uploadViews){
    		v.setEnabled(true);
    		((ImageView)v).setImageResource(R.drawable.ic_file_upload_black_48dp);
    	}
    }
    
    public void update(String sessionId) {
    	ArrayList<ImageView> views = new ArrayList<ImageView>();
		for(int i=0; i<uploadViews.size(); i++){
			if(((String)uploadViews.get(i).getTag()).equals(sessionId)){
				views.add((ImageView)uploadViews.get(i));
				uploadViews.get(i).setImageResource(R.drawable.ic_cloud_done_black_48dp);
			}
    	}
		
		for(ImageView view : views)
			uploadViews.remove(view);
	}

	@Override
	public Cursor swapCursor(Cursor c) {
		data = c;
		return data;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        HistoryItemHolder holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(R.layout.list_item_history, parent, false);
            
            holder = new HistoryItemHolder();
            holder.txtName = (TextView)row.findViewById(R.id.txtHistName);
            holder.txtTag = (TextView)row.findViewById(R.id.txtHistTag);
            //holder.txtSession = (TextView)row.findViewById(R.id.txtHistSession);
            holder.txtDate = (TextView)row.findViewById(R.id.txtHistTime);
            holder.imgUpload = (ImageView)row.findViewById(R.id.imgHistUpload);
            
            row.setTag(holder);
        }
        else
        {
            holder = (HistoryItemHolder)row.getTag();
        }
        
        data.moveToPosition(position);
        Date date = new Date(data.getLong(data.getColumnIndex(SenseDB.KEY_TIMESTAMP)));
        int uploaded = data.getInt(data.getColumnIndex(SenseDB.KEY_UPLOADED));
        
        holder.txtName.setText(data.getString(data.getColumnIndex(SenseDB.KEY_NAME)));
        holder.txtTag.setText(data.getString(data.getColumnIndex(SenseDB.KEY_TAG)));
        //holder.txtSession.setText(data.getString(data.getColumnIndex(SenseDB.KEY_SESSION)));
        holder.txtDate.setText(format.format(date));
        
        if(uploaded == 0){
        	if(!uploadEnabled)
        		holder.imgUpload.setImageResource(R.drawable.ic_file_upload_grey600_48dp);
        	else
        		holder.imgUpload.setImageResource(R.drawable.ic_file_upload_black_48dp);
        	holder.imgUpload.setTag(data.getString(data.getColumnIndex(SenseDB.KEY_SESSION)));
        	holder.imgUpload.setEnabled(false);
        	holder.imgUpload.setOnClickListener(listener);
        	uploadViews.add(holder.imgUpload);
        }
        
        return row;
    }
    
    public static class HistoryItemHolder
    {
        public TextView txtName;
        public TextView txtTag;
        //TextView txtSession;
        public TextView txtDate;
        public ImageView imgUpload;
    }
    
}
