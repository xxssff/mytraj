package test;

import java.util.ArrayList;

import algo.DBScan;

import com.vividsolutions.jts.geom.Coordinate;

import data.DataPoint;
import entity.MovingObject;

public class DBScanTest {

	public static void main(String[] args) {
		ArrayList<double[]> coords = new ArrayList<double[]>();
		coords.add(new double[] { 1.9, 1 });
		coords.add(new double[] { 1, 1.8 });
		coords.add(new double[] { 1, 0.2 });
		coords.add(new double[] {1, 1 });
		coords.add(new double[] { 0.1, 1 });
		
		coords.add(new double[] { 31.5, 44.6 });
		coords.add(new double[] { 1.5, 4.25 });
		coords.add(new double[] { 131.2, 414.7 });

		ArrayList<MovingObject> objs = new ArrayList<MovingObject>();

		for (int i = 0; i < coords.size(); i++) {
			MovingObject mo1 = null;
			objs.add(mo1);
		}
		
		DBScan.doDBScan(objs, 1.0, 3);
		
		for(MovingObject o : objs){
			System.out.println(o.toString());
		}

	}
}
