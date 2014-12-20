package com.staticvillage.sense.android.client.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.staticvillage.sense.android.R;
import com.staticvillage.sense.android.client.data.SensorDetail;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class SensorAdapter extends ArrayAdapter<SensorDetail> implements OnClickListener{
    private Context context;
    private ArrayList<SensorDetail> data;
    private ArrayList<String> selected;
    
    public SensorAdapter(Context context, ArrayList<SensorDetail> data) {
        super(context, R.layout.list_item_capture, data);
        this.context = context;
        this.data = data;

        selected = new ArrayList<String>(data.size());
    }
    
    public String[] getEnabled(){
    	return selected.toArray(new String[]{});
    }

	@Override
	public void add(SensorDetail object) {
		data.add(object);
	}

	@Override
	public void clear() {
		data.clear();
	}

	@Override
	public SensorDetail getItem(int position) {
		return data.get(position);
	}

	@Override
	public boolean isEnabled(int position) {
		return super.isEnabled(position);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SensorItemHolder holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(R.layout.list_item_capture, parent, false);
            
            holder = new SensorItemHolder();
            holder.txtName = (TextView)row.findViewById(R.id.txtCapName);
            holder.txtSummary = (TextView)row.findViewById(R.id.txtCapSum);
            holder.chkUse = (CheckBox)row.findViewById(R.id.chkCapUse);
            
            row.setTag(holder);
        }
        else
        {
            holder = (SensorItemHolder)row.getTag();
        }
        
        SensorDetail item = data.get(position);
        
        holder.txtName.setText(item.name);
        holder.txtSummary.setText(item.summary);
        holder.chkUse.setChecked(item.checked);
        
        row.setOnClickListener(this);
        
        return row;
    }
    
    public class SensorItemHolder
    {
        TextView txtName;
        TextView txtSummary;
        CheckBox chkUse;
    }

	@Override
	public void onClick(View v) {
		SensorItemHolder item = (SensorItemHolder)v.getTag();
        boolean state = !item.chkUse.isChecked();
        String name = item.txtName.getText().toString();

        item.chkUse.setChecked(state);

	    if(state){
            selected.add(name);
        }else{
            int index = -1;
            for(int i=0;i<selected.size();i++) {
                if (selected.get(i).equals(name)) {
                    index = i;
                    break;
                }
            }

            if(index > -1)
                selected.remove(index);
        }

	    Log.d("sense", String.format("name: %s, Checked: %b", name, item.chkUse.isChecked()));
	}
    
}
