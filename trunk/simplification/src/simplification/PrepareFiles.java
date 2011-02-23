/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simplification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import org.joda.time.LocalTime;
import org.joda.time.LocalDate;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

/**
 *
 * @author ceikute
 */
public class PrepareFiles {

    /**
     *
     * @param inUrl
     * @param outUrl
     * @param alg
     * @throws Exception
     *
     * alg:
     * 0 - reads files from url, stores data in DB, selects data ordered by id
     *     and sorted data is stores to new files into provided directory
     * 1 -
     */
    public void prepareAllFiles(String inUrl, String outUrl, int alg) throws Exception {
        System.out.println(inUrl + " " + outUrl);
        File inFolder = new File(inUrl);
        File[] listOfFiles = inFolder.listFiles();

        PreparedStatement ps = null;

        Database db = new Database();

        for (int i = 0; i < listOfFiles.length; i++) {
//        for (int i = 0; i < 1; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
                String fName = listOfFiles[i].getName();
                FileInputStream fstream = new FileInputStream(inUrl + "\\" + fName);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                String strLine = "";

                String getData = "delete from simpl_route_data";
                ps = db.getConnection().prepareStatement(getData);
                ps.execute();
                ps.close();

                String firstLine = br.readLine().trim() + " XWGS YWGS TIME0\n";

                while ((strLine = br.readLine()) != null) {
//                    System.out.println(strLine);
                    db.insertData(strLine);
                }
                in.close();

                if (alg == 0) {
                    sortData(outUrl, fName, firstLine, db.getConnection());
                } else if (alg == 1) {
                    getRoutesForDrivers(outUrl, fName, firstLine, db.getConnection());
                }

            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        db.closeConnection();
    }

    private void sortData(String outUrl, String fName, String firstLine, Connection conn) throws Exception {
        ResultSet result = null;
        PreparedStatement ps = null;

        String getData = "select * from simpl_route_data order by id asc";
        ps = conn.prepareStatement(getData);
        result = ps.executeQuery();

        String line = "";

        String[] fileNameArr = fName.split(".txt");
        String fileName = outUrl + "\\" + fileNameArr[0] + "_done.txt";

        FileWriter fstreamOut = new FileWriter(fileName);
        BufferedWriter out = new BufferedWriter(fstreamOut);
        out.write(firstLine.trim() + "\n");


        while (result.next()) {
            line = result.getInt(1) + " " + result.getString(2) + " " + result.getInt(3) + " " + result.getInt(4) + " " + result.getInt(5) + " " + result.getInt(6) + " " + result.getString(7) + " " + result.getString(8) + " " + result.getString(9) + " " + result.getString(10) + " " + result.getInt(11) + " " + result.getInt(12) + " " + result.getInt(13) + " " + result.getInt(14) + " " + result.getInt(15) + " " + result.getString(16) + " " + result.getString(17);
//                    System.out.println("               " + line);
            if (line.length() > 0) {
                out.write(line.trim() + "\n");
            }
        }
        out.flush();
        out.close();
        result.close();
        ps.close();
    }

    public void createTable(Connection conn, String tbName) throws Exception {
        String create = "SELECT * FROM simpl_create_table('" + tbName + "')";
        System.out.println("                 " + create);
        PreparedStatement ps = null;
        ps = conn.prepareStatement(create);
        ps.execute();
        ps.close();
    }

    public void insertData(Connection conn, String tbName, String insertSt) throws Exception {
        String insert = "INSERT INTO " + tbName + " VALUES(" + insertSt + ")";
//        System.out.println(insert);
        PreparedStatement ps = null;
        ps = conn.prepareStatement(insert);
        ps.execute();
        ps.close();
    }

    private void getRoutesForDrivers(String outUrl, String fName, String firstLine, Connection conn) throws Exception {
        ResultSet result = null;
        PreparedStatement ps = null;

        String tbName = fName.substring(0, 26);
        createTable(conn, tbName);

        ArrayList<String> driversTmp = new ArrayList<String>();

        String getData = "select distinct driverid from simpl_route_data";
        ps = conn.prepareStatement(getData);
        result = ps.executeQuery();

        while (result.next()) {
            String driver = result.getString("driverid");
            driversTmp.add(driver);
        }

        String[] drivers = new String[driversTmp.size()];
        driversTmp.toArray(drivers);

        ArrayList<String> dataArrTmp = null;

        String[] fileNameArr = fName.split(".txt");
        String fileName = fileNameArr[0];

        for (int i = 0; i < drivers.length; i++) {
            dataArrTmp = new ArrayList<String>();
            getData = "select * from simpl_route_data where driverid=" + drivers[i] + " order by id asc";
            System.out.println(getData);
//            getData = "select * from simpl_route_data where driverid=" + drivers[i] + " order by rdate, rtime asc";
            ps = conn.prepareStatement(getData);
            result = ps.executeQuery();
            System.out.println("             driver " + drivers[i]);
            while (result.next()) {
                String data = result.getInt(1) + " " + result.getString(2) + " " + result.getInt(3) + " " + result.getInt(4) + " " + result.getInt(5) + " " + result.getInt(6) + " " + result.getString(7) + " " + result.getString(8) + " " + result.getString(9) + " " + result.getString(10) + " " + result.getInt(11) + " " + result.getInt(12) + " " + result.getInt(13) + " " + result.getInt(14) + " " + result.getInt(15) + " " + result.getString(16) + " " + result.getString(17);
                dataArrTmp.add(data);
            }

            String[] dataArr = new String[dataArrTmp.size()];
            dataArrTmp.toArray(dataArr);

            Integer[] idxIntArr = getSeparateRouteIndexes(dataArr);

            // ---- store seperate routes into files

            int start = 0;

            double x = 0.0;
            double y = 0.0;

            LocalTime startTime = getTime("000000");

            for (int j = 0; j < idxIntArr.length; j++) {
                String fileName2 = outUrl + "\\" + fileName + "_" + drivers[i] + "_" + j + ".txt";
                System.out.println("             start " + start + " " + idxIntArr[j] + " " + fileName);
                FileWriter fstreamOut = new FileWriter(fileName2);
                BufferedWriter out = new BufferedWriter(fstreamOut);
                out.write(firstLine);

                String[] arr = dataArr[start].split(" ");
                x = Double.parseDouble(arr[8]);
                y = Double.parseDouble(arr[9]);
                startTime = getTime(arr[5]);
                int time0 = 0;
//                System.out.println("startTime: " + startTime.getHourOfDay() + ":" + startTime.getMinuteOfHour() + ":" + startTime.getSecondOfMinute());

                out.write(dataArr[start] + " " + time0 + "\n");
                out.flush();

                String[] insert = dataArr[start].split(" ");
                String timeString = startTime.getHourOfDay() + ":" + startTime.getMinuteOfHour() + ":" + startTime.getSecondOfMinute();
                String routeId = drivers[i] + Integer.toString(j);
                System.out.println("Name " + drivers[i] + " " + j + " " + routeId);
                String insertStr = insert[0] + ",'"
                        + insert[1] + "',"
                        + insert[2] + ","
                        + insert[3] + ","
                        + insert[4] + ","
                        + insert[5] + ",'"
                        + insert[6] + "','"
                        + insert[7] + "','"
                        + insert[8] + "','"
                        + insert[9] + "',"
                        + insert[10] + ","
                        + insert[11] + ","
                        + insert[12] + ","
                        + insert[13] + ","
                        + insert[14] + ",'"
                        + insert[15] + "','"
                        + insert[16] + "',"
                        + time0 + ",'"
                        + timeString + "','"
                        + routeId + "'";
                insertData(conn, tbName, insertStr);

                for (int k = (start + 1); k < idxIntArr[j]; k++) {
//                    System.out.println(dataArr[k]);
                    arr = dataArr[k].split(" ");
                    double distance = Math.sqrt(Math.pow(Double.parseDouble(arr[8]) - x, 2) + Math.pow(Double.parseDouble(arr[9]) - y, 2));
                    LocalTime currTime = getTime(arr[5]);
                    if (distance <= 50) {
                        Seconds s = Seconds.secondsBetween(startTime, currTime);
                        time0 = time0 + s.getSeconds();
                        startTime = currTime;
                        insert = dataArr[k].split(" ");
                        timeString = currTime.getHourOfDay() + ":" + currTime.getMinuteOfHour() + ":" + currTime.getSecondOfMinute();
                        routeId = drivers[i] + Integer.toString(j);
                        insertStr = insert[0] + ",'"
                                + insert[1] + "',"
                                + insert[2] + ","
                                + insert[3] + ","
                                + insert[4] + ","
                                + insert[5] + ",'"
                                + insert[6] + "','"
                                + insert[7] + "','"
                                + insert[8] + "','"
                                + insert[9] + "',"
                                + insert[10] + ","
                                + insert[11] + ","
                                + insert[12] + ","
                                + insert[13] + ","
                                + insert[14] + ",'"
                                + insert[15] + "','"
                                + insert[16] + "',"
                                + time0 + ",'"
                                + timeString + "','"
                                + routeId + "'";
                        insertData(conn, tbName, insertStr);
                        out.write(dataArr[k] + " " + time0 + "\n");
                        out.flush();
                        x = Double.parseDouble(arr[8]);
                        y = Double.parseDouble(arr[9]);
                    } else {
//                        System.out.println("               k " + k + " " + (k-1));
//                        System.out.println(dataArr[k - 1]);
//                        System.out.println(dataArr[k]);
                    }

                }
                out.flush();
                out.close();
                start = idxIntArr[j];
            }
        }
        ps.close();
        result.close();
    }

    public String correctTime(String timeStr) {
        while (timeStr.length() < 6) {
            timeStr = "0" + timeStr;
        }
        return timeStr;
    }

    public String correctDate(String dateStr) {
        while (dateStr.length() < 6) {
            dateStr = "0" + dateStr;
        }
        return dateStr;
    }

    private LocalTime getTime(String timeStr) {
        timeStr = correctTime(timeStr);

//        System.out.println(timeStr);
        String timeFull = timeStr.substring(0, 2) + ":" + timeStr.substring(2, 4) + ":" + timeStr.substring(4, 6);
        String time[] = timeFull.split(":");
//        System.out.println(timeFull);
        LocalTime lc = new LocalTime(Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]));
        return lc;
    }

//    private LocalTime getDate(String dateStr) {
//        dateStr = correctDate(dateStr);
//
////        System.out.println(timeStr);
//        String dateFull = dateStr.substring(0, 2) + "-" + dateStr.substring(2, 4) + "-" + dateStr.substring(4, 6);
//        String date[] = dateFull.split("-");
////        System.out.println(timeFull);
//
//        LocalDate ld = new LocalDate(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.date(date[2]));
//        LocalDate ld = new LocalDate();
//        return ld;
//    }


    private Integer[] getSeparateRouteIndexes(String[] data) {
        ArrayList<Integer> indexes = new ArrayList<Integer>();

        String[] lineData = data[0].split(" ");
        System.out.println(data[0]);
        LocalTime startTime = getTime(lineData[5]);
        System.out.println(startTime.getHourOfDay());
        for (int i = 1; i < data.length; i++) {
            lineData = data[i].split(" ");
            LocalTime currTime = getTime(lineData[5]);
            Seconds s = Seconds.secondsBetween(startTime, currTime);
            Minutes m = s.toStandardMinutes();
            int minBetween = m.getMinutes();

            if (minBetween > 10) {
                indexes.add(i);
            }
            startTime = currTime;
        }
        // ----

        // ---- store seperate routes into files
        Integer[] idxIntArr = new Integer[indexes.size()];
        indexes.toArray(idxIntArr);
        return idxIntArr;
    }
}
