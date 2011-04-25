package entity;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import trie.LeafEntry;

public class CandidatesPlus {
	public List<LeafEntry> candidates;

	// int minIdx;// pointer to the entry with minScore
	public CandidatesPlus() {
		candidates = new ArrayList<LeafEntry>(516);
	}

	public void add(LeafEntry le) {
		boolean dominated = false;
		// check if le is dominated by any leafEntry
		for (LeafEntry l : candidates) {
			if (LeafEntry.dominates(l, le)) {
				dominated = true;
			}
		}

		// check if le dominates someone else
		if (!dominated) {
			List<LeafEntry> cands = new ArrayList<LeafEntry>(
					candidates.size() + 1);
			for (LeafEntry l : candidates) {
				if (!LeafEntry.dominates(le, l)) {
					cands.add(l);
				}
			}
			cands.add(le);
			this.candidates.clear();
			this.candidates = cands;
		}
	}

	public void toFile(BufferedWriter bw) throws Exception {
		Collections.sort(candidates);
		for (LeafEntry le : candidates) {
			bw.append(le.toString());
			bw.newLine();
		}
	}

	/**
	 * @return String rep
	 */
	public String toString() {
		return candidates.toString();
	}

	public int size() {
		return candidates.size();
	}

}
