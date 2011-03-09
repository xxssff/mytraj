package entity;

public class MyEvent implements Comparable {
	public double time;
	public int CID;
	public MyEvent(double time, Integer CID) {
		this.time = time;
		this.CID = CID;
	}

	@Override
	public int compareTo(Object arg0) {
		if (arg0 instanceof MyEvent) {
			MyEvent e = (MyEvent) arg0;
			if (this.time > e.time) {
				return 1;
			} else if (this.time < e.time) {
				return -1;
			}
		}
		return 0;
	}

}
