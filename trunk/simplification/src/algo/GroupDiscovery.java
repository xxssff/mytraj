package algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Seconds;

import trie.Edge;
import trie.LeafEntry;
import trie.NumEdge;
import trie.NumNode;
import trie.NumTrie;

import com.vividsolutions.jts.geom.Coordinate;

import data.Data;
import data.DataPoint;
import entity.Breaker;
import entity.Candidates;
import entity.Cluster;
import entity.CombinationGenerator;
import entity.EventType;
import entity.Global;
import entity.MovingObject;
import entity.MyEvent;
import entity.TimeObject;

/**
 * 
 * TODO: snapping data in the database <br>
 * TODO: remove trajectories shorter than 12s<br>
 * 
 * TODO: delete some data from database <br>
 * 2001-03-31: change from string Trie to NumTrie <br>
 * 2011-04-02: examine trie when program terminates<br>
 * 2011-04-03: fill up OBJ and allObjs when update happens<br>
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
	static Candidates R = new Candidates();

	// map id to clusters
	static HashMap<Integer, Cluster> CS = new HashMap<Integer, Cluster>();
	static PriorityQueue<MyEvent> eventQ = new PriorityQueue<MyEvent>();

	/**
	 * real params
	 */
	static LocalTime nextReadTime = new LocalTime(Global.infati_MINTIME);
	static LocalTime currTime = new LocalTime(Global.infati_MINTIME);
	static LocalTime systemMaxTime = new LocalTime(Global.infati_MAXTIME);
	static LocalTime systemMinTime = new LocalTime(Global.infati_MINTIME);
	static String systemTable = Global.infatiTable;
	static int eps = 60;
	static int minPts = 10;
	static int tau = 10; // seconds
	static int gap = 180; // how long is a chunk (min)

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

	static int k = 10;
	static double alpha = 0.5;
	static double beta = 0.5;
	static double gamma = 0.5;
	static NumTrie aTrie = new NumTrie();;

	private static void ClusterObjects(ArrayList<Integer> U) throws Exception {
		// convert integer into movingobjects
		MovingObject mo;
		ArrayList<MovingObject> objList = new ArrayList<MovingObject>();
		for (Integer i : U) {
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
				}
				tempC.add(tempMo.oid);

				if (!tempMo.label) {
					if (tempMo.exitTime == null) {
						// tempMo does not exit in near future
						tempMo.exitTime = currTime.plusHours(1);
					}
					MyEvent event = new MyEvent(tempMo.exitTime, tempMo.oid,
							tempC.clusterId, EventType.EXIT);
					eventQ.add(event);
				}
			}
		}

		// get expire time for newly created clusters
		for (Integer i : newClusters) {
			tempC = CS.get(i);

			// sort newCluster
			Collections.sort(tempC.members);

			Breaker b = getExpireTime(tempC);
			tempC.expiryTime = b.time;

			// insert into eventQ
			eventQ.add(new MyEvent(b.time, b.moid, tempC.clusterId,
					EventType.EXPIRE));

			Collections.sort(tempC.members);
			System.out.println("cluster " + tempC.clusterId + " #members: "
					+ tempC.members.size());
			System.out
					.println("clusterID : " + tempC.clusterId + tempC.members);

			// insert into trie
			insertTrie(tempC);
		}
	}

	/**
	 * Fill up clusters hashmap; <br>
	 * fill up eventQ
	 */
	private static void ClusterObjects() {
		DBScan.doDBScan(OBJ, eps, minPts, currTime);
		/**
		 * check clustering
		 * 
		 */
		// for (MovingObject mo : OBJ) {
		// System.out.println(mo.dataPoint.routeId + " "
		// + mo.dataPoint.toString() + " " + mo.cid + " " + mo.label);
		// }

		Cluster tempC;
		for (MovingObject mo : OBJ) {
			if (mo.cid > 0) {
				tempC = CS.get(mo.cid);
				if (tempC == null) {
					tempC = new Cluster(mo.cid);
					CS.put(mo.cid, tempC);
				}
				tempC.add(mo.oid);
			}
		}

		for (Integer i : CS.keySet()) {
			tempC = CS.get(i);
			// sort members
			Collections.sort(tempC.members);

			// add events to eventQ
			for (Integer moid : tempC.members) {
				MovingObject mo = allObjs.get(moid);
				// insert exit event into eventQ for border objects
				if (!mo.label) {
					MyEvent event = new MyEvent(mo.exitTime, mo.oid,
							tempC.clusterId, EventType.EXIT);
					System.out.println(event);
					eventQ.add(event);
				}
				// insert expire event into eventQ
			}
			Breaker b = getExpireTime(tempC);
			tempC.expiryTime = b.time;
			eventQ.add(new MyEvent(b.time, b.moid, tempC.clusterId,
					EventType.EXPIRE));
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
				nei.remove(mo);
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
				if (minTime != null) {
					eventQ.add(new MyEvent(minTime, mo.oid, c.clusterId,
							EventType.JOIN));
				}
			}
		}

		// insert into trie
		for (Integer i : CS.keySet()) {
			insertTrie(CS.get(i));

		}
		// System.out.println(aTrie.toString());
		// System.out.println(aTrie.getNumPaths());
		// System.out.println(aTrie.getLeafEntry(new String[] { "1004", "1049",
		// "1082" }));
	}

	private static void insertTrie(Cluster cluster) {
		System.out.println("Inserting into Trie");
		int memSize = cluster.members.size();
		Integer[] elements = cluster.members.toArray(new Integer[memSize]);

		for (int numEle = minPts; numEle <= memSize; numEle++) {
			CombinationGenerator combGen = new CombinationGenerator(memSize,
					numEle);
			System.out.println("combGen size:" + combGen.getTotal());

			Integer[] combination;
			int[] indices;

			while (combGen.hasMore()) {
				combination = new Integer[numEle];
				indices = combGen.getNext();
				for (int i = 0; i < indices.length; i++) {
					combination[i] = elements[indices[i]];
				}
				aTrie.insert(combination, currTime);
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
		System.out.println("combGen size:" + combGen.getTotal());
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

	private static Breaker getExpireTime(Cluster aCluster) {
		Breaker breaker = new Breaker();

		// LocalTime resTime = null;
		boolean first = true;
		// MovingObject tempMO;
		ArrayList<MovingObject> objMembers = new ArrayList<MovingObject>(
				aCluster.members.size());
		for (Integer i : aCluster.members) {
			objMembers.add(allObjs.get(i));
		}
		for (MovingObject mo : objMembers) {
			if (mo.label) {
				// retrieve neighbors for the core object
				ArrayList<MovingObject> neighbors = DBScan.rangeQuery(mo, OBJ,
						eps);
				// System.out.println("neighbor size: "+neighbors.size());
				neighbors.remove(mo);
				TimeObject[] tos = new TimeObject[neighbors.size()];
				int counter = 0;
				// order them by exit time
				for (MovingObject neighbor : neighbors) {
					LocalTime time = DBScan.getExitTime(eps, neighbor, mo,
							currTime);
					if (time == null) {
						// no exit in near future
						// add one year to currTime
						time = currTime.plusHours(3);
					}
					tos[counter++] = new TimeObject(time, neighbor);
				}
				Arrays.sort(tos);
				// get the m-2's exit time as expiry time
				// System.out.println(mo.oid + " expire time: "
				// + tos[minPts - 2].time);
				if (first) {
					breaker.time = tos[minPts - 2].time;
					breaker.moid = mo.oid;
					first = false;
				} else {
					if (breaker.time.isAfter(tos[minPts - 2].time)) {
						breaker.time = tos[minPts - 2].time;
						breaker.moid = mo.oid;
					}
				}

			}

		}
		return breaker;
	}

	/**
	 * If leafEntry is a candidate, put into R <br>
	 * R updates its minScore if necessary
	 * 
	 * @param cluster
	 */
	private static void checkCandidate(LeafEntry entry) {
		entry.computeDuration();
		entry.setScore(alpha, beta, gamma);
		System.out.println(entry.toString());

		if (entry.ts.plusSeconds(tau).isBefore(entry.te)) {

			// true candidate
			if (R.candidates.size() < k) {
				R.candidates.add(entry);
				if (entry.getScore() < R.minScore) {
					R.minScore = entry.getScore();
				}
			} else if (entry.getScore() > R.minScore) {
				R.candidates.add(entry);
				R.updateMinScore();
			} else if (entry.getScore() == R.minScore) {
				R.candidates.add(entry);
			}
		}
	}

	/**
	 * 
	 * 
	 * @param hm
	 * @return list of new objects
	 */
	private static ArrayList<Integer> updateEventQNewData(
			HashMap<Integer, ArrayList<DataPoint>> hm) {
		ArrayList<Integer> res = new ArrayList<Integer>();
		// i is routeid
		for (Integer i : hm.keySet()) {
			if (allObjs.get(i) == null) {
				// new comers
				res.add(i);
			}
			ArrayList<DataPoint> dps = hm.get(i);
			if (dps != null) {
				// insert into eventQ
				for (DataPoint dp : dps) {
					MyEvent e = new MyEvent(dp.time, dp.routeId, -1,
							EventType.UPDATE);
					eventQ.add(e);
				}
			}
		}
		return res;
	}

	// time goes by gap variable
	// at every second if some events occur, process
	// otherwise go to next iteration
	public static void main(String[] args) throws Exception {

		// Cluster c1 = new Cluster(1);
		// c1.add(1004);
		// c1.add(1082);
		// c1.add(1049);
		// c1.add(1103);
		//
		// String[][] comm = getCombination(c1, 3);
		// for (String[] com : comm)
		// System.out.println(Arrays.toString(com));

		// String[] s1 = { "a", "b", "c" };
		// String[] s2 = { "a", "b", "c" };
		// System.out.println(Arrays.equals(s1, s2));
		doGroupDiscovery();

	}

	public static void doGroupDiscovery() throws Exception {
		// get a big chunk of data from database

		boolean readChunk = false;

		while (nextReadTime.isBefore(systemMaxTime)) {
			System.out.println("nextRead time: " + nextReadTime.toString());

			/**
			 * 1. Assume there are >=m objects at the beginning<br>
			 * 2. fill up containers <br>
			 * 3. update base time <br>
			 */
			if (nextReadTime.equals(systemMinTime)) {
				// initial run
				String nextTimeStr = Data.getNewTime(nextReadTime, gap, 0);
				String currTimeStr = Data.converTimeToString(nextReadTime);

				hm = dataSource.getDefinedTrajectories(systemTable,
						currTimeStr, nextTimeStr, 0);

				// start of the tracing
				// no need to do cluster after filling up
				// because subsequent update event takes care of
				// sync and cluster.
				updateEventQNewData(hm);

				nextReadTime = nextReadTime.plusMinutes(gap);

				System.out.println();
				System.out.println("After init:");
				System.out.println("HM size: " + hm.size());
				System.out.println("CS size: " + CS.size());
				printMutalDistance();
				printCluster();
				printEventQ();
				printTrie();
				System.out.println("=========");

			} else if (readChunk) {
				currTime = nextReadTime;
				// read from database
				System.err.println("Reading next chunk...");
				String nextTimeStr = Data.getNewTime(nextReadTime, gap, 0);
				String currTimeStr = Data.converTimeToString(nextReadTime);
				hm = dataSource.getDefinedTrajectories(systemTable,
						currTimeStr, nextTimeStr, 0);

				// insert new coming objects and
				// no need to do cluster after filling up
				// because subsequent update event takes care of
				// sync and cluster.
				ArrayList<Integer> newComers = updateEventQNewData(hm);
				System.out.println("newComer size: " + newComers.size());
				//
				// // cluster new coming obj
				// Insert(newComers);

				handleDisappear();

				System.out.println("CS size: " + CS.size());

				nextReadTime = nextReadTime.plusMinutes(gap);
				readChunk = false;
			}
			// process these data
			while (!eventQ.isEmpty()
					&& eventQ.peek().time.isBefore(nextReadTime)) {

				System.out.println("CS size: " + CS.size());

				// move time to event time
				currTime = eventQ.peek().time;
				System.out.println("curr Time: " + currTime);

				/**
				 * sync moving objects
				 */
				for (MovingObject tempMo : OBJ) {
					// get expected position
					// System.out.println(tempMo.oid+" prev:"+tempMo.getX()+","+
					// tempMo.getY());
					DataPoint dp = getExpectedDataPoint(tempMo.oid, currTime);
					tempMo.setDataPoint(dp);
				}

				/**
				 * U has unclassified objects
				 */
				ArrayList<Integer> U = new ArrayList<Integer>();
				// insert noise and unclassified data into U
				for (MovingObject tempMo : OBJ) {
					if (tempMo.cid <= 0) {
						U.add(tempMo.oid);
					}
				}

				// process events
				MyEvent evt = eventQ.poll();
				System.out.println(evt.toString());

				if (evt.type == EventType.UPDATE) {
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
						Breaker b = getExpireTime(cluster);
						// update eventQ
						cluster.expiryTime = b.time;
						updateEventQ(cluster, b);
					} else {
						//mo is unclassified
						U.add(evt.OID);
					}
					
					Insert(U);
				} else {
					// double duration = currTime - cluster.startTime;
					// cluster.updateScore(alpha, beta, gamma, duration);

					// check if cluster is a true candidate;
					// order of insert/deletion and updateTrie is important
					Cluster cluster = CS.get(evt.CID);
					MovingObject mo = allObjs.get(evt.OID);
					if (cluster == null) {
						continue;
					}
					if (evt.type == EventType.EXIT) {
						updateTrie(evt);
						cluster.delete(mo.oid);
						mo.cid = 0;
						U.add(mo.oid);
						Insert(U);
					} else if (evt.type == EventType.JOIN) {
						cluster.add(mo.oid);
						updateTrie(evt);

						mo.cid = cluster.clusterId;
						U.remove(mo);
						LocalTime t = getExitTime(mo, cluster);
						MyEvent event = new MyEvent(t, mo.oid,
								cluster.clusterId, EventType.EXIT);
						eventQ.add(event);
					} else if (evt.type == EventType.EXPIRE) {
						// check if the current cluster really needs to be
						// rebuilt
						System.out.println("handling expire event");
						ArrayList<MovingObject> L = DBScan.rangeQuery(mo, OBJ,
								eps);
						if (L.size() >= minPts) {
							// re-compute the expire time
							Breaker b = getExpireTime(cluster);
							cluster.expiryTime = b.time;
							updateEventQ(cluster, b);

						} else {
							// get combinations for old cluster
							// for each member, get its new member cluster
							// intersect to detect those that are still
							// traveling
							// tgr

							// Integer[][] com1 = getCombination(cluster,
							// minPts);
							// ArrayList<Integer[]> toBeDel = new
							// ArrayList<Integer[]>(
							// com1.length * 2);
							//
							// for (Integer[] sArr : com1) {
							// toBeDel.add(sArr);
							// }
							//
							// for (int numEle = minPts + 1; numEle <=
							// cluster.members.size();
							// numEle++) {
							// System.out.println("numEle: " + numEle);
							// Integer[][] combinations =
							// getCombination(cluster, numEle);
							// for (Integer[] sArr : combinations) {
							// toBeDel.add(0, sArr);
							// }
							// }
							// System.out.println("tobedel size;"
							// + toBeDel.size());
							System.out.println("getting oldMem...");
							Integer[] oldMems = cluster.members
									.toArray(new Integer[0]);

							MovingObject tempMo;
							for (int i : cluster.members) {
								// remove cluster id for each object
								tempMo = allObjs.get(i);
								tempMo.cid = 0;
								tempMo.label = false;
								U.add(tempMo.oid);
							}
							// remove from CS
							CS.remove(cluster.clusterId);

							Insert(U);

							int memSize = oldMems.length;

							// after re-clustering, check new clusters
							for (int numEle = minPts; numEle <= oldMems.length; numEle++) {
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

									// see if combination is the same cluster
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

				System.out.println();
				System.out.println("After event:");
				// printMutalDistance();
				// printCluster();
				// printEventQ();
				// printTrie();
				printR();
				System.out.println("===========");

			}// end while eventQ isEmpty

			readChunk = true;

			// end processing eventQ

		}// end while

		// close database connection
		dataSource.closeConnection();

		// examine entires in trie
		examineTrie(aTrie.getRoot());
		// print trie
		printTrie();

		// print result list to console
		printR();
	}

	private static void examineTrie(NumNode pNode) {
		// traverse all nodes that belong to the parent

		if (pNode.entry != null) {
			// there is an entry
			pNode.entry.te = systemMaxTime;
			checkCandidate(pNode.entry);
		}
		if (pNode.edges != null) {
			for (NumEdge edge : pNode.edges) {
				// traverse children
				examineTrie(edge.toNode);
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
	 */
	private static void handleDisappear() {
		ArrayList<Integer> toBeDel = new ArrayList<Integer>(allObjs.size());
		for (Integer i : allObjs.keySet()) {
			if (hm.get(i) == null) {
				// obj_i can be removed
				toBeDel.add(i);
			}
		}
		for (Integer i : toBeDel) {
			OBJ.remove(allObjs.get(i));
			allObjs.remove(i);
		}

		toBeDel = null;
	}

	/**
	 * update the eventQ when a cluster's expire time changes
	 * 
	 * @param cluster
	 * @param b
	 */
	private static void updateEventQ(Cluster cluster, Breaker b) {
		// iterate the eventQ for the cluster's expire event
		Iterator<MyEvent> eventIte = eventQ.iterator();
		MyEvent indexEvt = null;
		while (eventIte.hasNext()) {
			MyEvent evt = eventIte.next();
			if (evt.type == EventType.EXPIRE && evt.CID == cluster.clusterId) {
				indexEvt = evt;
				break;
			}
		}
		eventQ.remove(indexEvt);
		cluster.expiryTime = b.time;

		MyEvent newEvt = new MyEvent(b.time, b.moid, cluster.clusterId,
				EventType.EXPIRE);
		eventQ.add(newEvt);
	}

	private static DataPoint getExpectedDataPoint(int routeId, LocalTime time) {
		ArrayList<DataPoint> dPoints = hm.get(routeId);
		// System.out.println(dPoints);
		DataPoint dp1 = dPoints.get(0);
		DataPoint dp2 = dPoints.get(0);
		if (dp1.time.isAfter(time)) {
			// newly read chunk
			// get expected data point with velocity
			int secBetween = Seconds.secondsBetween(time, dp1.time)
					.getSeconds();
			LocalDate ld = dp1.dateTime.toLocalDate();
			String dateStr = ld.getYear() + "-"
					+ Data.fixDateOrTime(ld.getMonthOfYear()) + "-"
					+ Data.fixDateOrTime(ld.getDayOfMonth()) + "T"
					+ time.toString();

			LocalDateTime ldt = new LocalDateTime(dateStr);
			double x = dp1.p.x - dp1.vx * secBetween;
			double y = dp1.p.y - dp1.vy * secBetween;

			DataPoint newP = new DataPoint(dp1.routeId, new Coordinate(x, y),
					dp1.vx, dp1.vy, time, ldt, dp1.time0 - secBetween);

			return newP;
		}
		for (DataPoint dp : dPoints) {
			if (dp.time.equals((time))) {
				return dp;
			} else if (dp.time.isBefore(time)) {
				dp1 = dp;
			} else if (dp.time.isAfter(time)) {
				dp2 = dp;
				break;
			}
		}
		DataPoint res = dataSource.getImaginaryPoint(dp1, dp2, time);
		return res;
	}

	/**
	 * 
	 * @param sArr
	 * @param toBeDel
	 * @return index of sArr in toBeDel; -1 if not found
	 */
	private static int getIndex(Integer[] sArr, ArrayList<Integer[]> toBeDel) {
		int i = 0;
		for (Integer[] s : toBeDel) {
			if (Arrays.equals(s, sArr)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	/**
	 * DO NOT DELETE!
	 * 
	 * @param collection
	 * @param r
	 * @return the combination of length r in collection
	 */
	private static Integer[][] getCombination(Collection<Integer> collection,
			int r) {
		List<Integer> list = new ArrayList<Integer>(collection);

		int memSize = list.size();
		Integer[] elements = list.toArray(new Integer[memSize]);

		CombinationGenerator combGen = new CombinationGenerator(memSize, r);

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
	 * 
	 */
	private static void updateTrie(MyEvent evt) throws Exception {
		Cluster cluster = CS.get(evt.CID);
		if (cluster != null) {
			if (evt.type == EventType.EXIT) {
				// assuming object is not deleted yet
				Integer[][] strArr = getCombination(cluster, minPts);
				for (Integer[] str : strArr) {
					if (Arrays.asList(str).contains(evt.OID + "")) {
						LeafEntry entry = aTrie.remove(str, evt.OID + "",
								currTime);
						checkCandidate(entry);
					}
				}
			} else if (evt.type == EventType.JOIN) {
				// assuming object is already inserted
				Integer[][] strArr = getCombination(cluster, minPts);
				for (Integer[] str : strArr) {
					if (Arrays.asList(str).contains(evt.OID + "")) {
						aTrie.insert(str, currTime);
					}
					// LeafEntry entry = aTrie.remove(str, evt.OID+"",
					// currTime);
					// checkCandidate(entry);
					// }
				}
			}
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
	 * Insert unclassified objects as in the paper
	 * 
	 * @throws Exception
	 */
	public static void Insert(ArrayList<Integer> U) throws Exception {
		ArrayList<Integer> G = new ArrayList<Integer>();
		MovingObject mo;
		for (Integer i : U) {
			mo = allObjs.get(i);
			ArrayList<MovingObject> L = DBScan.rangeQuery(mo, OBJ, eps);
			L.remove(mo);
			for (MovingObject tempMo : L) {
				if (tempMo.cid > 0) {
					G.add(tempMo.oid);
				}
			}
		}

		System.out.println("G size: " + G.size());
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
						trieObjInsert(c, tempMo);
						U.remove(new Integer(tempMo.oid));
						DBScan.expandCluster(OBJ, tempMo, c.clusterId, eps,
								minPts, currTime);
					} else if (tempMo.cid != mo.cid && tempMo.label) {
						Cluster c1 = CS.get(tempMo.cid);
						// insert members of c1 into c
						merge(c, c1);
						trieMergeCluster(c);
					}
				}

				// handle expire time change due to insertion
				Breaker b = getExpireTime(c);
				if (b.time.isBefore(c.expiryTime)) {
					updateEventQ(c, b);
				}
			}
		}
		if (U.size() >= minPts) {
			ClusterObjects(U);
		}
	}

	private static void trieMergeCluster(Cluster cluster) {
		// form combination
		// if there is no path, create new path for string
		// assuming clusters are already merged
		Integer[][] strArr = getCombination(cluster, minPts);
		for (Integer[] str : strArr) {
			if (aTrie.getLeafEntry(str) == null) {
				aTrie.insert(str, currTime);
			}
		}
	}

	private static void trieObjInsert(Cluster cluster, MovingObject mo) {
		// assuming object is already inserted
		Integer[][] strArr = getCombination(cluster, minPts);
		for (Integer[] str : strArr) {
			if (Arrays.asList(str).contains(mo.oid + "")) {
				aTrie.insert(str, currTime);
			}
			// LeafEntry entry = aTrie.remove(str, evt.OID+"", currTime);
			// checkCandidate(entry);
			// }
		}
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
			OBJ.get(i).cid = c.clusterId;
			c1.delete(i);
		}
		// delete c1 from CS
		CS.remove(c1.clusterId);
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
		for (Integer i : CS.keySet()) {
			Cluster c = CS.get(i);
			System.out.println("clus" + i + ":" + " " + c.members);
		}
	}

	static void printEventQ() {
		Iterator<MyEvent> ite = eventQ.iterator();
		while (ite.hasNext()) {
			System.out.println("evt: " + ite.next().toString());
		}
	}

	static void printR() {
		System.out.println(R.toString());
	}

	static void printTrie() {
		System.out.println("printing trie...");
		System.out.println(aTrie.toString());
	}
}
