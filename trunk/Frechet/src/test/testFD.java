package test;

import java.awt.Point;
import java.awt.geom.Point2D;

import geom.FrechetDistance;

public class testFD {
	public static void main(String[] args) {
		Point2D.Double p11 = new Point2D.Double(5, 5);
		Point2D.Double p12 = new Point2D.Double(9, 10);
		Point2D.Double p13 = new Point2D.Double(17, 10);
		Point2D.Double p14 = new Point2D.Double(32, 15);
		Point2D[] p = new Point2D[]{p11, p12, p13, p14};
		
		Point2D.Double p21 = new Point2D.Double(15, 0);
		Point2D.Double p22 = new Point2D.Double(12, 5);
		Point2D.Double p23 = new Point2D.Double(18, 9);
		Point2D.Double p24 = new Point2D.Double(20, 12);
		Point2D[] q = new Point2D[]{p21, p22, p23, p24};
		
		FrechetDistance fd = new FrechetDistance(p, q);
//		Double[] values = fd.computeCriticalValues();
//		System.out.println(values.length);
		
//		fd.isFrechet(5);
//		System.out.println(fd.a[0][0]);
//		System.out.println(fd.b[0][0]);
		
		System.out.println(fd.computeFrechetDistance());
	}
}
