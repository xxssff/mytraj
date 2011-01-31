package test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class myTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GeometryFactory gf = new GeometryFactory();
		Coordinate c1 = new Coordinate(0, 0);
		Coordinate c2 = new Coordinate(1, 1);
		Coordinate c3 = new Coordinate(2, 1);
		Coordinate c4 = new Coordinate(2, 2);
		
		Coordinate c5 = new Coordinate(1, 1.5);
		Coordinate c6 = new Coordinate(3, 1.5);
		
		LineString ls1 = gf
				.createLineString(new Coordinate[] { c1, c2, c3, c4 });
		
		LineString ls2 = gf
		.createLineString(new Coordinate[] { c5, c6 });

		
		Geometry g = ls1.intersection(ls2);
		System.out.println(g.toString());
		
	}

	
}
