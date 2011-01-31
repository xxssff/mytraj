/**
 * 
 */
package geom;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;

/**
 * A circle class which sub-class Ellipse2D
 * 
 * @author Xiaohui
 * 
 */
public class Circle extends Ellipse2D.Double {

	private double radius;

	public Circle(Point2D centerPoint, double radius) {
		// super(
		// (centerPoint.getX() - radius) > 0 ? (centerPoint.getX() - radius)
		// : 0,
		// (centerPoint.getY() - radius) > 0 ? (centerPoint.getY() - radius)
		// : 0, radius * 2, radius * 2);
		super(centerPoint.getX() - radius, centerPoint.getY() - radius,
				radius * 2, radius * 2);
		this.radius = radius;
	}

	// public Circle(double[] center, double radius) {
	// super((center[0] - radius) > 0 ? (center[0] - radius) : 0,
	// (center[1] - radius) > 0 ? (center[1] - radius) : 0,
	// radius * 2, radius * 2);
	// this.radius = radius;
	// }

	public double getRadius() {
		return this.radius;
	}

	public static void main(String[] args) {
		Circle c1 = new Circle(new Point2D.Double(15, 18), 10);
		System.out.println("(" + c1.getCenterX() + "," + c1.getCenterY() + ")");
		System.out.println("radius:" + c1.getRadius());
		System.out.println("area: "
				+ (Math.PI * c1.getRadius() * c1.getRadius()));
		System.out.println("(" + c1.getX() + "," + c1.getY() + ")");

		GeometricShapeFactory gsf = new GeometricShapeFactory();
		gsf.setCentre(new Coordinate(20, 20));
		gsf.setSize(20);
		Polygon circle = gsf.createCircle();

		Coordinate co1 = new Coordinate(10, 10);
		Coordinate co2 = new Coordinate(30, 10);
		GeometryFactory gf = new GeometryFactory();
		LineString ls1 = gf.createLineString(new Coordinate[] { co1, co2 });

		Coordinate co3 = new Coordinate(10, 15);
		Coordinate co4 = new Coordinate(30, 15);

		Coordinate co5 = new Coordinate(10, 8);
		Coordinate co6 = new Coordinate(30, 8);
		
		System.out.println(circle.intersection(ls1).getCoordinates().length);
		System.out.println(circle.intersection(gf
				.createLineString(new Coordinate[] { co3, co4 })).getCoordinates().length);
		System.out.println(circle.intersection(gf
				.createLineString(new Coordinate[] { co5, co6 })).getCoordinates().length);
		
	}
}
