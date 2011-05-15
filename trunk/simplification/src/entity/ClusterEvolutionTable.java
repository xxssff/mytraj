package entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import trie.LeafEntry;

/**
 * A cluster evolution table records how a cluster evolves. It maps an CID to a
 * list of leafEntries
 * 
 * @author xiaohui
 * 
 */
public class ClusterEvolutionTable {
	HashMap<Integer, List<LeafEntry>> table;
	int minPts;

	public ClusterEvolutionTable(int minPts) {
		table = new HashMap<Integer, List<LeafEntry>>();
		this.minPts = minPts;
	}

	public void add(Integer key, LeafEntry anEntry) {
		List<LeafEntry> list = table.get(key);
		if (list == null) {
			list = new ArrayList<LeafEntry>(100);
			list.add(anEntry);
			table.put(key, list);
		} else {
			list.add(anEntry);
		}
	}

	/**
	 * This method computes the real duration for each entry by traversing the
	 * list in reverse order.
	 */
	public void cascadeAction() {
		for (Integer key : table.keySet()) {
			List<LeafEntry> entries = table.get(key);
			LeafEntry[] enArr = entries.toArray(new LeafEntry[0]);
			for (int i = enArr.length - 1; i > 0; i--) {
				cascade(i, enArr, entries);
			}
		}
	}

	private void cascade(int index, LeafEntry[] enArr, List<LeafEntry> entries) {
		LeafEntry le = entries.get(index);
		Set<Integer> memSet = convertArr2Set(le.subCluster);
		int size = memSet.size();

		for (int i = index - 1; i >= 0; i--) {
			Set<Integer> prevSet = convertArr2Set(enArr[i].subCluster);
			memSet.retainAll(prevSet);
			if (memSet.size() == size) {
				// memSetPt = memSet;
				le.ts = enArr[i].ts;
				le.updateScore();
			} else if (memSet.size() >= minPts) {
				// case 1: intersection is prevSet
				if (memSet.equals(prevSet)) {
					// update prevSet
					enArr[i].te = le.te;
					enArr[i].updateScore();
				} else {
					// new entry in the table
					LeafEntry le_new = new LeafEntry(
							memSet.toArray(new Integer[0]), enArr[i].ts,
							enArr[i].getDistStart());
					le_new.endCluster(le.te, le.getDistEnd(), le.getAlpha(),
							le.getBeta(), le.getGamma());
					entries.add(le_new);
				}
			} else {
				break;
			}
		}
	}

//	/**
//	 * implementation exactly as in the paper: RevHist
//	 * 
//	 * @param entries
//	 * @param enArr
//	 */
//	private void revHist(LeafEntry[] enArr, List<LeafEntry> entries) {
//		int i = entries.size() - 1;
//		while (i > 0) {
//			LeafEntry curr = entries.get(i);
//			LeafEntry prev = entries.get(i - 1);
//			Set<Integer> currMemSet = convertArr2Set(curr.subCluster);
//			int size = currMemSet.size();
//			Set<Integer> prevMemSet = convertArr2Set(prev.subCluster);
//			currMemSet.retainAll(prevMemSet);
//			if (currMemSet.size() == size) {
//				// case 1: intersection is currSet
//				curr.ts = prev.ts;
//				curr.updateScore();
//			} else if (currMemSet.equals(prevMemSet)) {
//				prev.te = curr.te;
//				prev.updateScore();
//			} else {
//				LeafEntry le_new = new LeafEntry(
//						currMemSet.toArray(new Integer[0]), prev.ts,
//						enArr[i].getDistStart());
//				le_new.endCluster(curr.te, curr.getDistEnd(), curr.getAlpha(),
//						curr.getBeta(), curr.getGamma());
//				// insert at the right pos
//				insertEntry(le_new, entries);
//				int j = i - 2;
//				while (j >= 0 && currMemSet.size() >= minPts) {
//					LeafEntry pEntry = entries.get(j);
//					Set<Integer> temp = convertArr2Set(pEntry.subCluster);
//					int size1 = currMemSet.size();
//					currMemSet.retainAll(temp);
//					if (currMemSet.size() < minPts) {
//						break;
//					} else if (currMemSet.size() == size1) {
//						le_new.ts = pEntry.ts;
//						le_new.updateScore();
//					} else {
//						LeafEntry le_new1 = new LeafEntry(
//								currMemSet.toArray(new Integer[0]), pEntry.ts,
//								pEntry.getDistStart());
//						le_new1.endCluster(curr.te, curr.getDistEnd(),
//								curr.getAlpha(), curr.getBeta(),
//								curr.getGamma());
//						// insert at the right pos
//						insertEntry(le_new, entries);
//					}
//					j--;
//				}
//			}
//			i--;
//		}
//	}

	/**
	 * 
	 * @param entry
	 * @param entries
	 *            insert at pos s.t. list is sorted on both ts and te
	 */
	private void insertEntry(LeafEntry le_new, List<LeafEntry> entries) {
		int index = 0;
		Iterator<LeafEntry> ite = entries.iterator();
		while (ite.hasNext()) {
			LeafEntry le = ite.next();
			if (le_new.ts.isBefore(le.ts) || (le_new.ts.equals(le.ts))
					&& le_new.te.isBefore(le.te)) {
				break;
			}
			index++;
		}
		entries.add(index, le_new);
	}

	/**
	 * 
	 * @param intArr
	 * @return set
	 */
	private Set<Integer> convertArr2Set(Integer[] intArr) {
		Set<Integer> memSet = new HashSet<Integer>(intArr.length);
		// insert le members into memset
		for (Integer m : intArr) {
			memSet.add(m);
		}
		return memSet;
	}

	public List<LeafEntry> get(int cid) {
		return table.get(cid);
	}

	public String toString() {
		String s = "";
		for (Integer key : table.keySet()) {
			s += key + "=>" + table.get(key) + "\n";
		}
		return s;
	}

	// public void toFile(BufferedWriter bw) throws IOException {
	// bw.write(this.toString());
	// bw.newLine();
	// }

	public void pushIntoCands(CandidatesPlus cands, int tau) {
		for (Integer key : table.keySet()) {
			List<LeafEntry> list = table.get(key);
			for (LeafEntry le : list) {
				if (le.getDuration() >= tau) {
					cands.add(le);
				}
			}

		}
	}

	public int getTotalSize() {
		int sum = 0;
		for (Integer key : table.keySet()) {
			sum += table.get(key).size();
		}
		return sum;
	}

}
