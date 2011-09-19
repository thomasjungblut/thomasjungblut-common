package de.jungblut.forkjoin;

import java.util.concurrent.Callable;

import org.apache.hadoop.io.Writable;

public abstract class ForkJoinBSPTask<T> implements Callable<T>, Writable {

	// TODO context could be static
	ForkJoinBSP context;
	boolean finished;
	int id;
	ForkJoinBSPTask<?> parent;

	private T result;

	@SuppressWarnings("unchecked")
	public ForkJoinBSPTask<T> fork(ForkJoinBSPTask<T> parent) {
		this.parent = parent;
		if (this.context == null)
			this.context = parent.context;
		return (ForkJoinBSPTask<T>) context.submit(this, parent);
	}

	protected abstract T compute();

	@Override
	public T call() {
		return compute();
	}

	public T join() {
		for (;;) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (result != null)
				break;
		}
		return result;
	}

	void setResult(T res) {
		this.result = res;
		this.finished = true;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ForkJoinBSPTask<?> other = (ForkJoinBSPTask<?>) obj;
		if (id != other.id)
			return false;
		return true;
	}
}