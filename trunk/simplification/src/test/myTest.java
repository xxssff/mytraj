package test;

import java.util.Arrays;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class myTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int[] a ={1, 2};
		int[] b = new int[a.length+1];
		System.arraycopy(a, 0 , b, 0, a.length);
		System.out.println(Arrays.toString(b));
		b[a.length]=3;
		System.out.println(Arrays.toString(b));
		
	}

	
}
