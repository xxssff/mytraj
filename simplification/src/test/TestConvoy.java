package test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDateTime;

import entity.Convoy;

public class TestConvoy {
	public static void main(String[] args) {
		LocalDateTime t1, t2;
		t1 = new LocalDateTime("2001-03-27T16:23:24");
		t2 = new LocalDateTime("2001-03-27T16:53:24");
		Set<Integer> mems1 = new HashSet<Integer>();
		mems1.add(1049);
		mems1.add(1082);
		mems1.add(1103);

		Convoy c1 = new Convoy();
		c1.members = mems1;
		c1.startTime = t1;
		c1.endTime = t2;

		Convoy c2 = new Convoy();
		c2.members = mems1;
		c2.startTime = t1;
		c2.endTime = t2;
		
		List<Convoy> cy = new ArrayList<Convoy>();
		cy.add(c1);
		System.out.println(cy.contains(c2));
		
		boolean contain= false;
		for(Convoy c : cy){
			if(c.equals(c2)){
				contain=true;
				break;
			}
		}
		if(!contain){
			cy.add(c2);
		}
		
//		if(!cy.contains(c2)){
//			cy.add(c2);
//		}
		
		System.out.println(cy);
		System.out.println(c1.equals(c2));
	}
}
