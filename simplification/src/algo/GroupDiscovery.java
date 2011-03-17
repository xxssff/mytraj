package algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.joda.time.LocalTime;

import trie.Trie;
import data.Data;
import data.DataPoint;
import entity.Candidates;
import entity.Cluster;
import entity.EventType;
import entity.Global;
import entity.MovingObject;
import entity.MyEvent;
import entity.TimeObject;
import entity.Velocity;

/**
 * 
 * @author xiaohui TODO 1. update event queue
 * 
 */
public class GroupDiscovery {
	// containers
	// map id to movingObjects
	static HashMap<Integer, MovingObject> allObjs = new HashMap<Integer, MovingObject>();
	// assume we also have a container for all objects
	static ArrayList<MovingObject> objects = new ArrayList<MovingObject>();
	static Candidates R;
	// map id to clusters
	static HashMap<Integer, Cluster> CS = new HashMap<Integer, Cluster>();
	static PriorityQueue<MyEvent> eventQ = new PriorityQueue<MyEvent>();

	// params
	// static int currTime = 0;
	static LocalTime currTime = new LocalTime(Global.MINTIME);
	static LocalTime maxTime = new LocalTime(Global.MAXTIME);

	static int eps = 40;
	static int minPts = 2;
	static int tau = 180; // seconds
	static int k = 5;
	static double alpha, beta, gamma;
	static Trie aTrie;

	/**
	 * Fill up clusters hashmap; <br>
	 * fill up eventQ
	 */
	private static void buildClusters() {
		DBScan.doDBScan(objects, eps, minPts, currTime);
		Cluster tempC;

		// //get core objects
		// ArrayList<MovingObject> coreObjs = new
		// ArrayList<MovingObject>(objects.size());
		// for (MovingObject tempMo : objects) {
		// if (tempMo.cid > 0 && tempMo.label){
		// coreObjs.add(tempMo);
		// }
		// }

		for (MovingObject mo : objects) {
			if (mo.cid > 0) {
				tempC = CS.get(mo.cid);
				if (tempC == null) {
					tempC = new Cluster(mo.cid);
					CS.put(mo.cid, tempC);
				}
				tempC.add(mo.oid);

				// insert exit event into eventQ for border objects
				if (!mo.label) {
					MyEvent event = new MyEvent(mo.exitTime, mo.oid,
							tempC.clusterId, EventType.EXIT);
					eventQ.add(event);
				}

				// insert expire event into eventQ
				LocalTime time = getExpireTime(tempC);
				tempC.expiryTime = time;
				eventQ.add(new MyEvent(time, -1, tempC.clusterId,
						EventType.EXPIRE));
			}
		}

		// process noise objects
		for (MovingObject mo : objects) {
			if (mo.cid == -1) {
				// insert join event for noise objects
				// find the soonest cluster to join
				double range = 5 * eps;
				ArrayList<MovingObject> nei = DBScan.rangeQuery(mo, objects,
						range);
				LocalTime minTime = null;
				Cluster c = null;
				for (MovingObject tempMo : nei) {
					if (tempMo.label && tempMo.cid > 0) {
						LocalTime t = getEnterTime(eps, mo, tempMo, currTime);
						if (minTime == null || minTime.isAfter(t)) {
							minTime = t;
							c = CS.get(tempMo.cid);
						}
					}
				}
				// insert the event into eventQ
				eventQ.add(new MyEvent(minTime, mo.oid, c.clusterId,
						EventType.JOIN));
			}
		}

		// process trie
		buildTrie();
	}

	private static void buildTrie() {
		aTrie = new Trie();
		Cluster tempC;
		for (Integer i : CS.keySet()) {
			tempC = CS.get(i);
			String[] combinations = getCombination(tempC, minPts);
			aTrie.insertNewCluster(combinations);
		}
	}

	private static String[] getCombination(Cluster aCluster, int minPts2) {
		// ArrayList<Integer> ints = aCluster.members;
		Collections.sort(aCluster.members);
		Integer[] ints = aCluster.members.toArray(new Integer[aCluster.members
				.size()]);
		return null;
	}

	private static LocalTime getEnterTime(int range, MovingObject noiseObj,
			MovingObject coreObj, LocalTime refTime) {

		// compute distances
		double objDist = noiseObj.distance(coreObj) - range;

		// compute relative velocities
		double deltaX = noiseObj.getX() - coreObj.getX();
		double deltaY = noiseObj.getY() - coreObj.getY();
		double sinTheta = deltaX / Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		double cosTheta = Math.sqrt(1 - sinTheta * sinTheta);

		double deltaVX = noiseObj.v.getVx() - coreObj.v.getVx();
		double deltaVY = noiseObj.v.getVy() - coreObj.v.getVy();
		double along = deltaVX * sinTheta + deltaVY * cosTheta;

		return refTime.plusSeconds((int) (objDist / along));
	}

	private static LocalTime getExpireTime(Cluster aCluster) {
		LocalTime resTime = null;
		boolean first = true;
		// MovingObject tempMO;
		ArrayList<MovingObject> objMembers = new ArrayList<MovingObject>(
				aCluster.members.size());
		for (Integer i : aCluster.members) {
			objMembers.add(objects.get(i));
		}
		for (MovingObject mo : objMembers) {
			if (mo.label) {
				// retrieve neighbors for the core object
				ArrayList<MovingObject> neighbors = DBScan.rangeQuery(mo,
						objects, eps);
				TimeObject[] tos = new TimeObject[neighbors.size()];
				int counter = 0;
				// order them by exit time
				for (MovingObject neighbor : neighbors) {
					LocalTime time = DBScan.getExitTime(eps, mo, neighbor,
							currTime);
					tos[counter++] = new TimeObject(time, neighbor);
				}
				Arrays.sort(tos);
				// get the m-2's exit time as expiry time
				if (first) {
					resTime = tos[minPts - 2].time;
				} else {
					if (resTime.isAfter(tos[minPts - 2].time)) {
						resTime = tos[minPts - 2].time;
					}
				}

			}
		}
		return resTime;
	}

	private static void buildClusters(ArrayList<Integer> u) {
		Cluster tempC;
		MovingObject mo;
		for (Integer i : u) {
			mo = allObjs.get(i);
			if (mo.cid > 0) {
				tempC = CS.get(mo.cid);

				if (tempC == null) {
					tempC = new Cluster(mo.cid);
					CS.put(mo.cid, tempC);
				}
				tempC.add(mo.oid);
			}
		}
		// fill in eventQ
		for (int key : CS.keySet()) {
			tempC = CS.get(key);

			// eventQ.add(new MyEvent(t, key));
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
	 */
	private static LocalTime getStartTime(
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

		return resTime;
	}

	/**
	 * 
	 * fill up containers
	 * 
	 * @param hm
	 */
	private static void fillup(HashMap<Integer, ArrayList<DataPoint>> hm) {
		// i is routeid
		for (Integer i : hm.keySet()) {
			DataPoint dp = hm.get(i).get(0);
			// // oid = routeid
			MovingObject mo = new MovingObject(dp.routeId, dp, new Velocity(
					dp.vx, dp.vy));
			allObjs.put(mo.oid, mo);
			objects.add(mo);
		}
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
		if (currTime.equals(Global.MINTIME)) {
			// put obj into their clusters

		} else {
			/**
			 * 2. deal with disappearing objects 3. deal with incoming objects
			 * */
			while (!eventQ.isEmpty() && eventQ.peek().time.isBefore(currTime)) {
				MyEvent evt = eventQ.poll();
				Cluster cluster = CS.get(evt.CID);
				// double duration = currTime - cluster.startTime;
				// cluster.updateScore(alpha, beta, gamma, duration);

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
		int gap = 10; // how long is a chunk (min)

		while (currTime.isBefore(maxTime)) {
			String currTimeStr = Data.converTimeToString(currTime);
			String nextTimeStr = Data.getNewTime(currTimeStr, gap, 0);

			HashMap<Integer, ArrayList<DataPoint>> hm = Data
					.getDefinedTrajectories(Global.testTable, currTimeStr,
							nextTimeStr, 0);

			/**
			 * 1. Assume there are >=m objects at the beginning<br>
			 * 2. fill up containers <br>
			 * 3. update base time <br>
			 */
			if (currTime.equals(new LocalTime(Global.MINTIME))) {
				System.out.println("system starts...");
				// start of the tracing
				fillup(hm);

				// 1. doDBScan
				// 2. fill up CS
				ClusterObjects();
			}

			// process these data
			process();
			currTime.plusMinutes(1);
		}
	}

	private static void ClusterObjects() {
		// TODO Auto-generated method stub
		buildClusters();
	}

	/**
	 * 
	 * @param cid
	 * @param cid2
	 */
	static void merge(int cid1, int cid2) {
		Cluster c1 = CS.get(cid1);
		Cluster c2 = CS.get(cid2);
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

		// double t = newCluster.getExpireTime();
		// eventQ.add(new MyEvent(t, newCluster.clusterId));

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
								minPts, currTime)) {
							// double time =
							// clusters.get(mo.cid).getExpireTime();
							// eventQ.add(new MyEvent(time, mo.cid));
						}
					} else if (o.cid != mo.cid && o.label) {
						merge(mo.cid, o.cid);
					}
				}
			}
		}
		if (U.size() >= minPts) {
			// DBScan.doDBScan(U, eps, minPts, allObjs);
			// put obj into their clusters
			buildClusters(U);
		}
	}

}
