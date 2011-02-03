/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity;

import com.vividsolutions.jts.geom.Coordinate;

/**
 *
 * @author ceikute
 */
public class TimeCoord {
    public Coordinate p;
    public double t;

    public TimeCoord(Coordinate p, double t) {
        this.p = p;
        this.t = t;
    }
}
