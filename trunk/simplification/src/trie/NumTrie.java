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
public class NumTrie {
	static int nid = 0;
	NumNode root;
	int numPaths;

	public NumTrie() {
		root = new NumNode(nid++, null); // root does not have pNumEdge
		numPaths = 0;
	}

	public NumNode getRoot(){
		return root;
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
	// NumEdge e = pNode.getNumEdge(ch);
	// if (e == null) {
	// cNode = new Node(nid++);
	// e = new NumEdge(pNode, cNode, ch);
	// pNode.addNumEdge(e);
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
	 * @param intArr
	 * @param currTime
	 */
	public void insert(Integer[] intArr, LocalTime currTime) {
		NumNode pNode = root;
		NumNode cNode = null;
		for (Integer ch : intArr) {
			NumEdge e = pNode.getNumEdge(ch);
			if (e == null) {
				e = new NumEdge(pNode, null, ch);
				cNode = new NumNode(nid++, e);
				e.setToNode(cNode);
				pNode.addNumEdge(e);
				pNode = cNode;
			} else {
				// go down one level
				// one more char
				pNode = e.toNode;
			}
		}
		// create new leaf node
		pNode.isLeaf = true;
		pNode.entry = new LeafEntry(intArr, currTime);
		this.numPaths++;
	}

//	public void insert(String[][] strs, LocalTime currTime) {
//		for (String[] str : strs) {
//			insert(str, currTime);
//		}
//	}

	/**
	 * Performs full tree traversal using recursion.
	 */
	public String traverse(NumNode parentNode) {
		// traverse all nodes that belong to the parent
		String retStr = "";

		if (parentNode.entry != null) {
			retStr += parentNode.entry.toString();

		}
		if (parentNode.edges != null) {
			for (NumEdge edge : parentNode.edges) {
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

	public LeafEntry getLeafEntry(Integer[] intArr) {
		NumNode pNode = root;
		for (Integer ch : intArr) {
			NumEdge e = pNode.getNumEdge(ch);
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
	public LeafEntry remove(Integer[] strArr, String s, LocalTime currTime)
			throws Exception {
		NumNode pNode = root;
		NumNode cNode = null;
		NumEdge cNumEdge = null;
		for (Integer ch : strArr) {
			NumEdge e = pNode.getNumEdge(ch);

			if (e == null) {
				throw new StringNotExistException(strArr);
			} else {
				// go down one level
				// one more char
				pNode = e.toNode;
				if (ch.equals(s)) {
					cNode = pNode;
					cNumEdge = e;
				}
			}
		}
		pNode.entry.te = currTime;

		// remove sub-tree
		cNode.edges = null;
		// remove edge
		cNumEdge.fromNode.removeNumEdge(cNumEdge);

		return pNode.entry;
	}

	/**
	 * called by expire event <br>
	 * remove leaf entry without removing path
	 * 
	 * @param intArr
	 * @param currTime
	 * @throws Exception
	 */
	public LeafEntry remove(Integer[] intArr, LocalTime currTime)
			throws Exception {
		NumNode pNode = root;
		for (Integer ch : intArr) {

			NumEdge e = pNode.getNumEdge(ch);

			if (e == null) {
				throw new StringNotExistException(intArr);
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

	private void iterRemove(NumNode cNode) {
		
		while (!cNode.equals(root)) {
			NumEdge pEdge = cNode.pEdge;
			NumNode pNode = cNode.pEdge.fromNode;
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
