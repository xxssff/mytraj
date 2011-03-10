package entity;

import java.util.ArrayList;

/**
 * Moving objects can change their membership <br>
 * so a cluster keeps only id of its members
 * 
 * @author xiaohui
 * 
 */
public class Cluster {
	public int clusterId;
	public ArrayList<Integer> members;
	public double startTime;
	public double duration;
	private double score;

	// public double endTime;

	public Cluster(int clusterId) {
		this.clusterId = clusterId;
		members = new ArrayList<Integer>();
		startTime = 0;
		duration = 0;
	}

	public void add(Integer moid) {
		members.add(moid);
	}

	public void delete(Integer moid) {
		members.remove(moid);
	}

	// TODO
	public double getBreakTime() {
		return 0;
	}

	
	public int getSize() {
		return members.size();
	}

	/**
	 * 
	 * compute ranking score
	 */
	public void updateScore(double alpha, double beta, double gamma,
			double duration) {
		this.duration = duration;
		// TODO think about the compactness of a cluster
		this.score = alpha * getSize() + beta * duration;
	}

	public double getScore() {
		return score;
	}
	
	
}
