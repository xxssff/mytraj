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
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

/**
 *
 * @author ceikute
 */
public class HandleFolder {

    public String removeWhiteSpacesInFiles(String folderUrl) throws Exception {
        String resUrl = folderUrl + "\\result";
        File resFolder = new File(resUrl);
        resFolder.mkdir();

        File inFolder = new File(folderUrl);
        File[] listOfFiles = inFolder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
//        for (int i = 4; i < 5; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
                prehandleFiles(folderUrl, resUrl, listOfFiles[i].getName());
//                getStartLine(folderUrl + "\\result\\" + listOfFiles[i].getName());

            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        return resUrl;
    }

    public void prehandleFiles(String fileUrl, String resultUrl, String fName) throws Exception {
        try {
            FileInputStream fstream = new FileInputStream(fileUrl + "\\" + fName);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String[] fileNameArr = fName.split(".txt");
            String fileName = resultUrl + "\\" + fileNameArr[0] + "_1.txt";

            FileWriter fstreamOut = new FileWriter(fileName);
            BufferedWriter out = new BufferedWriter(fstreamOut);

            String strLine;

            while ((strLine = br.readLine()) != null) {
                String[] arr = strLine.trim().split(" ");
                String newLine = "";
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i].length() > 0) {
                        newLine = newLine + arr[i] + " ";
                    }
                }
                if (newLine.length() > 0) {
                    out.write(newLine.trim() + "\n");
                    out.flush();
                }
            }
            out.flush();
            in.close();
            out.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public String generateSimplifiedVersion(String folderUrl, String outUrl, double epsilon) throws Exception {
        System.out.println("folder url " + folderUrl);
        String resUrl = outUrl;
//        File resFolder = new File(resUrl);
//        resFolder.mkdir();

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

    public void imoprtDataToDatabase(String folderUrl) throws SQLException {
        File inFolder = new File(folderUrl);
        File[] listOfFiles = inFolder.listFiles();
        System.out.println(folderUrl);
        Database db = null;

        try {
            db = new Database();

//        for (int i = 0; i < listOfFiles.length; i++) {
            for (int i = 0; i < 1; i++) {
                if (listOfFiles[i].isFile()) {
                    System.out.println("File " + listOfFiles[i].getName());
                    String fName = listOfFiles[i].getName();

                    FileInputStream fstream = new FileInputStream(folderUrl + "\\" + fName);
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));

                    String strLine;
                    strLine = br.readLine();
                    System.out.println(strLine);
                    while ((strLine = br.readLine()) != null) {
                        System.out.println(strLine);
                        db.insertData(strLine);
                    }
                }
            }
        } catch (Exception ex) {
            db.closeConnection();
        }
    }

    public void importDataToDatabase2(String folderUrl, String epsilon) throws SQLException {
        File inFolder = new File(folderUrl);
        File[] listOfFiles = inFolder.listFiles();
        System.out.println(folderUrl);
        Database db = null;

        try {
            db = new Database();
            PrepareFiles pf = new PrepareFiles();
            String tbName = "simpl_data_routes_" + epsilon;
            pf.createTable(db.getConnection(), tbName);

            for (int i = 0; i < listOfFiles.length; i++) {
//            for (int i = 0; i < 1; i++) {
                if (listOfFiles[i].isFile()) {
                    System.out.println("File " + listOfFiles[i].getName());
                    String fName = listOfFiles[i].getName();

                    FileInputStream fstream = new FileInputStream(folderUrl + "\\" + fName);
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));

                    String strLine;
                    strLine = br.readLine();
//                    System.out.println(strLine);
                    while ((strLine = br.readLine()) != null) {
//                        System.out.println(strLine);
                        String[] insert = strLine.split(" ");
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
                                + insert[17];
                        pf.insertData(db.getConnection(), tbName, insertStr);
                        db.insertData(strLine);
                    }
                }
            }
        } catch (Exception ex) {
            db.closeConnection();
        }
    }

    public void getRoutes(String carId, String driverId) throws SQLException, IOException {
        Database db = new Database();
        db.generateRouteFiles(carId, driverId);
        db.closeConnection();
    }

    private LocalTime getTime(String timeStr) {
//        if (timeStr.length() == 5) {
//            timeStr = "0" + timeStr;
//        }
        while (timeStr.length() < 6) {
            timeStr = "0" + timeStr;
        }
        String timeFull = timeStr.substring(0, 2) + ":" + timeStr.substring(2, 4) + ":" + timeStr.substring(4, 6);
        String time[] = timeFull.split(":");
        System.out.println(timeFull);
        LocalTime lc = new LocalTime(Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]));
        return lc;
    }

    public void test() {
        LocalTime startTime = getTime("230505");
        LocalTime currTime = getTime("120505");
        Seconds s = Seconds.secondsBetween(startTime, currTime);
        Minutes m = s.toStandardMinutes();
        int minBetween = m.getMinutes();
        System.out.println("minutes in between: " + minBetween);

        DateTime start = new DateTime("2001-01-01T23:55:05");
        DateTime end = new DateTime("2001-01-02T01:05:05");
        Minutes min = Minutes.minutesBetween(start, end);
        int bet = min.getMinutes();
        int day = start.getDayOfMonth();
        System.out.println("day " + day + " " + bet);
    }

    public void cutUserRoutes(String fileUrl, String resFolderUrl) {
        try {
            File inFolder = new File(fileUrl);
            File[] listOfFiles = inFolder.listFiles();
            System.out.println("files " + listOfFiles.length);
            for (int k = 0; k < listOfFiles.length; k++) {
//            for (int i = 1; i < 2; i++) {
                if (listOfFiles[k].isFile()) {
                    FileInputStream fstream = new FileInputStream(fileUrl + listOfFiles[k].getName());
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));

                    // ----transform file data into array of strings (one line == one array element)
                    String strLine = null;
                    ArrayList<String> fileLines = new ArrayList();
                    while ((strLine = br.readLine()) != null) {
                        fileLines.add(strLine);
                    }
                    in.close();
                    System.out.println(fileLines.size());
                    String[] lines = new String[fileLines.size()];
                    fileLines.toArray(lines);
                    // ----

                    // ---- find seperate routes
                    ArrayList<Integer> indexes = new ArrayList<Integer>();

                    String[] lineData = lines[0].split(" ");
                    System.out.println(lines[0]);
                    LocalTime startTime = getTime(lineData[4]);
                    System.out.println(startTime.getHourOfDay());
                    for (int i = 1; i < lines.length; i++) {
                        lineData = lines[i].split(" ");
                        LocalTime currTime = getTime(lineData[4]);
                        Seconds s = Seconds.secondsBetween(startTime, currTime);
                        Minutes m = s.toStandardMinutes();
                        int minBetween = m.getMinutes();

                        if (minBetween > 10) {
                            System.out.println("i " + i + " " + minBetween + " " + s.getSeconds() + " " + startTime.getSecondOfMinute() + " " + currTime.getSecondOfMinute());
                            indexes.add(i);
                        }
                        startTime = currTime;
                    }
                    // ----

                    // ---- store seperate routes into files
                    Integer[] idxIntArr = new Integer[indexes.size()];
                    indexes.toArray(idxIntArr);
                    int start = 0;
                    for (int j = 0; j < idxIntArr.length; j++) {
                        String fileName = resFolderUrl + "\\ruote_simpl_" + j + ".txt";
                        System.out.println("start " + start + " " + idxIntArr[j]);
                        FileWriter fstreamOut = new FileWriter(fileName);
                        BufferedWriter out = new BufferedWriter(fstreamOut);

                        for (int i = start; i < idxIntArr[j]; i++) {
                            out.write(lines[i] + "\n");
                            out.flush();
                        }
                        out.flush();
                        out.close();
                        start = idxIntArr[j];

                    }
                    // ----
                }
            }


        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
