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
public class Node {
	int id;
	Edge pEdge; //parent edge
	ArrayList<Edge> edges;
	boolean isLeaf;
	public LeafEntry entry;
	
	
	public Node(int id, Edge pEdge) {
		this.pEdge = pEdge;
		edges = null;
		this.id = id;
	}

	public void insertChild(String s) {

	}

	public Edge getEdge(String ch) {
		if (edges == null) {
			return null;
		}
		for (Edge e : edges) {
			if (e.label.equals(ch)) {
				return e;
			}
		}
		return null;
	}

	public void addEdge(Edge e) {
		if (edges == null) {
			edges = new ArrayList<Edge>();
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
	
	public void removeEdge(Edge e){
		edges.remove(e);
	}
}
