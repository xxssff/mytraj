package trie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.joda.time.LocalTime;

/**
 * LeafEntry for trie
 * 
 * @author xiaohui
 * 
 */
public class LeafEntry {
	public String[] subCluster;
	public LocalTime ts, te;
	double duration;
	double score;
	public void  computeDuration() {
		int sHour = te.getHourOfDay();
		int sMin = te.getMinuteOfHour();
		int sSec = te.getSecondOfMinute();

		int endHour = te.getHourOfDay();
		int endMin = te.getMinuteOfHour();
		int endSec = te.getSecondOfMinute();

		while (endHour < sHour) {
			endHour += 12;
		}

		endMin += 60 * (endHour - sHour);
		if (endSec < sSec) {
			endMin--;
			endSec += 60;
		}
		this.duration = endMin - sMin + (endSec - sSec) / 60.0;
	}
	
	public LeafEntry(String[] members, LocalTime currTime) {
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
	
	public double getDuration(){
		return duration;
	}

	public String toString() {
		return "[leafEntry: " + " " + ts.toString() + " " + Arrays.toString(subCluster) + "]";
	}

	public int size() {
		return subCluster.length;
	}

	

	/**
	 * 
	 * compute ranking score
	 */
	public void setScore(double alpha, double beta, double gamma,
			double duration) {
		// TODO think about the compactness of a cluster
		this.score = alpha * size() + beta * duration;
	}

}
