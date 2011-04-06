package trie;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.LocalTime;
import org.joda.time.Period;

/**
 * LeafEntry for trie
 * 
 * @author xiaohui
 * 
 */
public class LeafEntry implements Comparable {
	public Integer[] subCluster; // integers
	public LocalTime ts, te;
	double duration;
	double score;
	double avgDistStart, avgDist;
	static DecimalFormat formatter = new DecimalFormat("#.###");
	
	public LeafEntry(Integer[] members, LocalTime currTime, double avgDistStart) {
		this.ts = currTime;
		this.subCluster = members;
		this.avgDistStart = avgDistStart;
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
	 * @param te
	 * @param avgDistEnd
	 * @param alpha
	 * @param beta
	 * @param gamma
	 */
	public void endCluster(LocalTime te, double avgDistEnd, double alpha,
			double beta, double gamma) {
		Period p = new Period(ts, te);
		this.te = te;
		this.duration = p.toStandardSeconds().getSeconds();
		this.avgDist = (avgDistStart + avgDistEnd) / 2;
		this.score = alpha * size() + beta * duration + gamma / avgDist;
	}

	/**
	 * 
	 * @return candidate score; available only when a cluster ends
	 */

	public double getScore() {
		return score;
	}

	/**
	 * 
	 * @return duration;available only when a cluster ends
	 */
	public double getDuration() {
		return duration;
	}

	public String toString() {
		if (te == null) {
			return "[leafEntry: " + " " + ts.toString() + " "
					+ Arrays.toString(subCluster) + "]";
		} else {
			return "[score du avgD Mems: " + formatter.format(score) + " " + this.avgDist + " "
					// + Seconds.secondsBetween(ts, te).getSeconds()
					+ ts.toString() + " " + te.toString() + " "
					+ Arrays.toString(subCluster) + "]";
		}
	}

	public int size() {
		return subCluster.length;
	}

}
