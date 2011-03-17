package algo;

import java.util.ArrayList;
import java.util.HashMap;

import org.joda.time.LocalTime;

import entity.Global;
import entity.MovingObject;

/**
 * 
 * @author xiaohui 
 * 
 */
public class DBScan {

	public static void doDBScan(ArrayList<MovingObject> objects, double eps,
			double minPts, LocalTime currTime) {
		for (MovingObject obj : objects) {
			if (obj.cid == 0) {
				expandCluster(objects, obj, Global.nextCid(), eps, minPts,
						currTime);

			}
		}
	}

	/**
	 * 
	 * @param objects
	 *            : all objects
	 * @param obj
	 * @param cid
	 * @param eps
	 * @param minPts
	 * @return true if can expand false otherwise
	 */
	public static boolean expandCluster(ArrayList<MovingObject> objects,
			MovingObject obj, int cid, double eps, double minPts,
			LocalTime currTime) {
		ArrayList<MovingObject> seeds = rangeQuery(obj, objects, eps);
		if (seeds.size() < minPts) {
			obj.cid = -1; // noise
			return false;
		} else {
			for (MovingObject seed : seeds) {
				seed.cid = cid;
			}
			obj.label = true; // core object
			seeds.remove(obj);
			while (!seeds.isEmpty()) {
				MovingObject currP = seeds.get(0);
				ArrayList<MovingObject> result = rangeQuery(currP, objects, eps);

				if (result.size() >= minPts) {
					result.remove(currP);
					// currP is core object
					currP.label = true;
					for (MovingObject resultP : result) {
						if (resultP.cid == -1 || resultP.cid == 0) {
							resultP.cid = cid;
							if (resultP.cid == 0) {
								seeds.add(resultP);
							}
						}

					}
				} else {
					// currP is border object; compute exit time
					currP.label = false;
					currP.exitTime = getExitTime(eps, currP, obj, currTime);
				}
				seeds.remove(currP);
			}
			return true;
		}
	}

	/**
	 * 
	 * @param eps
	 * @param borderObj
	 * @param coreObj
	 * @param currTime
	 * @return exit time for currP
	 */
	public static LocalTime getExitTime(double eps, MovingObject borderObj,
			MovingObject coreObj, LocalTime currTime) {
		// compute distances
		double objDist = borderObj.distance(coreObj);
		double d1 = eps - objDist;
		double d2 = Math.sqrt(eps * eps - objDist * objDist);

		// compute velocities
		double deltaX = borderObj.getX() - coreObj.getX();
		double deltaY = borderObj.getY() - coreObj.getY();
		double sinTheta = deltaX / Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		double cosTheta = Math.sqrt(1 - sinTheta * sinTheta);

		double deltaVX = borderObj.v.getVx() - coreObj.v.getVx();
		double deltaVY = borderObj.v.getVy() - coreObj.v.getVy();
		double along = deltaVX * sinTheta + deltaVY * cosTheta;
		double perpendicular = deltaVX * cosTheta + deltaVY * sinTheta;

		return currTime.plusSeconds((int) Math.min(d1 / along, d2
				/ perpendicular));
	}

	/**
	 * naive in-memory implementation
	 * 
	 * @param currP
	 * @param objects
	 * @param eps
	 * @return all points in range eps
	 */
	public static ArrayList<MovingObject> rangeQuery(MovingObject currMo,
			ArrayList<MovingObject> objects, double eps) {
		ArrayList<MovingObject> res = new ArrayList<MovingObject>();

		for (MovingObject mo : objects) {
			if (mo.distance(currMo) <= eps) {
				res.add(mo);
			}
		}

		return res;
	}

	/**
	 * 
	 * @param U
	 * @param eps
	 * @param minPts
	 * @param allObjs
	 * @return list of CID's
	 */
	public static void doDBScan(ArrayList<Integer> U, int eps, int minPts,
			HashMap<Integer, MovingObject> allObjs, LocalTime currTime) {
		ArrayList<MovingObject> objs = new ArrayList<MovingObject>();

		for (Integer i : U) {
			objs.add(allObjs.get(i));
		}
		doDBScan(objs, eps, minPts, currTime);

	}

}
