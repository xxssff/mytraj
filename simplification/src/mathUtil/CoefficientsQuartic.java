package mathUtil;

/**
 * Helper class to solve the problem when two moving objects are away from each other
 * for at least distance.
 * @author xiaohui
 */
import java.awt.geom.Point2D;
import java.util.ArrayList;

import data.DataPoint;
import entity.Velocity;

public class CoefficientsQuartic {

	double x1, y1, v1x, v1y, x2, y2, v2x, v2y, distance;

	double a, b, c;

	/**
	 * 
	 * @param dp1
	 * @param dp2
	 */
	public CoefficientsQuartic(DataPoint dp1, DataPoint dp2, double distance) {
		this.x1 = dp1.p.x;
		this.y1 = dp1.p.y;
		this.v1x = dp1.vx;
		this.v1y = dp1.vy;
		this.x2 = dp2.p.x;
		this.y2 = dp2.p.y;
		this.v2x = dp2.vx;
		this.v2y = dp2.vy;
		this.distance = distance;

		this.a = (v1x - v2x) * (v1x - v2x) + (v1y - v2y) * (v1y - v2y);
		this.b = 2 * ((x1 - x2) * (v1x - v2x) + (y1 - y2) * (v1y - v2y));
		this.c = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) - distance
				* distance;

	}

	/**
	 * @param a
	 * @param b
	 * @param c
	 * @return null if no root; otherwise an ArrayList which contains the
	 *         root(s)
	 */
	public static ArrayList<Double> quadratics(double a, double b, double c) {
		ArrayList<Double> roots = new ArrayList<Double>();
		if (a == 0) {
			if (b == 0) {
				return null;
			} else {
				roots.add(new Double(-1 * c / b));
				return roots;
			}
		} else {
			double delta = (b * b - 4 * a * c);
			if (delta > 0) {
				double root1 = (-1 * b + Math.sqrt(delta)) * 1.01
						/ (2 * a * 1.01);
				double root2 = (-1 * b - Math.sqrt(delta)) * 1.01
						/ (2 * a * 1.01);
				roots.add(new Double(root1));
				roots.add(new Double(root2));
				return roots;
			} else if (delta == 0) {
				double root = (-1 * b * 1.01 / (2 * a * 1.01));
				roots.add(new Double(root));
				return roots;
			} else {
				return null;
			}
		}
	}

	/**
	 * 
	 * @return roots
	 */

	public ArrayList<Double> solve() {
		ArrayList<Double> roots = null;
		if (a == 0) {
			if (b == 0) {
				return null;
			} else {
				roots = new ArrayList<Double>(1);
				roots.add(new Double(-1 * c / b));
				return roots;
			}
		} else {
			double delta = (b * b - 4 * a * c);
			if (delta > 0) {
				roots = new ArrayList<Double>(2);
				double root1 = (-1 * b + Math.sqrt(delta)) * 1.01
						/ (2 * a * 1.01);
				double root2 = (-1 * b - Math.sqrt(delta)) * 1.01
						/ (2 * a * 1.01);
				roots.add(new Double(root1));
				roots.add(new Double(root2));
			} else if (delta == 0) {
				roots = new ArrayList<Double>(1);
				double root = (-1 * b * 1.01 / (2 * a * 1.01));
				roots.add(new Double(root));
			}
			return roots;
		}

	}
}
