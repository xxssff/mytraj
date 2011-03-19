package trie;

import java.util.ArrayList;

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

	public void insert(String s, LocalTime currTime) {
		Node pNode = root;
		Node cNode = null;
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
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
			pNode.entry = new LeafEntry(currTime);
		}
	}

	public void insert(String[] strs, LocalTime currTime) {
		for(String str : strs){
			insert(str, currTime);
		}
	}

	/**
	 * Performs full tree traversal using recursion.
	 */
	public String traverse(Node parentNode) {
		// traverse all nodes that belong to the parent
		String retStr = "";
		if (parentNode.edges == null) {
			return retStr;
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

	public LeafEntry getLeafEntry(String str) throws StringNotExistException {
		Node pNode = root;
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			Edge e = pNode.getEdge(ch);
			if (e == null) {
				throw new StringNotExistException(str);
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

	public static void main(String[] args) throws StringNotExistException {
		String s = "new";
		String s1 = "ntu";
		LocalTime currt = LocalTime.MIDNIGHT;

		// test insert string
		Trie trie = new Trie();
		trie.insert(s, currt);
		trie.insert(s1, currt);
		System.out.println(trie.toString());
		System.out.println(trie.getLeafEntry("abc"));
	}

	public static void update(MyEvent evt, Candidates r) {
		// TODO Auto-generated method stub
		
	}

	public void handleObjInsert(Cluster c, MovingObject tempMo) {
		// TODO Auto-generated method stub
		
	}

	public void handleMerge(Cluster c1, Cluster c) {
		// TODO Auto-generated method stub
		
	}
}
