package entity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import trie.LeafEntry;

public class Candidates {
	public ArrayList<LeafEntry> candidates;
	public double minScore;

	public Candidates() {
		candidates = new ArrayList<LeafEntry>();
		minScore = 0;
	}

	/**
	 * Remove candidates having a specified score
	 * 
	 * @param score
	 */
	public void removeCandidates(double score) {
		for (LeafEntry c : candidates) {
			if (c.getScore() == score) {
				candidates.remove(c);
			}
		}
	}

	/**
	 * remove candidates having minScore // update minScore
	 */
	public void updateMinScore() {
		double min = Double.MAX_VALUE;
		for (LeafEntry c : candidates) {
			if (c.getScore() < min) {
				min = c.getScore();
			}
		}
		this.minScore = min;
	}

	/**
	 * TODO: more reduction to be done along the way<br>
	 * Otherwise too many candidates
	 * 
	 * @param le
	 */
	public void add(LeafEntry le) {
		if (candidates.size() < 10) {
			candidates.add(le);
			minScore = le.getScore();
		} else if (le.getScore() > minScore) {
			candidates.add(le);
			this.minScore = le.getScore();
		}

	}

	/**
	 * @return String rep
	 */
	public String toString() {
		String resStr = "minScore candidateSize: " + minScore + " "
				+ candidates.size();
		return resStr;
	}

	public void toFile(BufferedWriter bw) throws Exception {
		for(LeafEntry le : candidates){
			bw.append(le.toString());
			bw.newLine();
		}
	}
}
