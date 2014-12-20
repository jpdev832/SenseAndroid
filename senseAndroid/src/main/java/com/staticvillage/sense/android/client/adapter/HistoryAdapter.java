package com.staticvillage.sense.android.client.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.staticvillage.sense.android.R;
import com.staticvillage.sense.android.client.data.HistoryDetail;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class HistoryAdapter extends ArrayAdapter<HistoryDetail>{
    private Context context; 
    private ArrayList<HistoryDetail> data;
    private SimpleDateFormat format;
    
    public HistoryAdapter(Context context, ArrayList<HistoryDetail> data) {
        super(context, R.layout.list_item_history, data);
        this.context = context;
        this.data = data;
        
        format = new SimpleDateFormat("EEE, MMM d h:mm:ss a");
    }

	@Override
	public void add(HistoryDetail object) {
		data.add(object);
	}

	@Override
	public void clear() {
		data.clear();
	}

	@Override
	public HistoryDetail getItem(int position) {
		return data.get(position);
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
            
            row.setTag(holder);
        }
        else
        {
            holder = (HistoryItemHolder)row.getTag();
        }
        
        HistoryDetail item = data.get(position);
        Date date = new Date(item.timestamp);
        
        holder.txtName.setText(item.name);
        holder.txtTag.setText(item.tag);
        //holder.txtSession.setText(item.session);
        holder.txtDate.setText(format.format(date));
        
        return row;
    }
    
    static class HistoryItemHolder
    {
        TextView txtName;
        TextView txtTag;
        //TextView txtSession;
        TextView txtDate;
    }
    
}
