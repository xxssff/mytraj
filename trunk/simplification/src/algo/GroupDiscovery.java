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
import entity.CombinationGenerator;
import entity.EventType;
import entity.Global;
import entity.MovingObject;
import entity.MyEvent;
import entity.TimeObject;
import entity.Velocity;

/**
 * TODO 1. update event queue
 * 
 * @author xiaohui
 * 
 */
public class GroupDiscovery {
	// containers
	// map id to movingObjects
	static HashMap<Integer, MovingObject> allObjs = new HashMap<Integer, MovingObject>();
	// assume we also have a container for all objects
	static ArrayList<MovingObject> OBJ = new ArrayList<MovingObject>();
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

	private static void buildTrie() {
		aTrie = new Trie();
		Cluster tempC;
		for (Integer i : CS.keySet()) {
			tempC = CS.get(i);
			for (int numEle = minPts; numEle < tempC.members.size(); numEle++) {
				String[] combinations = getCombination(tempC, minPts);
				aTrie.insert(combinations, currTime);
			}
		}
	}

	private static void ClusterObjects(ArrayList<Integer> U) {
		// convert integer into movingobjects
		MovingObject mo;
		ArrayList<MovingObject> objList = new ArrayList<MovingObject>();
		for (Integer i : U) {
			mo = allObjs.get(i);
			objList.add(mo);
		}
		DBScan.doDBScan(objList, eps, minPts, currTime);

	}

	/**
	 * Fill up clusters hashmap; <br>
	 * fill up eventQ
	 */
	private static void ClusterObjects() {
		DBScan.doDBScan(OBJ, eps, minPts, currTime);
		Cluster tempC;

		for (MovingObject mo : OBJ) {
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
		for (MovingObject mo : OBJ) {
			if (mo.cid == -1) {
				// insert join event for noise objects
				// range search for core objects
				// find the soonest cluster to join
				double range = 5 * eps;
				ArrayList<MovingObject> nei = DBScan.rangeQuery(mo, OBJ, range);
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

	static String[] getCombination(Cluster aCluster, int r) {

		Collections.sort(aCluster.members);
		int memSize = aCluster.members.size();
		Integer[] elements = aCluster.members.toArray(new Integer[memSize]);

		CombinationGenerator combGen = new CombinationGenerator(
				aCluster.members.size(), r);

		String[] resArr = new String[combGen.getTotal().intValue()];
		int counter = 0;
		StringBuffer combination;
		int[] indices;
		while (combGen.hasMore()) {
			combination = new StringBuffer();
			indices = combGen.getNext();
			for (int i = 0; i < indices.length; i++) {
				combination.append(elements[indices[i]]);
			}
			// System.out.println(combination.toString());
			resArr[counter++] = combination.toString();
		}
		return resArr;
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
			objMembers.add(OBJ.get(i));
		}
		for (MovingObject mo : objMembers) {
			if (mo.label) {
				// retrieve neighbors for the core object
				ArrayList<MovingObject> neighbors = DBScan.rangeQuery(mo, OBJ,
						eps);
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

	// private static void buildClusters(ArrayList<Integer> u) {
	// Cluster tempC;
	// MovingObject mo;
	// for (Integer i : u) {
	// mo = allObjs.get(i);
	// if (mo.cid > 0) {
	// tempC = CS.get(mo.cid);
	//
	// if (tempC == null) {
	// tempC = new Cluster(mo.cid);
	// CS.put(mo.cid, tempC);
	// }
	// tempC.add(mo.oid);
	// }
	// }
	// // fill in eventQ
	// for (int key : CS.keySet()) {
	// tempC = CS.get(key);
	//
	// // eventQ.add(new MyEvent(t, key));
	// }
	// }

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
			OBJ.add(mo);
		}
	}

	// time goes by gap variable
	// at every second if some events occur, process
	// otherwise go to next iteration
	public static void main(String[] args) throws Exception {

	}

	public static void doGroupDiscovery() throws Exception {
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
			} else {
				// process these data
				while (!eventQ.isEmpty()
						&& eventQ.peek().time.isBefore(currTime)) {
					MyEvent evt = eventQ.poll();
					Cluster cluster = CS.get(evt.CID);
					MovingObject mo = allObjs.get(evt.OID);
					ArrayList<Integer> U = new ArrayList<Integer>();
					// double duration = currTime - cluster.startTime;
					// cluster.updateScore(alpha, beta, gamma, duration);

					// check if cluster is a true candidate; TODO in trie update
					// checkCandidate(cluster);
					Trie.update(evt, R);

					if (evt.type == EventType.EXIT) {
						cluster.delete(mo.oid);
						mo.cid = 0;
						U.add(mo.oid);
						Insert(U);
					} else if (evt.type == EventType.JOIN) {
						cluster.add(mo.oid);
						mo.cid = cluster.clusterId;
						U.remove(mo);
						LocalTime t = getExitTime(mo, cluster);
						MyEvent event = new MyEvent(t, mo.oid,
								cluster.clusterId, EventType.EXIT);
						eventQ.add(event);
					} else if (evt.type == EventType.EXPIRE) {
						MovingObject tempMo;
						for (int i : cluster.members) {
							// remove cluster id for each object
							tempMo = allObjs.get(i);
							tempMo.cid = 0;
							U.add(tempMo.oid);
						}
						Insert(U);
					}
				}
			}
			currTime.plusMinutes(gap);
		}
	}

	/**
	 * 
	 * @param mo
	 * @param cluster
	 * @return the core object that longest together time
	 */
	private static LocalTime getExitTime(MovingObject mo, Cluster cluster) {
		MovingObject tempMo;
		LocalTime minTime = null;
		for (Integer i : cluster.members) {
			tempMo = OBJ.get(i);
			if (tempMo.label && tempMo.distance(mo) <= eps) {
				LocalTime t = DBScan.getExitTime(eps, mo, tempMo, currTime);
				if (minTime == null || minTime.isBefore(t)) {
					minTime = t;
				}
			}
		}
		return minTime;
	}

	/**
	 * 
	 * @param u2
	 * @param oBJ2
	 * @param eps2
	 * @param minPts2
	 * @param tau2
	 * @param r2
	 */
	private static void Insert(ArrayList<Integer> U) {
		ArrayList<Integer> G = new ArrayList<Integer>();
		MovingObject mo;
		for (Integer i : U) {
			mo = allObjs.get(i);
			ArrayList<MovingObject> L = DBScan.rangeQuery(mo, OBJ, eps);
			for (MovingObject tempMo : L) {
				if (tempMo.cid > 0) {
					G.add(tempMo.oid);
				}
			}
		}

		for (Integer i : G) {
			mo = allObjs.get(i);
			Cluster c = CS.get(mo.cid);
			ArrayList<MovingObject> L = DBScan.rangeQuery(mo, OBJ, eps);
			if (L.size() >= minPts) {
				L.remove(mo);
				mo.label = true;
				for (MovingObject tempMo : L) {
					if (tempMo.cid <= 0) {
						c.add(tempMo.oid);
						tempMo.cid = c.clusterId;
						aTrie.handleObjInsert(c, tempMo);
						U.remove(new Integer(tempMo.oid));
						DBScan.expandCluster(OBJ, tempMo, c.clusterId, eps,
								minPts, currTime);
					} else if (tempMo.cid != mo.cid && tempMo.label) {
						Cluster c1 = CS.get(tempMo.cid);
						aTrie.handleMerge(c1, c);
						c = merge(c, c1);
					}
				}
				LocalTime t = getExpireTime(c);
				MyEvent e = new MyEvent(t, -1, c.clusterId, EventType.EXPIRE);
				eventQ.add(e);
			}
		}
		if (U.size() >= minPts) {
			ClusterObjects(U);
		}
	}

	private static Cluster merge(Cluster c, Cluster c1) {
		// TODO Auto-generated method stub
		return null;
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

}
