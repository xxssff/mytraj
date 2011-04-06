package test;

import java.util.ArrayList;
import java.util.HashMap;

import data.Data;
import data.DataPoint;
import entity.Global;

public class DataTest {
	public static void main(String[] args) throws Exception {
//		HashMap<Integer, ArrayList<DataPoint>> hm = Data.getDefinedTrajectories(Global.testTable, "16:30:18",
//						"16:35:18", 0);
//		for (Integer key : hm.keySet()) {
//			ArrayList<DataPoint> points = hm.get(key);
//			System.out.println(key + ": " + points);
//		}
//
//		Data.getImaginaryPoint(t1_p1, t1_p2, p, type);
		ArrayList<Integer> arr = new ArrayList<Integer>(100);
		
		System.out.println(arr.size());
		arr.add(1);
		System.out.println(arr.size());
	}

}
