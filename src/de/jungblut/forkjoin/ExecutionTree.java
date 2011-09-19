package de.jungblut.forkjoin;

import java.util.HashMap;
import java.util.HashSet;

public class ExecutionTree {

	private ExecutionTreeNode root;
	private HashMap<Integer, ExecutionTreeNode> nodeLookupMap = new HashMap<Integer, ExecutionTreeNode>();
	private HashSet<ExecutionTreeNode> leafSet = new HashSet<ExecutionTreeNode>();

	public void add(ForkJoinBSPTask<?> task, ForkJoinBSPTask<?> parent) {
		// initial node
		if (parent == null) {
			root = new ExecutionTreeNode(task, null);
			nodeLookupMap.put(task.id, root);
			leafSet.add(root);
		} else {
			ExecutionTreeNode p = nodeLookupMap.get(parent.id);
			ExecutionTreeNode node = new ExecutionTreeNode(task, p);
			p.add(node);
			leafSet.add(node);
		}
	}

	public HashSet<ExecutionTreeNode> getLeafsToExecute() {
		return leafSet;
	}

	public void finish(int id) {
		ExecutionTreeNode node = nodeLookupMap.get(id);
		node.finishNode();
		if (node.parent.allChildrenFinished())
			leafSet.add(node.parent);
		leafSet.remove(node);
	}

	class ExecutionTreeNode {
		private ExecutionTreeNode parent;
		private ForkJoinBSPTask<?> value;
		private int finishedChildren = 0;
		private HashMap<Integer, ExecutionTreeNode> children = new HashMap<Integer, ExecutionTreeNode>();

		public ExecutionTreeNode(ExecutionTreeNode parent) {
			super();
			this.parent = parent;
		}

		public ExecutionTreeNode(ForkJoinBSPTask<?> task,
				ExecutionTreeNode parent) {
			this(parent);
			this.value = task;
		}

		public void add(ExecutionTreeNode e) {
			leafSet.remove(e);
			children.put(e.value.id, e);
		}

		public ExecutionTreeNode get(int id) {
			return children.get(id);
		}

		public boolean allChildrenFinished() {
			return finishedChildren == children.size() ? true : false;
		}

		public void finishNode() {
			this.parent.finishedChildren++;
		}

		public ExecutionTreeNode getParent() {
			return parent;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExecutionTreeNode other = (ExecutionTreeNode) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ExecutionTreeNode [value=" + value.id + "]";
		}

	}

}
