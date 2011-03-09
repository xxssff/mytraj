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
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

/**
 *
 * @author ceikute
 */
public class PrepareData {

    /**
     *
     * @param url for files to prepare
     */
    public void mainPreparation(String folderUrl, String resUrl) throws Exception {
        // remove whitespaces
//        String file = removeWhiteSpacesInFiles(folderUrl, resUrl + "\\data");
        // sort data
//        String sortFile1 = sortData(resUrl + "\\data_1", resUrl + "\\sort_1");
//        String sortFile2 = sortData(resUrl + "\\data_2", resUrl + "\\sort_2");
        int i = getRoutesForDrivers(resUrl + "\\sort_1", resUrl + "\\routes_1", 0);
        i = getRoutesForDrivers(resUrl + "\\sort_2", resUrl + "\\routes_1", i);
        insertRoutesIntoDB(resUrl + "\\routes_1", "simpl_data_double_1");
        simplifyRoutes(resUrl + "\\routes_1", resUrl + "\\simpl_1_", "simpl_data_double_1_", 25, 49, 25);
//        ArrayList<Integer> list = getDefinedTrajectories("simpl_data_team_1_25", "2001-01-02T13:17:44", "2001-01-02T14:37:44");
//        for (int i = 0; i < list.size(); i++) {
//            System.out.println(list.get(i));
//        }
    }

    private String removeWhiteSpacesInFiles(String folderUrl, String resUrl) throws Exception {
        // create provided folder for result file
        File resFolder = new File(resUrl);
        resFolder.mkdir();

        File inFolder = new File(folderUrl);
        File[] listOfFiles = inFolder.listFiles();

        String strLine;

        // read files from provided directory and store data in seperate result file
        for (int i = 0; i < listOfFiles.length; i++) {
//        for (int i = 0; i < 2; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
                String fileName = listOfFiles[i].getName();

                // input stream
                FileInputStream fstream = new FileInputStream(folderUrl + "\\" + fileName);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                // create result file
                String resFile = resUrl + "\\" + fileName;
                System.out.println(resFile);

                FileWriter fstreamOut = new FileWriter(resFile);
                BufferedWriter out = new BufferedWriter(fstreamOut);

//                if (i != 0) {
//                    strLine = br.readLine();
//                }
                // remove whitespaces
                while ((strLine = br.readLine()) != null) {
                    String[] arr = strLine.trim().split(" ");
                    String newLine = "";
                    for (int j = 0; j < arr.length; j++) {
                        if (arr[j].length() > 0) {
                            newLine = newLine + arr[j] + " ";
                        }
                    }
                    if (newLine.length() > 0) {
                        out.write(newLine.trim() + "\n");
                        out.flush();
                    }
                }
                out.flush();
                out.close();
                in.close();
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }

        return resUrl;
    }

    private String sortData(String inUrl, String outUrl) throws Exception {
        File inFolder = new File(inUrl);
        File[] listOfFiles = inFolder.listFiles();

        PreparedStatement ps = null;

        Database db = new Database();

        File resFolder = new File(outUrl);
        resFolder.mkdir();


        for (int i = 0; i < listOfFiles.length; i++) {
//        for (int i = 0; i < 1; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
                String fName = listOfFiles[i].getName();
                FileInputStream fstream = new FileInputStream(inUrl + "\\" + fName);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                String strLine = "";

                String delData = "delete from simpl_route_data";
                ps = db.getConnection().prepareStatement(delData);
                ps.execute();
                ps.close();
                System.out.println("after delete");
                String firstLine = br.readLine().trim() + " XWGS YWGS\n";
//                int a = 0;
                while ((strLine = br.readLine()) != null) {
//                    System.out.println(strLine);
                    db.insertData(strLine);
//                    if (a == 100)
//                        break;
//                    a++;
                }
                in.close();

                ResultSet result = null;

                String getData = "select * from simpl_route_data order by id asc";
                ps = db.getConnection().prepareStatement(getData);
                result = ps.executeQuery();

                String line = "";

                //result file
                String fileName = outUrl + "\\sort_" + fName;
                System.out.println(fileName);

                FileWriter fstreamOut = new FileWriter(fileName);
                BufferedWriter out = new BufferedWriter(fstreamOut);
                out.write(firstLine.trim() + "\n");


                while (result.next()) {
                    line = result.getInt(1) + " " + result.getString(2) + " " + result.getInt(3) + " " + result.getInt(4) + " " + result.getInt(5) + " " + result.getInt(6) + " " + result.getString(7) + " " + result.getString(8) + " " + result.getString(9) + " " + result.getString(10) + " " + result.getInt(11) + " " + result.getInt(12) + " " + result.getInt(13) + " " + result.getInt(14) + " " + result.getInt(15) + " " + result.getString(16) + " " + result.getString(17);
//                    System.out.println(line);
                    if (line.length() > 0) {
                        out.write(line.trim() + "\n");
                    }
                }
                out.flush();
                out.close();
                result.close();
                ps.close();

            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        db.closeConnection();
        return outUrl;
    }

    private String correctTime(String timeStr) {
        while (timeStr.length() < 6) {
            timeStr = "0" + timeStr;
        }
        return timeStr;
    }

    private String correctDate(String dateStr) {
        while (dateStr.length() < 6) {
            dateStr = "0" + dateStr;
        }
        return dateStr;
    }

    private String getTime(String timeStr) {
        timeStr = correctTime(timeStr);

//        System.out.println(timeStr);
        String timeFull = timeStr.substring(0, 2) + ":" + timeStr.substring(2, 4) + ":" + timeStr.substring(4, 6);
//        String time[] = timeFull.split(":");
//        System.out.println(timeFull);
//        LocalTime lt = new LocalTime(Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]));

        return timeFull;
    }

    private String getDate(String dateStr) {
        dateStr = correctDate(dateStr);

//        System.out.println(timeStr);
        String dateFull = dateStr.substring(4, 6) + "-" + dateStr.substring(2, 4) + "-" + dateStr.substring(0, 2);
//        String date[] = dateFull.split("-");
//        System.out.println(timeFull);

//        LocalDate ld = new LocalDate(dateFull);
//        System.out.println(ld.getYear() + " " + ld.getMonthOfYear() + " " + ld.getDayOfMonth());
        return dateFull;
    }

    private Integer[] getSeparateRouteIndexes(String[] data) {
        ArrayList<Integer> indexes = new ArrayList<Integer>();

        String[] lineData = data[0].split(" ");
        System.out.println(data[0]);

        DateTime startStamp = new DateTime(getDate(lineData[4]) + "T" + getTime(lineData[5]));

        double x = Double.parseDouble(lineData[8]);
        double y = Double.parseDouble(lineData[9]);

        indexes.add(0);

        for (int i = 1; i < data.length; i++) {
            lineData = data[i].split(" ");

            DateTime currStamp = new DateTime(getDate(lineData[4]) + "T" + getTime(lineData[5]));

            Seconds sec = Seconds.secondsBetween(startStamp, currStamp);
            int secBetween = sec.getSeconds();

            double x2 = Double.parseDouble(lineData[8]);
            double y2 = Double.parseDouble(lineData[9]);

            if (secBetween > 600 || secBetween < 0) {
                indexes.add(i);
            } else {
                double distance = Math.sqrt(Math.pow(x2 - x, 2) + Math.pow(y2 - y, 2));
                if (distance >= 2000) {
                    indexes.add(i);
                    System.out.println("-------------");
                    System.out.println(data[i]);
                    System.out.println("-------------");
                }
            }
            startStamp = currStamp;
            x = x2;
            y = y2;
        }
        // ----

        // ---- store seperate routes into files
        Integer[] idxIntArr = new Integer[indexes.size()];
        indexes.toArray(idxIntArr);
        return idxIntArr;
    }

    private int getRoutesForDrivers(String inUrl, String outUrl, int routeId) throws Exception {
        // read file from input Url and store into database
        File inFolder = new File(inUrl);
        File[] listOfFiles = inFolder.listFiles();

        // create output folder
        File resFolder = new File(outUrl);
        resFolder.mkdir();

        PreparedStatement ps = null;
        Database db = new Database();
//        int routeId = 0;

        for (int i = 0; i < listOfFiles.length; i++) {
//        for (int i = 0; i < 2; i++) {
            if (listOfFiles[i].isFile()) {
                String inFile = listOfFiles[i].getName();
                System.out.println("File " + inFile);

                String strLine = "";

                //make sure that table is empty
                String delData = "delete from simpl_route_data";
                ps = db.getConnection().prepareStatement(delData);
                ps.execute();
                ps.close();
                System.out.println("after delete");

                FileInputStream fstream = new FileInputStream(inUrl + "\\" + inFile);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String firstLine = br.readLine().trim() + " TIME DATE TIMESTAMP TIME0 ROUTEID\n";

                // store data from file in db
                while ((strLine = br.readLine()) != null) {
                    db.insertData(strLine);
                }
                in.close();
                System.out.println("upload completed..");

                ps = null;
                ResultSet result = null;

                // select distinct drivers from db
                ArrayList<String> driversTmp = new ArrayList<String>();

                String getData = "select distinct driverid from simpl_route_data";
                ps = db.getConnection().prepareStatement(getData);
                result = ps.executeQuery();

                while (result.next()) {
                    String driver = result.getString("driverid");
                    driversTmp.add(driver);
                }
                result.close();
                ps.close();

                String[] drivers = new String[driversTmp.size()];
                driversTmp.toArray(drivers);

                ArrayList<String> dataArrTmp = null;

                String[] fileNameArr = inFile.split(".txt");
                String fileName = fileNameArr[0];

                // store routes of diff drivers into seperate files
                for (int j = 0; j < drivers.length; j++) {
                    dataArrTmp = new ArrayList<String>();
//                    getData = "select * from simpl_route_data where driverid=" + drivers[j] + " order by id asc";
                    getData = "select * from simpl_route_data where driverid=" + drivers[j] + " order by entryid, id asc";
                    System.out.println(getData);

                    ps = db.getConnection().prepareStatement(getData);
                    result = ps.executeQuery();
                    System.out.println("             driver " + drivers[j]);
                    while (result.next()) {
                        String data = result.getInt(1) + " " + result.getString(2) + " " + result.getInt(3) + " " + result.getInt(4) + " " + result.getInt(5) + " " + result.getInt(6) + " " + result.getString(7) + " " + result.getString(8) + " " + result.getString(9) + " " + result.getString(10) + " " + result.getInt(11) + " " + result.getInt(12) + " " + result.getInt(13) + " " + result.getInt(14) + " " + result.getInt(15) + " " + result.getString(16) + " " + result.getString(17);
                        dataArrTmp.add(data);
                    }
                    ps.close();
                    result.close();

                    String[] dataArr = new String[dataArrTmp.size()];
                    dataArrTmp.toArray(dataArr);

                    Integer[] idxIntArr = getSeparateRouteIndexes(dataArr);
//                    for (int a = 0; a < idxIntArr.length; a++) {
//                        System.out.println(idxIntArr[a]);
//                    }
                    System.out.println("-------------");

                    int start = idxIntArr[0];


                    for (int k = 1; k < idxIntArr.length; k++) {
                        if ((start + 1) == idxIntArr[k]) {
                            System.out.println("nereikia: " + start + " " + idxIntArr[k]);
                        } else {
                            routeId++;
                            System.out.println("routes amount " + routeId);
//                            System.out.println("reikia: " + start + " " + idxIntArr[k]);

                            String[] arr = dataArr[start].split(" ");
                            double x = Double.parseDouble(arr[8]);
                            double y = Double.parseDouble(arr[9]);

                            String date = getDate(arr[4]);
                            String time = getTime(arr[5]);

                            String stamp = date + "T" + time;

                            DateTime startStamp = new DateTime(stamp);

                            String fileName2 = outUrl + "\\" + fileName + "_" + drivers[j] + "_" + k + ".txt";
//                            System.out.println("             start " + start + " " + idxIntArr[k] + " " + fileName2);
                            FileWriter fstreamOut = new FileWriter(fileName2);
                            BufferedWriter out = new BufferedWriter(fstreamOut);
                            out.write(firstLine);

                            int time0 = 0;
                            // TIME DATE TIMESTAMP TIME0
                            out.write(dataArr[start] + " " + time + " " + date + " " + stamp + " " + time0 + " " + routeId + "\n");
                            out.flush();

                            int kiek = 0;
                            for (int z = (start + 1); z < idxIntArr[k]; z++) {
                                arr = dataArr[z].split(" ");
//                                double distance = Math.sqrt(Math.pow(Double.parseDouble(arr[8]) - x, 2) + Math.pow(Double.parseDouble(arr[9]) - y, 2));

                                date = getDate(arr[4]);
                                time = getTime(arr[5]);

                                stamp = date + "T" + time;
                                DateTime currStamp = new DateTime(stamp);
//                                if (distance <= 2000) {
                                Seconds s = Seconds.secondsBetween(startStamp, currStamp);
                                time0 = time0 + s.getSeconds();
                                startStamp = currStamp;
                                out.write(dataArr[z] + " " + time + " " + date + " " + stamp + " " + time0 + " " + routeId + "\n");
                                out.flush();
//                                    x = Double.parseDouble(arr[8]);
//                                    y = Double.parseDouble(arr[9]);
//                                }
//                                else {
//                                    kiek++;
//                                    if (kiek > 10) {
//                                        System.out.println("        ne " + distance + " || " + x + " || " + y + " " + dataArr[z]);
//                                        System.out.println("             start " + start + " " + fileName2);
//                                    }
//                                }
                            }
                            out.close();
                        }
                        start = idxIntArr[k];
                    }

                }
            }

        }
        db.closeConnection();
        return routeId;
    }

    private void createTable(Connection conn, String tbName) throws Exception {
        String create = "SELECT * FROM simpl_create_table('" + tbName + "')";
        System.out.println("                 " + create);
        PreparedStatement ps = null;
        ps = conn.prepareStatement(create);
        ps.execute();
        ps.close();
    }

    private void insertData(Connection conn, String tbName, String insertSt) throws Exception {
        String insert = "INSERT INTO " + tbName + "(ID, ENTRYID, CARID, DRIVERID, RDATE, RTIME, XCOORD, YCOORD, MPX, MPY, SAT, HDOP, MAXSPD, SPD, STRTCOD, XWGS, YWGS, TIME, DATE, STAMP, TIME0, ROUTEID) VALUES(" + insertSt + ")";
//        System.out.println(insert);
        PreparedStatement ps = null;
        ps = conn.prepareStatement(insert);
        ps.execute();
        ps.close();
    }

    private void insertRoutesIntoDB(String inUrl, String tbName) throws Exception {

        File inFolder = new File(inUrl);
        File[] listOfFiles = inFolder.listFiles();

        Database db = new Database();
        createTable(db.getConnection(), tbName);

        String strLine;

        // read files from provided directory and store data in seperate result file
        for (int i = 0; i < listOfFiles.length; i++) {
//        for (int i = 205; i < 206; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
                String fileName = listOfFiles[i].getName();

                // input stream
                FileInputStream fstream = new FileInputStream(inUrl + "\\" + fileName);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                //read the header line
                strLine = br.readLine();

                while ((strLine = br.readLine()) != null) {
                    String[] arr = strLine.trim().split(" ");
                    String insert = arr[0] + ",'" + arr[1] + "'," + arr[2] + "," + arr[3] + "," + arr[4] + "," + arr[5] + ",'" + arr[6] + "','" + arr[7] + "','" + arr[8] + "','" + arr[9] + "'," + arr[10] + "," + arr[11] + "," + arr[12] + "," + arr[13] + "," + arr[14] + ",'" + arr[15] + "','" + arr[16] + "','" + arr[17] + "','" + arr[18] + "','" + arr[19] + "'," + arr[20] + "," + arr[21];
//                    System.out.println(insert);
                    insertData(db.getConnection(), tbName, insert);
                }
                in.close();
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        db.closeConnection();
    }

    private ArrayList<String> simplifyRoutes(String inUrl, String resUrl, String tbName, int from, int to, int add) throws Exception {
        ArrayList<String> tbNames = new ArrayList<String>();
        for (int i = from; i <= to; i = i + add) {
            resUrl = resUrl + i;
            String outUrl = generateSimplifiedVersion(inUrl, resUrl, (double) 25.0);
            tbName = tbName + i;
            insertRoutesIntoDB(outUrl, tbName);
            tbNames.add(tbName);
        }
        return tbNames;
    }

    private String generateSimplifiedVersion(String folderUrl, String outUrl, double epsilon) throws Exception {
        System.out.println("folder url " + folderUrl);
        String resUrl = outUrl;
        File resFolder = new File(resUrl);
        resFolder.mkdir();

        File inFolder = new File(folderUrl);
        File[] listOfFiles = inFolder.listFiles();
        System.out.println("files " + listOfFiles.length);
        for (int i = 0; i < listOfFiles.length; i++) {
//        for (int i = 1; i < 2; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
                Simplification simpl = new Simplification();

//                Integer[] resArr = simpl.getStartRoute(folderUrl + "\\" + listOfFiles[i].getName(), 0, 50.0);
                Integer[] resArr = simpl.getStartRoute(folderUrl + "\\" + listOfFiles[i].getName(), 0, epsilon);
                if (resArr != null) {
                    System.out.println(resArr.length);
                    createSimplifiedFile(folderUrl, listOfFiles[i].getName(), resArr, resUrl);
                }

            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        return resUrl;
    }

    private void createSimplifiedFile(String folderUrl, String fileNameStart, Integer[] linesArr, String resUrl) throws Exception {
        String fileUrl = folderUrl + "\\" + fileNameStart;

        FileInputStream fstream = new FileInputStream(fileUrl);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String strLine = null;
        ArrayList<String> fileLines = new ArrayList();
        while ((strLine = br.readLine()) != null) {
            fileLines.add(strLine);
        }
        in.close();
        System.out.println(fileLines.size());
        String[] lines = new String[fileLines.size()];

        fileLines.toArray(lines);

        String[] fileNameArr = fileNameStart.split(".txt");
        String fileName = resUrl + "\\" + fileNameArr[0] + "_simpl.txt";

        FileWriter fstreamOut = new FileWriter(fileName);
        BufferedWriter out = new BufferedWriter(fstreamOut);

        out.write(lines[0] + "\n");
        out.write(lines[1] + "\n");
        for (int i = 0; i < linesArr.length; i++) {
//            System.out.println("add line " + linesArr[i]);
            out.write(lines[linesArr[i] + 1] + "\n");
            out.flush();
        }
        out.write(lines[lines.length - 1] + "\n");
        out.flush();
        out.close();

    }

    private ArrayList<Integer> getDefinedTrajectories(String tbName, String timeFrom, String timeTo) throws Exception {
        ArrayList<Integer> trajList = new ArrayList<Integer>();

        Database db = new Database();

        String select = "select * from simpl_get_defined_routes('" + tbName + "', '" + timeFrom + "', '" + timeTo + "') as p(routeid bigint)";

        PreparedStatement ps = null;
        ResultSet result = null;

        ps = db.getConnection().prepareStatement(select);
        result = ps.executeQuery();

        while (result.next()) {
            int route = result.getInt("routeid");
            trajList.add(route);
        }
        ps.close();
        result.close();
        db.closeConnection();

        return trajList;
    }

    private void performSnapping(String tbName, int threshold) {
    }
}
