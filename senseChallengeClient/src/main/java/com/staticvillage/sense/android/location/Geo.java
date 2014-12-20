package com.staticvillage.sense.android.location;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.staticvillage.sense.android.data.JSONDBObject;

public class Geo implements JSONDBObject<Geo>{
	private Point point;

	/**
	 * Get point (coordinates)
	 * @return Point containing lat, lng, and alt
	 */
	public Point getPoint() {
		return point;
	}

	/**
	 * Set point
	 * @param point
	 */
	public void setPoint(Point point) {
		this.point = point;
	}

	@Override
	public DBObject toDBObject() {
		return point.toDBObject();
	}

	@Override
	public Geo fromDBObject(DBObject obj) {
		try{
			Geo geo = new Geo();
			geo.point = new Point().fromDBObject(obj);
			
			return geo;
		}catch(Exception e){}
		
		return null;
	}
}
