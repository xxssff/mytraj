package entity;

import data.DataPoint;

/**
 * A trajectory is an array of timeCoord
 * 
 * @author xiaohui
 * 
 */

public class Traj {
	public DataPoint[] dataPoints;

	public Traj() {
		dataPoints = new DataPoint[16];
	}

	public Traj(int size) {
		dataPoints = new DataPoint[size];
	}

	/**
	 * Trajectories are supposed to have the same number of points.
	 * 
	 * @param anotherTraj
	 * @return their dissimilarity
	 */
	public double getDissim(Traj anotherTraj) {
		double res = 0;
		for (int i = 0; i < dataPoints.length; i++) {
			double v1 = dataPoints[i].p.distance(anotherTraj.dataPoints[i].p);
			res += v1 * v1;
		}
		return res;
	}

	public double getDist(Cluster cluster) {
		int n = cluster.getSize();
//		TimeCoord tc = cluster.getCenter();
		return 0;
	}
	
}
