package algo;

import java.util.ArrayList;
import java.util.HashMap;

import mathUtil.CoefficientsQuartic;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import entity.Global;
import entity.MovingObject;

/**
 * 
 * @author xiaohui
 * 
 */
public class DBScan {

	/**
	 * 
	 * @param objects
	 * @param eps
	 * @param minPts
	 * @param currDateTime
	 */
	public static void doDBScan(ArrayList<MovingObject> objects, double eps,
			double minPts, LocalDateTime currDateTime) {
		for (MovingObject obj : objects) {
			if (obj.cid == 0) {
				expandCluster(objects, obj, Global.nextCid(), eps, minPts,
						currDateTime);

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
			LocalDateTime currTime) {
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
	public static LocalDateTime getExitTime(double eps, MovingObject borderObj,
			MovingObject coreObj, LocalDateTime currTime) {

		CoefficientsQuartic quad = new CoefficientsQuartic(borderObj.dataPoint,
				coreObj.dataPoint, eps);
		ArrayList<Double> roots = quad.solve();

		LocalDateTime resTime = null;
		int sec = 1;
		if (roots == null) {
			// no exit in near future
			return null;
		} else {
			if (roots.size() == 1) {
				if (roots.get(0) < 0) {
					return null;
				}
				sec = (int) Math.ceil(roots.get(0));
			} else if (roots.size() == 2) {
				// take the positive one
				sec = (int) Math.max(Math.ceil(roots.get(0)),
						Math.ceil(roots.get(1)));
				if (sec >= 36000 || sec <= 0) {
					return null;
				}
			}
		}

		return resTime;
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
			double distance = mo.distance(currMo);
			if (distance <= eps) {
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
			HashMap<Integer, MovingObject> allObjs, LocalDateTime currTime) {
		ArrayList<MovingObject> objs = new ArrayList<MovingObject>();

		for (Integer i : U) {
			objs.add(allObjs.get(i));
		}
		doDBScan(objs, eps, minPts, currTime);

	}

}
