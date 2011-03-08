package entity;

import java.util.ArrayList;

public class Candidates {
	public ArrayList<Cluster> candidates;
	public double minScore;
	public Candidates() {
		candidates = new ArrayList<Cluster>();
		minScore = Double.MAX_VALUE;
	}
	
	/**
	 * Remove candidates having a specified score
	 * @param score
	 */
	private void removeCandidates(double score) {
		for(Cluster c : candidates){
			if(c.getScore() == score){
				candidates.remove(c);
			}
		}
	}

	/**
	 * remove candidates having minScore //
	 * update minScore
	 */
	public void updateMinScore() {
		removeCandidates(minScore);
		double min = Double.MAX_VALUE;
		for(Cluster c: candidates){
			if(c.getScore()<min){
				min = c.getScore();
			}
		}
		this.minScore = min;
	}
}
