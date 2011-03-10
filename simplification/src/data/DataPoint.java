/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package data;

import com.vividsolutions.jts.geom.Coordinate;

/**
 *
 * @author ceikute
 */
public class DataPoint {
    public Coordinate p;
    public String time;
    public String dateTime;
    public int time0;

    public DataPoint() {
    }

    public DataPoint(Coordinate p, String time, String dateTime, int time0) {
        this.p = p;
        this.time = time;
        this.dateTime = dateTime;
        this.time0 = time0;
    }
}
