package com.staticvillage.sense.android.client;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by joelparrish on 12/11/14.
 */
public class SensorEditDialogFragment extends DialogFragment {
    private String[] AutoFillItems = new String[]{
        "Accelerometer",
        "Gravity",
        "Gyroscope",
        "Magnetic Field",
        "Linear Acceleration",
    };

    private HashMap<Integer, ArrayList<Pair>> map;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        map = new HashMap<Integer, ArrayList<Pair>>();
        init();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("AutoFill")
                .setItems(AutoFillItems, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ((SensorEditActivity)getActivity()).autoFill(map.get(which));
                    }
                });
        return builder.create();
    }

    protected void init(){
        ArrayList<Pair> threeAxis = new ArrayList<Pair>();
        threeAxis.add(new Pair("X", "0"));
        threeAxis.add(new Pair("Y", "1"));
        threeAxis.add(new Pair("Z", "2"));

        map.put(0, threeAxis);
        map.put(1, threeAxis);
        map.put(2, threeAxis);
        map.put(3, threeAxis);
        map.put(4, threeAxis);
    }
}
