package de.jungblut.bsp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hama.HamaConfiguration;
import org.apache.hama.bsp.BSP;
import org.apache.hama.bsp.BSPJob;
import org.apache.hama.bsp.BSPMessage;
import org.apache.hama.bsp.BSPPeer;
import org.apache.hama.bsp.SequenceFileInputFormat;
import org.apache.hama.bsp.SequenceFileOutputFormat;
import org.apache.hama.bsp.sync.SyncException;

public final class SamplingSort extends
    BSP<IntWritable, NullWritable, IntWritable, NullWritable> {

  private final NullWritable val = NullWritable.get();

  @Override
  public void bsp(
      BSPPeer<IntWritable, NullWritable, IntWritable, NullWritable> peer)
      throws IOException, SyncException, InterruptedException {
    int numPeers = peer.getNumPeers();
    int[] pivotArray = new int[numPeers];
    @SuppressWarnings("unchecked")
    LinkedList<IntWritable>[] partitions = new LinkedList[numPeers];

    if (isMaster(peer)) {
      // setup pivots by simply choosing the first n-items
      for (int i = 0; i < numPeers; i++) {
        IntWritable key = new IntWritable();
        peer.readNext(key, val);
        pivotArray[i] = key.get();
      }
      // sort the pivots ascending to compare them
      Arrays.sort(pivotArray);
      for (int i = 0; i < numPeers; i++) {
        for (String p : peer.getAllPeerNames()) {
          peer.send(p, new IntIntMessage(i, pivotArray[i]));
        }
      }
      peer.reopenInput();
    }
    peer.sync();

    IntIntMessage msg = null;
    while ((msg = (IntIntMessage) peer.getCurrentMessage()) != null) {
      // use direct field access to prevent autoboxing
      pivotArray[msg.tag] = msg.data;
    }

    // partition
    IntWritable key = new IntWritable();
    while (peer.readNext(key, val)) {
      IntWritable clone = new IntWritable(key.get());
      int value = clone.get();
      // loop through all the sorted pivots and assign the partition to the
      // first value that is larger
      for (int i = 0; i < numPeers; i++) {
        if (pivotArray[i] > value) {
          if (partitions[i] == null) {
            partitions[i] = new LinkedList<IntWritable>();
          }
          partitions[i].add(clone);
          break;
        }
      }
    }
    for (int i = 0; i < numPeers; i++) {
      if (partitions[i] != null) {
        peer.send(peer.getPeerName(i), new PartitionMessage(partitions[i]));
      }
    }

    peer.sync();

    // merge to a single array
    List<IntWritable[]> msgBuffer = new LinkedList<IntWritable[]>();
    int size = 0;
    PartitionMessage partMsg = null;
    while ((partMsg = (PartitionMessage) peer.getCurrentMessage()) != null) {
      msgBuffer.add(partMsg.outArray);
      size += partMsg.outArray.length;
    }
    System.out.println(peer.getPeerName() + " -> " + size);
    IntWritable[] input = new IntWritable[size];
    int index = 0;
    for (IntWritable[] partitionMessage : msgBuffer) {
      System.arraycopy(partitionMessage, 0, input, index,
          partitionMessage.length);
      index += partitionMessage.length;
    }

    // sort it in-place with java code
    Arrays.sort(input);

    for (IntWritable i : input) {
      peer.write(i, val);
    }

  }

  public boolean isMaster(
      BSPPeer<IntWritable, NullWritable, IntWritable, NullWritable> peer) {
    return peer.getPeerName().equals(peer.getPeerName(0));
  }

  final class PartitionMessage extends BSPMessage {

    int size;
    IntWritable[] outArray;

    public PartitionMessage() {
    }

    public PartitionMessage(LinkedList<IntWritable> in) {
      this.outArray = in.toArray(new IntWritable[in.size()]);
      this.size = in.size();
    }

    @Override
    public void write(DataOutput out) throws IOException {
      out.writeInt(size);
      for (IntWritable writable : outArray) {
        writable.write(out);
      }
    }

    @Override
    public void readFields(DataInput in) throws IOException {
      this.size = in.readInt();
      this.outArray = new IntWritable[size];
      for (int i = 0; i < size; i++) {
        IntWritable intWritable = new IntWritable();
        intWritable.readFields(in);
        outArray[i] = intWritable;
      }
    }

    @Override
    public IntWritable[] getData() {
      return null;
    }

    @Override
    public Integer getTag() {
      return null;
    }

  }

  public static void main(String[] args) throws IOException,
      ClassNotFoundException, InterruptedException {

    if (args.length != 3) {
      System.out
          .println("<input path> <output path> <how many records to generate and sort>");
    }
    Path input = new Path(args[0]);
    Path output = new Path(args[1]);
    int numRows = Integer.parseInt(args[2]);

    Configuration conf = new Configuration();
    generateInput(conf, input, numRows);

    BSPJob job = new BSPJob(new HamaConfiguration(conf));
    job.setBspClass(SamplingSort.class);
    job.setInputFormat(SequenceFileInputFormat.class);
    job.setOutputFormat(SequenceFileOutputFormat.class);
    job.setJarByClass(SamplingSort.class);
    job.setInputPath(input);
    job.setOutputPath(output);
    job.setOutputKeyClass(IntWritable.class);
    job.setOutputValueClass(NullWritable.class);
    job.setJobName("Sampling sort");
    job.waitForCompletion(true);
    // print output
    printOutput(conf, output);
  }

  public static void printOutput(Configuration conf, Path out)
      throws IOException {
    // the output is ascending of task name (string order) and within these
    // partitions sorted.
    FileSystem fs = FileSystem.get(conf);
    FileStatus[] listStatus = fs.listStatus(out);
    for (FileStatus f : listStatus) {
      if (!f.getPath().toString().endsWith(".crc")) {
        System.out.println("from " + f.getPath().toString());
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, f.getPath(),
            conf);
        IntWritable key = new IntWritable();
        while (reader.next(key)) {
          System.out.print(key.get() + " ");
        }
        System.out.println();
      }
    }

  }

  public static void generateInput(Configuration conf, Path out, int count)
      throws IOException {
    FileSystem fs = FileSystem.get(conf);
    fs.delete(out, false);
    Random rand = new Random();
    SequenceFile.Writer writer = null;
    try {
      writer = new SequenceFile.Writer(fs, conf, out, IntWritable.class,
          NullWritable.class);
      IntWritable key = new IntWritable();
      NullWritable val = NullWritable.get();
      for (int i = 0; i < count; i++) {
        key.set(rand.nextInt());
        writer.append(key, val);
      }
    } finally {
      if (writer != null)
        writer.close();
    }

  }

}
