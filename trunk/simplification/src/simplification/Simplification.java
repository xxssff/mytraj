/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simplification;

import com.vividsolutions.jts.geom.Coordinate;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Vaida
 */
public class Simplification {

    private Coordinate[] routeCoords;
    private ArrayList<Integer> pointsToKeep;

    public Integer[] getStartRoute(String fileUrl, int algorithm, double epsilon) {
        Integer[] resArr = null;
        try {
            FileInputStream fstream = new FileInputStream(fileUrl);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String strLine;
            br.readLine();
            ArrayList<Coordinate> coordArr = new ArrayList();

            while ((strLine = br.readLine()) != null) {
//                System.out.println(j + " " + strLine.length() + " " + strLine);
                String[] arr = strLine.split(" ");
//                System.out.println(arr[6] + " " + arr[7]);
                Coordinate coord = new Coordinate(Double.parseDouble(arr[8]), Double.parseDouble(arr[9]), Double.parseDouble(arr[5]));
                coordArr.add(coord);
            }
            in.close();
            routeCoords = new Coordinate[coordArr.size()];
            coordArr.toArray(routeCoords);

            pointsToKeep = new ArrayList();
//            simplifyDouglasPeuckerFunctionStar(0, fixes.size() - 1);
            if (algorithm == 0) {
                simplifyDouglasPeuckerFunction(0, routeCoords.length - 1, epsilon);
            } else if (algorithm == 1) {
                simplifyDouglasPeuckerFunctionStar(0, routeCoords.length - 1, epsilon);
            } else if (algorithm == 2) {
                simplifyDouglasPeuckerFunctionPlus(0, routeCoords.length - 1, epsilon);
            }
//            System.out.println("rest points " + pointsToKeep.size());
            resArr = new Integer[pointsToKeep.size()];
            pointsToKeep.toArray(resArr);
            Arrays.sort(resArr);
//            for (int i = 0; i < resArr.length; i++) {
//                System.out.println(resArr[i]);
//            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return resArr;
    }

    public Integer[] simplifyFromArr(int algorithm, double epsilon) {
        Integer[] resArr = null;
        pointsToKeep = new ArrayList();

        if (algorithm == 0) {
            simplifyDouglasPeuckerFunction(0, routeCoords.length - 1, epsilon);
        }
//        System.out.println("rest points " + pointsToKeep.size());
        resArr = new Integer[pointsToKeep.size()];
        pointsToKeep.toArray(resArr);

        return resArr;
    }

    private double distanceToSegment(double p1x, double p1y, double p2x, double p2y, double px, double py) {
        double area = Math.abs(0.5 * (p1x * p2y + p2x * py + px * p1y - p2x * p1y - px * p2y - p1x * py));
        double bottom = Math.sqrt(Math.pow(p1x - p2x, 2) + Math.pow(p1y - p2y, 2));
        double dist = area / bottom * 2;

        return dist;
    }

    public double distToSeg3D(double x1, double y1, double z1, double x2, double y2, double z2, double x, double y, double z) {
        // vector from point to end of line
        double v1 = x2 - x; //x1
        double v2 = y2 - y; //y1
        double v3 = z2 - z; //z1

        // directional vector of line
        double dv1 = (x2 - x1); //x2
        double dv2 = (y2 - y1); //y2
        double dv3 = (z2 - z1); //z2

        double d1 = (v2 * dv3) - (v3 * dv2);
        double d2 = (v3 * dv1) - (v1 * dv3);
        double d3 = (v1 * dv2) - (v2 * dv1);

        double d_2 = Math.pow(d1, 2) + Math.pow(d2, 2) + Math.pow(d3, 2);
        double dist = Math.sqrt(d_2) / Math.sqrt(dv1 * dv1 + dv2 * dv2 + dv3 * dv3);

        return dist;
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

    private void simplifyDouglasPeuckerFunction(int start, int end, double epsilon) {

        double maxDistance = 0.0;
        int farthestIndex = 0;
        double x = 0.0;
        double y = 0.0;

        for (int i = start; i < end; i++) {
            double distance = distanceToSegment(routeCoords[start].x, routeCoords[start].y, routeCoords[end].x,
                    routeCoords[end].y, routeCoords[i].x, routeCoords[i].y);
//            double distance = distToSeg3D(routeCoords[start].x, routeCoords[start].y, routeCoords[start].z, routeCoords[end].x,
//                    routeCoords[end].y, routeCoords[end].z, routeCoords[i].x, routeCoords[i].y, routeCoords[i].z);
            if (distance > maxDistance) {
                maxDistance = distance;
                farthestIndex = i;
                x = routeCoords[i].x;
                y = routeCoords[i].y;
            }
        }
//        System.out.println("tolimiausias " + farthestIndex + " dist=" + maxDistance);
        if (maxDistance > epsilon && farthestIndex != 0) {
            pointsToKeep.add(farthestIndex);
//            System.out.println(farthestIndex + " " + x + " " + y);
//            distances.put(farthestIndex, maxDistance);
            simplifyDouglasPeuckerFunction(start, farthestIndex, epsilon);
            simplifyDouglasPeuckerFunction(farthestIndex, end, epsilon);
        }
    }

    private void simplifyDouglasPeuckerFunctionStar(int start, int end, double epsilon) {

        double maxDistance = 0.0;
        int farthestIndex = 0;

//        System.out.println("atejo " + start + " " + end);

        for (int i = start; i < end; i++) {
            double distance = distanceToSegmentStar(routeCoords[start].x, routeCoords[start].y, routeCoords[end].x, routeCoords[end].y, routeCoords[i].x, routeCoords[i].y);
            if (distance > maxDistance && i != start) {
                maxDistance = distance;
                farthestIndex = i;
            }
        }
//        System.out.println("tolimiausias " + farthestIndex + " dist=" + maxDistance + " " + pointsToKeep.size());
        if (maxDistance > epsilon && farthestIndex != 0) {
//            System.out.println("         pasiliekam " + farthestIndex + " " + pointsToKeep.size());
            pointsToKeep.add(farthestIndex);
//            distances.put(farthestIndex, maxDistance);
            simplifyDouglasPeuckerFunctionStar(start, farthestIndex, epsilon);
            simplifyDouglasPeuckerFunctionStar(farthestIndex, end, epsilon);
        }
    }

    private void simplifyDouglasPeuckerFunctionPlus(int start, int end, double epsilon) {

        double maxDistanceOrt = 0.0;
        int farthestIndex = 0;
        double diffPoints = distanceToSegmentStar(routeCoords[start].x, routeCoords[start].y, routeCoords[end].x, routeCoords[end].y, routeCoords[0].x, routeCoords[0].y);

//        System.out.println("atejo " + start + " " + end);

        for (int i = start; i < end; i++) {
            double distanceOrt = distanceToSegment(routeCoords[start].x, routeCoords[start].y, routeCoords[end].x, routeCoords[end].y, routeCoords[i].x, routeCoords[i].y);

            if (distanceOrt > epsilon && i != 0) {
//                System.out.println("i " + i);
                double distanceEuc = distanceToSegmentStar(routeCoords[start].x, routeCoords[start].y, routeCoords[end].x, routeCoords[end].y, routeCoords[i].x, routeCoords[i].y);
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
            simplifyDouglasPeuckerFunctionPlus(start, farthestIndex, epsilon);
            simplifyDouglasPeuckerFunctionPlus(farthestIndex, end, epsilon);
        }
    }

    public void setRouteCoords(Coordinate[] routeCoords) {
        this.routeCoords = routeCoords;
    }

    public Coordinate[] getRouteCoords() {
        return routeCoords;
    }
}
