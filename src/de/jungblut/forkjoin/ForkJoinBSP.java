package de.jungblut.forkjoin;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hama.HamaConfiguration;
import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPJob;
import org.apache.hama.bsp.BSPPeerProtocol;
import org.apache.zookeeper.KeeperException;

public class ForkJoinBSP extends BSP {

	private Configuration conf;
	private int currentId = 0;
	private boolean master = false;
	private HashMap<ForkJoinBSPTask<?>, ForkJoinBSPTask<?>> taskMap;
	private HashMap<String, ForkJoinBSPTask<?>> groomToTaskMap;
	ExecutionTree executionTree = new ExecutionTree();
	ExecutorService pool = Executors.newCachedThreadPool();

	@Override
	public void bsp(BSPPeerProtocol peer) throws IOException, KeeperException,
			InterruptedException {
		master = peer.getPeerName().equals(conf.get("fork.join.master.task"));
		if (isMaster()) {
			taskMap = new HashMap<ForkJoinBSPTask<?>, ForkJoinBSPTask<?>>();
			groomToTaskMap = new HashMap<String, ForkJoinBSPTask<?>>();

			try {
				ForkJoinFibonacci fib = new ForkJoinFibonacci(5);
				fib.context = this;
				executionTree.add(fib, null);
				System.out.println(pool.submit(fib).get());
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

		}

		// TODO
		// create the first instance of the forkjoin task
		// while we have tasks to manage: DO
		// -schedule available tasks to grooms
		// -receive the new tasks that got forked and queue them

	}

	public ForkJoinBSPTask<?> submit(ForkJoinBSPTask<?> task,
			ForkJoinBSPTask<?> parent) {
		task.parent = parent;
		task.id = currentId++;
		this.schedule(task, parent);
		return task;
	}

	private void schedule(ForkJoinBSPTask<?> task, ForkJoinBSPTask<?> parent) {
		executionTree.add(task, parent);
		pool.submit(task);
	}

	@Override
	public Configuration getConf() {
		return conf;
	}

	@Override
	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public boolean isMaster() {
		return master;
	}

	public static void main(String[] args) throws IOException,
			ClassNotFoundException, InterruptedException {
		HamaConfiguration conf = new HamaConfiguration();
		conf.set("bsp.local.tasks.maximum", "1");
		conf.set("fork.join.master.task", "localrunner 0");
		BSPJob job = new BSPJob(conf);
		job.setBspClass(ForkJoinBSP.class);
		job.waitForCompletion(true);
	}
}
