package algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.joda.time.LocalTime;

import data.Data;
import data.DataPoint;
import entity.Candidates;
import entity.Cluster;
import entity.Global;
import entity.MovingObject;
import entity.MyEvent;
import entity.Velocity;

public class GroupDiscovery {
	// containers
	// map id to movingObjects
	static HashMap<Integer, MovingObject> allObjs = new HashMap<Integer, MovingObject>();
	// assume we also have a container for all objects
	static ArrayList<MovingObject> objects = new ArrayList<MovingObject>();
	static Candidates R;
	// map id to clusters
	static HashMap<Integer, Cluster> clusters = new HashMap<Integer, Cluster>();
	static PriorityQueue<MyEvent> eventQ = new PriorityQueue<MyEvent>();

	// params
	static int currTime = 0;
	static int eps = 40;
	static int minPts = 3;
	static int tau = 180; // seconds
	static int k = 5;
	static double alpha, beta, gamma;

	/**
	 * Fill up clusters hashmap; <br>
	 * fill up eventQ
	 */
	private static void buildClusters() {
		Cluster tempC;
		MovingObject mo;
		for (int key : allObjs.keySet()) {
			mo = allObjs.get(key);
			if (mo.cid > 0) {
				tempC = clusters.get(mo.cid);
				if (tempC == null) {
					tempC = new Cluster(mo.cid);
					clusters.put(mo.cid, tempC);
				}
				tempC.add(mo.oid);
			}
		}
		// fill in eventQ
		for (int key : clusters.keySet()) {
			tempC = clusters.get(key);
			double t = tempC.getBreakTime();
			eventQ.add(new MyEvent(t, key));
		}
	}

	private static void buildClusters(ArrayList<Integer> u) {
		Cluster tempC;
		MovingObject mo;
		for (Integer i : u) {
			mo = allObjs.get(i);
			if (mo.cid > 0) {
				tempC = clusters.get(mo.cid);

				if (tempC == null) {
					tempC = new Cluster(mo.cid);
					clusters.put(mo.cid, tempC);
				}
				tempC.add(mo.oid);
			}
		}
		// fill in eventQ
		for (int key : clusters.keySet()) {
			tempC = clusters.get(key);
			double t = tempC.getBreakTime();
			eventQ.add(new MyEvent(t, key));
		}
	}

	/**
	 * If cluster is a candidate, put into R; R updates its minScore if
	 * necessary
	 * 
	 * @param cluster
	 */
	private static void checkCandidate(Cluster cluster) {
		if (cluster.duration >= tau) {
			// true candidate
			if (R.candidates.size() < k) {
				R.candidates.add(cluster);
				if (cluster.getScore() < R.minScore) {
					R.minScore = cluster.getScore();
				}
			} else if (cluster.getScore() > R.minScore) {
				R.candidates.add(cluster);
				R.updateMinScore();
			} else if (cluster.getScore() == R.minScore) {
				R.candidates.add(cluster);
			}
		}
	}

	

	/**
	 * 1. pinpoint the time where there are exactly m objects <br>
	 * 2. fill up containers
	 */
	private static LocalTime getStartTimeAndFillup(
			HashMap<Integer, ArrayList<DataPoint>> hm) {
		if (hm == null) {
			System.err.println("in getStartTime, hm is null");
			System.exit(0);
		}

		LocalTime resTime = null;
		DataPoint[] dpArray = new DataPoint[hm.size()];
		int counter = 0;
		for (Integer key : hm.keySet()) {
			dpArray[counter++] = hm.get(key).get(0);
		}
		Arrays.sort(dpArray);

		resTime = dpArray[minPts - 1].time;
		for (int i = 0; i < minPts; i++) {
			DataPoint dp = dpArray[i];
			MovingObject mo = new MovingObject(dp.routeId, dp, new Velocity(
					dp.vx, dp.vy));
			allObjs.put(mo.oid, mo);
			objects.add(mo);

		}
		return resTime;
	}

	static void insert(ArrayList<Integer> U) {
		// fill up G
		ArrayList<Integer> G = new ArrayList<Integer>();
		for (Integer i : U) {
			MovingObject mo = allObjs.get(i);
			ArrayList<MovingObject> L = DBScan.rangeQuery(mo, objects, eps);
			for (MovingObject tempMO : L) {

				if (tempMO.cid != 0 && tempMO.cid != -1) {
					G.add(tempMO.oid);
				}
			}
		}
		// rebuild with objects from U and G
		Rebuild(U, G);
	}

	static void process() {
		if (currTime == 0) {
			DBScan.doDBScan(objects, eps, minPts);
			// put obj into their clusters
			buildClusters();
		} else {
			/**
			 * 2. deal with disappearing objects 3. deal with incoming objects
			 * */
			while (eventQ.peek().time <= currTime) {
				Cluster cluster = clusters.get(eventQ.poll());
				double duration = currTime - cluster.startTime;
				cluster.updateScore(alpha, beta, gamma, duration);

				// check if cluster is a true candidate
				checkCandidate(cluster);

				// remove cluster id for each object
				ArrayList<Integer> U = new ArrayList<Integer>();
				for (int i : cluster.members) {
					allObjs.get(i).cid = 0;
					U.add(i);
				}

				// add noises into U
				for (MovingObject o : objects) {
					if (o.cid == -1) {
						U.add(o.oid);
					}
				}

				insert(U);

			}
		}
	}

	// time goes second by second
	// at every second if some events occur, process
	// otherwise go to next iteration
	public static void main(String[] args) throws Exception {

		// get a big chunk of data from database
		int gap = 1; // how long is a chunk
		LocalTime baseTime = new LocalTime(Global.MINTIME);
		LocalTime maxTime = new LocalTime(Global.MAXTIME);

		while (baseTime.isBefore(maxTime)) {
			String currTimeStr = Data.converTimeToString(baseTime);
			String nextTimeStr = Data.getNewTime(currTimeStr, gap, 0);

			HashMap<Integer, ArrayList<DataPoint>> hm = Data
					.getDefinedTrajectories(Global.testTable, currTimeStr,
							nextTimeStr, 0);

			/**
			 * 1. pinpoint the time where there are exactly m objects<br>
			 *  2. fill up containers <br>
			 *  3. update base time <br>
			 */
			if (baseTime.equals(new LocalTime(Global.MINTIME))) {
				System.out.println("system starts...");
				// start of the tracing
				baseTime = getStartTimeAndFillup(hm);
			}

			// process these data
			process();
			currTime++;
			baseTime.plusMinutes(1);
		}
	}

	/**
	 * 
	 * @param cid
	 * @param cid2
	 */
	static void merge(int cid1, int cid2) {
		Cluster c1 = clusters.get(cid1);
		Cluster c2 = clusters.get(cid2);
		if (c1.duration >= tau) {
			checkCandidate(c1);
		}
		if (c2.duration >= tau) {
			checkCandidate(c2);
		}
		Cluster newCluster = new Cluster(Global.nextCid());
		for (Integer i : c1.members) {
			newCluster.add(i);
			allObjs.get(i).cid = newCluster.clusterId;
		}
		for (Integer i : c2.members) {
			newCluster.add(i);
			allObjs.get(i).cid = newCluster.clusterId;
		}
		double t = newCluster.getBreakTime();
		eventQ.add(new MyEvent(t, newCluster.clusterId));

	}

	/**
	 * 
	 * @param U
	 * @param G
	 */
	private static void Rebuild(ArrayList<Integer> U, ArrayList<Integer> G) {
		MovingObject mo;
		ArrayList<MovingObject> L;
		for (Integer i : G) {
			mo = allObjs.get(i);

			L = DBScan.rangeQuery(mo, objects, eps);
			if (L.size() >= minPts) {
				mo.label = true;
				for (MovingObject o : L) {
					if (o.cid <= 0) {
						o.cid = mo.cid;
						U.remove(o);
						if (DBScan.expandCluster(objects, o, mo.cid, eps,
								minPts)) {
							double time = clusters.get(mo.cid).getBreakTime();
							eventQ.add(new MyEvent(time, mo.cid));
						}
					} else if (o.cid != mo.cid && o.label) {
						merge(mo.cid, o.cid);
					}
				}
			}
		}
		if (U.size() >= minPts) {
			DBScan.doDBScan(U, eps, minPts, allObjs);
			// put obj into their clusters
			buildClusters(U);
		}
	}

}
