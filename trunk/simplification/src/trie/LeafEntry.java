package trie;

import java.util.Arrays;

import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.Seconds;

/**
 * LeafEntry for trie
 * 
 * @author xiaohui
 * 
 */
public class LeafEntry {
	public Object[] subCluster;
	public LocalTime ts, te;
	double duration;
	double score;

	/**
	 * compute Duration And Score of this entry
	 */
	public void computeDuration() {
		Period p = new Period(ts, te);
		this.duration = p.toStandardSeconds().getSeconds();
	}

	public LeafEntry(Object[] members, LocalTime currTime) {
		this.ts = currTime;
		this.subCluster = members;
	}

	/**
	 * 
	 * @return candidate score
	 */

	public double getScore() {
		return score;
	}

	public double getDuration() {
		return duration;
	}

	public String toString() {
		if (te == null) {
			return "[leafEntry: " + " " + ts.toString() + " "
					+ Arrays.toString(subCluster) + "]";
		} else {
			return "[score dura Mems: " + score + " "
					// + Seconds.secondsBetween(ts, te).getSeconds()
					+ ts.toString() + " " + te.toString() + " "
					+ Arrays.toString(subCluster) + "]";
		}
	}

	public int size() {
		return subCluster.length;
	}

	/**
	 * 
	 * compute ranking score
	 */
	public void setScore(double alpha, double beta, double gamma) {
		// TODO think about the compactness of a cluster
		this.score = alpha * size() + beta * duration;
	}

}
