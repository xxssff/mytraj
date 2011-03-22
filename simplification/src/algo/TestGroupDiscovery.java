package algo;

import java.util.Arrays;

import entity.Cluster;

public class TestGroupDiscovery {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// test combination
		Cluster c1 = new Cluster(1);
		c1.add(1);
		c1.add(2);
		c1.add(3);
//		c1.add(4);
		System.out.println(c1.members);

		/**
		 * test get combination
		 */
		String[][] ss = GroupDiscovery.getCombination(c1, 3);
		for (String[] s : ss){
			System.out.println(Arrays.toString(s));
		}
			
		
		/**
		 * test build trie
		 */
//		GroupDiscovery.buildTrie();
		System.out.println(GroupDiscovery.aTrie.toString());
		

			
		System.out.println(GroupDiscovery.getCombination(c1, 4)[0]);
	}

}
