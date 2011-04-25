package entity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

}
