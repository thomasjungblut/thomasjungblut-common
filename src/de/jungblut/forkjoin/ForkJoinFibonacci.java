package de.jungblut.forkjoin;

public class ForkJoinFibonacci extends ForkJoinBSPTask<Long> {

	private final int n;

	public ForkJoinFibonacci(int n) {
		super();
		this.n = n;
	}

	@Override
	protected Long compute() {
		switch (n) {
		case 0:
			return 0L;
		case 1:
			return 1L;
		default:
			return new ForkJoinFibonacci(n - 1).fork(this).join()
					+ new ForkJoinFibonacci(n - 2).fork(this).join();
		}
	}

}
