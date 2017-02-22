package naming;

import java.util.List;

/**
 * @author feichao (feqian @ ucsd.edu)
 */
public class Node {
	/**
	 * The parent node of this node
	 */
	private Node parent;
	
	/**
	 * The children nodes of this node;
	 */
	private List<Node> children;
	
	/**
	 * An file indicator 
	 */
	private boolean isFile;
	
	/**
	 * The name of this node;
	 */
	private String name;
		
	public Node(Node p, boolean fileTag, String name) {
		this.setParent(p);
		this.setIsFile(fileTag);
		this.setName(name);
		this.children = null;
	}

	/**
	 * Given the name of the child node, return the child node
	 * @return the child node
	 */
	public Node getChild(String name) {
		for (Node f : children) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		return null;
	}
	
	/**
	 * Add a new child to this node
	 * @param name
	 */
	public void addChild(Node p) {
		this.children.add(p);
	}
	
	/**
	 * Return the children of this node
	 * @return
	 */
	public Node[] getChildren() {
		Node[] res = new Node[this.children.size()];
		return this.children.toArray(res);
	}
	
	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public boolean getIsFile() {
		return isFile;
	}

	public void setIsFile(boolean isFile) {
		this.isFile = isFile;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
