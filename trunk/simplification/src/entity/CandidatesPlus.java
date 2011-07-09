package entity;

/**
 * @author xiaohui
 * add minScore field
 */
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import trie.LeafEntry;

public class CandidatesPlus {

	public LeafEntry[] candidates; // size = k
	int numEntries;
	int k;
	// minScore should be increasing
	public double minScore = Double.MIN_VALUE;

	// int minIdx;// pointer to the entry with minScore
	public CandidatesPlus(int k) {
		candidates = new LeafEntry[k];
		this.numEntries = 0;
		this.k = k;
	}

	public void add(LeafEntry le) {
		boolean dominated = false;

		// check if le is dominated by any leafEntry
		for (LeafEntry l : candidates) {
			if (l != null && LeafEntry.dominates(l, le)) {
				dominated = true;
			}
		}

		// check if le dominates someone else
		if (!dominated) {
			for (int i = 0; i < candidates.length; i++) {
				if (candidates[i] != null
						&& LeafEntry.dominates(le, candidates[i])) {
					candidates[i] = null;
					numEntries--;
				}
			}
			// add le
			if (numEntries < k) {
				for (int i = 0; i < candidates.length; i++) {
					if (candidates[i] == null) {
						candidates[i] = le;
						numEntries++;
						break;
					}
				}
			} else {
				// the array is full
				// take the least score one
				for (int i = 0; i < candidates.length; i++) {
					if (candidates[i].getScore() == minScore) {
						candidates[i] = le;
						break;
					}
				}
			}

			// update the min score
			minScore = Double.MAX_VALUE;
			for (int i = 0; i < candidates.length; i++) {
				if (candidates[i] != null
						&& candidates[i].getScore() < minScore) {
					minScore = candidates[i].getScore();
				}
			}
		}
	}

	public void toFile(BufferedWriter bw) throws Exception {
		for (LeafEntry le : candidates) {
			if (le != null) {
				bw.append(le.toString());
				bw.newLine();
			}
		}
	}

	/**
	 * @return String rep
	 */
	public String toString() {
		return candidates.toString();
	}

	public int size() {
		return candidates.length;
	}

	public List<LeafEntry> getList() {
		List list = new ArrayList(Arrays.asList(candidates));
		return list;
	}

	public List<LeafEntry> getTopK(int k) {
		List list = new ArrayList(Arrays.asList(candidates));
		return list;
	}
}
