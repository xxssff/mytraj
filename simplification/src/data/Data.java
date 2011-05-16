/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.Seconds;

import simplification.Database;

import com.vividsolutions.jts.geom.Coordinate;

import entity.Global;

/**
 * 
 * @author ceikute, xiaohui
 */
public class Data {

    Database db;

    public Data() {
        db = new Database();
    }

    public static String converTimeToString(LocalTime lt) {
        String ret = lt.toString();
        return ret.substring(0, ret.indexOf("."));
    }

    /**
     * method to retrieve defines trajectories from provided DB table and time
     * range type
     *
     * time format: "00:00:00" or "2001-01-01 00:00:00"<br>
     *
     *
     * @param tbName
     *            : table name
     * @param timeFrom
     * @param timeTo
     * @param type
     *            : = {1, 0} : 1 - considering date and time;<br>
     *            0 - considering time with the same day
     * @return hashmap : route id -> arraylist of datapoints within provided
     *         time period
     * @throws Exception
     */
    public HashMap<Integer, ArrayList<DataPoint>> getDefinedTrajectories(
            String tbName, String timeFrom, String timeTo, int type)
            throws Exception {

        String select = "";
        if (type > 0) {
            select = "select * from " + tbName + " where stamp between '"
                    + timeFrom + "' and '" + timeTo
                    + "' order by routeid, stamp";
            System.out.println(select);
        } else if (type == 0) {
            select = "select * from " + tbName + " where time between '"
                    + timeFrom + "' and '" + timeTo
                    + "' order by routeid, time";
            System.out.println(select);
        }
        PreparedStatement ps = null;
        ResultSet result = null;

        ps = db.getConnection().prepareStatement(select);
        result = ps.executeQuery();

        HashMap<Integer, ArrayList<DataPoint>> hm = new HashMap<Integer, ArrayList<DataPoint>>();

        ArrayList<DataPoint> points = null;

        int routeId = -1;

        while (result.next()) {
            int routeNext = result.getInt("routeid");
            if (routeId == -1) {
                routeId = routeNext;
                points = new ArrayList<DataPoint>();
            } else if (routeId != routeNext) {
                /**
                 * new routes
                 */
                hm.put(routeId, points);
                routeId = routeNext;
                points = new ArrayList<DataPoint>();
            }
            Coordinate c = new Coordinate(Double.parseDouble(result.getString("mpx")), Double.parseDouble(result.getString("mpy")));

            LocalDateTime dateTime = null;
            if (type == 0) {
                dateTime = new LocalDateTime(Global.infatiDay + "T"
                        + result.getString("time"));
            } else {
                dateTime = new LocalDateTime(result.getString("stamp").replace(
                        " ", "T"));
            }
            double vx = result.getFloat("xv");
            double vy = result.getFloat("yv");

            DataPoint dp = null;
            dp = new DataPoint(routeId, c, vx, vy, dateTime,
                    result.getInt("time0"));
            points.add(dp);
        }
        hm.put(routeId, points);

        ps.close();
        result.close();

        return hm;
    }

    public void closeConnection() throws Exception {
        db.closeConnection();
    }

    /**
     *
     * @param routeId
     * @param lt
     * @return expected dataPoint of trajectory routeid at time lt
     * @throws Exception
     */
    public DataPoint getExpectedDataPoint(int routeId, LocalDateTime lt)
            throws Exception {

        PreparedStatement ps = null;
        ResultSet result, result1;
        DataPoint dp = null;
        DataPoint dp1 = null;

        String select = "select * from " + Global.testTable + " where routeid="
                + routeId + " and time <'" + lt.toString() + "' limit 1";
        // System.out.println(select);
        ps = db.getConnection().prepareStatement(select);
        result = ps.executeQuery();
        if (result.next()) {
            // System.out.println("enter first");
            Coordinate c = new Coordinate(Double.parseDouble(result.getString("mpx")), Double.parseDouble(result.getString("mpy")));
            LocalDateTime dateTime = new LocalDateTime(result.getString("stamp").replace(" ", "T"));
            LocalDateTime aTime = new LocalDateTime(result.getString("time"));
            double vx = result.getFloat("xv");
            double vy = result.getFloat("yv");
            dp = new DataPoint(routeId, c, vx, vy, dateTime,
                    result.getInt("time0"));

        }

        // second point
        String select1 = "select * from " + Global.testTable
                + " where routeid=" + routeId + " and time >'" + lt.toString()
                + "' limit 1";
        // System.out.println(select1);
        ps = db.getConnection().prepareStatement(select1);
        result1 = ps.executeQuery();
        if (result1.next()) {
            // System.out.println("enter second");
            Coordinate c1 = new Coordinate(Double.parseDouble(result1.getString("mpx")), Double.parseDouble(result1.getString("mpy")));
            LocalDateTime dateTime1 = new LocalDateTime(result1.getString(
                    "stamp").replace(" ", "T"));
            LocalDateTime aTime1 = new LocalDateTime(result1.getString("time"));
            double vx1 = result1.getFloat("xv");
            double vy1 = result1.getFloat("yv");
            dp1 = new DataPoint(routeId, c1, vx1, vy1, aTime1,
                    result1.getInt("time0"));
        }
        result.close();
        result1.close();
        if (dp != null && dp1 != null) {
            return getImaginaryPoint(dp, dp1, lt);
        }
        return null;
    }

    /**
     *
     * @param t1_p1
     * @param t1_p2
     * @param currTime2
     * @return expected data point between t1_p1 and t1_p2 for a trajectory t1
     */
    public DataPoint getImaginaryPoint(DataPoint t1_p1, DataPoint t1_p2,
            LocalDateTime currTime) {

        //check if t1_p1 and t1_p2 are same day
        int period1 = Seconds.secondsBetween(t1_p1.dateTime, currTime).getSeconds();
        int period2 = Seconds.secondsBetween(t1_p1.dateTime, t1_p2.dateTime).getSeconds();
//		// handle midnight special case
//		if (secBetween < 0) {
//			// String[] date = t1_p1.dateTime.split(" ");
//			// LocalDate ld = new LocalDate(date[0]);
//			LocalDate ld = t1_p1.dateTime.toLocalDate();
//			ld = ld.plusDays(1);
//			String dateStr = ld.getYear() + "-"
//					+ fixDateOrTime(ld.getMonthOfYear()) + "-"
//					+ fixDateOrTime(ld.getDayOfMonth());
//			LocalDateTime midnight = new LocalDateTime(dateStr + "T00:00:00");
//			// DateTime p1 = new DateTime(t1_p1.dateTime.replace(" ", "T"));
//			LocalDateTime p1 = t1_p1.dateTime;
//
//			sec = Seconds.secondsBetween(p1, midnight);
//			secBetween = sec.getSeconds();
//			// DateTime p2 = new DateTime(p.dateTime.replace(" ", "T"));
//
//			// LocalDateTime p2 = p.dateTime;
//			sec = Seconds.secondsBetween(midnight, currTime);
//			secBetween = secBetween + sec.getSeconds();
//		}

        // compute the relative time
        int time = t1_p1.time0 + period1;
        double tau = (double) period1 / period2;

        double x = tau * (t1_p2.p.x - t1_p1.p.x) + t1_p1.p.x;
        double y = tau * (t1_p2.p.y - t1_p1.p.y) + t1_p1.p.y;

        DataPoint newP = new DataPoint(t1_p1.routeId, new Coordinate(x, y),
                t1_p1.vx, t1_p1.vy, currTime, time);

        return newP;
    }

    /**
     *
     * @param t1_p1
     *            - trajectory point 1
     * @param t1_p2
     *            - trajectory point 2
     * @param p
     *            - sample point
     * @param type
     *            - 1 or 0
     * @return imaginary data point between provided trajectory 1 points, at
     *         time from p
     */
    public static DataPoint getImaginaryPoint(DataPoint t1_p1, DataPoint t1_p2,
            DataPoint p, int type) {
        Seconds sec = null;
        int secBetween = -1;

        if (type == 1) {
            // DateTime startStamp = new DateTime(t1_p1.dateTime.replace(" ",
            // "T"));
            // DateTime currStamp = new DateTime(p.dateTime.replace(" ", "T"));

            LocalDateTime startStamp = t1_p1.dateTime;
            LocalDateTime currStamp = p.dateTime;
            sec = Seconds.secondsBetween(startStamp, currStamp);
            secBetween = sec.getSeconds();
        } else {
            // LocalTime startStamp = new LocalTime(t1_p1.time);
            // LocalTime currStamp = new LocalTime(p.time);

            LocalDateTime startStamp = t1_p1.dateTime;
            LocalDateTime currStamp = p.dateTime;
            sec = Seconds.secondsBetween(startStamp, currStamp);
            secBetween = sec.getSeconds();

            if (secBetween < 0) {
                // String[] date = t1_p1.dateTime.split(" ");
                // LocalDate ld = new LocalDate(date[0]);
                LocalDate ld = t1_p1.dateTime.toLocalDate();
                ld = ld.plusDays(1);
                String dateStr = ld.getYear() + "-"
                        + fixDateOrTime(ld.getMonthOfYear()) + "-"
                        + fixDateOrTime(ld.getDayOfMonth());
                LocalDateTime midnight = new LocalDateTime(dateStr
                        + "T00:00:00");
                // DateTime p1 = new DateTime(t1_p1.dateTime.replace(" ", "T"));
                LocalDateTime p1 = t1_p1.dateTime;

                sec = Seconds.secondsBetween(p1, midnight);
                secBetween = sec.getSeconds();
                // DateTime p2 = new DateTime(p.dateTime.replace(" ", "T"));

                LocalDateTime p2 = p.dateTime;
                sec = Seconds.secondsBetween(midnight, p2);
                secBetween = secBetween + sec.getSeconds();
            }
        }

        int time = t1_p1.time0 + secBetween;

        double tau = (time - t1_p1.time0) / (t1_p2.time0 - t1_p1.time0);

        double x = tau * (t1_p2.p.x - t1_p1.p.x) + t1_p1.p.x;
        double y = tau * (t1_p2.p.y - t1_p1.p.y) + t1_p1.p.y;

        DataPoint newP = new DataPoint(t1_p1.routeId, new Coordinate(x, y),
                t1_p1.vx, t1_p1.vy, p.dateTime, time);

        return newP;
    }

    // public ArrayList<DataPoint> getNextDataPoint() {
    //
    // }
    public static String fixDateOrTime(int time) {
        String t_str = Integer.toString(time);
        if (t_str.length() < 2) {
            t_str = "0" + t_str;
        }
        return t_str;
    }

    /**
     *
     * @param time
     *            - time from
     * @param hours
     *            - number of hours to add
     * @param type
     *            - 1 - date and time; 0 - time
     * @return new string of next time
     */
    public static String getNewTime(String time, int hours, int type) {
        String newTime = "";

        if (type == 1) {
            DateTime dt = new DateTime(time.replace(" ", "T"));
            dt = dt.plusHours(hours);

            newTime = dt.getYear() + "-" + fixDateOrTime(dt.getMonthOfYear())
                    + "-" + fixDateOrTime(dt.getDayOfMonth()) + " "
                    + fixDateOrTime(dt.getHourOfDay()) + ":"
                    + fixDateOrTime(dt.getMinuteOfHour()) + ":"
                    + fixDateOrTime(dt.getSecondOfMinute());
        } else if (type == 0) {
            LocalTime lt = new LocalTime(time);
            lt = lt.plusHours(hours);

            newTime = fixDateOrTime(lt.getHourOfDay()) + ":"
                    + fixDateOrTime(lt.getMinuteOfHour()) + ":"
                    + fixDateOrTime(lt.getSecondOfMinute());
        }

        return newTime;
    }

    /**
     *
     * @param time
     *            - time from
     * @param minutes
     *            - number of minutes
     * @param type
     *            - 1 - date and time; 0 - time
     * @return new string of next time
     */
    public static String getNewTime(LocalTime time, int minutes, int type) {
        String newTime = "";
        LocalTime lt = new LocalTime(time);
        lt = lt.plusMinutes(minutes);

        newTime = fixDateOrTime(lt.getHourOfDay()) + ":"
                + fixDateOrTime(lt.getMinuteOfHour()) + ":"
                + fixDateOrTime(lt.getSecondOfMinute());

        return newTime;
    }

    // /**
    // *
    // * @param time
    // * @param hours
    // * @param type
    // * @return new string of next time
    // */
    // public static String getNewTime(LocalTime time, int hours, int type) {
    // String timestring = time.toString(); // toString returns "00:00:00.000"
    // timestring = timestring.substring(0, timestring.indexOf("."));
    // return getNewTime(timestring, hours, type);
    // }
    /**
     *
     * @param tbName
     * @param timeFrom
     * @param timeTo
     * @param type
     * @return
     * @throws Exception
     */
    public HashMap<Integer, ArrayList<DataSegment>> getDefinedTrajSegements(
            String tbName, String timeFrom, String timeTo, int type)
            throws Exception {

        HashMap<Integer, ArrayList<DataPoint>> hashPoints = getDefinedTrajectories(
                tbName, timeFrom, timeTo, type);

        HashMap<Integer, ArrayList<DataSegment>> hashSegments = new HashMap<Integer, ArrayList<DataSegment>>();

        for (Integer i : hashPoints.keySet()) {
            ArrayList<DataPoint> dataArr = hashPoints.get(i);
            ArrayList<DataSegment> segArr = new ArrayList<DataSegment>();
            for (int j = 0; j < dataArr.size()-1; j++) {
                DataPoint dpSt = dataArr.get(j);
                DataPoint dpEn = dataArr.get(j+1);
                DataSegment ds = new DataSegment(i, dpSt.p, dpEn.p, dpSt.dateTime, dpEn.dateTime, dpSt.time0, dpEn.time0);
                segArr.add(ds);
            }
            hashSegments.put(i, segArr);
        }
        return hashSegments;
    }
}
