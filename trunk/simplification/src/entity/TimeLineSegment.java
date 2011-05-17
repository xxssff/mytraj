/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import org.joda.time.Interval;
import org.joda.time.LocalDateTime;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

/**
 * Line segment implementation
 * 
 * @author ceikute
 */
public class TimeLineSegment extends LineSegment {

	public LocalDateTime dateTimeSt;
	public LocalDateTime dateTimeEn;
	public int routeId;

	public int time0St; // the time of the point if starting time is 0
	public int time0En;
	Interval interval;
	public int cid; // clusterID; -1 is noise; 0 is unclassified
	public boolean label; //core or border
	
	public TimeLineSegment(int routeId, Coordinate coordinateSt,
			Coordinate coordinateEn, LocalDateTime timeSt,
			LocalDateTime timeEn, int time0St, int time0En) {
		super(coordinateSt, coordinateEn);
		this.routeId = routeId;
		this.dateTimeSt = timeSt;
		this.dateTimeEn = timeEn;
		this.time0St = time0St;
		this.time0En = time0En;
		this.interval = new Interval(dateTimeSt.toDateTime(),
				dateTimeEn.toDateTime());
		this.cid =0;
	}


	public String toString() {
		return "[DataSegment: " + routeId + ", " + this.p0.toString() + ", "
				+ this.p1.toString() + ", " + dateTimeSt.toString() + " "
				+ dateTimeEn.toString() + " " + time0St + " " + time0En + "]";
	}

	public Interval getInterval() {
		return this.interval;
	}
	

}
