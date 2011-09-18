package de.jungblut.forkjoin;

import java.io.IOException;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPPeerProtocol;
import org.apache.zookeeper.KeeperException;

public class ForkJoinBSP extends BSP {

	private Configuration conf;
	private int currentId = 0;
	private boolean master = false;
	private HashMap<ForkJoinBSPTask<?>, ForkJoinBSPTask<?>> taskMap;
	private HashMap<String, ForkJoinBSPTask<?>> groomToTaskMap;

	@Override
	public void bsp(BSPPeerProtocol peer) throws IOException, KeeperException,
			InterruptedException {
		master = peer.getPeerName().equals(conf.get("fork.join.master.task"));
		if (isMaster()) {
			taskMap = new HashMap<ForkJoinBSPTask<?>, ForkJoinBSPTask<?>>();
			groomToTaskMap = new HashMap<String, ForkJoinBSPTask<?>>();
		}

		// create the first instance of the forkjoin task
		// while we have tasks to manage: DO
		// -schedule available tasks to grooms
		// -receive the new tasks that got forked and queue them

	}

	public ForkJoinBSPTask<?> submit(ForkJoinBSPTask<?> task,
			ForkJoinBSPTask<?> parent) {
		task.parent = parent;
		task.context = this;
		task.id = currentId++;
		this.schedule(task);
		return task;
	}

	private void schedule(ForkJoinBSPTask<?> task) {

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
}
