package test;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Seconds;

public class myTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		int[] a ={1, 2};
//		int[] b = new int[a.length+1];
//		System.arraycopy(a, 0 , b, 0, a.length);
//		System.out.println(Arrays.toString(b));
//		b[a.length]=3;
//		System.out.println(Arrays.toString(b));
		
		System.out.println(LocalTime.MIDNIGHT.minusMinutes(1));
		LocalTime lt = new LocalTime("22:49:50");
		System.out.println(lt);
		
		LocalDateTime l1 = new LocalDateTime("2011-01-12T22:49:50");
		LocalDateTime l2 = new LocalDateTime("2011-01-13T22:30:50");
		System.out.println(Seconds.secondsBetween(l1, l2).getSeconds());
		
	}

	
}
