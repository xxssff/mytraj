package entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.joda.time.LocalDateTime;
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
	public Set<Integer> members;
	public LocalDateTime expiryTime;
	public Integer expireOID;
	
	// for convoy
	public boolean assigned;
	
	// public double endTime;

	public Cluster(int clusterId) {
		assigned = false;
		this.clusterId = clusterId;
		members = new TreeSet<Integer>();
	}

	public void add(Integer moid) {
		members.add(moid);
	}

	/**
	 * remove a member
	 * 
	 * @param moid
	 */
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

	/**
	 * 
	 * @return avg distance among all members
	 */
	public double getAvgDist(HashMap<Integer, MovingObject> allObjs) {
		double sumDist = 0;
		int n = members.size();
		Integer[] moidArr = members.toArray(new Integer[0]);

		for (int i = 0; i < n; i++) {
			MovingObject mo1 = allObjs.get(moidArr[i]);
			for (int j = i + 1; j < n; j++) {
				MovingObject mo2 = allObjs.get(moidArr[j]);
				if (mo1 != null && mo2 != null)
					sumDist += mo1.distance(mo2);
			}
		}

		return 2 * sumDist / (n * (n - 1));
	}

	public String toString() {
		return this.clusterId + " " + members;
	}
}
