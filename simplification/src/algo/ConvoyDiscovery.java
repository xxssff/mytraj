package algo;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Seconds;

import com.vividsolutions.jts.geom.Coordinate;

import data.Data;
import data.DataPoint;
import entity.CandidatesPlus;
import entity.Cluster;
import entity.Convoy;
import entity.Global;
import entity.MovingObject;
import entity.Statistics;

public class ConvoyDiscovery {
	// assume we also have a container for all objects
	static CandidatesPlus cands;

	// map id to clusters
	static Statistics stats;
	/**
	 * real params
	 */
	// static HashMap<Integer, MovingObject> allObjs = new HashMap<Integer,
	// MovingObject>();
	static LocalDateTime currTime = new LocalDateTime(Global.infati_MINTIME);
	static LocalDateTime systemMaxTime = new LocalDateTime(
			Global.infati_MAXTIME);
	static LocalDateTime systemMinTime = new LocalDateTime(
			Global.infati_MINTIME);
	static String systemTable = Global.infatiTable;
	static int eps; // UTM coordinates distances
	static int minPts;
	static int tau; // seconds
	static int gap; // how long is a chunk (min)
	static HashMap<Integer, ArrayList<DataPoint>> hm;
	static Data dataSource = new Data();

	// fill allobjs
	// public void fillAllObjs(){
	// for(Integer i : hm.keySet()){
	// MovingObject mo = new MovingObject(i, null);
	// }
	// allObjs.put(i, mo);
	// }

	public void doConvoyDiscovery(String startTime, String endTime,
			BufferedWriter bw) throws Exception {
		long t1 = System.currentTimeMillis();
		if (systemTable.contains("elk")) {
			hm = dataSource.getDefinedTrajectories(systemTable, startTime,
					endTime, 1);
			Global.currMaxDateTIME = Global.elkMaxDateTIME;
		} else {
			hm = dataSource.getDefinedTrajectories(systemTable, startTime,
					endTime, 0);
		}
		long t2 = System.currentTimeMillis();
		stats.loadDataTime = (t2 - t1) / 1000.0;
		cleanHM();
		// fillAllObjs();
	}

	/**
	 * /** Given a set of trajectories in HM, and a set of time points 1. create
	 * virtual points 2. do dbscan with all points 3. precond: times are sorted
	 * 
	 * @param times
	 * @param minPts
	 * @param k
	 * @param e
	 * @throws Exception
	 */
	public static List<Convoy> CMC(List<LocalDateTime> times, int minPts,
			int k, double e) throws Exception {
		List<Convoy> V = new ArrayList<Convoy>();
		List<Convoy> Vresult = new ArrayList<Convoy>();
		for (LocalDateTime ldt : times) {
			List<Convoy> Vnext = new ArrayList<Convoy>();
			ArrayList<MovingObject> mos = getExpectedPoints(ldt);
			if (mos.size() < minPts) {
				continue;
			}
			DBScan.doDBScan(mos, e, minPts, ldt);
			HashMap<Integer, Cluster> C = new HashMap<Integer, Cluster>();
			fillClusters(C, mos);
			for (Convoy v : V) {
				v.assigned = false;
				for (Integer i : C.keySet()) {
					Cluster c = C.get(i);
					Set<Integer> tempSet = new HashSet<Integer>(c.members);
					tempSet.retainAll(v.members);
					if (tempSet.size() >= minPts) {
						v.assigned = true;
						v.members = tempSet;
						v.endTime = ldt;
						Vnext.add(v);
						c.assigned = true;
					}
				}
				if (!v.assigned
						&& Seconds.secondsBetween(v.startTime, v.endTime)
								.getSeconds() >= k) {
					Vresult.add(v);
				}
			}
			for (Integer i : C.keySet()) {
				Cluster c = C.get(i);
				Convoy con = new Convoy();
				if (!c.assigned) {
					con.startTime = ldt;
					con.endTime = ldt;
					Vnext.add(con);
				}
			}
			V = Vnext;
		}
		return Vresult;
	}

	private static ArrayList<MovingObject> getExpectedPoints(LocalDateTime ldt) {
		ArrayList<MovingObject> res = new ArrayList<MovingObject>();
		for (Integer i : hm.keySet()) {
			ArrayList<DataPoint> points = hm.get(i);
			if (points.get(0).dateTime.isBefore(ldt)
					&& points.get(points.size() - 1).dateTime.isAfter(ldt)) {
				DataPoint dp = getExpectedPointOneRoute(i, ldt);
				MovingObject mo = new MovingObject(i, dp);
				if (mo != null)
					res.add(mo);
			}
		}
		return res;
	}

	private static DataPoint getExpectedPointOneRoute(int routeId,
			LocalDateTime ldt) {
		ArrayList<DataPoint> dPoints = hm.get(routeId);
		// System.out.println(dPoints);
		DataPoint dp1 = dPoints.get(0);
		DataPoint dp2 = dPoints.get(0);
		for (DataPoint dp : dPoints) {
			if (dp.dateTime.equals((ldt))) {
				return dp;
			} else if (dp.dateTime.isBefore(ldt)) {
				dp1 = dp;
			} else if (dp.dateTime.isAfter(ldt)) {
				dp2 = dp;
				break;
			}
		}
		DataPoint res = dataSource.getImaginaryPoint(dp1, dp2, ldt);
		return res;
	}

	/**
	 * fill cluster with results from dbscan
	 * 
	 * @param CS
	 * @param mos
	 */
	private static void fillClusters(HashMap<Integer, Cluster> CS,
			ArrayList<MovingObject> mos) {
		Cluster tempC;
		Set<Integer> newClusters = new HashSet<Integer>();
		for (MovingObject tempMo : mos) {
			if (tempMo.cid > 0) {
				newClusters.add(tempMo.cid);
				tempC = CS.get(tempMo.cid);
				if (tempC == null) {
					tempC = new Cluster(tempMo.cid);
					CS.put(tempMo.cid, tempC);
				}
				tempC.add(tempMo.oid);
			}
		}
	}

	/**
	 * remove trajectories having less than the duration requirement
	 * 
	 */
	private static void cleanHM() {
		ArrayList<Integer> toBeDel = new ArrayList<Integer>(hm.size());
		for (Integer key : hm.keySet()) {
			ArrayList<DataPoint> points = hm.get(key);
			LocalDateTime t1 = points.get(0).dateTime;
			LocalDateTime t2 = points.get(points.size() - 1).dateTime;
			int sec = Seconds.secondsBetween(t1, t2).getSeconds();
			if (sec < tau) {
				toBeDel.add(key);
			}
		}
		for (Integer key : toBeDel) {
			hm.remove(key);
		}
	}
}
