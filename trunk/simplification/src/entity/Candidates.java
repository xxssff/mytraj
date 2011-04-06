package entity;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import trie.LeafEntry;

public class Candidates {
	public LeafEntry[] candidates;
	// int minIdx;// pointer to the entry with minScore
	int numCans; // how many entries are not null

	public Candidates(int size) {
		candidates = new LeafEntry[size];
		// minIdx = -1;
		numCans = 0;
	}

	/**
	 * 
	 * @param le
	 */
	public void add(LeafEntry le) {
		if (numCans == 0) {
			// just put in
			candidates[0] = le;
			numCans++;
			return;
		}
		// case 1: le is dominated
		boolean dominated = false;
		for (int i = 0; i < candidates.length; i++) {
			LeafEntry l = candidates[i];
			if (l != null && LeafEntry.dominates(l, le)) {
				dominated = true;
			}
		}
		if (!dominated) {
			// case 2: le dominates someone else
			int insertPos = 0;
			boolean dominating = false;

			for (int i = 0; i < candidates.length; i++) {
				LeafEntry l = candidates[i];
				if (l != null && LeafEntry.dominates(le, l)) {
					candidates[i] = null;
					numCans--;
					dominating = true;
					insertPos = i;
				}
			}
			if (dominating) {
				// find the first last position to put le
				candidates[insertPos] = le;
				numCans++;
			} else {
				// if there are empty slot, insert le
				if (numCans < candidates.length) {
					for (int i = 0; i < candidates.length; i++) {
						if (candidates[i] == null) {
							candidates[i] = le;
							numCans++;
							return;
						}
					}
				} else {
					// replace one least score entry
					int minIdx = 0;
					for (int i = 0; i < candidates.length; i++) {
						if (candidates[i].getScore() < candidates[minIdx]
								.getScore()) {
							minIdx = i;
						}
					}
					candidates[minIdx] = le;
				}
			}
		}
	}

	public int getNumCans() {
		return numCans;
	}

	/**
	 * @return String rep
	 */
	public String toString() {
		if (numCans == 0) {
			return "No candidates";
		}
		return "candidate size: " + candidates.length;
	}

	public void toFile(BufferedWriter bw) throws Exception {
		Arrays.sort(candidates);
		for (LeafEntry le : candidates) {
			bw.append(le.toString());
			bw.newLine();
		}
	}

}
