package de.jungblut.bsp;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hama.HamaConfiguration;
import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPJob;
import org.apache.hama.bsp.BSPPeer;
import org.apache.hama.bsp.TextInputFormat;
import org.apache.hama.bsp.TextOutputFormat;
import org.apache.hama.bsp.sync.SyncException;
import org.apache.hama.util.KeyValuePair;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset.Entry;

/**
 * Basic wordcount hello world for BSP.
 * 
 * @author thomas.jungblut
 * 
 */
public final class WordCountBSP extends
    BSP<LongWritable, Text, Text, LongWritable, LongMessage> {

  private static final Log LOG = LogFactory.getLog(WordCountBSP.class);

  /**
   * The plan:<br/>
   * - every peer is reading lines from chunked input, word counting locally in
   * a map<br/>
   * - each token is send with a message to a master task that combines them
   * (The master task was a previous slave, and can still be a slave as well.
   * Scalability can be improved by electing multiple masters that group tokens
   * based on their hashcode e.g via: peer.getPeerName(token.hashCode() %
   * peer.getNumPeers()).
   */
  @Override
  public void bsp(
      BSPPeer<LongWritable, Text, Text, LongWritable, LongMessage> peer)
      throws IOException, SyncException, InterruptedException {

    HashMultiset<String> countingSet = HashMultiset.create();
    KeyValuePair<LongWritable, Text> pair = null;
    while ((pair = peer.readNext()) != null) {
      // if the token was already inserted, this call automatically increments a
      // counter
      countingSet.addAll(Arrays
          .asList(pair.getValue().toString().split("\\s+")));
    }

    // now send everything to task 0
    Set<Entry<String>> entrySet = countingSet.entrySet();
    for (Entry<String> entry : entrySet) {
      // send frequency along with the token as message
      peer.send(peer.getPeerName(0),
          new LongMessage(entry.getCount(), entry.getElement()));
    }

    // messages are exchanged now
    peer.sync();
    // clear the set to free memory, on the master task this is needed to sum
    // the right values
    countingSet.clear();

    // if I'm the peer at index 0 every messaged me do..
    if (peer.getPeerIndex() == 0) {
      // read the stuff and put it into the set again
      LongMessage msg = null;
      while ((msg = peer.getCurrentMessage()) != null) {
        countingSet.add(msg.getData(), (int) msg.getTag());
      }

      // last step, write it to the output collector
      entrySet = countingSet.entrySet();
      for (Entry<String> entry : entrySet) {
        peer.write(new Text(entry.getElement()),
            new LongWritable(entry.getCount()));
      }
    }

  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      LOG.fatal("Usage: <input paths> <output path>");
      System.exit(1);
    }
    Configuration conf = new Configuration();
    // limit the number of threads in local mode
    conf.set("bsp.local.tasks.maximum", "4");

    BSPJob job = new BSPJob(new HamaConfiguration(conf));

    job.setBspClass(WordCountBSP.class);

    job.setInputFormat(TextInputFormat.class);
    job.setOutputFormat(TextOutputFormat.class);

    job.setInputKeyClass(LongWritable.class);
    job.setInputValueClass(Text.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(LongWritable.class);

    job.setInputPath(new Path(args[0]));
    job.setOutputPath(new Path(args[1]));

    job.waitForCompletion(true);
  }

}
