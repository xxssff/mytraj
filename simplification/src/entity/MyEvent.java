package entity;

import org.joda.time.LocalTime;

public class MyEvent implements Comparable {
	public LocalTime time;
	public int OID;
	public int CID;
	public EventType type;

	public MyEvent(LocalTime time, int OID, int CID, EventType type) {
		this.time = time;
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

}