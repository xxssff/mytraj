package test;

import org.joda.time.LocalDateTime;


public class ClusterTest {

	public static void main(String[] args) {
//		Cluster c1 = new Cluster(1);
//		Cluster c2 = new Cluster(1);
//		c1.add(1);
//		c1.add(2);
//		c1.add(3);
//		c1.add(4);
		
		LocalDateTime ldt = new LocalDateTime("2011-02-23T12:14:00");
		LocalDateTime ldt1 = new LocalDateTime("2011-02-23T12:14:00");
		System.out.println(ldt.isEqual((ldt1)));
		
	}
	
	
}
