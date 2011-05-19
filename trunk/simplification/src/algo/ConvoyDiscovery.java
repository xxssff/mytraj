package algo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.Seconds;

import data.Data;
import data.DataPoint;
import entity.CandidatesPlus;
import entity.Cluster;
import entity.ConfReader;
import entity.Convoy;
import entity.Global;
import entity.MovingObject;
import entity.Statistics;
import entity.TimeLineSegment;

/**
 * @version 0.1 remove subsets from C
 * @author xiaohui
 * 
 */
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

	static String systemTable = Global.infatiTable;
	static double eps; // UTM coordinates distances
	static int minPts;
	static int tau; // seconds
	static int gap; // how long is a chunk (min)
	static HashMap<Integer, ArrayList<DataPoint>> hm;
	static HashMap<Integer, ArrayList<TimeLineSegment>> idToLineSegs;
	static Data dataSource = new Data();
	static int sysInterval;

	// fill allobjs
	// public void fillAllObjs(){
	// for(Integer i : hm.keySet()){
	// MovingObject mo = new MovingObject(i, null);
	// }
	// allObjs.put(i, mo);
	// }
	public static void doConvoyDiscovery(String startTime, String endTime,
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
		// construct line segments from data points
		idToLineSegs = new HashMap<Integer, ArrayList<TimeLineSegment>>();
		for (Integer i : hm.keySet()) {
			ArrayList<DataPoint> dps = hm.get(i);
			DataPoint[] pts = dps.toArray(new DataPoint[0]);
			for (int j = 0; j < pts.length - 1; j++) {
				DataPoint p1 = pts[j];
				DataPoint p2 = pts[j + 1];
				TimeLineSegment tls = new TimeLineSegment(i, p1.p, p2.p,
						p1.dateTime, p2.dateTime, p1.time0, p2.time0);

				ArrayList<TimeLineSegment> tlss = idToLineSegs.get(i);
				if (tlss == null) {
					tlss = new ArrayList<TimeLineSegment>(dps.size());
					tlss.add(tls);
					idToLineSegs.put(i, tlss);
				} else {
					tlss.add(tls);
				}
			}
		}
		List<Interval> list = new ArrayList<Interval>();
		DateTime sTime = new DateTime(startTime);
		DateTime eTime = new DateTime(endTime);
		while (sTime.isBefore(eTime)) {
			Interval tempInterval = new Interval(sTime,
					sTime.plusSeconds(sysInterval));
			sTime = sTime.plusSeconds(sysInterval);
			list.add(tempInterval);
		}

		List<Convoy> cv = cutsFilter(list, minPts, tau, eps);
		System.out.println("before refinement: " + cv);
		Set<Convoy> convoySet = cutsRefinement(cv, minPts, tau, eps);
		System.out.println(convoySet);
	}

	/**
	 * Given a set of trajectories in HM, and a set of time points 1. create
	 * virtual points 2. do dbscan with all points 3. precond: times are sorted
	 * 
	 * @param times
	 * @param minPts
	 * @param k
	 * @param e
	 * @throws Exception
	 */
	public static List<Convoy> CMC(Set<Integer> oids,
			List<LocalDateTime> times, int minPts, int k, double e)
			throws Exception {
		List<Convoy> V = new ArrayList<Convoy>();
		List<Convoy> Vresult = new ArrayList<Convoy>();
		for (LocalDateTime ldt : times) {
			List<Convoy> Vnext = new ArrayList<Convoy>();
			ArrayList<MovingObject> mos = getExpectedPoints(oids, ldt);
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
				Convoy con = new Convoy(c);
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

	private static ArrayList<MovingObject> getExpectedPoints(Set<Integer> oids,
			LocalDateTime ldt) {
		ArrayList<MovingObject> res = new ArrayList<MovingObject>();
		for (Integer i : oids) {
			ArrayList<DataPoint> points = hm.get(i);
			if ((points.get(0).equals(ldt) || points.get(0).dateTime
					.isBefore(ldt))
					&& (points.get(points.size() - 1).dateTime.isAfter(ldt))
					|| points.get(points.size() - 1).equals(ldt)) {
				DataPoint dp = getExpectedPointOneRoute(i, ldt);
				MovingObject mo = new MovingObject(i, dp);
				if (mo != null) {
					res.add(mo);
				}
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

	
	private static void fillResult(List<Convoy> convoyList,
			List<TimeLineSegment> g, LocalDateTime startTime,
			LocalDateTime endTime) {

		Set<Integer> cids = new HashSet<Integer>();
		for (TimeLineSegment tempMo : g) {
			if (tempMo.cid > 0) {
				cids.add(tempMo.cid);
			}
		}
		Cluster[] cls = new Cluster[cids.size()];
		boolean[] toBeDel = new boolean[cids.size()];
		int index = 0;
		for (Integer cid : cids) {
			Cluster tempC = new Cluster(cid);
			for (TimeLineSegment tempMo : g) {
				if (tempMo.cid == tempC.clusterId) {
					tempC.add(tempMo.routeId);
				}
			}
			cls[index++] = tempC;
		}

		// loop the array to remove subsets
		for (int i = 0; i < cls.length; i++) {
			for (int j = 0; j < cls.length; j++) {
				if (isProperSubset(cls[i], cls[j])) {
					toBeDel[i] = true;
				}
			}
		}
		for (int i = 0; i < toBeDel.length; i++) {
			if (toBeDel[i]) {
				cls[i] = null;
			}
		}
		// add into CS, avoid duplicate clusters with identical members
		for (Cluster c : cls) {
			if (c != null) {
				boolean put = true;
				for (Convoy con : convoyList) {
					if (con.members.equals(c.members)) {
						put = false;
					}
				}
				if (put) {
					convoyList.add(new Convoy(c, startTime, endTime));
				}
			}
		}
	}

	
	
	/**
	 * fill cluster with results from dbscan
	 * 
	 * @param startTime
	 * @param endTime
	 * 
	 * @param convoyList
	 * @param g
	 *            : set of lineSegments
	 * 
	 */
	private static void fillClusters(List<Convoy> convoyList,
			List<TimeLineSegment> g, LocalDateTime startTime,
			LocalDateTime endTime) {

		Set<Integer> cids = new HashSet<Integer>();
		for (TimeLineSegment tempMo : g) {
			if (tempMo.cid > 0) {
				cids.add(tempMo.cid);
			}
		}
		Cluster[] cls = new Cluster[cids.size()];
		boolean[] toBeDel = new boolean[cids.size()];
		int index = 0;
		for (Integer cid : cids) {
			Cluster tempC = new Cluster(cid);
			for (TimeLineSegment tempMo : g) {
				if (tempMo.cid == tempC.clusterId) {
					tempC.add(tempMo.routeId);
				}
			}
			cls[index++] = tempC;
		}

		// loop the array to remove subsets
		for (int i = 0; i < cls.length; i++) {
			for (int j = 0; j < cls.length; j++) {
				if (isProperSubset(cls[i], cls[j])) {
					toBeDel[i] = true;
				}
			}
		}
		for (int i = 0; i < toBeDel.length; i++) {
			if (toBeDel[i]) {
				cls[i] = null;
			}
		}
		// add into CS, avoid duplicate clusters with identical members
		for (Cluster c : cls) {
			if (c != null) {
				boolean put = true;
				for (Convoy con : convoyList) {
					if (con.members.equals(c.members)) {
						put = false;
					}
				}
				if (put) {
					convoyList.add(new Convoy(c, startTime, endTime));
				}
			}
		}
	}

	/**
	 * 
	 * @param c1
	 * @param c2
	 * @return true iff c1 is proper subset of c2
	 */
	private static boolean isProperSubset(Cluster c1, Cluster c2) {
		Set<Integer> mem1 = c1.members;
		int size = mem1.size();
		Set<Integer> mem2 = c2.members;
		if (mem1.size() >= mem2.size()) {
			return false;
		}
		mem1.retainAll(mem2);
		if (mem1.size() == size) {
			return true;
		} else
			return false;
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

	/**
	 * Implementation of cuts-filter
	 * 
	 * @param partitions
	 * @param minPts
	 * @param k
	 * @param e
	 * @return
	 */
	private static List<Convoy> cutsFilter(List<Interval> partitions,
			int minPts, int k, double e) {
		int lambda = 1800;// 86400 / 2; // for elk data: 101001600
		List<Convoy> V = new ArrayList<Convoy>();
		List<Convoy> Vcand = new ArrayList<Convoy>();
		// HashMap<Integer, Cluster> C = new HashMap<Integer, Cluster>();
		List<Convoy> convoyList = new ArrayList<Convoy>();
		List<Convoy> Vnext;

		for (int l = 0; l < partitions.size(); l++) {
			Vnext = new ArrayList<Convoy>();
			Interval interval = partitions.get(l);
			
			System.out.println("iteraion " + l);
			List<TimeLineSegment> G = new ArrayList<TimeLineSegment>();

			for (Integer i : idToLineSegs.keySet()) {
				// System.out.println("here " + isIntersect(i, interval)
				// + " i=" + i.toString() + " /// int="
				// + interval.toString());
				if (isIntersect(i, interval)) {
					ArrayList<TimeLineSegment> tlss = idToLineSegs.get(i);
					for (TimeLineSegment tl : tlss) {
						if (tl.getInterval().overlaps(interval)) {
							G.add(tl);
						}
					}
				}
			}

			TrajDBScan.doDBScan(G, e, minPts);

			// for(TimeLineSegment tls : G){
			// System.out.println(tls);
			// }
			convoyList = new ArrayList<Convoy>();
			fillClusters(convoyList, G, interval.getStart().toLocalDateTime(),
					interval.getEnd().toLocalDateTime());

			for (Convoy v : V) {
				System.out.println("looping V...");
				v.assigned = false;
				for (Convoy c : convoyList) {
					Set<Integer> tempSet = new HashSet<Integer>(c.members);
					// c \cup v
					tempSet.retainAll(v.members);
					if (tempSet.size() >= minPts) {
						v.assigned = true;
						v.members = tempSet;
						v.lifetime = v.lifetime + lambda;
						v.endTime = c.endTime;
						Vnext.add(v);
						c.assigned = true;
					}
				}
				if (v.lifetime >= k) {
					if (!v.assigned || l == partitions.size() - 1)
						Vcand.add(v);
				}
			}
			for (Convoy c : convoyList) {
				if (!c.assigned) {
					c.lifetime = lambda;
					Vnext.add(c);
				}
			}
			V = Vnext;

		}
		return Vcand;
	}

	/**
	 * 
	 * @param oid
	 * @param interval
	 * @return true if timelineseg's interval intersects with interval
	 */
	private static boolean isIntersect(Integer oid, Interval interval) {
		ArrayList<DataPoint> dps = hm.get(oid);
		Interval tempInterval = new Interval(dps.get(0).dateTime.toDateTime(),
				dps.get(dps.size() - 1).dateTime.toDateTime());
		// System.out.println(tempInterval.toString());
		return (interval.overlaps(tempInterval));
	}

	// private static ArrayList<MovingObject> getExpectedSegments(
	// ArrayList<Interval> partitions, LocalDateTime ldt) {
	// ArrayList<MovingObject> res = new ArrayList<MovingObject>();
	//
	// return res;
	// }

	// private static DataSegment getExpectedSegmentOneRoute(int routeId,
	// LocalDateTime ldt) {
	// ArrayList<DataSegment> dSegments = hmSeg.get(routeId);
	// // System.out.println(dPoints);
	// DataPoint dp1 = dPoints.get(0);
	// DataPoint dp2 = dPoints.get(0);
	// for (DataPoint dp : dPoints) {
	// if (dp.dateTime.equals((ldt))) {
	// return dp;
	// } else if (dp.dateTime.isBefore(ldt)) {
	// dp1 = dp;
	// } else if (dp.dateTime.isAfter(ldt)) {
	// dp2 = dp;
	// break;
	// }
	// }
	// DataPoint res = dataSource.getImaginaryPoint(dp1, dp2, ldt);
	// return res;
	// }
	private static Set<Convoy> cutsRefinement(List<Convoy> Vcand, int minPts,
			int k, double e) throws Exception {
		Set<Convoy> convoySet = new HashSet<Convoy>();
		for (Convoy v : Vcand) {
			LocalDateTime t_start = v.startTime;
			LocalDateTime t_end = v.endTime;
			List<LocalDateTime> ldts = new ArrayList<LocalDateTime>();
			for (LocalDateTime l = t_start; l.isBefore(t_end); l = l
					.plusSeconds(1)) {
				ldts.add(l);
			}
			List<Convoy> cons = CMC(v.members, ldts, minPts, k, eps);
			for (Convoy c : cons) {
				convoySet.add(c);
			}
		}
		
		return convoySet;
	}

	public static void main(String[] args) throws Exception {
		/**
		 * Get parameters from conf file
		 * 
		 */
		System.out.println("===============Convoy Discovery==================");
		ConfReader reader = new ConfReader();
		HashMap<String, String> conf = reader.read(args[0]);

		BufferedWriter bw = new BufferedWriter(new FileWriter(
				conf.get("outFile")));
		eps = Integer.parseInt(conf.get("eps"));
		minPts = Integer.parseInt(conf.get("minPts"));
		tau = Integer.parseInt(conf.get("tau"));
		systemTable = conf.get("systemTable");
		sysInterval = Integer.parseInt(conf.get("k"));

		bw.write("Convoy Discovery Output=====");
		bw.newLine();
		System.out.println("e:" + eps + "\t" + "m:" + minPts + "\t" + "tau:"
				+ tau);

		stats = new Statistics();

		String ts = "2001-03-27T16:23:24"; // conf.get("ts");
		String te = "2001-03-27T17:01:17"; // conf.get("te");

		long t_start = System.currentTimeMillis();
		doConvoyDiscovery(ts, te, bw);
		long t_end = System.currentTimeMillis();
		// write candidates into result file
		stats.startTime = "16:02:00"; // conf.get("ts");
		stats.endTime = "17:01:59"; // conf.get("te");

		stats.elapsedTime = (t_end - t_start) / 1000.0;

		// write param to file
		bw.write("m: " + minPts + "; e: " + eps + "; tau:" + tau);
		bw.newLine();
		bw.write("table: " + systemTable);
		bw.newLine();
		bw.write(ts + "; " + te);
		bw.newLine();

		stats.toFile(bw);

		bw.close();
	}
}
