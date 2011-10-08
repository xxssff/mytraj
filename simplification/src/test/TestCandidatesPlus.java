package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import trie.LeafEntry;
import entity.CandidatesPlus;
import entity.Global;

public class TestCandidatesPlus {

	public static void main(String[] args) {
		LocalDateTime ldt = new LocalDateTime(Global.infati_MAXTIME);

		Integer[] mem1 = { 102, 8, 84, 116, 28 };
		Integer[] mem2 = { 138, 27, 53, 64, 105 };
		Integer[] mem3 = { 138, 29, 105, 127, 122 };
		Integer[] mem4 = { 201, 202, 198, 212, 210, 189 };
		Integer[] mem5 = { 201, 202, 198, 210, 189 };
		Integer[] mem6 = { 206, 202, 217, 210, 184, 190, 189, 182, 181 };
		Integer[] mem7 = { 206, 202, 217, 210, 184, 190, 189, 181 };
		Integer[] mem8 = { 206, 202, 217, 210, 184, 190, 189 };
		Integer[] mem9 = { 202, 217, 210, 184, 190, 189 };
		Integer[] mem10 = { 202, 210, 184, 190, 189 };

		LeafEntry[] larray = new LeafEntry[10];

		larray[0] = new LeafEntry(mem1, ldt);
		larray[1] = new LeafEntry(mem2, ldt);
		larray[2] = new LeafEntry(mem3, ldt);
		larray[3] = new LeafEntry(mem4, ldt);
		larray[4] = new LeafEntry(mem5, ldt);
		larray[5] = new LeafEntry(mem6, ldt);
		larray[6] = new LeafEntry(mem7, ldt);
		larray[7] = new LeafEntry(mem8, ldt);
		larray[8] = new LeafEntry(mem9, ldt);
		larray[9] = new LeafEntry(mem10, ldt);

		larray[0].endCluster(ldt.plusMinutes(10), 0.5, 0.5);
		for (int i = 1; i < larray.length; i++) {
			larray[i].endCluster(ldt.plusMinutes(1), 0.5, 0.5);
		}
		for (int i = 0; i < larray.length; i++) {
			for (int j = i + 1; j < larray.length; j++) {
				System.out.println((i + 1) + "-" + (j + 1) + ":"
						+ larray[i].getSimilarity(larray[j]));
			}

		}

		Integer[] a1 = { 1, 2, 3 };
		Integer[] a2 = { 1, 2, 3 };
		Integer[] a3 = { 1, 2, 3, 4 };
		Integer[] a4 = { 3, 5 };
		Integer[] a5 = { 3, 6, 7 };
		List<Integer> l1 = new ArrayList<Integer>(Arrays.asList(a1));
		List<Integer> l2 = new ArrayList<Integer>(Arrays.asList(a2));
		List<Integer> l3 = new ArrayList<Integer>(Arrays.asList(a3));
		List<Integer> l4 = new ArrayList<Integer>(Arrays.asList(a4));
		List<Integer> l5 = new ArrayList<Integer>(Arrays.asList(a5));

		//
		// LeafEntry le1 = new LeafEntry(a1, ldt);
		// le1.endCluster(ldt.plusMinutes(1), 0.5, 0.5);
		// System.out.println(le1.toString());
		//
		// LeafEntry le2 = new LeafEntry(a2, ldt);
		// le2.endCluster(ldt.plusMinutes(1), 0.5, 0.5);
		// System.out.println(le2.toString());
		// System.out.println(LeafEntry.dominates(le2, le1));
		// System.out.println(LeafEntry.dominates(le1, le2));
		//
		// LeafEntry le3 = new LeafEntry(a3, ldt);
		// le3.endCluster(ldt.plusMinutes(1), 0.5, 0.5);
		//
		// LeafEntry le4 = new LeafEntry(a4, ldt);
		// le4.endCluster(ldt.plusMinutes(1), 0.5, 0.5);
		//
		// LeafEntry le5 = new LeafEntry(a5, ldt);
		// le5.endCluster(ldt.plusMinutes(1), 0.5, 0.5);

		CandidatesPlus cList = new CandidatesPlus(10, null);
		for (int i = 0; i < larray.length; i++) {
			cList.add(larray[i], 0.3);
		}

		//		
		// cList.add(le1);
		// cList.add(le2);
		// cList.add(le4);
		// cList.add(le5);
		System.out.println("===========After cleaning======");
		System.out.println(cList.toString());
		
		for (LeafEntry le : cList.getTopK()) {
			System.out.println(le.toString());
		}
		
		
	}
	
	static double getAvgSimilarity(List<LeafEntry> resultEntries) {
		LeafEntry[] array = resultEntries.toArray(new LeafEntry[0]);
		double sum = 0.0;
		int size = array.length;
		for (int i = 0; i < array.length; i++) {
			for (int j = i + 1; j < array.length; j++) {
				sum += array[i].getSimilarity(array[j]);
			}
		}
		return sum / (size * (size + 1));
	}
}
