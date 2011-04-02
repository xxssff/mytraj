package entity;

import java.util.ArrayList;

import org.joda.time.LocalTime;

/**
 * Moving objects can change their membership <br>
 * so a cluster keeps only id of its members <br>
 * For static candidate, refer to Candidate class
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
	public LocalTime expiryTime;

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

	/**
	 * 
	 * @return cluster size
	 */
	public int getSize() {
		return members.size();
	}
}