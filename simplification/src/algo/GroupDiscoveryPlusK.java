package algo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.joda.time.LocalDateTime;
import org.joda.time.Seconds;

import trie.LeafEntry;
import trie.NumTrie;

import com.vividsolutions.jts.geom.Coordinate;

import data.Data;
import data.DataPoint;
import entity.CandidatesPlus;
import entity.Cluster;
import entity.ClusterEvolutionTable;
import entity.ConfReader;
import entity.EventType;
import entity.Global;
import entity.MovingObject;
import entity.MyEvent;
import entity.Statistics;

/**
 * 
 * 
 * 2011-04-24: finish groupDiscoveryPlus + clusterEvolveTable <br>
 * 2011-05-24: expand search and compare topK result <br>
 * 2011-07-09: test online time 2011-10-06: add leafentry similarity def <br>
 * TODO: run experiments with similarity.
 * 
 * @author xiaohui
 * 
 */
public class GroupDiscoveryPlusK {
	// containers
	// map id to movingObjects
	static HashMap<Integer, MovingObject> allObjs = new HashMap<Integer, MovingObject>();
	// assume we also have a container for all objects
	static ArrayList<MovingObject> OBJ = new ArrayList<MovingObject>();
	static CandidatesPlus cands;

	// map id to clusters
	static HashMap<Integer, Cluster> CS = new HashMap<Integer, Cluster>();
	static PriorityQueue<MyEvent> eventQ = new PriorityQueue<MyEvent>();
	static Statistics stats;
	/**
	 * real params
	 */
	static LocalDateTime currTime;
	static LocalDateTime systemMinTime, systemMaxTime;
	static String systemTable;
	static int eps = 800; // UTM coordinates distances
	static int minPts = 5;
	static int tau = 10; // seconds
	static int k = 10;
	static double theta = 0.3;
	static ClusterEvolutionTable cet;
	/**
	 * Test parameters
	 */
	// static LocalDateTime nextReadTime = new
	// LocalDateTime(Global.Test_MINTIME);
	// static LocalDateTime currTime = new LocalDateTime(Global.Test_MINTIME);
	// static LocalDateTime systemMinTime = new
	// LocalDateTime(Global.Test_MINTIME);
	// static LocalDateTime systemMaxTime = new
	// LocalDateTime(Global.Test_MAXTIME);
	// static String systemTable = Global.testTable;
	// static int eps = 50;
	// static int minPts = 3;
	// static int tau = 3; // seconds
	// static int gap = 60; // how long is a chunk (min)

	static Data dataSource = new Data();
	// hm hold data points of current chunk
	static HashMap<Integer, ArrayList<DataPoint>> hm;

	static double alpha = 1;
	static double beta = 1;

	/**
	 * cluster the moving objects in U
	 * 
	 * @param u
	 * @throws Exception
	 */
	private static void ClusterObjects(Set<Integer> u) throws Exception {
		// convert integer into movingobjects
		MovingObject mo;
		ArrayList<MovingObject> objList = new ArrayList<MovingObject>();
		for (Integer i : u) {
			mo = allObjs.get(i);
			if (mo == null) {
				throw new ObjectNullException();
			}
			objList.add(mo);
		}
		DBScan.doDBScan(objList, eps, minPts, currTime);

		// fill CS with new cluster
		Cluster tempC;
		Set<Integer> newClusters = new HashSet<Integer>();
		for (MovingObject tempMo : objList) {
			if (tempMo.cid > 0) {
				newClusters.add(tempMo.cid);
				tempC = CS.get(tempMo.cid);
				if (tempC == null) {
					tempC = new Cluster(tempMo.cid);
					CS.put(tempMo.cid, tempC);
					stats.numClusters++;
				}
				tempC.add(tempMo.oid);

				// compute exit time for both core and border objects
				if (tempMo.exitTime == null) {
					tempMo.exitTime = getExitTime(tempMo, tempC);

					// tempMo does not exit in near future
					if (tempMo.exitTime == null) {
						tempMo.exitTime = Global.infatiMaxDateTIME;
					}
				}

				MyEvent event = new MyEvent(tempMo.exitTime, tempMo.oid,
						tempC.clusterId, EventType.EXIT);
				eventQ.add(event);
			}
		}

		// insert into cet
		for (Integer i : newClusters) {
			tempC = CS.get(i);
			LeafEntry le = new LeafEntry(tempC.members, currTime);
			cet.add(i, le);
		}
	}

	/**
	 * 
	 * 
	 * @param hm
	 * @return list of new objects
	 */
	private static ArrayList<Integer> HmUpdateEventQNewData() {
		ArrayList<Integer> res = new ArrayList<Integer>();

		stats.numMos = hm.size();

		int totalPoints = 0;
		// i is routeid
		for (Integer i : hm.keySet()) {
			if (allObjs.get(i) == null) {
				// new comers
				res.add(i);
			}
			ArrayList<DataPoint> dps = hm.get(i);
			totalPoints += dps.size();

			if (dps != null) {
				// insert into eventQ
				for (DataPoint dp : dps) {
					if (Math.abs(dp.vx) == 0 && Math.abs(dp.vy) == 0
							&& dp.time0 == dps.get(dps.size() - 1).time0) {
						MyEvent e = new MyEvent(dp.dateTime, dp.routeId, -1,
								EventType.DISAPPEAR);
						eventQ.add(e);
					} else {
						MyEvent e = new MyEvent(dp.dateTime, dp.routeId, -1,
								EventType.UPDATE);
						eventQ.add(e);
					}
				}
			}
		}

		stats.numDataPoints = totalPoints;
		return res;
	}

	public static void main(String[] args) throws Exception {
		/**
		 * Get parameters from conf file
		 * 
		 */
		System.out
				.println("===============Group Discovery Plus UB==================");
		ConfReader reader = new ConfReader();
		HashMap<String, String> conf = reader.read(args[0]);

		BufferedWriter bw = new BufferedWriter(new FileWriter(conf
				.get("outFile")));
		bw.write("Group Discovery Plus UB Output=====");
		bw.newLine();

		eps = Integer.parseInt(conf.get("eps"));
		minPts = Integer.parseInt(conf.get("minPts"));
		tau = Integer.parseInt(conf.get("tau"));
		k = Integer.parseInt(conf.get("k"));
		alpha = Double.parseDouble(conf.get("alpha"));
		beta = Double.parseDouble(conf.get("beta"));
		theta = Double.parseDouble(conf.get("theta"));
		systemTable = conf.get("systemTable");
		int tolerance = Integer.parseInt(systemTable.substring(systemTable
				.lastIndexOf("_") + 1));

		System.out.println("e:" + eps + "\t" + "m:" + minPts + "\t" + "tau:"
				+ tau + "\tk:" + k);
		stats = new Statistics();
		cands = new CandidatesPlus(k, stats);

		cet = new ClusterEvolutionTable(minPts);

		String ts = conf.get("ts");
		String te = conf.get("te");
		systemMinTime = new LocalDateTime(ts);
		systemMaxTime = new LocalDateTime(te);
		currTime = systemMinTime;

		long t_start = System.currentTimeMillis();
		doGroupDiscovery(tolerance, ts, te, bw);
		long t_end = System.currentTimeMillis();
		// write candidates into result file
		stats.startTime = conf.get("ts");
		stats.endTime = conf.get("te");

		stats.elapsedTime = (t_end - t_start) / 1000.0;
		stats.numGroups = cands.size();

		// write param to file
		bw.write("m: " + minPts + "; e: " + eps + "; tau:" + tau + "; k:" + k
				+ "; theta:" + theta);
		bw.newLine();
		bw.write("table: " + systemTable);
		bw.newLine();
		bw.write(ts + "; " + te);
		bw.newLine();

		stats.toFile(bw);
		cands.toFile(bw);

		bw.close();
	}

	public static void doGroupDiscovery(int tolerance, String startTime,
			String endTime, BufferedWriter bw) throws Exception {
		/**
		 * 1. fill up containers <br>
		 * 2. update base time <br>
		 */
		// initial run
		// enlarge range search according to lemma
		eps += 2 * tolerance;

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
		// start of the tracing
		// no need to do cluster after filling up
		// because subsequent update event takes care of
		// sync and cluster.
		HmUpdateEventQNewData();

		System.out.println();
		System.out.println("After init:");
		System.out.println("HM size: " + hm.size());
		System.out.println("eventQ size: " + eventQ.size());
		// printEventQ();
		System.out.println("=========");

		// process these events
		while (!eventQ.isEmpty()) {
			long mem1 = Runtime.getRuntime().totalMemory()
					- Runtime.getRuntime().freeMemory();
			if (mem1 > stats.memUsage) {
				stats.memUsage = mem1;
			}

			// process events
			MyEvent evt = eventQ.poll();
			// move time to event time
			if (evt.time.isAfter(currTime)) {
				currTime = evt.time;
			}
			System.out.println("curr Time: " + currTime);
			System.out.println(evt.toString());

			if (currTime.isAfter(systemMaxTime)) {
				OBJ.clear();
				allObjs.clear();
				eventQ.clear();
			}

			syncMovingObjects();

			if (evt.type == EventType.DISAPPEAR) {
				MovingObject mo = allObjs.get(evt.OID);
				if (mo != null) {
					handleDisappear(mo);
				}
			} else {
				/**
				 * U has unclassified objects
				 */
				Set<Integer> U = new TreeSet<Integer>();
				// insert noise and unclassified data into U
				for (MovingObject tempMo : OBJ) {
					if (tempMo.cid <= 0) {
						U.add(tempMo.oid);
					}
				}

				if (evt.type == EventType.UPDATE) {
					System.out.println("Handling update...");
					MovingObject mo = null;
					if (allObjs.get(evt.OID) == null) {
						// new comers
						mo = new MovingObject(evt.OID, hm.get(evt.OID).get(0));
						allObjs.put(mo.oid, mo);
						OBJ.add(mo);
					} else {
						mo = allObjs.get(evt.OID);
					}

					// if (mo.cid > 0 && CS.get(mo.cid)!=null) {
					if (mo.cid > 0) {
						// mo belongs to a cluster
						// update the cluster of mo
						System.out.println("mo.cid" + mo.cid);
						Cluster cluster = CS.get(mo.cid);
						int n1 = cluster.members.size();
						handleUpdateInCluster(mo, cluster, U);
						int n2 = cluster.members.size();

						// update cet
						if (n1 != n2) {
							List<LeafEntry> list = cet.get(mo.cid);
							LeafEntry oldLe = list.get(list.size() - 1);
							oldLe.endCluster(currTime, alpha, beta);
							LeafEntry le1 = new LeafEntry(cluster.members,
									currTime);
							list.add(le1);

						}

					} else {
						// mo is unclassified
						U.add(evt.OID);
					}
					Insert(U);

				} else {
					// Exit event
					if (evt.type == EventType.EXIT) {
						System.out.println("Handling exit...");
						// check it is real exit
						handleExitEvent(evt, U);
					}
					// // Join event
					// else if (evt.type == EventType.JOIN) {
					// handleJoinEvent(evt, U);
					// }

					// Expire event: come from handleExit
					// no need to end the last entry, because it is already
					// handled in hanleExit
					else if (evt.type == EventType.EXPIRE) {
						System.out.println("Handling expire event...");
						MovingObject mo = allObjs.get(evt.OID);
						Cluster cluster = CS.get(evt.CID);
						// cluster really expires
						// get combinations for old cluster
						// for each member, get its new member cluster
						// intersect to detect those that are still
						// traveling
						// tgr

						mo.cid = 0;
						mo.label = false;

						MovingObject tempMo = null;
						for (int i : cluster.members) {
							// remove cluster id for each object
							tempMo = allObjs.get(i);
							tempMo.cid = 0;
							tempMo.label = false;
							U.add(tempMo.oid);
							removeFromEventQ(mo.oid, cluster.clusterId,
									EventType.EXIT);
						}
						// remove from CS and eventQ
						CS.remove(cluster.clusterId);
						Insert(U);
					}
				}

				// handle candidate list
				// System.out.println("After event:");
				// printMutalDistance();
				printCluster();
				// if(!CS.isEmpty()){
				// System.exit(0);
				// }

				// if(evt.OID==87812 && evt.type==EventType.EXPIRE){
				// System.exit(0);
				// }

				// printEventQ();
				// printTrie();
				// printR();
			}
		}// end while eventQ!=empty

		// close database connection
		dataSource.closeConnection();

		stats.numCandidates = cet.getTotalSize();
		List<LeafEntry> canEntries = ClusterEvolutionTable.pushIntoCandsUB(cet,
				cands, tau, k, alpha, beta, theta);

		// sort list

		if (canEntries != null) {
			Collections.sort(canEntries);
			List<LeafEntry> topK = new ArrayList<LeafEntry>(k);
			int count = 0;
			for (LeafEntry le : canEntries) {
				if (count >= k) {
					break;
				}
				topK.add(le);
				count++;
			}

			// compute average similarity among topK
			stats.avgSimilarity = getAvgSimilarity(topK);

			for (LeafEntry le : topK) {
				bw.write(le.toString());
				bw.newLine();
			}
		}
	}

	/**
	 * @param resultEntries
	 * @return the average similarity among topK members
	 */
	private static double getAvgSimilarity(List<LeafEntry> resultEntries) {
		LeafEntry[] array = resultEntries.toArray(new LeafEntry[0]);
		double sum = 0.0;
		int size = array.length;
		for (int i = 0; i < array.length; i++) {
			for (int j = i + 1; j < array.length; j++) {
				sum += array[i].getSimilarity(array[j]);
			}
		}
		return sum / (size * (size + 1));
	}

	/**
	 * sync moving objects, get expected position for each mo at current
	 * timestamp
	 */
	private static void syncMovingObjects() {
		for (MovingObject tempMo : OBJ) {
			DataPoint dp = getExpectedDataPoint(tempMo.oid, currTime);
			tempMo.setDataPoint(dp);
		}
	}

	/**
	 * Method to handle Exit Event
	 * 
	 * @throws Exception
	 */
	private static void handleExitEvent(MyEvent evt, Set<Integer> U)
			throws Exception {
		MovingObject mo = allObjs.get(evt.OID);
		Cluster cluster = CS.get(evt.CID);
		if (mo == null || cluster == null) {
			return;
		}
		ArrayList<MovingObject> neighbors = DBScan.rangeQuery(mo, OBJ, eps);
		if (!hasCoreNeighbor(mo, neighbors)) {
			List<LeafEntry> list = cet.get(cluster.clusterId);
			list.get(list.size() - 1).endCluster(currTime, alpha, beta);

			cluster.delete(mo.oid);
			mo.cid = 0;
			updateClusterStatus(cluster);

			// if cluster itself expires
			if (!haveEnoughMembers(cluster)) {

				cluster.expiryTime = currTime;
				cluster.expireOID = mo.oid;
				updateEventQ(cluster);
			} else {
				LeafEntry le = new LeafEntry(cluster.members, currTime);
				list.add(le);
			}

			// try to cluster U
			for (MovingObject tempMo : OBJ) {
				if (tempMo.cid <= 0) {
					U.add(mo.oid);
				}
			}
			Insert(U);

		} else {
			// mo not really exit
			// recompute exit time
			mo.exitTime = getExitTime(mo, cluster);
			if (mo.exitTime != null) {
				eventQ.add(new MyEvent(mo.exitTime, mo.oid, mo.cid,
						EventType.EXIT));
			}
		}
	}

	/**
	 * check if every member is still connected update core or border <br>
	 * set each member's exit time <br>
	 * 
	 * @param cluster
	 */
	private static void updateClusterStatus(Cluster cluster) {
		List<Integer> toDel = new ArrayList<Integer>(cluster.members.size());

		for (Integer i : cluster.members) {
			MovingObject mo = allObjs.get(i);
			ArrayList<MovingObject> neighbors = DBScan.rangeQuery(mo, OBJ, eps);
			if (neighbors.size() >= minPts) {
				mo.label = true;
				mo.exitTime = getExitTime(mo, cluster);
				updateEventQ(mo);
			} else {
				if (!hasCoreNeighbor(mo, neighbors)) {
					toDel.add(i);
					mo.cid = 0;
				} else {
					// border obj
					mo.label = false;
					mo.exitTime = getExitTime(mo, cluster);
					if (mo.exitTime != null)
						updateEventQ(mo);
				}
			}
		}

		for (Integer i : toDel) {
			cluster.delete(i);
		}
	}

	/**
	 * 
	 * @param cluster
	 * @return true if the cluster has at least m members
	 */
	private static boolean haveEnoughMembers(Cluster cluster) {
		MovingObject tempMo;
		if (cluster.members.size() < minPts) {
			return false;
		}
		for (Integer i : cluster.members) {
			tempMo = allObjs.get(i);
			if (tempMo.label) {
				ArrayList<MovingObject> neis = DBScan.rangeQuery(tempMo, OBJ,
						eps);
				if (neis.size() >= minPts) {
					return true;
				}
			}
		}
		return false;
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
	 * 
	 * @param mo
	 * @param neighbors
	 * @return true if one of mo's neighbors is a core object from same cluster
	 */
	private static boolean hasCoreNeighbor(MovingObject mo,
			ArrayList<MovingObject> neighbors) {
		for (MovingObject mm : neighbors) {
			if (mm.cid == mo.cid && mm.label) {
				return true;
			}
		}
		return false;
	}

	/**
	 * mo was a member of cluster c; check current status of mo <br>
	 * 
	 * @param mo
	 * @param cluster
	 * @throws Exception
	 */
	private static void handleUpdateInCluster(MovingObject mo, Cluster cluster,
			Set<Integer> u) throws Exception {
		if (!mo.label) {
			// mo was a border obj
			ArrayList<MovingObject> neighbors = DBScan.rangeQuery(mo, OBJ, eps);
			if (neighbors.size() >= minPts) {
				// mo is promoted into a core obj
				// expand from mo
				mo.label = true;
				expandClusterMember(mo, neighbors, u);
			} else {
				// mo was a border obj
				// check if there is a core obj in neighbors
				boolean connected = false;
				boolean exitTimeChanged = false;
				for (MovingObject nei : neighbors) {
					if (nei.label && nei.cid == mo.cid) {
						// still a border obj
						// re-compute mo's exit time
						LocalDateTime t1 = DBScan.getExitTime(eps, mo, nei,
								currTime);
						if (t1 == null) {
							t1 = Global.currMaxDateTIME;
						}
						if (mo.exitTime == null || mo.exitTime.isAfter(t1)) {
							mo.exitTime = t1;
							exitTimeChanged = true;
						}
						connected = true;
					}
				}
				if (!connected) {
					// handle mo exit as a border
					// by updating eventQ
					// System.err.println("border obj exit");
					mo.exitTime = currTime;
					updateEventQ(mo);
				} else if (exitTimeChanged) {
					updateEventQ(mo);
				}
			}
		} else {
			// mo was a core obj
			ArrayList<MovingObject> neighbors = DBScan.rangeQuery(mo, OBJ, eps);
			if (neighbors.size() >= minPts) {
				// still a core obj
				mo.exitTime = getExitTime(mo, cluster);
				updateEventQ(mo);
			} else {
				// no longer a core obj
				// Note: assume obj is border obj
				boolean connected = false;
				for (MovingObject nei : neighbors) {
					if (nei.label && nei.cid == mo.cid) {
						LocalDateTime t1 = DBScan.getExitTime(eps, mo, nei,
								currTime);
						if (t1 == null) {
							t1 = Global.currMaxDateTIME;
						}
						if (mo.exitTime == null || mo.exitTime.isAfter(t1)) {
							mo.exitTime = t1;
						}
						connected = true;
					}
				}
				if (connected) {
					// mo is a border member
					mo.label = false;
					updateEventQ(mo);
				} else {
					// mo becomes unclassified
					mo.exitTime = currTime;
					updateEventQ(mo);
				}
			}
		}
	}

	/**
	 * 
	 * @param combination
	 * @return true if all objects in combination are in the same cluster
	 */
	private static boolean inSameCluster(Integer[] combination) {
		int cid = allObjs.get(combination[0]).cid;
		if (cid == 0) {
			return false;
		}
		for (int i = 1; i < combination.length; i++) {
			if (allObjs.get(combination[i]).cid != cid) {
				return false;
			}
		}
		return true;
	}

	/**
	 * handle disappearing objects
	 * 
	 * @param mo
	 * @throws Exception
	 */
	private static void handleDisappear(MovingObject mo) throws Exception {
		if (mo.cid > 0) {
			System.out.println(CS);
			System.out.println(CS.isEmpty());

			Cluster cluster = CS.get(mo.cid);
			if (cluster == null) {
				return;
			}

			// update cet
			// trieUpdate(cluster, mo.oid, 0);
			List<LeafEntry> list = cet.get(mo.cid);
			LeafEntry last = list.get(list.size() - 1);
			last.endCluster(currTime, alpha, beta);

			cluster.delete(mo.oid);
			updateClusterStatus(cluster);

			// if cluster itself expires
			if (cluster.members.size() < minPts) {
				System.out.println("cluster expires due to disappear");
				cluster.expiryTime = currTime;
				cluster.expireOID = mo.oid;
				updateEventQ(cluster);
			} else {
				// create new LE with current time
				LeafEntry le = new LeafEntry(cluster.members, currTime);
				cet.add(mo.cid, le);
			}
		}
		OBJ.remove(mo);
		allObjs.remove(mo);
	}

	static void removeFromEventQ(int moid, int cid, EventType type) {
		Iterator<MyEvent> eventIte = eventQ.iterator();
		MyEvent indexEvt = null;
		boolean found = false;
		while (eventIte.hasNext()) {
			MyEvent evt = eventIte.next();
			if (evt.OID == moid && evt.CID == cid && type == EventType.EXIT) {
				indexEvt = evt;
				found = true;

				// if(evt.CID == 190 && evt.OID==91){
				// printEventQ();
				// System.exit(0);
				// }

				break;
			}
		}
		if (found) {
			eventQ.remove(indexEvt);
		}

	}

	/**
	 * update the eventQ when an object's exit time changes
	 * 
	 * @param cluster
	 * @param b
	 */
	private static void updateEventQ(MovingObject mo) {
		// iterate the eventQ for the cluster's expire event
		Iterator<MyEvent> eventIte = eventQ.iterator();
		MyEvent indexEvt = null;
		boolean found = false; // to prevent that mo is not in the eventQ
		while (eventIte.hasNext()) {
			MyEvent evt = eventIte.next();
			if (evt.type == EventType.EXIT && evt.OID == mo.oid) {
				indexEvt = evt;
				found = true;
				break;
			}
		}
		if (found) {
			eventQ.remove(indexEvt);
		}
		if (mo.exitTime != null) {
			LocalDateTime exitTime = mo.exitTime;
			if (exitTime.isBefore(currTime)) {
				exitTime = currTime.plusSeconds(1);
			}
			MyEvent newEvt = new MyEvent(exitTime, mo.oid, mo.cid,
					EventType.EXIT);
			eventQ.add(newEvt);
		}
	}

	/**
	 * update the eventQ when a cluster's expire time changes
	 * 
	 * @param cluster
	 * @param b
	 */
	private static void updateEventQ(Cluster cluster) {
		// iterate the eventQ for the cluster's expire event
		Iterator<MyEvent> eventIte = eventQ.iterator();
		MyEvent indexEvt = null;
		boolean found = false;
		while (eventIte.hasNext()) {
			MyEvent evt = eventIte.next();
			if (evt.type == EventType.EXPIRE && evt.CID == cluster.clusterId) {
				indexEvt = evt;
				found = true;
				break;
			}
		}
		if (found) {
			eventQ.remove(indexEvt);
		}

		MyEvent newEvt = new MyEvent(cluster.expiryTime, cluster.expireOID,
				cluster.clusterId, EventType.EXPIRE);
		eventQ.add(newEvt);
	}

	private static DataPoint getExpectedDataPoint(int routeId,
			LocalDateTime time) {
		ArrayList<DataPoint> dPoints = hm.get(routeId);
		for (DataPoint dp : dPoints) {
			if (dp.dateTime.equals(time)) {
				return dp;
			}
		}

		DataPoint dp1 = dPoints.get(0);
		DataPoint dp2 = dPoints.get(0);
		if (dp1.dateTime.isAfter(time)) {
			// new chunk
			// get expected data point with velocity
			int secBetween = Seconds.secondsBetween(time, dp1.dateTime)
					.getSeconds();

			double x = dp1.p.x - dp1.vx * secBetween;
			double y = dp1.p.y - dp1.vy * secBetween;

			DataPoint newP = new DataPoint(dp1.routeId, new Coordinate(x, y),
					dp1.vx, dp1.vy, time, dp1.time0 - secBetween);

			return newP;
		}
		for (DataPoint dp : dPoints) {
			if (dp.dateTime.equals((time))) {
				return dp;
			} else if (dp.dateTime.isBefore(time)) {
				dp1 = dp;
			} else if (dp.dateTime.isAfter(time)) {
				dp2 = dp;
				break;
			}
		}
		DataPoint res = dataSource.getImaginaryPoint(dp1, dp2, time);
		return res;
	}

	/**
	 * 
	 * @param mo
	 * @param cluster
	 * @return the core object that longest together time
	 */
	private static LocalDateTime getExitTime(MovingObject mo, Cluster cluster) {
		MovingObject tempMo;
		LocalDateTime minTime = null;
		for (Integer i : cluster.members) {
			if (mo.oid != i) {
				tempMo = allObjs.get(i);
				if (tempMo.label && tempMo.distance(mo) <= eps) {
					LocalDateTime t = DBScan.getExitTime(eps, mo, tempMo,
							currTime);
					if (t == null) {
						t = Global.currMaxDateTIME;
					}
					if (minTime == null || minTime.isBefore(t)) {
						minTime = t;
					}
				}
			}
		}
		return minTime;
	}

	/**
	 * Insert unclassified objects as in the paper
	 * 
	 * @throws Exception
	 */
	public static void Insert(Set<Integer> u) throws Exception {
		Set<Integer> G = new HashSet<Integer>();
		MovingObject mo;
		for (Integer i : u) {
			mo = allObjs.get(i);
			ArrayList<MovingObject> L = DBScan.rangeQuery(mo, OBJ, eps);
			L.remove(mo);
			for (MovingObject tempMo : L) {
				if (tempMo.cid > 0) {
					G.add(tempMo.oid);
				}
			}
		}
		for (Integer i : G) {
			mo = allObjs.get(i);
			Cluster cluster = CS.get(mo.cid);
			ArrayList<MovingObject> L = DBScan.rangeQuery(mo, OBJ, eps);
			// System.out.println("i L: " + i + " " + L);
			if (L.size() >= minPts) {
				L.remove(mo);
				mo.label = true;
				if (expandClusterMember(mo, L, u)) {
					// update cet
					List<LeafEntry> list = cet.get(mo.cid);
					list.get(list.size() - 1).endCluster(currTime, alpha, beta);
					LeafEntry le = new LeafEntry(cluster.members, currTime);
					list.add(le);
					updateClusterStatus(cluster);
				}
			}

		}
		// if (currTime.equals(new LocalDateTime("22:49:50"))) {
		// System.out.println("G:" + G);
		// printCluster();
		// System.exit(0);
		// }

		System.out.println("Cluster U...");
		if (u.size() >= minPts) {
			ClusterObjects(u);
		}
	}

	/**
	 * Expand coreObj's cluster from coreObj
	 * 
	 * @param objects
	 * @param coreObj
	 * @param neighbors
	 * @param u
	 * @throws Exception
	 */
	static boolean expandClusterMember(MovingObject coreObj,
			ArrayList<MovingObject> neighbors, Set<Integer> u) throws Exception {
		boolean inced = false;
		Cluster c = CS.get(coreObj.cid);
		for (MovingObject tempMo : neighbors) {
			if (tempMo.cid <= 0) {
				// set unclassified obj to classified
				c.add(tempMo.oid);
				tempMo.cid = c.clusterId;
				inced = true;
				u.remove(new Integer(tempMo.oid));

				ArrayList<MovingObject> L = DBScan.rangeQuery(tempMo, OBJ, eps);
				if (L.size() >= minPts) {
					// tempMo is another core obj
					L.remove(tempMo);
					tempMo.label = true;
					expandClusterMember(tempMo, L, u);
				} else {
					// tempMo is border obj
					tempMo.exitTime = DBScan.getExitTime(eps, tempMo, coreObj,
							currTime);
					if (tempMo.exitTime == null) {
						tempMo.exitTime = Global.currMaxDateTIME;
					}
					// update eventQ
					updateEventQ(tempMo);
				}

			} else if (tempMo.cid != coreObj.cid && tempMo.label) {
				// insert members of c1 into c
				Cluster c1 = CS.get(tempMo.cid);
				merge(c, c1);
				inced = true;
			}
		}
		return inced;
	}

	/**
	 * add cluster c1 into c
	 * 
	 * @param c
	 * @param c1
	 * @return
	 */
	private static void merge(Cluster c, Cluster c1) {
		for (Integer i : c1.members) {
			c.add(i);
			allObjs.get(i).cid = c.clusterId;
		}
		// delete c1 from CS
		CS.remove(c1.clusterId);
		c1.members = null;
	}

	static void printMutalDistance() {
		int i = 0;
		for (; i < OBJ.size(); i++) {
			for (int j = i + 1; j < OBJ.size(); j++) {
				MovingObject mo1 = OBJ.get(i);
				MovingObject mo2 = OBJ.get(j);
				System.out.println("Dist(" + mo1.oid + "," + mo2.oid + ")="
						+ mo1.distance(mo2));
			}
		}
	}

	static void printCluster() {
		System.out.println("Printing CS...");
		for (Integer i : CS.keySet()) {
			Cluster c = CS.get(i);
			System.out.println("cid size " + i + " " + c.members.size() + ":"
					+ " " + c.members);
		}
	}

	static void printEventQ() {
		Iterator<MyEvent> ite = eventQ.iterator();
		while (ite.hasNext()) {
			System.out.println("evt: " + ite.next().toString());
		}
	}

	static void printR() {
		System.out.println("Candidates: " + cands.toString());
	}

}
