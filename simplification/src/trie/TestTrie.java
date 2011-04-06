package trie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.LocalTime;

import entity.Candidates;

public class TestTrie {
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

		LeafEntry le1 = new LeafEntry(a1, LocalTime.MIDNIGHT, 1);
		le1.endCluster(LocalTime.MIDNIGHT.plusMinutes(1), 1, 0.5, 0.5, 1);
		System.out.println(le1.toString());
		
		LeafEntry le2 = new LeafEntry(a2, LocalTime.MIDNIGHT, 1);
		le2.endCluster(LocalTime.MIDNIGHT.plusMinutes(1), 1, 0.5, 0.5, 1);
		System.out.println(le2.toString());
		System.out.println(LeafEntry.dominates(le2, le1));
		System.out.println(LeafEntry.dominates(le1, le2));
		
	
		LeafEntry le3 = new LeafEntry(a3, LocalTime.MIDNIGHT, 1);
		le3.endCluster(LocalTime.MIDNIGHT.plusMinutes(1), 1, 0.5, 0.5, 1);
		
		
		LeafEntry le4 = new LeafEntry(a4, LocalTime.MIDNIGHT, 1);
		le4.endCluster(LocalTime.MIDNIGHT.plusMinutes(1), 1, 0.5, 0.5, 1);
		
		LeafEntry le5 = new LeafEntry(a5, LocalTime.MIDNIGHT, 1);
		le5.endCluster(LocalTime.MIDNIGHT.plusMinutes(1), 1, 0.5, 0.5, 1);
		
		Candidates cList = new Candidates(2);
		cList.add(le3);
		cList.add(le1);
		cList.add(le2);
		cList.add(le4);
		cList.add(le5);
	}
}
