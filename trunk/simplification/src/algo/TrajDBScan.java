package algo;

import java.util.ArrayList;
import java.util.List;

import entity.Global;
import entity.TimeLineSegment;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author xiaohui
 */
public class TrajDBScan {
	/**
	 * 
	 * @param objects
	 * @param eps
	 * @param minPts
	 * @param currDateTime
	 */
	public static void doDBScan(List<TimeLineSegment> objects, double eps,
			int minPts) {
		for (TimeLineSegment obj : objects) {
			if (obj.cid == 0) {
				expandCluster(objects, obj, Global.nextCid(), eps, minPts);

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
	public static boolean expandCluster(List<TimeLineSegment> objects,
			TimeLineSegment obj, int cid, double eps, double minPts) {
		ArrayList<TimeLineSegment> seeds = rangeQuery(obj, objects, eps);
		if (seeds.size() < minPts) {
			obj.cid = -1; // noise
			return false;
		} else {
			for (TimeLineSegment seed : seeds) {
				seed.cid = cid;
			}
			obj.label = true; // core object
			seeds.remove(obj);
			while (!seeds.isEmpty()) {
				TimeLineSegment currP = seeds.get(0);
				ArrayList<TimeLineSegment> result = rangeQuery(currP, objects,
						eps);

				if (result.size() >= minPts) {
					result.remove(currP);
					// currP is core object
					currP.label = true;
					for (TimeLineSegment resultP : result) {
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
				}
				seeds.remove(currP);
			}
			return true;
		}
	}

	/**
	 * naive in-memory implementation
	 * 
	 * @param currP
	 * @param objects
	 * @param eps
	 * @return all points in range eps
	 */
	public static ArrayList<TimeLineSegment> rangeQuery(TimeLineSegment currMo,
			List<TimeLineSegment> objects, double eps) {
		ArrayList<TimeLineSegment> res = new ArrayList<TimeLineSegment>();

		for (TimeLineSegment mo : objects) {
			double distance = mo.distance(currMo);
			if (distance <= eps) {
				res.add(mo);
			}
		}

		return res;
	}
}
