package algo;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import java.io.FileWriter;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Interval;

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
    static HashMap<Integer, ArrayList<TimeLineSegment>> idToLineSegs;
    static Data dataSource = new Data();

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
        List<Interval> list = new ArrayList();
        Interval tempInterval = new Interval(new DateTime(startTime),
                new DateTime("2001-03-27T16:29:59"));
        list.add(tempInterval);
        tempInterval = new Interval(new DateTime("2001-03-27T16:30:00"),
                new DateTime(endTime));
        list.add(tempInterval);

        List<Convoy> cv = cutsFilter(list, minPts, tau, eps);
        System.out.println("size=" + cv.size());
//                cutsRefinement(cv, list, minPts, tau, eps);
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
                        && Seconds.secondsBetween(v.startTime, v.endTime).getSeconds() >= k) {
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

    /**
     * fill cluster with results from dbscan
     *
     * @param CS
     * @param mos
     *            HashMap<Integer, Cluster> LCS = new HashMap<Integer,
     *            Cluster>();
     */
    private static void fillClusters(HashMap<Integer, Cluster> CS,
            List<TimeLineSegment> g) {
        Cluster tempC;
        Set<Integer> newClusters = new HashSet<Integer>();
        for (TimeLineSegment tempMo : g) {
            if (tempMo.cid > 0) {
                newClusters.add(tempMo.cid);
                tempC = CS.get(tempMo.cid);
                if (tempC == null) {
                    tempC = new Cluster(tempMo.cid);
                    CS.put(tempMo.cid, tempC);
                }
                tempC.add(tempMo.routeId);
            }
        }
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
        int lambda = 1800;//86400 / 2; // for elk data: 101001600

        List<Convoy> V = new ArrayList<Convoy>();
        List<Convoy> Vcand = new ArrayList<Convoy>();
        for (Interval interval : partitions) {
            System.out.println("iteraion..");
            List<Convoy> Vnext = new ArrayList<Convoy>();
            List<TimeLineSegment> G = new ArrayList<TimeLineSegment>();

            for (Integer i : idToLineSegs.keySet()) {
                System.out.println("here " + isIntersect(i, interval) + " i=" + i.toString() + " /// int=" + interval.toString());
                if (isIntersect(i, interval)) {
                    ArrayList<TimeLineSegment> tlss = idToLineSegs.get(i);
                    for (TimeLineSegment tl : tlss) {
                        if (tl.getInterval().overlaps(interval)) {
                            G.add(tl);
                        }
                    }
                }
            }

//            if (G.size() < minPts) {
//                System.out.println("continue");
//                continue;
//            }

            TrajDBScan.doDBScan(G, e, minPts);
            HashMap<Integer, Cluster> C = new HashMap<Integer, Cluster>();
            fillClusters(C, G);

            for (Convoy v : V) {
                System.out.println("going...");
                v.assigned = false;
                for (Integer i : C.keySet()) {
                    Cluster c = C.get(i);
                    Set<Integer> tempSet = new HashSet<Integer>(c.members);
                    tempSet.retainAll(v.members);
                    if (tempSet.size() >= minPts) {
                        v.assigned = true;
                        Convoy v_prime = new Convoy();
                        v_prime.members = tempSet;
                        v_prime.lifetime = v.lifetime + lambda;
                        Vnext.add(v_prime);
                        c.assigned = true;

                    }
                }
                if (!v.assigned
                        && v.lifetime >= k) { //
                    Vcand.add(v);
                    System.out.println("add to Vcand");
                }
            }
            for (Integer i : C.keySet()) {
                Cluster c = C.get(i);
                Convoy con = new Convoy();
                if (!c.assigned) {
                    con.lifetime = lambda;
                    Vnext.add(con);
                }
            }
            V = Vnext;
            System.out.println("V size " + V.size());
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
        System.out.println(tempInterval.toString());
        return (interval.overlaps(tempInterval));
    }

    private static ArrayList<MovingObject> getExpectedSegments(
            ArrayList<Interval> partitions, LocalDateTime ldt) {
        ArrayList<MovingObject> res = new ArrayList<MovingObject>();

        return res;
    }

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
    private static List<Convoy> cutsRefinement(List<Convoy> Vcand,
            List<LocalDateTime> times, int minPts, int k, double e)
            throws Exception {
        for (Convoy v : Vcand) {
            LocalDateTime t_start = v.startTime;
            LocalDateTime t_end = v.endTime;

            return CMC(null, minPts, eps, eps);
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        /**
         * Get parameters from conf file
         *
         */
        System.out.println("===============Group Discovery==================");
        ConfReader reader = new ConfReader();
//		HashMap<String, String> conf = reader.read(args[0]);

        BufferedWriter bw = new BufferedWriter(new FileWriter(
                "convoy.out")); //new BufferedWriter(new FileWriter(conf.get("outFile")));
        eps = 50; //Integer.parseInt(conf.get("eps"));
        minPts = 3; //Integer.parseInt(conf.get("minPts"));
        tau = 3; //Integer.parseInt(conf.get("tau"));

        systemTable = "test_table"; //conf.get("systemTable");

        bw.write("Convoy Discovery Output=====");
        bw.newLine();
        System.out.println("e:" + eps + "\t" + "m:" + minPts + "\t" + "tau:"
                + tau);

        stats = new Statistics();

        String ts = "2001-03-27T00:00:00"; //conf.get("ts");
        String te = "2001-03-27T23:59:59"; //conf.get("te");
        systemMaxTime = new LocalDateTime(te);

        long t_start = System.currentTimeMillis();
        doConvoyDiscovery(ts, te, bw);
        long t_end = System.currentTimeMillis();
        // write candidates into result file
        stats.startTime = "16:02:00"; //conf.get("ts");
        stats.endTime = "17:01:59"; //conf.get("te");

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
