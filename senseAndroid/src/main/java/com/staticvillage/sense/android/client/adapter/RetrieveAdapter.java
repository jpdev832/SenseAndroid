package com.staticvillage.sense.android.client.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.staticvillage.sense.android.R;
import com.staticvillage.sense.android.client.data.RetrieveDetail;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RetrieveAdapter extends ArrayAdapter<RetrieveDetail>{
    private Context context; 
    private ArrayList<RetrieveDetail> data;
    private SimpleDateFormat format;
    
    public RetrieveAdapter(Context context, ArrayList<RetrieveDetail> data) {
        super(context, R.layout.list_item_retrieve, data);
        this.context = context;
        this.data = data;
        
        format = new SimpleDateFormat("EEE, MMM d h:mm:ss a");
    }

	@Override
	public void add(RetrieveDetail object) {
		data.add(object);
	}

	@Override
	public void clear() {
		data.clear();
	}

	@Override
	public RetrieveDetail getItem(int position) {
		return data.get(position);
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RetrieveItemHolder holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(R.layout.list_item_retrieve, parent, false);
            
            holder = new RetrieveItemHolder();
            holder.txtDate = (TextView)row.findViewById(R.id.txtRetDate);
            holder.txtSession = (TextView)row.findViewById(R.id.txtRetSession);
            
            row.setTag(holder);
        }
        else
        {
            holder = (RetrieveItemHolder)row.getTag();
        }
        
        RetrieveDetail item = data.get(position);
        Date date = new Date(item.timestamp);
        holder.txtDate.setText(format.format(date));
        holder.txtSession.setText(item.session);
        
        return row;
    }
    
    static class RetrieveItemHolder
    {
        TextView txtDate;
        TextView txtSession;
    }
    
}
