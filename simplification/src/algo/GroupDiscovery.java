package algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import entity.Candidates;
import entity.Cluster;
import entity.MovingObject;
import entity.MyEvent;

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

	// time goes second by second
	// at every second if some events occur, process
	// otherwise go to next iteration
	public static void main(String[] args) {
		if (currTime == 0) {
			DBScan.doDBScan(objects, eps, minPts);
			// put obj into their clusters
			buildClusters();
		} else {
			while (eventQ.peek().time <= currTime) {
				Cluster cluster = clusters.get(eventQ.poll());
				double duration = currTime - cluster.startTime;
				cluster.updateScore(alpha, beta, gamma, duration);

				// check if cluster is a true candidate
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

	private static void insert(ArrayList<Integer> U) {
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
		Rebuild(U, G);
	}

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
	 * merge clusters cid and cid2
	 * 
	 * @param cid
	 * @param cid2
	 */
	private static void merge(int cid, int cid2) {
		// TODO Auto-generated method stub

	}

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
}
