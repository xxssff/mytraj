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

	public Trie() {
		root = new Node(nid++);
	}

	/**
	 * 
	 * @param cMembers for leaf entry
	 * @param strArr
	 * @param currTime
	 */
	public void insert(ArrayList<Integer> cMembers, String[] strArr, LocalTime currTime) {
		Node pNode = root;
		Node cNode = null;
		for (int i = 0; i < strArr.length; i++) {
			String ch = strArr[i];
			Edge e = pNode.getEdge(ch);
			if (e == null) {
				cNode = new Node(nid++);
				e = new Edge(pNode, cNode, ch);
				pNode.addEdge(e);
				pNode = cNode;
			} else {
				// go down one level
				// one more char
				pNode = e.toNode;
			}
		}
		if (pNode.edges == null) {
			// create new leaf node
			pNode.isLeaf = true;
			pNode.entry = new LeafEntry(cMembers, currTime);
		}
	}

	public void insert(ArrayList<Integer> cMembers, String[][] strs, LocalTime currTime) {
		for (String[] str : strs) {
			insert(cMembers, str, currTime);
		}
	}

	/**
	 * Performs full tree traversal using recursion.
	 */
	public String traverse(Node parentNode) {
		// traverse all nodes that belong to the parent
		String retStr = "";
		if (parentNode.edges == null) {
			return retStr + parentNode.entry.toString();
		} else {
			for (Edge edge : parentNode.edges) {
				// print node information
				retStr += edge.label + " ";
				// traverse children
				retStr += traverse(edge.toNode);
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

	/**
	 * @return string rep of trie
	 */
	public String toString() {
		return traverse(root);
	}

	public static void main(String[] args) throws Exception {
		String[] s = { "y", "e", "a", "r" };
		String[] s1 = { "y", "e", "s" };
		LocalTime currt = LocalTime.MIDNIGHT;

		ArrayList<Integer> m1 = new ArrayList<Integer>();
		m1.add(10);
		m1.add(20);
		m1.add(30);
		// test insert string
		Trie trie = new Trie();
		trie.insert(m1, s, currt);
		trie.insert(m1, s1, currt);
		System.out.println(trie.toString());

		trie.remove(s, "a", currt);
		System.out.println(trie.toString());
		// System.out.println(trie.getLeafEntry("abc"));
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
	 * @param sArr
	 * @param currTime
	 * @throws Exception 
	 */
	public LeafEntry remove(String[] strArr, LocalTime currTime) throws Exception {
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
		
		return res;
	}

}
