package geom;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;

public class FrechetDistance {

	double[][] a, b, c, d;
	Point2D[] pl1;
	Point2D[] pl2;
	double epsilon;
	static GeometricShapeFactory gsf = new GeometricShapeFactory();
	static GeometryFactory gf = new GeometryFactory();

	public double computeFrechetDistance() {
		return 0;
	}

	/**
	 * P and Q are two polylines
	 * 
	 * @param P
	 * @param Q
	 * @param epsilon
	 */
	public FrechetDistance(Point2D[] P, Point2D[] Q, double epsilon) {
		pl1 = P;
		pl2 = Q;
		this.epsilon = epsilon;
		int p = P.length;
		int q = Q.length;
		a = new double[p][q];
		b = new double[p][q];
		c = new double[p][q];
		d = new double[p][q];
	}

	/**
	 * 
	 * @param P
	 * @param Q
	 * @param epsilon
	 * @return true if the Frechet distance is <= epsilon
	 */
	private boolean isFrechet() {
		int pLength = pl1.length - 1;
		int qLength = pl2.length - 1;
		// check first pair of segments
		if (Line2D.ptSegDist(pl1[0].getX(), pl1[0].getY(), pl1[1].getX(),
				pl1[1].getY(), pl2[0].getX(), pl2[0].getY()) > this.epsilon
				&& Line2D.ptSegDist(pl1[0].getX(), pl1[0].getY(),
						pl1[1].getX(), pl1[1].getY(), pl2[1].getX(),
						pl2[1].getY()) > this.epsilon) {
			return false;
		}
		if (Line2D.ptSegDist(pl2[0].getX(), pl2[0].getY(), pl2[1].getX(),
				pl2[1].getY(), pl1[0].getX(), pl1[0].getY()) > this.epsilon
				&& Line2D.ptSegDist(pl2[0].getX(), pl2[0].getY(),
						pl2[1].getX(), pl1[2].getY(), pl1[1].getX(),
						pl1[1].getY()) > this.epsilon) {
			return false;
		}

		// check last pair of segments
		if (Line2D.ptSegDist(pl1[pLength-1].getX(), pl1[pLength-1].getY(), pl1[pLength].getX(),
				pl1[pLength].getY(), pl2[qLength-1].getX(), pl2[qLength-1].getY()) > this.epsilon
				&& Line2D.ptSegDist(pl1[pLength-1].getX(), pl1[pLength-1].getY(),
						pl1[pLength].getX(), pl1[pLength].getY(), pl2[qLength].getX(),
						pl2[qLength].getY()) > this.epsilon) {
			return false;
		}
		if (Line2D.ptSegDist(pl2[qLength-1].getX(), pl2[qLength-1].getY(), pl2[qLength].getX(),
				pl2[qLength].getY(), pl1[pLength-1].getX(), pl1[pLength-1].getY()) > this.epsilon
				&& Line2D.ptSegDist(pl2[qLength-1].getX(), pl2[qLength-1].getY(),
						pl2[qLength].getX(), pl2[qLength].getY(), pl1[pLength].getX(),
						pl1[pLength].getY()) > this.epsilon) {
			return false;
		}
		
		
		LineString tempLsQ;
		LineString tempLsP;
		Coordinate p11, p12, p21, p22;
		Polygon tempCircle;
		Geometry tempGeom;

		for (int i = 0; i < pl1.length - 1; i++) {
			for (int j = 0; j < pl2.length - 1; j++) {

				if (Line2D.ptSegDist(pl2[j].getX(), pl2[j].getY(),
						pl2[j + 1].getX(), pl2[j + 1].getY(), pl1[i].getX(),
						pl1[i].getY()) > epsilon) {
					a[i][j] = b[i][j] = -1;
				} else {

					// make line string out of j's two end points
					p21 = new Coordinate(pl2[j].getX(), pl2[j].getY());
					p22 = new Coordinate(pl2[j + 1].getX(), pl2[j + 1].getY());
					tempLsQ = gf
							.createLineString(new Coordinate[] { p21, p22 });

					// make circle with i's first end point
					p11 = new Coordinate(pl1[i].getX(), pl1[i].getY());
					p12 = new Coordinate(pl1[i + 1].getX(), pl1[i + 1].getY());
					tempLsP = gf
							.createLineString(new Coordinate[] { p11, p12 });

					gsf.setCentre(p11);
					gsf.setSize(2 * this.epsilon);
					tempCircle = gsf.createCircle();

					if (tempCircle.contains(tempLsQ)) {
						a[i][j] = 0;
						b[i][j] = 1;
					} else {

						// collapse the circle and the line
						tempGeom = tempCircle.intersection(tempLsQ);
						int numCoords = tempGeom.getCoordinates().length;

						if (numCoords == 2) {
							// 2 points
							Coordinate[] intersections = tempGeom
									.getCoordinates();
							a[i][j] = getProportion(intersections[0], tempLsQ);
							b[i][j] = getProportion(intersections[1], tempLsQ);
						} else if (numCoords == 1) {
							// 1 point
							Coordinate intersection = tempGeom.getCoordinate();
							if (intersection.distance(p21) < intersection
									.distance(p22)) {
								a[i][j] = 0;
								b[i][j] = getProportion(intersection, tempLsQ);
							} else {
								a[i][j] = getProportion(intersection, tempLsQ);
								b[i][j] = 1;
							}
						}
					}

					// fill up c_ij and d_ij
					gsf.setCentre(p21);
					tempCircle = gsf.createCircle();
					if (tempCircle.contains(tempLsP)) {
						c[i][j] = 0;
						d[i][j] = 1;
					} else {
						tempGeom = tempCircle.intersection(tempLsP);
						int numCoords = tempGeom.getCoordinates().length;
						if (numCoords == 1) {
							Coordinate intersect = tempGeom.getCoordinate();
							if (intersect.distance(p11) < intersect
									.distance(p12)) {
								c[i][j] = 0;
								d[i][j] = getProportion(intersect, tempLsP);
							} else {
								c[i][j] = getProportion(intersect, tempLsP);
								d[i][j] = 1;
							}
						} else {
							Coordinate[] intersections = ((LineString) tempGeom)
									.getCoordinates();
							c[i][j] = getProportion(intersections[0], tempLsP);
							d[i][j] = getProportion(intersections[1], tempLsP);
						}
					}
				}
			}
		}

		// determine B^R_i,1
		boolean flag = true;
		for (int i = 0; i < pl1.length; i++) {
			if (flag && c[i][0] == -1 && d[i][0] == -1) {
				flag = false;
			} else if (!flag) {
				c[i][0] = d[i][0] = -1;
			}
		}

		flag = true;
		// determine L^R_1,j
		for (int j = 1; j < pl2.length; j++) {
			if (flag && a[0][j] == -1 && b[0][j] == -1) {
				flag = false;
			} else if (!flag) {
				a[0][j] = b[0][j] = -1;
			}
		}

		// TODO: the complicated loop to compute L^R_(i+1),j and B^R_i,(j+1)
		boolean retVal = true;
		
		// cannot enter the upper right cell
		if (a[pLength][qLength] == -1 && b[pLength][qLength] == -1
				&& c[pLength][qLength] == -1 && d[pLength][qLength] == -1) {
			retVal = false;
		}
		return retVal;
	}

	private double getProportion(Coordinate coord, LineString ls) {
		// coord is a point in line ls
		Coordinate[] ends = ls.getCoordinates();
		return coord.distance(ends[0]) / ls.getLength();
	}
}
