package algo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Seconds;

import trie.LeafEntry;
import trie.NumEdge;
import trie.NumNode;
import trie.NumTrie;

import com.vividsolutions.jts.geom.Coordinate;

import data.Data;
import data.DataPoint;
import entity.CandidatesPlus;
import entity.Cluster;
import entity.CombinationGenerator;
import entity.EventType;
import entity.Global;
import entity.MovingObject;
import entity.MyEvent;
import entity.Statistics;

/**
 * 
 * 2001-03-31: change from string Trie to NumTrie <br>
 * 2011-04-02: examine trie when program terminates<br>
 * 2011-04-03: fill up OBJ and allObjs when update happens<br>
 * 2011-04-03: add boolean field to datapoint to indicate the end of a
 * trajectory <br>
 * 2011-04-04: refactor expandClusterMember method <br>
 * 2011-04-04: add handleUpdateInCluster method <br>
 * 2011-04-04: remove Breaker class<br>
 * 2011-04-04: change how results are added into result list<br>
 * 2011-04-04: read from conf file 2011-04-04: remove trajectories shorter than
 * 10s <br>
 * 2011-04-06: implement average distance for a cluster <br>
 * 2011-04-06: implement domination. implemented in two parts:<br>
 * 1. when obj exits or cluster expires <br>
 * 2. when candidates are checked <br>
 * 2011-04-06: statistics <br>
 * 2011-04-22: update handleExit <br>
 * 2011-04-22: remove setExpireTime for cluster <br>
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
	static CandidatesPlus R;

	// map id to clusters
	static HashMap<Integer, Cluster> CS = new HashMap<Integer, Cluster>();
	static PriorityQueue<MyEvent> eventQ = new PriorityQueue<MyEvent>();
	static Statistics stats;
	/**
	 * real params
	 */
	static LocalDateTime currTime = new LocalDateTime(Global.infati_MINTIME);
	static LocalDateTime systemMaxTime = new LocalDateTime(
			Global.infati_MAXTIME);
	static LocalDateTime systemMinTime = new LocalDateTime(
			Global.infati_MINTIME);
	static String systemTable = Global.infatiTable;
	static int eps = 800; // UTM coordinates distances
	static int minPts = 5;
	static int tau = 10; // seconds
	static int gap = 300; // how long is a chunk (min)

	/**
	 * Test parameters
	 */
	// static LocalTime nextReadTime = new LocalTime(Global.Test_MINTIME);
	// static LocalTime currTime = new LocalTime(Global.Test_MINTIME);
	// static LocalTime systemMinTime = new LocalTime(Global.Test_MINTIME);
	// static LocalTime systemMaxTime = new LocalTime(Global.Test_MAXTIME);
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
	static double gamma = 1;
	static NumTrie aTrie = new NumTrie();;

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
						tempMo.exitTime = Global.currMaxDateTIME;
					}
				}

				MyEvent event = new MyEvent(tempMo.exitTime, tempMo.oid,
						tempC.clusterId, EventType.EXIT);
				eventQ.add(event);

			}
		}

		// insert into trie

		for (Integer i : newClusters) {
			tempC = CS.get(i);
			updateTrie(tempC);
			// setExpireTime(tempC);
			// // insert into eventQ
			// eventQ.add(new MyEvent(tempC.expiryTime, tempC.expireOID,
			// tempC.clusterId, EventType.EXPIRE));
		}
	}

	/**
	 * called when a new cluster is formed.
	 * 
	 * @param cluster
	 */
	private static void updateTrie(Cluster cluster) {
		System.out.println("Inserting into Trie...");
		int memSize = cluster.members.size();
		Integer[] elements = cluster.members.toArray(new Integer[memSize]);

		for (int numEle = minPts; numEle <= memSize; numEle++) {
			CombinationGenerator combGen = new CombinationGenerator(memSize,
					numEle);

			Integer[] combination;
			int[] indices;

			while (combGen.hasMore()) {
				combination = new Integer[numEle];
				indices = combGen.getNext();
				for (int i = 0; i < indices.length; i++) {
					combination[i] = elements[indices[i]];
				}
				double avgDist = getAvgDist(combination);
				aTrie.insert(avgDist, combination, currTime);
			}
		}
	}

	/**
	 * 
	 * @param aCluster
	 * @param r
	 * @return
	 */
	static Integer[][] getCombination(Cluster aCluster, int r) {
		int memSize = aCluster.members.size();
		Integer[] elements = aCluster.members.toArray(new Integer[memSize]);

		CombinationGenerator combGen = new CombinationGenerator(
				aCluster.members.size(), r);

		Integer[][] resArr = new Integer[combGen.getTotal().intValue()][];
		int counter = 0;
		Integer[] combination;
		int[] indices;
		// System.out.println("combGen size:" + combGen.getTotal());
		while (combGen.hasMore()) {
			combination = new Integer[r];
			indices = combGen.getNext();
			for (int i = 0; i < indices.length; i++) {
				combination[i] = elements[indices[i]];
			}

			resArr[counter++] = combination;
		}
		return resArr;
	}

	// /**
	// *
	// * @param aCluster
	// * @param r
	// * @throws Exception
	// */
	// static void getCombinationExternal(Cluster aCluster, int r)
	// throws Exception {
	// int memSize = aCluster.members.size();
	// Integer[] elements = aCluster.members.toArray(new Integer[memSize]);
	//
	// CombinationGenerator combGen = new CombinationGenerator(
	// aCluster.members.size(), r);
	//
	// String[] combination;
	// int[] indices;
	// System.out.println("combGen size:" + combGen.getTotal());
	// while (combGen.hasMore()) {
	// combination = new String[r];
	// indices = combGen.getNext();
	// for (int i = 0; i < indices.length; i++) {
	// combination[i] = elements[indices[i]].toString();
	// }
	// // bw.append();
	// out.writeObject(combination);
	// }
	// }

	private static LocalTime getEnterTime(int range, MovingObject noiseObj,
			MovingObject coreObj, LocalTime refTime) {

		// compute distances
		double objDist = noiseObj.distance(coreObj) - range;

		// compute relative velocities
		double deltaX = noiseObj.getX() - coreObj.getX();
		double deltaY = noiseObj.getY() - coreObj.getY();
		double sinTheta = deltaX / Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		double cosTheta = Math.sqrt(1 - sinTheta * sinTheta);

		double deltaVX = noiseObj.getVx() - coreObj.getVx();
		double deltaVY = noiseObj.getVy() - coreObj.getVy();
		double along = deltaVX * sinTheta + deltaVY * cosTheta;

		return refTime.plusSeconds((int) (objDist / along));
	}

	/**
	 * If leafEntry is a candidate, put into R <br>
	 * R updates its minScore if necessary
	 * 
	 * @param cluster
	 */
	// private static void checkCandidate(LeafEntry entry) {
	// entry.computeDuration();
	// entry.setScore(alpha, beta, gamma);
	// System.out.println(entry.toString());
	//
	// if (entry.ts.plusSeconds(tau).isBefore(entry.te)) {
	//
	// // true candidate
	// if (R.candidates.size() < k) {
	// R.candidates.add(entry);
	// if (entry.getScore() < R.minScore) {
	// R.minScore = entry.getScore();
	// }
	// } else if (entry.getScore() > R.minScore) {
	// // R.removeCandidates(R.minScore);
	//
	// R.candidates.add(entry);
	// R.updateMinScore();
	// } else if (entry.getScore() == R.minScore) {
	// R.candidates.add(entry);
	// }
	// }
	// }

	private static void checkCandidate(LeafEntry entry) {
		stats.numCandidates++;
		double avgDistEnd = getAvgDist((Integer[]) entry.subCluster);
		entry.endCluster(currTime, avgDistEnd, alpha, beta, gamma);
		// System.out.println(entry.toString());
		if (entry.getDuration() >= tau) {
			R.add(entry);
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
					if (Math.abs(dp.vx) < 0.1 && Math.abs(dp.vy) < 0.1) {
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
		System.out.println("===============Group Discovery==================");
		BufferedReader br = new BufferedReader(new FileReader(args[0]));

		// loop meta infor
		String line;
		while ((line = br.readLine()).startsWith("#")) {
			// do nothing
		}
		String outFile = line;
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		bw.write("Group Discovery Output=====");
		bw.newLine();

		systemTable = br.readLine();
		eps = Integer.parseInt(br.readLine());
		minPts = Integer.parseInt(br.readLine());
		tau = Integer.parseInt(br.readLine());
		int k = Integer.parseInt(br.readLine());
		System.out.println("e:" + eps + "\t" + "m:" + minPts + "\t" + "tau:"
				+ tau + "\tk:" + k);

		StringTokenizer st = new StringTokenizer(br.readLine(), " ");
		alpha = Double.parseDouble(st.nextToken());
		beta = Double.parseDouble(st.nextToken());
		gamma = Double.parseDouble(st.nextToken());

		st = new StringTokenizer(br.readLine(), " ");
		String ts = st.nextToken();
		String te = st.nextToken();

		R = new CandidatesPlus();
		stats = new Statistics();

		long t_start = System.currentTimeMillis();
		doGroupDiscovery(ts, te, bw);
		long t_end = System.currentTimeMillis();
		// write candidates into result file
		stats.startTime = ts;
		stats.endTime = te;

		stats.elapsedTime = (t_end - t_start) / 1000.0;
		stats.numGroups = R.size();

		stats.toFile(bw);
		R.toFile(bw);

		br.close();
		bw.close();
	}

	public static void doGroupDiscovery(String startTime, String endTime,
			BufferedWriter bw) throws Exception {
		// get a big chunk of data from database
		/**
		 * 1. fill up containers <br>
		 * 2. update base time <br>
		 */
		// initial run
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

			// process events
			MyEvent evt = eventQ.poll();
			System.out.println(evt);
			// if (evt.CID == 15 && evt.type == EventType.EXIT) {
			// System.out.println(evt.toString());
			// Cluster c = CS.get(15);
			// for (Integer i : c.members) {
			// System.out.println(allObjs.get(i));
			// System.out.println(i + " exit time: "
			// + getExitTime(allObjs.get(i), c));
			// }
			// System.out.println("c expiry time:" + c.expiryTime);
			// }

			// System.out.println(evt.toString());
			System.out.println("CS size: " + CS.size());

			// move time to event time
			if (evt.time.isAfter(currTime)) {
				currTime = evt.time;
			}
			System.out.println("curr Time: " + currTime);
			if (currTime.equals(LocalTime.MIDNIGHT.minusSeconds(1))) {
				// stop the process
				// examine entires in trie
				trieFlush(aTrie.getRoot(), endTime);
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
				// System.out.println("OBJ: " + OBJ);

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

					if (mo.cid > 0) {
						// mo belongs to a cluster
						// update the cluster of mo
						Cluster cluster = CS.get(mo.cid);
						handleUpdateInCluster(mo, cluster, U);
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
					// Join event
					else if (evt.type == EventType.JOIN) {
						handleJoinEvent(evt, U);
					}

					// Expire event: come from handleUpdate or handleExit
					else if (evt.type == EventType.EXPIRE) {
						// check if the current cluster really needs to be
						// rebuilt
						System.out.println("Handling expire event...");
						MovingObject mo = allObjs.get(evt.OID);
						Cluster cluster = CS.get(evt.CID);

						ArrayList<MovingObject> L = DBScan.rangeQuery(mo, OBJ,
								eps);

						if (L.size() >= minPts) {
							// not really expires

						} else if (hasCoreNeighbor(mo, L)) {
							// still connected to the cluster
							mo.label = false;
							// set exit time
							mo.exitTime = getExitTime(mo, cluster);
							if (mo.exitTime == null) {
								mo.exitTime = Global.currMaxDateTIME;
							}
							eventQ.add(new MyEvent(mo.exitTime, mo.oid, mo.cid,
									EventType.EXIT));
						} else {
							// core object really exits from cluster
							// case 1: cluster does not expire
							mo.cid = 0;
							mo.label = false;
							if (haveEnoughMembers(cluster)) {
								// no need to re-build
								updateTrie(cluster, evt.OID, 0);
								cluster.delete(mo.oid);
								mo.cid = 0;
								updateClusterStatus(cluster);

								U.add(mo.oid);
								Insert(U);
							} else {

								// case 2: cluster really expires
								// get combinations for old cluster
								// for each member, get its new member cluster
								// intersect to detect those that are still
								// traveling
								// tgr

								Integer[] oldMems = cluster.members
										.toArray(new Integer[0]);
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

								int memSize = oldMems.length;

								// after re-clustering, check new clusters
								for (int numEle = oldMems.length; numEle >= minPts; numEle--) {
									CombinationGenerator combGen = new CombinationGenerator(
											memSize, numEle);

									Integer[] combination;
									int[] indices;

									while (combGen.hasMore()) {
										combination = new Integer[numEle];
										indices = combGen.getNext();
										for (int i = 0; i < indices.length; i++) {
											combination[i] = oldMems[indices[i]];
										}

										// see if combination is the same
										// cluster
										if (!inSameCluster(combination)) {
											LeafEntry le = aTrie.remove(
													combination, currTime);
											checkCandidate(le);
										}
									}
								}
							}
						}
					}
				}

				// printEventQ();
				printCluster();
//				if (currTime.isBefore(new LocalDateTime(
//						"2001-03-27T22:56:10.000"))) {
//					printTrie();
//				} else {
////					System.exit(0);
//				}
				// printR();

				System.out.println("===========");
			}
		}// end while eventQ!=empty or peekTime<nextReadTime

		// close database connection
		dataSource.closeConnection();
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
	 * Handle join event
	 * 
	 * @param evt
	 * @param U
	 * @throws Exception
	 */
	private static void handleJoinEvent(MyEvent evt, Set<Integer> U)
			throws Exception {
		MovingObject mo = allObjs.get(evt.OID);
		Cluster cluster = CS.get(evt.CID);
		System.out.println("Handling join...");
		cluster.add(mo.oid);
		updateTrie(cluster, evt.OID, 1);

		mo.cid = cluster.clusterId;
		U.remove(mo);
		LocalDateTime t = getExitTime(mo, cluster);

		if (mo.cid == -1) {
			throw new WrongClusterException(mo.toString());
		}
		MyEvent event = new MyEvent(t, mo.oid, cluster.clusterId,
				EventType.EXIT);
		eventQ.add(event);
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
			updateTrie(cluster, evt.OID, 0);
			cluster.delete(mo.oid);
			mo.cid = 0;

			updateClusterStatus(cluster);

			// if cluster itself expires
			if (cluster.members.size() < minPts) {
				cluster.expiryTime = currTime;
				cluster.expireOID = mo.oid;
				updateEventQ(cluster);
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
			eventQ.add(new MyEvent(mo.exitTime, mo.oid, mo.cid, EventType.EXIT));
		}
	}

	/**
	 * check if every member is still connected update core or border <br>
	 * set each member's exit time <br>
	 * 
	 * @param cluster
	 */
	private static void updateClusterStatus(Cluster cluster) {
		for (Integer i : cluster.members) {
			MovingObject mo = allObjs.get(i);
			ArrayList<MovingObject> neighbors = DBScan.rangeQuery(mo, OBJ, eps);
			if (neighbors.size() >= minPts) {
				mo.label = true;
				mo.exitTime = getExitTime(mo, cluster);
				updateEventQ(mo);
			} else {
				if (!hasCoreNeighbor(mo, neighbors)) {
					mo.exitTime = currTime;
					updateEventQ(mo);
				} else {
					// border obj
					mo.label = false;
					mo.exitTime = getExitTime(mo, cluster);
					if (mo.exitTime != null)
						updateEventQ(mo);
				}
			}
		}

	}

	/**
	 * 
	 * @param cluster
	 * @return true if the cluster has at least m members
	 */
	private static boolean haveEnoughMembers(Cluster cluster) {
		MovingObject tempMo;
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
		// System.out.println(mo);
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
	 * when time ends, check all remaining leaf nodes
	 * 
	 * @param pNode
	 * @param endTime
	 */
	private static void trieFlush(NumNode pNode, String endTime) {
		// traverse all nodes that belong to the parent
		if (pNode.edges != null) {
			for (NumEdge edge : pNode.edges) {
				// traverse children
				trieFlush(edge.toNode, endTime);
			}
		}
		if (pNode.entry != null) {
			// there is an entry
			if (endTime.equals("24:00:00")) {
				endTime = "23:59:59";
			}
			pNode.entry.te = new LocalDateTime(endTime);
			checkCandidate(pNode.entry);
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
			Cluster cluster = CS.get(mo.cid);
			if (cluster == null) {
				return;
			}

			updateTrie(cluster, mo.oid, 0);
			cluster.delete(mo.oid);
			updateClusterStatus(cluster);

			// if cluster itself expires
			if (cluster.members.size() < minPts) {
				System.err.println("cluster expires due to disappear");
				cluster.expiryTime = currTime;
				cluster.expireOID = mo.oid;
				updateEventQ(cluster);
			}

		}
		OBJ.remove(mo);
		allObjs.remove(mo);
		/**
		 * if mo belongs to a cluster, then corresponding trie combination
		 * should be removed.
		 * 
		 */

	}

	private static boolean contains(Integer[] ints, Integer id) {
		for (Integer i : ints) {
			if (i == id) {
				return true;
			}
		}
		return false;
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
			System.err.println("remove from eventQ, found and deleted");
			eventQ.remove(indexEvt);
		}

	}

	/**
	 * @param mo
	 * @param type
	 */
	private static void removeFromEventQ(MovingObject mo, int type) {
		if (type == 0) {
			// exit event
			// iterate the eventQ for the cluster's expire event
			Iterator<MyEvent> eventIte = eventQ.iterator();
			MyEvent indexEvt = null;
			boolean found = false;
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
		}
	}

	/**
	 * update the eventQ when an object's exit time changes
	 * 
	 * @param cluster
	 * @param b
	 */
	private static void updateEventQ(MovingObject mo) {
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
			LocalDateTime currTime2) {
		ArrayList<DataPoint> dPoints = hm.get(routeId);
		for (DataPoint dp : dPoints) {
			if (dp.dateTime.equals(currTime2)) {
				return dp;
			}
		}

		// System.out.println(dPoints);
		DataPoint dp1 = dPoints.get(0);
		DataPoint dp2 = dPoints.get(0);
		if (dp1.dateTime.isAfter(currTime2)) {
			// new chunk
			// get expected data point with velocity
			int secBetween = Seconds.secondsBetween(currTime2, dp1.dateTime)
					.getSeconds();
			LocalDate ld = dp1.dateTime.toLocalDate();
			String dateStr = ld.getYear() + "-"
					+ Data.fixDateOrTime(ld.getMonthOfYear()) + "-"
					+ Data.fixDateOrTime(ld.getDayOfMonth()) + "T"
					+ currTime2.toString();

			LocalDateTime ldt = new LocalDateTime(dateStr);
			double x = dp1.p.x - dp1.vx * secBetween;
			double y = dp1.p.y - dp1.vy * secBetween;

			DataPoint newP = new DataPoint(dp1.routeId, new Coordinate(x, y),
					dp1.vx, dp1.vy, currTime2, dp1.time0 - secBetween);

			return newP;
		}
		for (DataPoint dp : dPoints) {
			if (dp.dateTime.equals((currTime2))) {
				return dp;
			} else if (dp.dateTime.isBefore(currTime2)) {
				dp1 = dp;
			} else if (dp.dateTime.isAfter(currTime2)) {
				dp2 = dp;
				break;
			}
		}
		DataPoint res = dataSource.getImaginaryPoint(dp1, dp2, currTime2);
		return res;
	}

	/**
	 * 
	 * @param sArr
	 * @param toBeDel
	 * @return index of sArr in toBeDel; -1 if not found
	 */
	private static int getIndex(Integer[] intArr, ArrayList<Integer[]> toBeDel) {
		int i = 0;
		for (Integer[] s : toBeDel) {
			if (Arrays.equals(s, intArr)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	/**
	 * 
	 * @param elements
	 * @param r
	 * @return the combination of length r in collection
	 */
	private static Integer[][] getCombination(Integer[] elements, int r) {

		CombinationGenerator combGen = new CombinationGenerator(
				elements.length, r);

		Integer[][] resArr = new Integer[combGen.getTotal().intValue()][];
		int counter = 0;
		Integer[] combination;
		int[] indices;
		while (combGen.hasMore()) {
			combination = new Integer[r];
			indices = combGen.getNext();
			for (int i = 0; i < indices.length; i++) {
				combination[i] = elements[indices[i]];
			}
			// System.out.println(combination.toString());
			resArr[counter++] = combination;
		}
		return resArr;
	}

	/**
	 * called when a moving object causes cluster to update
	 * 
	 * @param cluster
	 * @param moid
	 * @param type
	 *            ==0: exit <br>
	 *            type==1: join
	 */
	private static void updateTrie(Cluster cluster, Integer moid, int type)
			throws Exception {
		if (cluster == null) {
			return;
		}
		// System.out.println("In updating trie, cluster moid type"
		// + cluster.clusterId + " " + moid + " " + type);
		Integer[] members = cluster.members.toArray(new Integer[0]);
		// assuming object is not deleted yet
		if (type == 0) {
			for (int numEle = minPts; numEle <= members.length; numEle++) {
				CombinationGenerator combGen = new CombinationGenerator(
						members.length, numEle);

				Integer[] combination;
				int[] indices;
				while (combGen.hasMore()) {
					combination = new Integer[numEle];
					indices = combGen.getNext();
					for (int i = 0; i <indices.length; i++) {
						combination[i] = members[indices[i]];
					}
					if (Arrays.asList(combination).contains(moid)) {
						LeafEntry entry = aTrie.remove(combination, moid,
								currTime);

						if (entry != null) {
							checkCandidate(entry);
						}
					}
				}
			}
		}

		else if (type == 1) {
			// assuming object is already inserted
			// System.out.println("update, inserting into trie...");
			for (int numEle = minPts; numEle <= members.length; numEle++) {
				CombinationGenerator combGen = new CombinationGenerator(
						members.length, numEle);
				Integer[] combination;
				int[] indices;
				while (combGen.hasMore()) {
					combination = new Integer[numEle];
					indices = combGen.getNext();
					for (int i = 0; i < indices.length; i++) {
						combination[i] = members[indices[i]];
					}
					if (Arrays.asList(combination).contains(moid)) {
						double avgDist = getAvgDist(combination);
						aTrie.insert(avgDist, combination, currTime);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param cluster
	 * @return avg distance among all members
	 */
	private static double getAvgDist(Integer[] moidArr) {
		double sumDist = 0;
		int n = moidArr.length;
		for (int i = 0; i < n; i++) {
			MovingObject mo1 = allObjs.get(moidArr[i]);
			for (int j = i + 1; j < n; j++) {
				MovingObject mo2 = allObjs.get(moidArr[j]);
				if (mo1 != null && mo2 != null) {
					sumDist += mo1.distance(mo2);
				}
			}
		}
		return 2 * sumDist / (n * (n - 1));
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
		// System.out.println("Inserting u size..." + u.size() + " " + u);
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
		// Cluster c = CS.get(mo.cid);
		for (Integer i : G) {
			mo = allObjs.get(i);
			ArrayList<MovingObject> L = DBScan.rangeQuery(mo, OBJ, eps);
			// System.out.println("i L: " + i + " " + L);
			if (L.size() >= minPts) {
				L.remove(mo);
				mo.label = true;
				expandClusterMember(mo, L, u);
				// handle core-core exit time
				mo.exitTime = getExitTime(mo, CS.get(mo.cid));
				if (mo.exitTime != null) {
					// update eventQ
					updateEventQ(mo);
				}

				// // handle expire time change due to insertion
				// boolean changed = setExpireTime(c, mo, L);
				// if (changed) {
				// updateEventQ(c);
				// }
			}
		}

		// if (currTime.equals(new LocalTime("22:49:50"))) {
		// System.out.println("G: " + G);
		// System.out.println(CS.size());
		// for (Integer key : CS.keySet()) {
		// System.out.println(CS.get(key));
		// }
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
	static void expandClusterMember(MovingObject coreObj,
			ArrayList<MovingObject> neighbors, Set<Integer> u) throws Exception {
		Cluster c = CS.get(coreObj.cid);
		for (MovingObject tempMo : neighbors) {
			if (tempMo.cid <= 0) {
				// set unclassified obj to classified
				c.add(tempMo.oid);
				tempMo.cid = c.clusterId;
				updateTrie(c, tempMo.oid, 1);

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
				trieMergeCluster(c);
				updateClusterStatus(c);
			}
		}
	}

	private static void trieMergeCluster(Cluster cluster) {
		// form combination
		// if there is no path, create new path for string
		// assuming clusters are already merged

		Integer[][] combinations = getCombination(cluster, minPts);
		for (Integer[] comb : combinations) {

			if (aTrie.getLeafEntry(comb) == null) {
				double avgDist = getAvgDist(comb);
				aTrie.insert(avgDist, comb, currTime);
			}
		}
	}

	/**
	 * pre-cond: mo is already added into cluster
	 * 
	 * @param cluster
	 * @param mo
	 */
	// private static void trieObjInsert(Cluster cluster, MovingObject mo) {
	// Integer[] members = cluster.members.toArray(new Integer[0]);
	//
	// for (int numEle = minPts ; numEle <=members.length; numEle++) {
	// Integer[][] intArr = getCombination(members, numEle);
	// for (Integer[] ints : intArr) {
	// if(Arrays.asList(ints).contains(mo.oid)){
	// aTrie.insert(ints, currTime);
	// }
	// }
	// }

	// Integer[] members = new Integer[cluster.members.size() - 1];
	// int count = 0;
	// for (Integer i : cluster.members) {
	// if (i != mo.oid) {
	// members[count++] = i;
	// }
	// }
	// for (int numEle = minPts - 1; numEle < members.length; numEle++) {
	// Integer[][] intArr = getCombination(members, numEle);
	// for (Integer[] ints : intArr) {
	// Integer[] aug = insertOneNumber(ints, mo.oid);
	//
	// aTrie.insert(aug, currTime);
	// }
	// }

	/**
	 * Insert oid into ints and sort
	 * 
	 * @param ints
	 * @param oid
	 * @return
	 */
	private static Integer[] insertOneNumber(Integer[] ints, int oid) {
		Integer[] res = new Integer[ints.length + 1];
		System.arraycopy(ints, 0, res, 0, ints.length);
		res[ints.length] = oid;
		Arrays.sort(res);

		return res;
	}

	/**
	 * add cluster c1 into c
	 * 
	 * @param c
	 * @param c1
	 * @return
	 */
	private static void merge(Cluster c, Cluster c1) {
		System.out.println("merging cluster " + c + " and " + c1);
		for (Integer i : c1.members) {
			c.add(i);
			allObjs.get(i).cid = c.clusterId;
		}
		// delete c1 from CS
		CS.remove(c1.clusterId);
		c1.members = null;
		System.out.println("after mering " + c);
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
		System.out.println("Candidates: " + R.toString());
	}

	static void printTrie() {
		System.out.println("printing trie...");
		System.out.println(aTrie.toString());
	}

	static void printTrieSize() {
		System.out.println("Trie Size:" + aTrie.getNumPaths());
	}
}
