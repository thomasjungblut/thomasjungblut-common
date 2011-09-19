package de.jungblut.forkjoin;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ExecutionTree implements Runnable {

	enum State {
		PENDING, FINISHED
	}

	private ExecutionTreeNode root;
	private HashMap<Integer, ExecutionTreeNode> nodeLookupMap = new HashMap<Integer, ExecutionTreeNode>();
	private Set<ExecutionTreeNode> leafSet = Collections
			.synchronizedSet(new HashSet<ExecutionTreeNode>());
	private ConcurrentHashMap<Integer, Future<?>> tasksToComplete = new ConcurrentHashMap<Integer, Future<?>>();

	@Override
	public void run() {
		List<Integer> toRemove = new LinkedList<Integer>();
		while (root.state != State.FINISHED) {
			for (Entry<Integer, Future<?>> f : tasksToComplete.entrySet()) {
				try {
					Object result = f.getValue().get(10,
							TimeUnit.MICROSECONDS);
					finish(f.getKey(), result);
					toRemove.add(f.getKey());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					// this is going to happen periodically
				}
			}

			for(int id : toRemove)
				tasksToComplete.remove(id);
					
			toRemove.clear();
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized Future<?> add(ForkJoinBSPTask<?> task,
			ForkJoinBSPTask<?> parent, Future<?> future) {
		// initial node
		if (parent == null) {
			root = new ExecutionTreeNode(task, null);
			nodeLookupMap.put(task.id, root);
			leafSet.add(root);
		} else {
			ExecutionTreeNode p = nodeLookupMap.get(parent.id);
			ExecutionTreeNode node = new ExecutionTreeNode(task, p);
			nodeLookupMap.put(task.id, node);
			p.add(node);
			leafSet.add(node);
		}
		tasksToComplete.put(task.id, future);
		return future;
	}

	public Set<ExecutionTreeNode> getLeafsToExecute() {
		return leafSet;
	}

	public void finish(int id, Object result) {
		ExecutionTreeNode node = nodeLookupMap.get(id);
		node.value.setResult(result);
		node.finishNode();
		while (!node.parent.allChildrenFinished()) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		synchronized (node.value) {
			node.value.notify();
		}
		leafSet.remove(node);
	}

	class ExecutionTreeNode {
		private State state = State.PENDING;
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
			if (this.parent != null)
				this.parent.finishedChildren++;
			state = State.FINISHED;
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
