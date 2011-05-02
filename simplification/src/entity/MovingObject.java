package entity;

import org.joda.time.LocalDateTime;
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
	public DataPoint dataPoint; // has location and time
	public boolean label; // {true=CORE, false=BORDER}
	public int cid; // clusterID; -1 is noise; 0 is unclassified
	public LocalDateTime exitTime;

	public MovingObject() {
		oid = 0;
		dataPoint = null;
		label = false;
		cid = 0;
		exitTime = null;
	}

	public MovingObject(int oid, DataPoint dataPoint) {
		this.oid = oid;
		this.dataPoint = dataPoint;
		this.label = false;
		this.cid = 0;
		exitTime = null;
	}

	/**
	 * constructor for testing purpose
	 * 
	 * @param oid
	 * @param time
	 * @param coordinate
	 * @param velocity
	 */
	public MovingObject(int oid, int routeid, int time, Coordinate coordinate,
			Velocity velocity) {
		this.oid = oid;
		this.dataPoint = new DataPoint();
		this.dataPoint.p = coordinate;
		this.dataPoint.routeId = routeid;
		this.dataPoint.time0 = time;
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

	public double getX() {
		return this.dataPoint.p.x;
	}

	public double getY() {
		return this.dataPoint.p.y;
	}

	public double getVx() {
		return this.dataPoint.vx;
	}

	public double getVy() {
		return this.dataPoint.vy;
	}

	public String toString() {
		return "[" + oid + "," + dataPoint.toString() + ")" + " " + cid + " "
				+ label + "]";
	}

	/**
	 * change moving object location
	 * 
	 * @param dataPoint
	 */
	public void setDataPoint(DataPoint dataPoint) {
		this.dataPoint = dataPoint;
	}
}
