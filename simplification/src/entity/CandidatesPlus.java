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
		return candidates.size();
	}

	/**
	 * run through the list to remove dominated cands
	 */
	public void clean() {
		Collections.sort(candidates);
		LeafEntry[] temp = candidates.toArray(new LeafEntry[0]);
		for (int i = 0; i < temp.length; i++) {
			boolean domed = false;
			for (int j = i + 1; j < temp.length; j++) {
				if (LeafEntry.dominates(temp[j], temp[i])) {
					domed = true;
					break;
				}
			}
			if (domed) {
				temp[i] = null;
			}
		}
		List<LeafEntry> cands = new ArrayList<LeafEntry>(temp.length);
		for (LeafEntry le : temp) {
			if (le != null) {
				cands.add(le);
			}
		}
		this.candidates = cands;
	}

	/**
	 * sort the entries based on scores
	 */
	public void sortOnScore() {
		Collections.sort(candidates);
	}

	public void sort() {
		Collections.sort(candidates);
	}

	public List<LeafEntry> getTopK(int k) {
		if(this.candidates.size() <=k)
			return candidates;
		else
		{
			List<LeafEntry> entries = new ArrayList<LeafEntry>();
			int i=0;
			while(i<k){
				entries.add(this.candidates.get(i));
				i++;
			}
			return entries;
		}
	}

}
