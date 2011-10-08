package entity;

/**
 * @author xiaohui
 * 2011-07-15 add minScore field <br>
 * 2011-10-07 add similarity comparison <br>
 * 
 */
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import trie.LeafEntry;

public class CandidatesPlus {
	public List<LeafEntry> topK; // size = k
	List<LeafEntry> candidateList;
	int numEntries;
	int k;
	// minScore should be increasing
	public double minScore = Double.MIN_VALUE;
	Statistics stats;

	// int minIdx;// pointer to the entry with minScore
	public CandidatesPlus(int k, Statistics stats) {
		topK = new ArrayList<LeafEntry>(k);
		this.numEntries = 0;
		this.k = k;
		candidateList = new ArrayList<LeafEntry>();
		this.stats = stats;
	}

	/**
	 * 
	 * @param bw
	 * @throws Exception
	 */
	public void toFile(BufferedWriter bw) throws Exception {
		for (LeafEntry le : candidateList) {
			if (le != null) {
				bw.append(le.toString());
				bw.newLine();
			}
		}
	}

	/**
	 * If two leaf entries are similar, only keep the one with bigger score.
	 * 
	 * @param le
	 * @param theta
	 */
	public void add(LeafEntry le, double theta) {
		boolean added = false;

		// check if le is dominating or dominated by any leafEntry in R
		List<LeafEntry> dominatedItems = new ArrayList<LeafEntry>();
		for (LeafEntry l : candidateList) {
			if (l != null && LeafEntry.dominates(l, le)) {
				if (stats != null)
					stats.numPrunedByDomination++;
				return;
			} else if (LeafEntry.dominates(le, l)) {
				dominatedItems.add(le);
			}
		}

		// remove dominated items
		if (dominatedItems.size() > 0) {
			stats.numPrunedByDomination += dominatedItems.size();
			for (LeafEntry domLe : dominatedItems)
				candidateList.remove(domLe);
		}
		dominatedItems.clear();

		// create similarItems
		List<LeafEntry> similarItems = new ArrayList<LeafEntry>();
		for (LeafEntry l : candidateList) {
			if (l.isSimilar(le, theta)) {
				similarItems.add(l);
			}
		}
		// look for smallest value in similarItems, if any
		if (similarItems.size() > 0) {
			LeafEntry smallestValue = getSmallestScoreItem(similarItems);
			if (smallestValue.getScore() < le.getScore()) {
				candidateList.remove(smallestValue);
				candidateList.add(le);
				numEntries++;
				added = true;
			}
			stats.numPrunedCandBySim++;

		} else if (similarItems.size() == 0) {
			// add le
			candidateList.add(le);
			numEntries++;
			added = true;
		}
		similarItems.clear();

		// invariant: candidateList has no domination and no similarity when
		// reach here

		if (added && (le.getScore() > minScore)) {
			if (candidateList.size() <= k) {
				topK = candidateList;
				minScore = 0; // to make checking always true
			} else {
				Collections.sort(candidateList, Collections.reverseOrder());
				topK = candidateList.subList(0, k);
				minScore = topK.get(k - 1).getScore();
			}
		}
	}

	/**
	 * @param similarItems
	 * @return item with smallest score
	 */
	private LeafEntry getSmallestScoreItem(List<LeafEntry> leafEntryList) {
		LeafEntry small = leafEntryList.get(0);
		for (LeafEntry le : leafEntryList) {
			if (le.getScore() < small.getScore()) {
				small = le;
			}
		}
		return small;
	}

	//
	// /**
	// * rm dominated and similar items from candidate list
	// */
	// private void cleanList(double theta) {
	// List<LeafEntry> tempList = new ArrayList<LeafEntry>(candidateList
	// .size());
	// for (LeafEntry l : candidateList) {
	// if (!l.dominated) {
	// tempList.add(l);
	// }
	// }
	// // sort the tempList in reverse order
	// Collections.sort(tempList, Collections.reverseOrder());
	// // empty candidateList
	// candidateList.clear();
	// // push items back
	// for (LeafEntry l : tempList) {
	// if (hasSimilarItem(l, candidateList, theta)) {
	// if (stats != null)
	// stats.numPrunedCandBySim++;
	// } else {
	// candidateList.add(l);
	// }
	// }
	// }

	// /**
	// * precond: the candidate list is already sorted. <br>
	// * the item to be added is similar => not added into the list<br>
	// *
	// * @param le
	// * @param candidateList2
	// * @param theta
	// * @return true if there is a similar item
	// */
	// private boolean hasSimilarItem(LeafEntry le, List<LeafEntry> candList,
	// double theta) {
	// boolean similar = false;
	// for (LeafEntry l : candList) {
	// if (l.isSimilar(le, theta)) {
	// similar = true;
	// break;
	// }
	// }
	// return similar;
	// }

	/**
	 * @return String rep
	 */
	public String toString() {
		String temp = "";
		for (LeafEntry le : candidateList) {
			temp += le.toString() + "\n";
		}
		return temp;
	}

	public int size() {
		return candidateList.size();
	}

	public List<LeafEntry> getCandidateList() {
		return candidateList;
	}

	public List<LeafEntry> getTopK() {
		return topK;
	}
}
