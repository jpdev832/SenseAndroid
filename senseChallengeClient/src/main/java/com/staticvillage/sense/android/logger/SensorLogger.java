package com.staticvillage.sense.android.logger;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Observable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.staticvillage.sense.android.SenseClient;
import com.staticvillage.sense.android.SenseListener;
import com.staticvillage.sense.android.data.SensorData;
import com.staticvillage.sense.android.data.SensorInfo;
import com.staticvillage.sense.android.data.SensorInfoManager;
import com.staticvillage.sense.android.exception.NetworkUnavailableException;
import com.staticvillage.sense.android.exception.RunnerInitializationException;
import com.staticvillage.sense.android.location.Coordinate;
import com.staticvillage.sense.android.location.Geo;
import com.staticvillage.sense.android.location.Point;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SensorLogger extends Observable implements SensorEventListener,LocationListener, SenseListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String SENSOR_FILENAME = "senor_info";

    private SensorManager sensorManager;
    private SenseClient client;
    private int registeredSensors;
    private String tag;
    private Context context;
    private HashMap<String, SensorInfo> sensorInfo;
    private boolean enableLocation;
    private GoogleApiClient mGoogleAPIClient;
    //private LocationClient locationClient;
    private Geo lastLocation;
    private HashMap<String, Integer> addedSensors;

    public SensorLogger(Context context, String appId, int bufferSize){
        this.context = context;
        this.sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        this.registeredSensors = 0;

        client = new SenseClient(this, appId, bufferSize);
        sensorInfo = new HashMap<String, SensorInfo>();
        addedSensors = new HashMap<String, Integer>();

        mGoogleAPIClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize logger
     *
     * @throws IOException Exception when loading sensor info
     */
    private void init() throws IOException{
        File file = new File(context.getFilesDir(), SENSOR_FILENAME);
        if(!file.exists())
            SensorInfoManager.initBaseData(context);

        SensorInfo[] sensors = SensorInfoManager.loadSensorInfo(context);
        for(SensorInfo info : sensors){
            sensorInfo.put(info.name, info);
        }

        //locationClient = new LocationClient(context, this, this);
        lastLocation = new Geo();
        lastLocation.setPoint(new Point());

        mGoogleAPIClient.connect();
    }

    /**
     * Capture location with sensor data
     */
    public void enableLocation(){
        enableLocation = true;
    }

    /**
     * Disable location capture
     */
    public void disableLocation(){
        enableLocation = false;
    }

    /**
     * Add sensor to be captured
     *
     * @param name sensor name
     */
    public void addSensor(String name){
        registeredSensors++;

        addedSensors.put(name, sensorInfo.get(name).typeValue);
    }

    /**
     * Register for android sensor events
     */
    private void registerSensors(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        for(int typeVal : addedSensors.values()){
            Sensor sensor = sensorManager.getDefaultSensor(typeVal);
            sensorManager.registerListener(this, sensor, prefs.getInt("sensor_speed_values", 3));
        }
    }

    /**
     * Remove sensor from capture list
     *
     * @param name sensor name
     */
    public void removeSensor(String name){
        registeredSensors--;

        Sensor sensor = sensorManager.getDefaultSensor(sensorInfo.get(name).typeValue);
        sensorManager.unregisterListener(this, sensor);
        addedSensors.remove(name);
    }

    /**
     * remove all sensors from capture list
     */
    public void removeAllSensors(){
        sensorManager.unregisterListener(this);
        addedSensors.clear();

        registeredSensors = 0;
    }

    /**
     * Get session identifier associated with capture
     * @return session identifier
     */
    public String getSessionId(){
        return client.getSessionId();
    }

    /**
     * Start Capturing Sensor data and storing in MongoDB database
     *
     * @param connectionString MongoDB Connection String
     * @param db MongoDB Database
     * @param tag capture tag
     * @return Session identifier
     * @throws NetworkUnavailableException Unable to connect
     */
    public String start(String connectionString, String db, String tag) throws NetworkUnavailableException{
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(!isConnected)
            throw new NetworkUnavailableException("Please check network connection");

        this.tag = tag;

        if(enableLocation) {
            LocationRequest mLocationRequest = LocationRequest.create();
            // Use high accuracy
            mLocationRequest.setPriority(
                    LocationRequest.PRIORITY_HIGH_ACCURACY);
            // Set the update interval to 5 seconds
            mLocationRequest.setInterval(5000);
            // Set the fastest update interval to 1 second
            mLocationRequest.setFastestInterval(1000);
            PendingResult<Status> result = LocationServices.FusedLocationApi.
                    requestLocationUpdates(mGoogleAPIClient, mLocationRequest, this);
        }

        if(!client.isConnected())
            return client.connect(connectionString, db);

        return null;
    }

    /**
     * Start capturing Sensor data and storing locally in file
     *
     * @param file file to store captured data
     * @param tag capture tag
     * @return Session identifier
     */
    public String start(File file, String tag) {
        this.tag = tag;

        if(enableLocation){
            LocationRequest mLocationRequest = LocationRequest.create();
            // Use high accuracy
            mLocationRequest.setPriority(
                    LocationRequest.PRIORITY_HIGH_ACCURACY);
            // Set the update interval to 5 seconds
            mLocationRequest.setInterval(5000);
            // Set the fastest update interval to 1 second
            mLocationRequest.setFastestInterval(1000);
            PendingResult<Status> result = LocationServices.FusedLocationApi.
                    requestLocationUpdates(mGoogleAPIClient, mLocationRequest, this);
        }

        return client.connect(file);
    }

    /**
     * Stop capturing
     */
    public void stop(){
        this.tag = "";
        client.close();

        if(enableLocation && mGoogleAPIClient != null && mGoogleAPIClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleAPIClient, this);
        }
    }

    /**
     * Get sensor name from type value
     *
     * @param typeValue android sensor type value
     * @return Sensor Name
     */
    public String getSensorName(int typeValue){
        String name = null;

        for(Entry<String, SensorInfo> pair : sensorInfo.entrySet()){
            if(pair.getValue().typeValue == typeValue){
                name = pair.getKey();
                break;
            }
        }

        return name;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String sensorName = getSensorName(event.sensor.getType());

        try {
            SensorData sensorData = new SensorData(sensorName);
            sensorData.app_id = client.getAppId();
            sensorData.session_id = client.getSessionId();
            sensorData.timestamp = System.currentTimeMillis();
            sensorData.geo = lastLocation;

            SensorInfo info = sensorInfo.get(sensorName);
            for(Entry<String, Integer> pair: info.properties.entrySet()){
                sensorData.addProperty(pair.getKey(), 0, pair.getValue());
            }

            sensorData.initFromSensorEvent(event);

            client.report(sensorData, tag);
            setChanged();
            notifyObservers(sensorData);
        } catch (RunnerInitializationException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("sense", "connection failed");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("sense", "Connected for location");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("sense", "update location");
        Coordinate coord = new Coordinate();
        coord.setAltitude(location.getAltitude());
        coord.setLatitude(location.getLatitude());
        coord.setLongitude(location.getLongitude());

        lastLocation.getPoint().setCoordinates(coord);
    }

    @Override
    public void onConnect(String appId, String sessionId) {
        Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();

        if(registeredSensors < 1){
            Toast.makeText(context, "No sensors registered", Toast.LENGTH_SHORT).show();
            stop();
            return;
        }

        registerSensors();
    }

    @Override
    public void onDisconnect() {
        Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectError(String message) {
        stop();
        removeAllSensors();
        Toast.makeText(context, "Error Connecting: "+message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataUploaded(String sessionId) {
        Toast.makeText(context, "Data Recorded", Toast.LENGTH_SHORT).show();
    }
}
