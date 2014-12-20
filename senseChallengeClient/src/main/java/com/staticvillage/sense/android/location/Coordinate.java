package com.staticvillage.sense.android.location;

import com.mongodb.DBObject;
import com.staticvillage.sense.android.data.JSONDBObject;

public class Coordinate implements JSONDBObject<Coordinate>{
	public static final String KEY_COORDINATE 	= "coordinates";
	public static final String KEY_LATITUDE 	= "latitude";
	public static final String KEY_LONGITUDE 	= "longitude";
	public static final String KEY_ALTITUDE 	= "altitude";
	
	private double longitude;
	private double latitude;
	private double altitude;
	
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getAltitude() {
		return altitude;
	}
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
	
	/**
	 * Output coordinates as an array [ lat, lng, alt ]
	 * @return coordinate array [lat,lng,alt]
	 */
	public double[] getCoordinates(){
		return new double[]{ latitude, longitude, altitude };
	}
	
	@Override
	public DBObject toDBObject() {
		return null;
	}
	@Override
	public Coordinate fromDBObject(DBObject obj) {
		Coordinate coordinates = new Coordinate();
		coordinates.setLatitude((Double)obj.get(Coordinate.KEY_LATITUDE));
		coordinates.setLongitude((Double)obj.get(Coordinate.KEY_LONGITUDE));
		coordinates.setAltitude((Double)obj.get(Coordinate.KEY_ALTITUDE));
		
		return coordinates;
	}
}
