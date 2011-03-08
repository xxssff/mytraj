package entity;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Base class for moving object
 * 
 * @author xiaohui
 * 
 */

public class MovingObject {
	public int oid;
	public TimeCoord t_coord;
	public Velocity v;
	public boolean label; // {true=CORE, false=BORDER}
	public int cid; // clusterID; -1 is noise; 0 is unclassified

	public MovingObject() {
		oid = 0;
		t_coord = null;
		v = null;
		label = false;
		cid = 0;
	}

	public MovingObject(int oid, TimeCoord timeCoord, Velocity velocity) {
		this.oid = oid;
		this.t_coord = timeCoord;
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
		return this.t_coord.p.distance(aMo.t_coord.p);
	}

	public String toString() {
		return "[" + oid + " " + "(" + t_coord.p.x + "," + t_coord.p.y + ","
				+ t_coord.t + ") " + v + " " + cid + " " + label+"]";
	}
}
