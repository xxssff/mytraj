package entity;

/**
 * A trajectory is an array of timeCoord
 * 
 * @author xiaohui
 * 
 */

public class Traj {
	public TimeCoord[] timeCoords;

	public Traj() {
		timeCoords = new TimeCoord[16];
	}

	public Traj(int size) {
		timeCoords = new TimeCoord[size];
	}

	/**
	 * Trajectories are supposed to have the same number of points.
	 * 
	 * @param anotherTraj
	 * @return their dissimilarity
	 */
	public double getDissim(Traj anotherTraj) {
		double res = 0;
		for (int i = 0; i < timeCoords.length; i++) {
			double v1 = timeCoords[i].p.distance(anotherTraj.timeCoords[i].p);
			res += v1 * v1;
		}
		return res;
	}

	public double getDist(Cluster cluster) {
		int n = cluster.getSize();
		TimeCoord tc = cluster.getCenter();
		return 0;
	}

	/**
	 * 
	 * @param time
	 * @return the point at the particular time
	 */
	public TimeCoord getPoint(double time) {
		//check time is valid
		if (time < timeCoords[0].t || time > timeCoords[timeCoords.length-1].t){
			System.err.println("Time is wrong: "+time +" "+timeCoords[0].t+" "+timeCoords[timeCoords.length-1].t);
			System.exit(0);
		}
		//find the right line seg
		
		//find the right point
		
		return null;
	}
}
