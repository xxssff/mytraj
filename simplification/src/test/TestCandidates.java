package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import trie.LeafEntry;
import entity.CandidatesPlus;
import entity.Global;

public class TestCandidates {

	public static void main(String[] args) {
		Integer[] a1 = { 1, 2, 3 };
		Integer[] a2 = { 1, 2, 3 };
		Integer[] a3 = { 1, 2, 3, 4 };
		Integer[] a4 = { 3, 5};
		Integer[] a5 = { 3, 6, 7};
		List<Integer> l1 = new ArrayList<Integer>(Arrays.asList(a1));
		List<Integer> l2 = new ArrayList<Integer>(Arrays.asList(a2));
		List<Integer> l3 = new ArrayList<Integer>(Arrays.asList(a3));
		List<Integer> l4 = new ArrayList<Integer>(Arrays.asList(a4));
		List<Integer> l5 = new ArrayList<Integer>(Arrays.asList(a5));

		LocalDateTime ldt = new LocalDateTime(Global.infati_MAXTIME);
		
		LeafEntry le1 = new LeafEntry(a1, ldt);
		le1.endCluster(ldt.plusMinutes(1), 0.5, 0.5 );
		System.out.println(le1.toString());
		
		LeafEntry le2 = new LeafEntry(a2, ldt);
		le2.endCluster(ldt.plusMinutes(1),  0.5, 0.5 );
		System.out.println(le2.toString());
		System.out.println(LeafEntry.dominates(le2, le1));
		System.out.println(LeafEntry.dominates(le1, le2));
		
	
		LeafEntry le3 = new LeafEntry(a3, ldt);
		le3.endCluster(ldt.plusMinutes(1),0.5, 0.5);
		
		
		LeafEntry le4 = new LeafEntry(a4, ldt);
		le4.endCluster(ldt.plusMinutes(1),  0.5, 0.5);
		
		LeafEntry le5 = new LeafEntry(a5, ldt);
		le5.endCluster(ldt.plusMinutes(1), 0.5, 0.5);
		
		CandidatesPlus cList = new CandidatesPlus(10);
		cList.add(le3);
		cList.add(le1);
		cList.add(le2);
		cList.add(le4);
		cList.add(le5);
		
	}
}
