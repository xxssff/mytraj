/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simplification;

import com.vividsolutions.jts.geom.Coordinate;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import com.vividsolutions.jts.simplify.*;


/**
 *
 * @author ceikute
 */
public class ManageFolder {
    private ArrayList<double[]> fixes;
    private ArrayList<Integer> pointsToKeep;
    private Object[] arr1;

    public void readFilesFromFolder(String folderUrl) {
        File folder = new File(folderUrl + "\\result");
        File[] listOfFiles = folder.listFiles();

//        File f = new File(folderUrl + "\\result");
//        f.mkdir();
//        for (int i = 0; i < listOfFiles.length; i++) {
//        for (int i = 4; i < 5; i++) {
//            if (listOfFiles[i].isFile()) {
//                System.out.println("File " + listOfFiles[i].getName());
////                prehandleFiles(folderUrl, listOfFiles[i].getName());
//                getStartLine(folderUrl + "\\result\\" + listOfFiles[i].getName());
//
//            } else if (listOfFiles[i].isDirectory()) {
//                System.out.println("Directory " + listOfFiles[i].getName());
//            }
//        }
//        testFunc();
//        test();
        testFunc_1();
    }

    public void prehandleFiles(String fileUrl, String fName) {
        System.out.println(fileUrl);
        try {
            FileInputStream fstream = new FileInputStream(fileUrl + "\\" + fName);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String[] fileNameArr = fName.split(".txt");
            String fileName = fileUrl + "\\result\\" + fileNameArr[0] + "_1.txt";

            FileWriter fstreamOut = new FileWriter(fileName);
            BufferedWriter out = new BufferedWriter(fstreamOut);

            String strLine;

            int j = 1;
            while ((strLine = br.readLine().trim()) != null) {
//                System.out.println(j + " " + strLine.length() + " " + strLine);
                String[] arr = strLine.split(" ");
                String newLine = "";
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i].length() > 0) {
                        newLine = newLine + arr[i] + " ";
                    }
                }
//                System.out.println("new line " + newLine);
                if (newLine.length() > 0) {
                    out.write(newLine.trim() + "\n");
                    out.flush();
                }
                j++;
            }
            out.flush();
            in.close();
            out.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void test() {
        double x0 = 1.0;
        double y0 = 1.0;
        double xn = 7.0;
        double yn = 7.0;
        double x = 6.0;
        double y = 2.0;

        double ortho = distanceToSegment(x0, y0, xn, yn, x, y);
        double euc = Math.sqrt(Math.pow((x - x0), 2) + Math.pow((y - y0), 2));
        double length = Math.sqrt(Math.pow(euc, 2) - Math.pow(ortho, 2));

        System.out.println("ortho=" + ortho + " euc=" + euc + " len=" + length);

        double x1 = x - Math.sqrt(Math.pow(ortho, 2) - Math.pow((y - 4), 2));
        System.out.println("x1=" + Math.round(x1));
    }

    public void getStartLine(String fileUrl) {
        try {
            FileInputStream fstream = new FileInputStream(fileUrl);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String strLine;
            br.readLine();
            fixes = new ArrayList();

            while ((strLine = br.readLine()) != null) {
//                System.out.println(j + " " + strLine.length() + " " + strLine);
                String[] arr = strLine.split(" ");
//                System.out.println(arr[6] + " " + arr[7]);

                double[] coord = new double[2];
                coord[0] = Double.parseDouble(arr[6]);
                coord[1] = Double.parseDouble(arr[7]);
                fixes.add(coord);
            }
            arr1 = new Object[fixes.size()];
            fixes.toArray(arr1);
            in.close();
            System.out.println(fixes.size() + " " + fixes.get(0)[0]);
            pointsToKeep = new ArrayList();
//            simplifyDouglasPeuckerFunctionStar(0, fixes.size() - 1);
            simplifyDouglasPeuckerFunctionPlus(0, fixes.size() - 1);
            System.out.println("rest points " + pointsToKeep.size());
            Integer[] arr = new Integer[pointsToKeep.size()];
            pointsToKeep.toArray(arr);
            Arrays.sort(arr);
//            for (int i = 0; i < arr.length; i++) {
//                System.out.println(arr[i]);
//            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void testFunc_1() {
        Coordinate[] list = testArrCoor();

        System.out.println(list.length);

        DouglasPeuckerLineSimplifier simpl = new DouglasPeuckerLineSimplifier(list);
        simpl.setDistanceTolerance(1.5);
        Coordinate[] listRes = simpl.simplify();
        for (int i = 0; i < listRes.length; i++) {
            System.out.println(listRes[i].x + " " + listRes[i].y + " " + listRes[i].toString());
        }
    }

    private void testFunc() {
        fixes = testArray();
        System.out.println(fixes.size());
        arr1 = new Object[fixes.size()];
        fixes.toArray(arr1);
        pointsToKeep = new ArrayList();
        simplifyDouglasPeuckerFunction(0, fixes.size() - 1);

        System.out.println("rest points " + pointsToKeep.size());

        Integer[] arr = new Integer[pointsToKeep.size()];

        pointsToKeep.toArray(arr);
        Arrays.sort(arr);
        for (int i = 0; i < arr.length; i++) {
            System.out.println(arr[i]);
        }
    }

    /**
     * The implementation of the Douglas-Peucker algorithm.
     *
     * @param fixes The complete list of fixes (points)
     * @param start The index of the start point of the segment we are looking at now
     * @param end The index of the end point of the segment we are looking at now
     * @param pointsToKeep The current list of points to keep (to which we might add one)
     * @param distances The current distances corresponding to pointsToKeep
     * (to which we might add one)
     */
    private void simplifyDouglasPeuckerFunction(int start, int end) {

        double maxDistance = 0.0;
        int farthestIndex = 0;

        for (int i = start; i < end; i++) {
            double distance = distanceToSegment(((double[])arr1[start])[0], ((double[])arr1[start])[1], ((double[])arr1[end])[0],
                    ((double[])arr1[end])[1], ((double[])arr1[i])[0], ((double[])arr1[i])[1]);

//            System.out.println(start + " " + end + " dist=" + distance + " max=" + maxDistance + " " + fixes.get(i)[0] + " " + fixes.get(i)[1]);
            if (distance > maxDistance) {
                maxDistance = distance;
                farthestIndex = i;
            }
        }
//        System.out.println("tolimiausias " + farthestIndex + " dist=" + maxDistance);
        if (maxDistance > 1.5 && farthestIndex != 0) {
            pointsToKeep.add(farthestIndex);
//            distances.put(farthestIndex, maxDistance);
            simplifyDouglasPeuckerFunction(start, farthestIndex);
            simplifyDouglasPeuckerFunction(farthestIndex, end);
        }
    }

    private void simplifyDouglasPeuckerFunctionStar(int start, int end) {

        double maxDistance = 0.0;
        int farthestIndex = 0;

//        System.out.println("atejo " + start + " " + end);

        for (int i = start; i < end; i++) {
            double distance = distanceToSegmentStar(((double[])arr1[start])[0], ((double[])arr1[start])[1], ((double[])arr1[end])[0],
                    ((double[])arr1[end])[1], ((double[])arr1[i])[0], ((double[])arr1[i])[1]);
            if (distance > maxDistance && i != start) {
                maxDistance = distance;
                farthestIndex = i;
            }
        }
//        System.out.println("tolimiausias " + farthestIndex + " dist=" + maxDistance + " " + pointsToKeep.size());
        if (maxDistance > 20000.0 && farthestIndex != 0) {
//            System.out.println("         pasiliekam " + farthestIndex + " " + pointsToKeep.size());
            pointsToKeep.add(farthestIndex);
//            distances.put(farthestIndex, maxDistance);
            simplifyDouglasPeuckerFunctionStar(start, farthestIndex);
            simplifyDouglasPeuckerFunctionStar(farthestIndex, end);
        }
    }

    private void simplifyDouglasPeuckerFunctionPlus(int start, int end) {

        double maxDistanceOrt = 0.0;
        int farthestIndex = 0;
        double diffPoints = distanceToSegmentStar(((double[])arr1[start])[0], ((double[])arr1[start])[1], ((double[])arr1[end])[0],
                    ((double[])arr1[end])[1], ((double[])arr1[0])[0], ((double[])arr1[0])[1]);

//        System.out.println("atejo " + start + " " + end);

        for (int i = start; i < end; i++) {
            double distanceOrt = distanceToSegment(((double[])arr1[start])[0], ((double[])arr1[start])[1], ((double[])arr1[end])[0],
                    ((double[])arr1[end])[1], ((double[])arr1[i])[0], ((double[])arr1[i])[1]);

            if (distanceOrt > 25.0 && i != 0) {
//                System.out.println("i " + i);
                double distanceEuc = distanceToSegmentStar(((double[])arr1[start])[0], ((double[])arr1[start])[1], ((double[])arr1[end])[0],
                    ((double[])arr1[end])[1], ((double[])arr1[i])[0], ((double[])arr1[i])[1]);
                double dP = Math.sqrt(distanceEuc * distanceEuc - distanceOrt * distanceOrt);

                if (dP < diffPoints) {
                    farthestIndex = i;
                }
            }


//            if (distanceOrt > maxDistanceOrt) {
//                maxDistanceOrt = distanceOrt;
//                farthestIndex = i;
//            }
        }
//        System.out.println("tolimiausias " + farthestIndex + " dist=" + maxDistance + " " + pointsToKeep.size());
        if (farthestIndex != 0) {
//            System.out.println("         pasiliekam " + farthestIndex + " " + pointsToKeep.size());
            pointsToKeep.add(farthestIndex);
//            distances.put(farthestIndex, maxDistance);
            simplifyDouglasPeuckerFunctionPlus(start, farthestIndex);
            simplifyDouglasPeuckerFunctionPlus(farthestIndex, end);
        }
    }

    /**
     * Calculates the orthogonal (perpendicular) distance between a given point and a line
     * segment (the minimum distance from the point to the line).
     *
     * @param p1x The X coordinate of the start point of the line segment
     * @param p1y The Y coordinate of the start point of the line segment
     * @param p2x The X coordinate of the end point of the line segment
     * @param p2y The Y coordinate of the end point of the line segment
     * @param px The X coordinate of the point
     * @param py The Y coordinate of the point
     * @return The distance between the point and the line segment
     */
    private double distanceToSegment(double p1x, double p1y, double p2x, double p2y, double px, double py) {
        double area = Math.abs(0.5 * (p1x * p2y + p2x * py + px * p1y - p2x * p1y - px * p2y - p1x * py));
        double bottom = Math.sqrt(Math.pow(p1x - p2x, 2) + Math.pow(p1y - p2y, 2));
        double height = area / bottom * 2;

        return height;
    }

    private double distanceToSegmentStar(double p1x, double p1y, double p2x, double p2y, double px, double py) {
        double midX = p1x + (p2x - p1x) / 2;
        double midY = p1y + (p2y - p2y) / 2;
//        System.out.println((midX - px) + " " + (midY - py));
//        double distance = Math.sqrt(Math.pow((midX - px), 2) + Math.pow((midY - py), 2));
        double diffX = midX - px;
        double diffY = midY - py;
        double distance = Math.sqrt(diffX * diffX + diffY * diffY);

        return distance;
    }

    private double[] getPoint(double x, double y) {
        double[] p = new double[2];
        p[0] = x;
        p[1] = y;
        return p;
    }

    private Coordinate getCoord(double x, double y) {
        Coordinate c = new Coordinate(x, y);
        return c;
    }

    private Coordinate[] testArrCoor() {
        ArrayList<Coordinate> list = new ArrayList();
        Coordinate p0 = getCoord(0.0, 0.0);
        list.add(p0);
        Coordinate p1 = getCoord(4.0, 2.0);
        list.add(p1);
        Coordinate p2 = getCoord(6.0, 6.0);
        list.add(p2);
        Coordinate p3 = getCoord(10.0, 5.0);
        list.add(p3);
        Coordinate p4 = getCoord(13.0, 1.0);
        list.add(p4);
        Coordinate p5 = getCoord(16.0, 2.0);
        list.add(p5);
        Coordinate p6 = getCoord(20.0, 1.0);
        list.add(p6);
        Coordinate p7 = getCoord(22.0, 0.0);
        list.add(p7);

        Coordinate[] coordList = new Coordinate[list.size()];
        list.toArray(coordList);

        return coordList;
    }

    private ArrayList<double[]> testArray() {
        ArrayList<double[]> list = new ArrayList();
        double[] p0 = getPoint(0.0, 0.0);
        list.add(p0);
        double[] p1 = getPoint(4.0, 2.0);
        list.add(p1);
        double[] p2 = getPoint(6.0, 6.0);
        list.add(p2);
        double[] p3 = getPoint(10.0, 5.0);
        list.add(p3);
        double[] p4 = getPoint(13.0, 1.0);
        list.add(p4);
        double[] p5 = getPoint(16.0, 2.0);
        list.add(p5);
        double[] p6 = getPoint(20.0, 1.0);
        list.add(p6);
        double[] p7 = getPoint(22.0, 0.0);
        list.add(p7);

        return list;
    }
}
