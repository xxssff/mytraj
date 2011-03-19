package trie;

import java.util.ArrayList;
import org.joda.time.LocalTime;

/**
 * LeafEntry for trie
 * 
 * @author xiaohui
 * 
 */
public class LeafEntry {
	// ArrayList<Integer> subCluster;
	public LocalTime ts, te;

	public LeafEntry(LocalTime currTime) {
		this.ts = currTime;
	}

	public String toString() {
		return "[leafEntry: " + ts.toString() + "]";
	}

}
