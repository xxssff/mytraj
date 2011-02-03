/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package geom;

import com.vividsolutions.jts.geom.Coordinate;
import entity.TimeCoord;

/**
 *
 * @author ceikute
 */
public class Area {

    /**
     *
     * @param p1
     * @param p2
     * @return Euclidean distance between two points
     */
    private double getEuclideanDist(Coordinate p1, Coordinate p2) {
        double dist = Math.sqrt(Math.pow((p1.x - p2.x), 2) + Math.pow((p1.y - p2.y), 2));
        return dist;
    }

    /**
     *
     * @param p1 - segment point1
     * @param p2 - segment point2
     * @param p - point
     * @return Orthogonal distance from point to segment
     */
    private double getOrthoDist(Coordinate p1, Coordinate p2, Coordinate p) {
        double area = Math.abs(0.5 * (p1.x * p2.y + p2.x * p.y + p.x * p1.y - p2.x * p1.y - p.x * p2.y - p1.x * p.y));
        double bottom = getEuclideanDist(p1, p2);
        double dist = area / bottom * 2;

        return dist;
    }

    /**
     *
     * @param l1_p1 - segment1 point1
     * @param l1_p2 - segment1 point2
     * @param l2_p1 - segment2 point1
     * @param l2_p2 - segment2 point2
     * @return area of trapezoid
     */
    private double getTrapezoidArea(Coordinate l1_p1, Coordinate l1_p2, Coordinate l2_p1, Coordinate l2_p2) {
        double a = getEuclideanDist(l1_p1, l2_p1);
        double b = getEuclideanDist(l1_p2, l2_p2);

        double h = getOrthoDist(l1_p2, l2_p2, l2_p1);
        System.out.println(a + " " + b + " " + h);
        double area = h * (a + b) / 2;

        return area;
    }

    private TimeCoord getTmpTimeCoord(TimeCoord l1_p1, TimeCoord l1_p2, TimeCoord l2_p1, TimeCoord l2_p2) {
        double timeBetween;
        TimeCoord line_p1;
        TimeCoord line_p2;

        //check which timepoint is earlier
        if (l1_p2.t < l2_p2.t) {
            System.out.println("first");
            timeBetween = l1_p2.t;
            line_p1 = l2_p1;
            line_p2 = l2_p2;
        }
        else {
            System.out.println("second");
            timeBetween = l2_p2.t;
            line_p1 = l1_p1;
            line_p2 = l1_p2;
        }

        double tau = (timeBetween - line_p1.t) / (line_p2.t - line_p1.t);
        System.out.println("tau: " + tau + " " + timeBetween);
        double x = tau * (line_p2.p.x - line_p1.p.x) + line_p1.p.x;
        double y = tau * (line_p2.p.y - line_p1.p.y) + line_p1.p.y;
        System.out.println(x + " " + y);
        return new TimeCoord(new Coordinate(x, y), timeBetween);
    }

    public static void main(String[] args) {
        Area a = new Area();

        Coordinate l1_p1 = new Coordinate(0.0, 0.0);
        Coordinate l1_p2 = new Coordinate(1.0, 2.0);

        Coordinate l2_p1 = new Coordinate(4.0, 0.0);
        Coordinate l2_p2 = new Coordinate(3.0, 2.0);

        double area = a.getTrapezoidArea(l1_p1, l1_p2, l2_p1, l2_p2);
        System.out.println("area: " + area);
        
        Coordinate ll1_p1 = new Coordinate(0.0, 0.0);
        Coordinate ll1_p2 = new Coordinate(4.0, 1.0);

        Coordinate ll2_p1 = new Coordinate(0.0, 2.0);
        Coordinate ll2_p2 = new Coordinate(3.0, 3.0);

        TimeCoord tc = a.getTmpTimeCoord(new TimeCoord(ll1_p1, 0.0), new TimeCoord(ll1_p2, 4.0), new TimeCoord(ll2_p1, 0.0), new TimeCoord(ll2_p2, 3.0));
        System.out.println(tc.p.x + " " + tc.p.y + " " + tc.t);
    }
}
