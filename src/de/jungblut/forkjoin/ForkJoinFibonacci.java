package de.jungblut.forkjoin;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ForkJoinFibonacci extends ForkJoinBSPTask<Long> {

	private int n;

	@SuppressWarnings("unused")
	private ForkJoinFibonacci() {
	}

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

	@Override
	public void readFields(DataInput in) throws IOException {
		n = in.readInt();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.write(n);
	}

}
