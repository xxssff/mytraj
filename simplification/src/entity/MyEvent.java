package entity;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

public class MyEvent implements Comparable {
	public LocalDateTime time;
	public int OID;
	public int CID;
	public EventType type;

	public MyEvent(LocalDateTime exitTime, int OID, int CID, EventType type) {
		this.time = exitTime;
		this.OID = OID;
		this.CID = CID;
		this.type = type;
	}

	@Override
	public int compareTo(Object arg0) {
		if (arg0 instanceof MyEvent) {
			MyEvent e = (MyEvent) arg0;
			if (this.time.isAfter(e.time)) {
				return 1;
			} else if (this.time.isBefore(e.time)) {
				return -1;
			}
		}
		return 0;
	}
	
	public String toString(){
		return "("+time.toString()+" "+ OID+" "+CID+" "+type+")";
	}

}
