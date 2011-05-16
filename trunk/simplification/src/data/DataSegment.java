/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.vividsolutions.jts.geom.Coordinate;
import org.joda.time.LocalDateTime;

/**
 *
 * @author ceikute
 */
public class DataSegment {

    public LocalDateTime dateTimeSt;
    public LocalDateTime dateTimeEn;
    public int routeId;
    public Coordinate pSt;
    public Coordinate pEn;
    public int time0St; // the time of the point if starting time is 0
    public int time0En;

    public DataSegment(int routeId, Coordinate coordinateSt, Coordinate coordinateEn, LocalDateTime timeSt, LocalDateTime timeEn, int time0St, int time0En) {
        this.routeId = routeId;
        this.pSt = coordinateSt;
        this.pEn = coordinateEn;
        this.dateTimeSt = timeSt;
        this.dateTimeEn = timeEn;
        this.time0St = time0St;
        this.time0En = time0En;
    }

    public DataSegment() {
    }

    public String toString() {
        return "[DataSegment: " + routeId + ", " + pSt.toString() + ", " + pEn.toString() + ", "
                + dateTimeSt.toString() + " " + dateTimeEn.toString() + " " + time0St + " " + time0En + "]";
    }
    
}
