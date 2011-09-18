package de.jungblut.forkjoin;

public abstract class ForkJoinBSPTask<T> {

	ForkJoinBSP context;
	boolean finished;
	int id;
	ForkJoinBSPTask<?> parent;

	private T result;

	@SuppressWarnings("unchecked")
	public ForkJoinBSPTask<T> fork(ForkJoinBSPTask<T> parent) {
		return (ForkJoinBSPTask<T>) context.submit(this, parent);
	}

	protected abstract T compute();

	public T join() {
		// TODO don't busy wait, actually this won't work
		while (!finished)
			;
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