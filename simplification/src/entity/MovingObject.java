package entity;

import org.joda.time.LocalTime;

import com.vividsolutions.jts.geom.Coordinate;

import data.DataPoint;

/**
 * Base class for moving object
 * 
 * @author xiaohui
 * 
 */

public class MovingObject {
	public int oid;
	public DataPoint dataPoint; //has location and time
	public Velocity v;
	public boolean label; // {true=CORE, false=BORDER}
	public int cid; // clusterID; -1 is noise; 0 is unclassified
	public LocalTime exitTime;

	public MovingObject() {
		oid = 0;
		dataPoint = null;
		v = null;
		label = false;
		cid = 0;
	}

	public MovingObject(int oid, DataPoint dataPoint, Velocity velocity) {
		this.oid = oid;
		this.dataPoint = dataPoint;
		this.v = velocity;
		this.label = false;
		this.cid = 0;
	}

	/**
	 * constructor for testing purpose
	 * 
	 * @param oid
	 * @param time
	 * @param coordinate
	 * @param velocity
	 */
	public MovingObject(int oid, int routeid, int time,
			Coordinate coordinate, Velocity velocity) {
		this.oid = oid;
		this.dataPoint = new DataPoint();
		this.dataPoint.p = coordinate;
		this.dataPoint.routeId = routeid;
		this.dataPoint.time0 = time;
		this.v = velocity;
		this.label = false;
		this.cid = 0;
	}

	/**
	 * Caution: time is not taken care in this method. <br>
	 * caller should make sure time is same
	 * 
	 * @param aMo
	 * @return distance
	 */
	public double distance(MovingObject aMo) {
		return this.dataPoint.p.distance(aMo.dataPoint.p);
	}

	public double getX(){
		return this.dataPoint.p.x;
	}
	
	public double getY(){
		return this.dataPoint.p.y;
	}
	
	public String toString() {
		return "[" + oid + " " + "(" + dataPoint.p.x + "," + dataPoint.p.y
				+ "," + dataPoint.toString() + ") " + v + " " + cid + " "
				+ label + "]";
	}
}
