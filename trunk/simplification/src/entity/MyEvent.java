package entity;

public class MyEvent implements Comparable {
	public double time;
	public Cluster cluster;

	public MyEvent(double time, Cluster cluster) {
		this.time = time;
		this.cluster = cluster;
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
