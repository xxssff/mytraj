package trie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Node for Trie
 * 
 * @author xiaohui
 * 
 */
public class NumNode {
	int id;
	NumEdge pEdge; // parent edge
	public ArrayList<NumEdge> edges;
	boolean isLeaf;
	public LeafEntry entry;

	public NumNode(int id, NumEdge pEdge) {
		this.pEdge = pEdge;
		edges = null;
		this.id = id;
	}

	public void insertChild(String s) {

	}

	public NumEdge getNumEdge(Integer i) {
		if (edges == null) {
			return null;
		}
		for (NumEdge e : edges) {
			if (e.label == i) {
				return e;
			}
		}
		return null;
	}

	public void addNumEdge(NumEdge e) {
		if (edges == null) {
			edges = new ArrayList<NumEdge>();
		}
		edges.add(e);

	}

	public String toString() {
		if (!isLeaf) {
			return id + "";
		} else {
			return id + " leaf";
		}
	}

	public void removeNumEdge(NumEdge e) {
		edges.remove(e);
	}
}
