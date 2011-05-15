package trie;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.Seconds;

/**
 * LeafEntry for trie
 * 
 * @author xiaohui
 * 
 */
public class LeafEntry implements Comparable {
	public Integer[] subCluster; // integers
	public LocalDateTime ts;
	public LocalDateTime te;
	double alpha, beta, gamma;
	double duration;
	double score;
	double avgDistStart, avgDistEnd, compactness;
	static DecimalFormat formatter = new DecimalFormat("#.###");

	public LeafEntry(Integer[] members, LocalDateTime currTime, double avgDistStart) {
		this.ts = currTime;
		this.subCluster = members;
		this.avgDistStart = avgDistStart;
	}

	public LeafEntry(Set<Integer> members, LocalDateTime currTime, double avgDist) {
		subCluster = members.toArray(new Integer[0]);
		this.ts = currTime;
		this.avgDistStart = avgDist;
	}

	@Override
	public int compareTo(Object leafEntry) {
		if (leafEntry instanceof LeafEntry) {
			LeafEntry le = (LeafEntry) leafEntry;
			if (this.score > le.getScore())
				return 1;
			else if (this.score < le.getScore())
				return -1;
		}
		return 0;
	}

	/**
	 * 
	 * @param le1
	 * @param le2
	 * @return true if le1 dominates le2
	 */
	public static boolean dominates(LeafEntry le1, LeafEntry le2) {
		if (le1.getScore() < le2.getScore()) {
			return false;
		}
		List<Integer> mem1 = new ArrayList<Integer>(
				Arrays.asList(le1.subCluster));
		List<Integer> mem2 = new ArrayList<Integer>(
				Arrays.asList(le2.subCluster));
		boolean sub = CollectionUtils.isSubCollection(mem2, mem1);
		return sub;

	}

	/**
	 * set duration, avgDistEnd, score
	 * 
	 * @param currTime
	 * @param avgDistEnd
	 * @param alpha
	 * @param beta
	 * @param gamma
	 */
	public void endCluster(LocalDateTime currTime, double avgDistEnd, double alpha,
			double beta, double gamma) {
		this.te = currTime;
		this.avgDistEnd = avgDistEnd;
		if (ts == null || currTime == null) {
			this.duration = 100;
		} else {
			this.duration = Seconds.secondsBetween(ts, currTime).getSeconds();
		}
		this.compactness = 2 / (avgDistStart + avgDistEnd);
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;
		this.score = alpha * size() + beta * duration + gamma * compactness;
	}

	public double getAlpha() {
		return this.alpha;
	}

	public double getBeta() {
		return this.beta;
	}

	public double getGamma() {
		return this.gamma;
	}

	/**
	 * 
	 * @return duration;available only when a cluster ends
	 */
	public double getDuration() {
		return duration;
	}

	public double getDistStart() {
		return avgDistStart;
	}

	/**
	 * 
	 * @return avgDistEnd
	 */
	public double getDistEnd() {
		return this.avgDistEnd;
	}

	/**
	 * 
	 * @return candidate score; available only when a cluster ends
	 */

	public double getScore() {
		return score;
	}
	
	public LocalDateTime getStartTime(){
		return this.ts;
	}

	public String toString() {
		if (te == null) {
			return "[leafEntry: " + " " + ts.toString() + " "
					+ Arrays.toString(subCluster) + "]";
		} else {
			String s1 = ts.toString().substring(0, ts.toString().indexOf("."));
			String s2 = te.toString().substring(0, te.toString().indexOf("."));

			return "[s compact dur mems: " + formatter.format(score) + " "
					+ formatter.format(this.compactness) + " "
					// + Seconds.secondsBetween(ts, te).getSeconds()
					+ s1 + " " + s2 + " " + Arrays.toString(subCluster) + "]";
		}
	}

	public int size() {
		return subCluster.length;
	}

	/**
	 * update score when any param is changed.
	 */
	public void updateScore() {
		if (ts == null || te == null) {
			this.duration = 100;
		} else {
			this.duration = Seconds.secondsBetween(ts, te).getSeconds();
		}
		this.score = alpha * size() + beta * duration + gamma * compactness;
	}
}
