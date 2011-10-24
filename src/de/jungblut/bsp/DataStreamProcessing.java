package de.jungblut.bsp;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.hama.HamaConfiguration;
import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPJob;
import org.apache.hama.bsp.BSPJobClient;
import org.apache.hama.bsp.BSPPeer;
import org.apache.hama.bsp.ClusterStatus;
import org.apache.zookeeper.KeeperException;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class DataStreamProcessing extends BSP {

  private Twitter twitter;
  private String userName;
  private HashSet<Status> alreadyProcessedStatusses = new HashSet<Status>();
  private boolean isMaster;
  private int numPeers;
  private String[] otherPeers;

  @Override
  public void setup(BSPPeer peer) throws IOException, KeeperException,
      InterruptedException {
    // gets the default twitter
    twitter = new TwitterFactory().getInstance();
    userName = peer.getConfiguration().get("twitter.user.name");
    isMaster = peer.getConfiguration().get("master.task")
        .equals(peer.getPeerName());
    if (isMaster) {
      // get the other peer names in an array
      String[] allPeerNames = peer.getAllPeerNames();
      numPeers = allPeerNames.length - 1;
      HashSet<String> peers = new HashSet<String>();
      peers.addAll(Arrays.asList(allPeerNames));
      peers.remove(peer.getPeerName());
      otherPeers = peers.toArray(new String[numPeers]);
    }
  }

  @Override
  public void bsp(BSPPeer bspPeer) throws IOException, KeeperException,
      InterruptedException {

    if (isMaster) {
      while (true) {
        try {
          int numMessagesSend = 0;
          // this should get us the least 20 tweets of this user
          List<Status> statuses = twitter.getUserTimeline(userName);
          for (Status s : statuses) {
            if (alreadyProcessedStatusses.add(s)) {
              System.out.println("Got new status from: "
                  + s.getUser().getName() + " with message " + s.getText());
              // we distribute messages to the other peers for
              // processing via user id partitioning
              // so a task gets all messages for a user
              bspPeer.send(
                  otherPeers[(int) (s.getUser().getId() % otherPeers.length)],
                  new LongMessage(s.getUser().getId(), s.getText()));
              numMessagesSend++;
            }
          }
          // if we send no messages, wait for a few seconds and go
          // further without syncing
          if (numMessagesSend == 0) {
            Thread.sleep(5000l);
          } else {
            // sync before we get new statusses again.
            bspPeer.sync();
          }
        } catch (TwitterException e) {
          e.printStackTrace();
        }
      }
    } else {
      while (true) {
        // wait for some work...
        bspPeer.sync();
        LongMessage message = null;
        while ((message = (LongMessage) bspPeer.getCurrentMessage()) != null) {
          System.out.println("Got work in form of text: " + message.getData()
              + " for the userid: " + message.getTag().longValue());
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {

    // BSP job configuration
    HamaConfiguration conf = new HamaConfiguration();

    conf.set("twitter.user.name", "tjungblut");
    // I'm always testing in localmode so I use 2 tasks.
    conf.set("bsp.local.tasks.maximum", "2");

    BSPJob bsp = new BSPJob(conf);
    // Set the job name
    bsp.setJobName("Twitter stream processing");
    bsp.setBspClass(DataStreamProcessing.class);

    BSPJobClient jobClient = new BSPJobClient(conf);
    ClusterStatus cluster = jobClient.getClusterStatus(true);

    // Choose one as a master
    for (String hostName : cluster.getActiveGroomNames().keySet()) {
      conf.set("master.task", hostName);
      break;
    }

    bsp.waitForCompletion(true);

  }

}
