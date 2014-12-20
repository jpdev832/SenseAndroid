package com.staticvillage.sense.android.client.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.staticvillage.sense.android.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class SensorPropertyAdapter extends ArrayAdapter<Entry<String, String>> implements OnClickListener{
    private Context context; 
    private ArrayList<Entry<String, String>> data;
    
    public SensorPropertyAdapter(Context context, ArrayList<Entry<String, String>> data) {
        super(context, R.layout.list_item_property, data);
        this.context = context;
        this.data = data;
    }

	@Override
	public void add(Entry<String, String> object) {
		data.add(object);
	}

	@Override
	public void clear() {
		data.clear();
	}

	@Override
	public Entry<String, String> getItem(int position) {
		return data.get(position);
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        PropertyItemHolder holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(R.layout.list_item_property, parent, false);
            
            holder = new PropertyItemHolder();
            holder.txtPropertyName = (TextView)row.findViewById(R.id.txtPropertyName);
            holder.txtPropertyIndex = (TextView)row.findViewById(R.id.txtPropertyIndex);
            holder.btnDelete = (ImageButton)row.findViewById(R.id.imgDelete);
            
            row.setTag(holder);
        }
        else
        {
            holder = (PropertyItemHolder)row.getTag();
        }
        
        Entry<String, String> item = data.get(position);
        
        holder.txtPropertyName.setText("Property: " + item.getKey());
        holder.txtPropertyIndex.setText("Index: " + String.valueOf(item.getValue()));
        holder.btnDelete.setTag(item);
        
        holder.btnDelete.setOnClickListener(this);
        
        return row;
    }

	@Override
	public void onClick(View v) {
		Entry<String, String> entry = (Entry<String, String>)v.getTag();
		data.remove(getPosition(entry));
		
		notifyDataSetChanged();
	}
	
	public HashMap<String, Integer> getProperties(){
		HashMap<String, Integer> props = new HashMap<String, Integer>();
		for(Entry<String, String> p : data){
			props.put(p.getKey(), Integer.valueOf(p.getValue()));
		}
		
		return props;
	}
    
	static class PropertyItemHolder
    {
        TextView txtPropertyName;
        TextView txtPropertyIndex;
        ImageButton btnDelete;
    }
}
