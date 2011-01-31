package geom;

import java.awt.Point;
import java.awt.geom.Point2D;

import entity.TLocation;

/**
 * line seg with time
 * 
 * @author xiaohui
 * 
 */
public class LineSegment {

	double x0, y0, t0; // starting points
	double x1, y1, t1; // ending points
	double[] velocity; // for the segment

	public LineSegment(double x0, double y0, double t0, double x1, double y1,
			double t1) {
		this.x0 = x0;
		this.x1 = x1;
		this.y0 = y0;
		this.y1 = y1;
		this.t0 = t0;
		this.t1 = t1;
		this.velocity = getVelocity(x0, y0, t0, x1, y1, t1);
	}

	public double[] getVelocity(double x0, double y0, double t0, double x1,
			double y1, double t1) {
		double[] res = new double[2];

		double period = t1 - t0;
		res[0] = (x1 - x0) / period;
		res[0] = (y1 - y0) / period;

		return res;
	}

	public TLocation getLocation(double x0, double y0, double t0, double x1,
			double y1, double t1, double midTime) {
		if (midTime < t0 || midTime >= t1) {
			System.err
					.println("getTLocation: midTime should fall in time of loc1 and loc2");
			;
			System.exit(0);
		}
		double r = (midTime - t0) / (t1 - t0);

		return new TLocation(x0 + r * (x1 - x0), y0 + r * (y1 - y0), midTime);
	}

	/**
	 * 
	 * @return true if this seg's time interval covers argument's time interval
	 */

	public boolean isCover(LineSegment ls) {
		if (this.t0 < ls.t0 && this.t1 > ls.t1)
			return true;
		return false;
	}

	/**
	 * y = ax + b
	 * 
	 * @param x
	 *            coordinates of the two end points
	 * @param y
	 *            coordinates of the two end points
	 * @return values of a and b
	 */
	public static double[] getCoefficients(double[] x, double[] y) {
		double[] ret = new double[2];
		if (x[0] - x[1] == 0) {
			// vertical line
			ret[0] = 1;
			ret[1] = 0 - y[0];
		} else {
			ret[0] = (y[1] - y[0]) / (x[1] - x[0]);
			ret[1] = y[0] - x[0] * ret[0];
		}
		return ret;
	}

	/**
	 * 
	 * @param ls
	 * @return integral distance
	 */
	public double distance(LineSegment ls) {
		// find the time interval during which both don't change velocity
		double t_lower = 0, t_upper = 0;
		if (this.t0 < ls.t0) {
			t_lower = ls.t0;
		} else {
			t_lower = this.t0;
		}
		if (this.t1 < ls.t1) {
			t_upper = this.t1;
		} else {
			t_upper = ls.t1;
		}

		return computeIntegral(new double[] { this.x0, this.y0 },
				this.velocity, new double[] { ls.x0, ls.y0 }, ls.velocity,
				t_lower, t_upper);
	}

	/**
	 * 
	 * 
	 * Compute the integral of two line segments with velocity
	 * 
	 * @param location1
	 *            , first line seg ref location
	 * @param v1
	 *            , mo velocity
	 * @param location2
	 *            , second line seg ref location
	 * @param v2
	 *            , mo velocity
	 * @param t_lower
	 *            , upper limit of t
	 * @param t_upper
	 *            , lower limit of t
	 * @return a*t^3/3 + b*t^2/2 + c*t |_{t_lower}^{t_upper}
	 */
	public static double computeIntegral(double[] location1, double[] v1,
			double[] location2, double[] v2, double t_lower, double t_upper) {

		double a = (v1[0] - v2[0]) * (v1[0] - v2[0]) + (v1[1] - v2[1])
				* (v1[1] - v2[1]);
		double b = 2 * (location1[0] - location2[0]) * (v1[0] - v2[0])
				+ (location1[1] - location2[1]) * (v1[1] - v2[1]);
		double c = Math.pow((location1[0] - location2[0]), 2)
				+ Math.pow((location1[1] - location2[1]), 2);

		return (a * Math.pow(t_upper, 3) / 3 + b * Math.pow(t_upper, 2) / 2 + c
				* t_upper)
				- (a * Math.pow(t_lower, 3) / 3 + b * Math.pow(t_lower, 2) / 2 + c
						* t_lower);
	}

	public static void main(String[] args) {
		double[] l1 = { 1, 1 };
		double[] v1 = { 1, 1 };
		double[] v2 = { 1, -1 };
		System.out.println(computeIntegral(l1, v1, l1, v2, 4, 5));
	}

	public boolean isOverlap(LineSegment qls) {
		if(this.t0 > qls.t1 || this.t1<qls.t0){
			return false;
		}
		return true;
	}
}
