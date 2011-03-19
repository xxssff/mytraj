package trie;

/**
 * Edge class
 * 
 * @author xiaohui
 * 
 */
public class Edge {
	char label;
	// int fromNodeId, toNodeId;
	Node fromNode, toNode;

	// public Edge(int fromNodeId, int toNodeId, char label) {
	// this.label = label;
	// this.fromNodeId = fromNodeId;
	// this.toNodeId = toNodeId;
	// }

	public Edge(Node fromNode, Node toNode, char label) {
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.label = label;
	}

	public boolean equals(Edge aEdge) {
		return this.label == aEdge.label;
	}

	public String toString() {
		return "[" + fromNode.id + "--" + toNode.id + " " + label + "]";
	}
}
