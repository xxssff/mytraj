package trie;

import java.util.ArrayList;
import java.util.Arrays;

import org.joda.time.LocalTime;

import entity.Candidates;
import entity.Cluster;
import entity.MovingObject;
import entity.MyEvent;

/**
 * Trie class
 * 
 * @author xiaohui
 * 
 */
public class Trie {
	static int nid = 0;
	Node root;
	int numPaths;

	public Trie() {
		root = new Node(nid++, null); // root does not have pEdge
		numPaths = 0;
	}

	// /**
	// *
	// * @param cMembers for leaf entry
	// * @param strArr
	// * @param currTime
	// */
	// public void insert(ArrayList<Integer> cMembers, String[] strArr,
	// LocalTime currTime) {
	// Node pNode = root;
	// Node cNode = null;
	// for (int i = 0; i < strArr.length; i++) {
	// String ch = strArr[i];
	// Edge e = pNode.getEdge(ch);
	// if (e == null) {
	// cNode = new Node(nid++);
	// e = new Edge(pNode, cNode, ch);
	// pNode.addEdge(e);
	// pNode = cNode;
	// } else {
	// // go down one level
	// // one more char
	// pNode = e.toNode;
	// }
	// }
	// if (pNode.edges == null) {
	// // create new leaf node
	// pNode.isLeaf = true;
	// pNode.entry = new LeafEntry(cMembers, currTime);
	// }
	// this.numPaths++;
	// }

	/**
	 * 
	 * @param cMembers
	 *            for leaf entry
	 * @param strArr
	 * @param currTime
	 */
	public void insert(String[] strArr, LocalTime currTime) {
		Node pNode = root;
		Node cNode = null;
		for (int i = 0; i < strArr.length; i++) {
			String ch = strArr[i];
			Edge e = pNode.getEdge(ch);
			if (e == null) {
				e = new Edge(pNode, null, ch);
				cNode = new Node(nid++, e);
				e.setToNode(cNode);
				pNode.addEdge(e);
				pNode = cNode;
			} else {
				// go down one level
				// one more char
				pNode = e.toNode;
			}
		}
		// create new leaf node
		pNode.isLeaf = true;
		pNode.entry = new LeafEntry(strArr, currTime);
		this.numPaths++;
	}

	public void insert(String[][] strs, LocalTime currTime) {
		for (String[] str : strs) {
			insert(str, currTime);
		}
	}

	/**
	 * Performs full tree traversal using recursion.
	 */
	public String traverse(Node parentNode) {
		// traverse all nodes that belong to the parent
		String retStr = "";

		if (parentNode.entry != null) {
			retStr += parentNode.entry.toString();

		}
		if (parentNode.edges != null) {
			for (Edge edge : parentNode.edges) {
				if (parentNode.equals(root)) {
					retStr += "root ";
				}
				// print node information
				retStr += edge.label + " ";
				// traverse children
				retStr += traverse(edge.toNode) + " ";
			}
		}
		return retStr;
	}

	public LeafEntry getLeafEntry(String[] strArr) {
		Node pNode = root;
		for (int i = 0; i < strArr.length; i++) {
			String ch = strArr[i];
			Edge e = pNode.getEdge(ch);
			if (e == null) {
				return null;
			} else {
				pNode = e.toNode;
			}
		}
		return pNode.entry;
	}

	public int getNumPaths() {
		return this.numPaths;
	}

	/**
	 * @return string rep of trie
	 */
	public String toString() {
		return traverse(root);
	}

	/**
	 * 
	 * @param strArr
	 * @param s
	 * @param currTime
	 * @return leaf node entry
	 * @throws Exception
	 */
	public LeafEntry remove(String[] strArr, String s, LocalTime currTime)
			throws Exception {
		Node pNode = root;
		Node cNode = null;
		Edge cEdge = null;
		for (int i = 0; i < strArr.length; i++) {
			String ch = strArr[i];

			Edge e = pNode.getEdge(ch);

			if (e == null) {
				throw new StringNotExistException(strArr);
			} else {
				// go down one level
				// one more char
				pNode = e.toNode;
				if (ch.equals(s)) {
					cNode = pNode;
					cEdge = e;
				}
			}
		}
		pNode.entry.te = currTime;

		// remove sub-tree
		cNode.edges = null;
		// remove edge
		cEdge.fromNode.removeEdge(cEdge);

		return pNode.entry;
	}

	/**
	 * called by expire event <br>
	 * remove leaf entry without removing path
	 * 
	 * @param sArr
	 * @param currTime
	 * @throws Exception
	 */
	public LeafEntry remove(String[] strArr, LocalTime currTime)
			throws Exception {
		Node pNode = root;
		for (int i = 0; i < strArr.length; i++) {
			String ch = strArr[i];

			Edge e = pNode.getEdge(ch);

			if (e == null) {
				throw new StringNotExistException(strArr);
			} else {
				// go down one level
				// one more char
				pNode = e.toNode;
			}
		}
		pNode.entry.te = currTime;
		LeafEntry res = pNode.entry;
		pNode.entry = null;

		// house keeping
		// go reverse direction to remove nodes
		iterRemove(pNode);
		return res;
	}

	private void iterRemove(Node cNode) {
		
		while (!cNode.equals(root)) {
			Edge pEdge = cNode.pEdge;
			Node pNode = cNode.pEdge.fromNode;
			if ((cNode.edges == null || cNode.edges.size() == 0)
					&& (cNode.entry == null)) {
				//remove cNode
				pEdge.toNode=null;
				pNode.edges.remove(pEdge);
			}
			cNode = pNode;
		}
	}

}
