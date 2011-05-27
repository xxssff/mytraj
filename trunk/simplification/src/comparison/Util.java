/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package comparison;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.collections.CollectionUtils;

/**
 *
 * @author ceikute
 */
public class Util {

    public ArrayList<Group> getSet(String fileUrl) throws FileNotFoundException, IOException {
        FileInputStream fstream = new FileInputStream(fileUrl);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        ArrayList groups = new ArrayList<Group>();

        String line = br.readLine(); //first line
        int id = 0;
        while ((line = br.readLine()) != null) {
            //[s compact dur mems: 439230.001 0.001 1993-06-17T17:58:52 1993-06-22T19:58:48 [186, 187, 191, 188, 189, 179, 183, 181, 205, 207, 206, 201, 203, 202, 197, 196, 198, 193, 192, 195, 194, 221, 216, 217, 213, 214, 215, 208, 209, 210, 211, 227, 224, 105]]
            String[] str = line.split("\\[");
            Group g = new Group();
            for (int i = 0; i < str.length; i++) {
                if (str.length > 1) {
//                    System.out.println("line " + str.length + " " + str[i]);
                    if (str[i].length() > 1 && i == 1) {
                        String[] first = str[i].split(" ");
//                        System.out.println("score " + first[4] + " " + Float.parseFloat(first[4]));
                        g.score = Double.parseDouble(first[4]); // set score;
                    } else if (str[i].length() > 1 && i > 1) {
                        String[] group = str[i].split("\\]")[0].split(", ");
                        //System.out.println("line " + str[i] + " " + str[i].length());
                        ArrayList<Integer> list = new ArrayList<Integer>();
                        for (int j = 0; j < group.length; j++) {
//                            System.out.println(group[j]);
                            list.add(Integer.parseInt(group[j]));
                        }
                        g.members = list;
                        g.id = id;
                        id++;
                        groups.add(g);
                    }
                }
            }
        }
        in.close();
        return groups;
    }

    public void doCompare() throws IOException {
        ArrayList<Group> list1 = getSet("C:\\paper_2\\list_comparision\\varying_m\\elk_0_5.out");
//        System.out.println("final size " + list.size());

        ArrayList<Group> list2 = getSet("C:\\paper_2\\list_comparision\\varying_m\\elk_50_5.out");
        for (int i = 0; i < list2.size(); i++) {
            System.out.println(i + " group_idx=" + list2.get(i).id);
        }

        ArrayList<Match> resFinal = new ArrayList<Match>();

        while (list2.size() > 0) {
            ArrayList<Match> res = new ArrayList<Match>();
            for (int i = 0; i < list2.size(); i++) {
                Group g2 = list2.get(i);
                int diff = Integer.MAX_VALUE;
                int idx = -1;
                int id = -1;
                for (int j = 0; j < list1.size(); j++) {
                    Group g1 = list1.get(j);
                    CollectionUtils u = new CollectionUtils();
                    Collection c = u.intersection(g2.members, g1.members);
                    int check = c.size();
                    if (check < diff) {
                        diff = check;
                        idx = j;
                        id = g1.id;
                    }
//                    System.out.println(g1.members.size() + " " + g2.members.size() + " " + check);
                }
                Match m = new Match(idx, id, diff);
                res.add(m);
            }
            System.out.println("-----------------");
            for (int i = 0; i < res.size(); i++) {
                System.out.println(i + " group_idx=" + res.get(i).idx + " gorup_id=" + res.get(i).id + " " + res.get(i).diff);
            }
            System.out.println("-----------first Set --------------");
            int id = res.get(0).id; //id of list1
            int diff = res.get(0).diff;
            int idx_rem = 0; // if to remove from list2
            for (int i = 1; i < res.size(); i++) {
                //System.out.println( i + " group=" + res.get(i).id + " " + res.get(i).diff);
                if (res.get(i).id == id && res.get(i).diff > diff) {
                    idx_rem = i;
                    diff = res.get(i).diff;
                }
            }
            System.out.println("first match fits: " + idx_rem + " " + res.get(idx_rem).id + " " + res.get(idx_rem).idx);
            resFinal.add(res.get(idx_rem));
            list2.remove(idx_rem);
            for (int k = 0; k < list1.size(); k++) {
                if (res.get(idx_rem).id == list1.get(k).id) {
                    list1.remove(res.get(idx_rem).id);
                    System.out.println("removing.. " + res.get(idx_rem).id + "=================");
                    break;
                }
            }
            System.out.println("========= list1_size=" + list1.size());
        }
        for (int i = 1; i < resFinal.size(); i++) {
            System.out.println(i + " group_idx=" + resFinal.get(i).idx + " gorup_id=" + resFinal.get(i).id + " " + resFinal.get(i).diff);
        }
    }

    private double doCompareInOrder(String urlRaw, String urlSimpl) throws IOException {
        ArrayList<Group> list1 = getSet(urlRaw);
//        System.out.println("final size " + list.size());

        ArrayList<Group> list2 = getSet(urlSimpl);
//        for (int i = 0; i < list2.size(); i++) {
//            System.out.println(i + " group_idx=" + list2.get(i).id);
//        }

        ArrayList<Match> res = new ArrayList<Match>();
        for (int i = 0; i < list2.size(); i++) {
            Group g2 = list2.get(i);
            Group g1 = list1.get(i);

            CollectionUtils u = new CollectionUtils();
            Collection c = u.intersection(g2.members, g1.members);
            int check = c.size();

            Match m = new Match(i, i, check);
            res.add(m);
        }
        System.out.println("-----------------");
        double sum = 0;
        for (int i = 0; i < res.size(); i++) {
           System.out.println(i + " group_diff=" + res.get(i).diff + " size=" + list2.get(i).members.size() + " size1=" + list1.get(i).members.size());
        }
        for (int i = 0; i < list2.size(); i++) {
           //System.out.println(i + " group_idx=" + res.get(i).idx + " gorup_id=" + res.get(i).id + " " + res.get(i).diff);
            sum = sum + ((double)res.get(i).diff / (double)list2.get(i).members.size());
        }
        System.out.println("precision: " + (sum / (double)list2.size()));
        return sum / (double)list2.size();
    }

    public void compareVaryingM() throws IOException {
        int simpl = 50;

        FileWriter fstreamOut = new FileWriter("C:\\paper_2\\list_comparision\\varying_m\\precisionM.dat");
        BufferedWriter out = new BufferedWriter(fstreamOut);

        String line = "delta m P MAP nDCG";
        out.write(line + "\n");

        while (simpl <= 200) {
            line = Integer.toString(simpl);
            System.out.println("simpl=" + simpl);
            int m = 5;

            String urlRaw = "C:\\paper_2\\list_comparision\\varying_m\\elk_0_" + m + ".out";
            String urlSimpl = "C:\\paper_2\\list_comparision\\varying_m\\elk_" + simpl + "_" + m + ".out";
            double p = doCompareInOrder(urlRaw, urlSimpl);
            double map = doMAP(urlRaw, urlSimpl);
            double nDCG = do_nDCG(urlRaw, urlSimpl);

            String lineOut = simpl + " " + m + " " + p + " " + map + " " + nDCG;
            out.write(lineOut + "\n");

            m = 7;
            urlRaw = "C:\\paper_2\\list_comparision\\varying_m\\elk_0_" + m + ".out";
            urlSimpl = "C:\\paper_2\\list_comparision\\varying_m\\elk_" + simpl + "_" + m + ".out";
            p = doCompareInOrder(urlRaw, urlSimpl);
            map = doMAP(urlRaw, urlSimpl);
            nDCG = do_nDCG(urlRaw, urlSimpl);

            lineOut = simpl + " " + m + " " + p + " " + map + " " + nDCG;

            out.write(lineOut + "\n");

            m = 10;
            urlRaw = "C:\\paper_2\\list_comparision\\varying_m\\elk_0_" + m + ".out";
            urlSimpl = "C:\\paper_2\\list_comparision\\varying_m\\elk_" + simpl + "_" + m + ".out";
            p = doCompareInOrder(urlRaw, urlSimpl);
            map = doMAP(urlRaw, urlSimpl);
            nDCG = do_nDCG(urlRaw, urlSimpl);

            lineOut = simpl + " " + m + " " + p + " " + map + " " + nDCG;
            out.write(lineOut + "\n");

            m = 15;
            urlRaw = "C:\\paper_2\\list_comparision\\varying_m\\elk_0_" + m + ".out";
            urlSimpl = "C:\\paper_2\\list_comparision\\varying_m\\elk_" + simpl + "_" + m + ".out";
            p = doCompareInOrder(urlRaw, urlSimpl);
            map = doMAP(urlRaw, urlSimpl);
            nDCG = do_nDCG(urlRaw, urlSimpl);

            lineOut = simpl + " " + m + " " + p + " " + map + " " + nDCG;
            out.write(lineOut + "\n");

            if (simpl == 50)
                simpl += 50;
            else
                simpl += 100;
        }
        out.close();
    }

    public void compareVaryingE() throws IOException {
        int simpl = 50;

        FileWriter fstreamOut = new FileWriter("C:\\paper_2\\list_comparision\\varying_e\\precisionE.dat");
        BufferedWriter out = new BufferedWriter(fstreamOut);

        String line = "delta e P MAP nDCG";
        out.write(line + "\n");

        while (simpl <= 200) {
            System.out.println("simpl=" + simpl);
            int e = 500;

            while (e <= 2000) {
                String urlRaw = "C:\\paper_2\\list_comparision\\varying_e\\elk_0_" + e + ".out";
                String urlSimpl = "C:\\paper_2\\list_comparision\\varying_e\\elk_" + simpl + "_" + e + ".out";
                double p = doCompareInOrder(urlRaw, urlSimpl);
                double map = doMAP(urlRaw, urlSimpl);
                double nDCG = do_nDCG(urlRaw, urlSimpl);

                String lineOut = simpl + " " + e + " " + p + " " + map + " " + nDCG;

                out.write(lineOut + "\n");

                e += 500;
            }

            if (simpl == 50)
                simpl += 50;
            else
                simpl += 100;
        }
        out.close();
    }

    private double doMAP(String urlRaw, String urlSimpl) throws FileNotFoundException, IOException {
        ArrayList<Group> list1 = getSet(urlRaw);
        ArrayList<Group> list2 = getSet(urlSimpl);

       
        double sum = (double)0;

        for (int i = 0; i < list2.size(); i++) {
            Group g1 = list1.get(i);
            Group g2 = list2.get(i);

            ArrayList<Integer> members = new ArrayList<Integer>();
            double sumAveP = 0.0;
            CollectionUtils u = new CollectionUtils();
            int prev = 0;
            for (int j = 0; j < g2.members.size(); j++) {
                members.add(g2.members.get(j));
                
                Collection c = u.intersection(members, g1.members);
                int check = c.size();
                if (check > prev) {
                    sumAveP = sumAveP + ((double)check / ((double)j + (double)1));
                    prev = check;
                    System.out.println("        j=" + j + " " + check + " " + sumAveP + " " + check + " " + j + " " + ((double)check / ((double)j + 1.0)));
                }
            }
            System.out.println(sumAveP);
            Collection c = u.intersection(g2.members, g1.members);
            int check = c.size();
            sum += (sumAveP / (double)check);
            System.out.println("check " + check + " " + (sumAveP / (double)check));
        }

        double MAP = sum / (double) list2.size();
        System.out.println("MAP " + MAP);
        return MAP;
    }

    public double do_nDCG(String urlRaw, String urlSimpl) throws IOException {
        double nDCG = 0.0;

        ArrayList<Group> list1 = getSet(urlRaw);
        ArrayList<Group> list2 = getSet(urlSimpl);

        ArrayList<Double> matchList = new ArrayList<Double>();
        for (int i = 0; i < list2.size(); i++) {
            Group g1 = list1.get(i);
            Group g2 = list2.get(i);

            CollectionUtils u = new CollectionUtils();
            Collection c = u.intersection(g2.members, g1.members);
            int check = c.size();

            double value = (double)check * (double)100.0 / (double)g2.members.size();
            matchList.add(value);
        }

        double sum = 0.0;
        for (int i = 0; i < matchList.size(); i++) {
            double value = matchList.get(i);
            double top = Math.pow((double)2, value) - (double)1;
            double bottom = Math.log((double)2 + (double)i) / Math.log((double)2);
            sum += top / bottom;
        }

        System.out.println("---------");
        Collections.sort(matchList);

        double iSum = 0.0;
        int idx = 1;
        for (int i = matchList.size()-1; i >= 0; i--) {
            double value = matchList.get(i);
            double top = Math.pow((double)2, value) - (double)1;
            double bottom = Math.log((double)1 + (double)idx) / Math.log((double)2);
            iSum += top / bottom;
            idx++;
        }
        
        nDCG = sum / iSum;
        System.out.println("nDCG " + nDCG + " " + sum + " " + iSum);
        
        return nDCG;
    }
}
