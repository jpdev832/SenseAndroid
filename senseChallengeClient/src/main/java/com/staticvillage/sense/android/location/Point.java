package com.staticvillage.sense.android.location;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.staticvillage.sense.android.data.JSONDBObject;

public class Point implements JSONDBObject<Point>{
	public static final String KEY_POINT = "point";
	
	private Coordinate coordinates;

	/**
	 * Get the coordinates associate with this point
	 * @return coordinates
	 */
	public Coordinate getCoordinates() {
		return coordinates;
	}

	/**
	 * Set coordinates associated with this point
	 * @param coordinates
	 */
	public void setCoordinates(Coordinate coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public DBObject toDBObject() {
		DBObject obj = new BasicDBObject();
		obj.put("type", "point");
		
		if(coordinates == null)
			return null;
		
		double[] c = coordinates.getCoordinates();
		if(c == null)
			return null;
		
		obj.put("coordinates", coordinates.getCoordinates());
		
		return obj;
	}

	@Override
	public Point fromDBObject(DBObject obj) {
		BasicDBList coords = (BasicDBList)obj.get(Coordinate.KEY_COORDINATE);
		
		Coordinate coord = new Coordinate();
		coord.setLatitude((Double)coords.get(0));
		coord.setLongitude((Double)coords.get(1));
		coord.setAltitude((Double)coords.get(2));
		
		Point point = new Point();
		point.setCoordinates(coord);
		
		return point;
	}
	
}
