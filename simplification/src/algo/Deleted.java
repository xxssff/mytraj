package algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalTime;

import entity.Cluster;
import entity.MovingObject;
import entity.TimeObject;

public class Deleted {
	/**
	 * set expire time of a new cluster
	 * 
	 * @param aCluster
	 */
//	private static void setExpireTime(Cluster aCluster) {
//
//		// LocalTime resTime = null;
//		boolean first = true;
//		// MovingObject tempMO;
//		ArrayList<MovingObject> objMembers = new ArrayList<MovingObject>(
//				aCluster.members.size());
//		for (Integer i : aCluster.members) {
//			objMembers.add(allObjs.get(i));
//		}
//		for (MovingObject mo : objMembers) {
//			if (mo.label) {
//				// retrieve neighbors for the core object
//				ArrayList<MovingObject> neighbors = DBScan.rangeQuery(mo,
//						objMembers, eps);
//				// System.out.println("neighbor size: "+neighbors.size());
//				if (neighbors.size() < minPts) {
//					mo.label = false;
//				} else {
//					neighbors.remove(mo);
//					TimeObject[] tos = new TimeObject[neighbors.size()];
//					int counter = 0;
//					// order them by exit time
//					for (MovingObject neighbor : neighbors) {
//						LocalTime time = DBScan.getExitTime(eps, neighbor, mo,
//								currTime);
//						if (time == null) {
//							// no exit in near future
//							// add one year to currTime
//							time = currTime.plusHours(1);
//						}
//						tos[counter++] = new TimeObject(time, neighbor);
//					}
//					Arrays.sort(tos);
//					// get the m-2's exit time as expiry time
//					// System.out.println(mo.oid + " expire time: "
//					// + tos[minPts - 2].time);
//					if (first) {
//						if (tos.length > (minPts - 2)
//								&& tos[minPts - 2] != null
//								&& tos[minPts - 2].time != null) {
//							aCluster.expiryTime = tos[minPts - 2].time;
//							aCluster.expireOID = mo.oid;
//						} else {
//							System.out.println("when first: "
//									+ Arrays.toString(tos));
//							System.out.println("Neighbors: " + neighbors);
//							System.err.println("exit because of tos");
//							System.exit(0);
//						}
//						first = false;
//					} else {
//						if (tos.length > (minPts - 2)
//								&& tos[minPts - 2] != null
//								&& tos[minPts - 2].time != null) {
//							if (aCluster.expiryTime
//									.isAfter(tos[minPts - 2].time)) {
//								aCluster.expiryTime = tos[minPts - 2].time;
//								aCluster.expireOID = mo.oid;
//							}
//						} else {
//							System.out.println("checking core obj: " + mo);
//							System.out.println("when not first, tos: "
//									+ Arrays.toString(tos));
//							System.out.println("Neighbors: " + neighbors);
//							System.err.println("exit because of tos");
//							System.exit(0);
//						}
//					}
//				}
//			}
//
//		}
//	}
	
	
//	/**
//	 * possible set new expire of cluster because of mo's update <br>
//	 * pre-cond: mo is a core obj
//	 * 
//	 * @param cluster
//	 * @param mo
//	 * @return true is expire time changes
//	 */
//	static boolean setExpireTime(Cluster cluster, MovingObject mo,
//			List<MovingObject> neighbors) {
//		boolean changed = false;
//		TimeObject[] tos = new TimeObject[neighbors.size()];
//		int counter = 0;
//		// order them by exit time
//		for (MovingObject neighbor : neighbors) {
//			LocalTime time = DBScan.getExitTime(eps, neighbor, mo, currTime);
//			if (time == null) {
//				// no exit in near future
//				// add one year to currTime
//				time = currTime.plusHours(1);
//			}
//			tos[counter++] = new TimeObject(time, neighbor);
//		}
//		if (cluster.expiryTime.isAfter(tos[minPts - 2].time)) {
//			cluster.expiryTime = tos[minPts - 2].time;
//			cluster.expireOID = mo.oid;
//			changed = true;
//		}
//		return changed;
//	}

	
}
