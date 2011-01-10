package geom;

import java.awt.Point;
import java.awt.geom.Point2D;

public class LineFunction {

	/**
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

	public static final Point2D closestPointonLine(float lx1, float ly1,
			float lx2, float ly2, float x0, float y0) {
		float A1 = ly2 - ly1;
		float B1 = lx1 - lx2;
		double C1 = (ly2 - ly1) * lx1 + (lx1 - lx2) * ly1;
		double C2 = -B1 * x0 + A1 * y0;
		double det = A1 * A1 - -B1 * B1;
		double cx = 0;
		double cy = 0;
		if (det != 0) {
			cx = (float) ((A1 * C1 - B1 * C2) / det);
			cy = (float) ((A1 * C2 - -B1 * C1) / det);
		} else {
			cx = x0;
			cy = y0;
		}
		return new Point2D.Double(cx, cy);
	}

	/**
	 * 
	 * 
	 * Compute the integral of two line segments
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
		double [] l1 ={1, 1};
		double[] v1 = {1, 1};
		double[] v2 = {1, -1};
		System.out.println(computeIntegral(l1, v1, l1, v2, 4, 5));
	}
}
