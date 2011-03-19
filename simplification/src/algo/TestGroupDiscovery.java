package algo;

import entity.Cluster;

public class TestGroupDiscovery {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//test combination
		Cluster c1 = new Cluster(1);
		c1.add(1);
		c1.add(2);
		c1.add(3);
		c1.add(4);
		System.out.println(c1.members);
		String[] ss = GroupDiscovery.getCombination(c1, 3);
		for(String s : ss)
		System.out.println(s);
		System.out.println(GroupDiscovery.getCombination(c1, 4)[0]);
	}

}
