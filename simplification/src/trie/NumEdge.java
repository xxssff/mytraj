package trie;

/**
 * Number Edge class
 * 
 * @author xiaohui
 * 
 */
public class NumEdge {
	Integer label;
	// int fromNodeId, toNodeId;
	public NumNode fromNode, toNode;

	// public Edge(int fromNodeId, int toNodeId, char label) {
	// this.label = label;
	// this.fromNodeId = fromNodeId;
	// this.toNodeId = toNodeId;
	// }

	public NumEdge(NumNode fromNode, NumNode toNode, Integer label) {
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.label = label;
	}

	public boolean equals(NumEdge aEdge) {
		return this.label == aEdge.label;
	}

	public String toString() {
		return "[" + fromNode.id + "--" + toNode.id + " " + label + "]";
	}

	public void setToNode(NumNode node) {
		this.toNode = node;
	}
}
