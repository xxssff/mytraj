package test;

import org.joda.time.LocalTime;

import entity.ClusterEvolutionTable;

import trie.LeafEntry;

public class TestClusterEvoleTable {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Integer[] a1 = { 1, 2, 3, 4 };
		Integer[] a2 = { 1, 2, 3, 4, 5 };
		Integer[] a3 = { 1, 2, 4, 5 };
		Integer[] a4 = { 1, 2, 5 };
		int minPts = 3;
		ClusterEvolutionTable cet = new ClusterEvolutionTable(minPts);
		Integer cid= 10;
		
		//At midnight, cluster <1, 2, 3, 4> forms  
		LeafEntry le1 = new LeafEntry(a1, LocalTime.MIDNIGHT, 1);
		
		//At time 00:01:00, object 5 joins cluster <1, 2, 3, 4> 
		le1.endCluster(LocalTime.MIDNIGHT.plusMinutes(1), 1, 0.5, 0.5, 1);
		cet.add(cid, le1);
		LeafEntry le2 = new LeafEntry(a2, LocalTime.MIDNIGHT.plusMinutes(1), 1);
		
		//At time 00:02:00, object 3 leaves cluster <1, 2, 3, 4, 5> 
		le2.endCluster(LocalTime.MIDNIGHT.plusMinutes(2), 1, 0.5, 0.5, 1);
		cet.add(cid, le2);
		LeafEntry le3 = new LeafEntry(a3, LocalTime.MIDNIGHT.plusMinutes(2), 1);
		
		//At time 00:03:00, object 4 leaves cluster <1, 2, 4, 5> 
		le3.endCluster(LocalTime.MIDNIGHT.plusMinutes(3), 1, 0.5, 0.5, 1);
		cet.add(cid,  le3);
		LeafEntry le4 = new LeafEntry(a4, LocalTime.MIDNIGHT.plusMinutes(3), 1);
		
		//At time 00:04:00, cluster 10 expires
		cet.add(cid, le4);
		le4.endCluster(LocalTime.MIDNIGHT.plusMinutes(4), 1, 0.5, 0.5, 1);
		
		System.out.println("before cascading...");
		System.out.println(cet.toString());
		
		cet.cascadeAction();
		System.out.println("after cascading...");
		System.out.println(cet.toString());

	}

}
