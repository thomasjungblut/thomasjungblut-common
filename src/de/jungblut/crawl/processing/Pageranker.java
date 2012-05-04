package de.jungblut.crawl.processing;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hama.examples.PageRank;
import org.apache.hama.graph.VertexArrayWritable;
import org.apache.hama.graph.VertexWritable;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Some 20% time project to rank my work's homepage via linkages and Hama's Pagerank.
 */
public final class Pageranker {

  static final VertexArrayWritable dangling = new VertexArrayWritable();

  static class WebGraphMapper
      extends
      Mapper<VertexWritable, VertexArrayWritable, VertexWritable, VertexArrayWritable> {

    static {
      dangling.set(new Writable[0]);
    }

    @Override
    protected void map(VertexWritable key, VertexArrayWritable value,
        Context context) throws IOException, InterruptedException {

      context.write(key, value);

      for (Writable k : value.get()) {
        context.write((VertexWritable) k, dangling);
      }
    }
  }

  private static class WebGraphReducer
      extends
      Reducer<VertexWritable, VertexArrayWritable, VertexWritable, VertexArrayWritable> {

    @Override
    protected void reduce(VertexWritable key,
        Iterable<VertexArrayWritable> values, Context context)
        throws IOException, InterruptedException {

      boolean isDangling = true;
      VertexArrayWritable realOutlinks = null;
      for (VertexArrayWritable outlinks : values) {
        if (outlinks.get().length > 0) {
          isDangling = false;
          realOutlinks = outlinks;
          break;
        }
      }

      if (isDangling) {
        context.write(key, dangling);
      } else {
        context.write(key, realOutlinks);
      }

    }

  }

  public static void main(String[] args) throws Exception {
    writeSequenceFile();
    prepareWebgraph();
    runPagerank();
    writeResult();
  }

  private static void writeResult() throws IOException {
    PriorityQueue<RankedTuple> queue = new PriorityQueue<>();
    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.get(conf);
    FileStatus[] stati = fs.listStatus(new Path(
        "/Users/thomas.jungblut/Downloads/crawledRanked/"));
    for (FileStatus status : stati) {
      if (!status.isDir() && !status.getPath().getName().endsWith(".crc")) {
        Path path = status.getPath();
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
        Text key = new Text();
        DoubleWritable value = new DoubleWritable();
        while (reader.next(key, value)) {
          queue.add(new RankedTuple(key.toString(), value.get()));
        }
        reader.close();
      }
    }

    CSVWriter writer = new CSVWriter(new FileWriter(
        "/Users/thomas.jungblut/Downloads/rankedSorted.csv"), '|');
    RankedTuple tuple = null;
    while ((tuple = queue.poll()) != null) {
      writer.writeNext(new String[] { tuple.key, tuple.rank + "" });
    }

    writer.close();
  }

  private static class RankedTuple implements Comparable<RankedTuple> {

    final String key;
    final double rank;

    public RankedTuple(String key, double rank) {
      super();
      this.key = key;
      this.rank = rank;
    }

    @Override
    public int compareTo(RankedTuple o) {
      return Double.compare(o.rank, rank);
    }

  }

  private static void runPagerank() throws IOException, InterruptedException,
      ClassNotFoundException, InstantiationException, IllegalAccessException {
    PageRank.main(new String[] {
        "/Users/thomas.jungblut/Downloads/crawledProcessed/",
        "/Users/thomas.jungblut/Downloads/crawledRanked" });
  }

  private static void prepareWebgraph() throws IOException,
      InterruptedException, ClassNotFoundException {
    Configuration conf = new Configuration();
    Job job = new Job(conf);
    job.setMapperClass(WebGraphMapper.class);
    job.setReducerClass(WebGraphReducer.class);
    job.setJarByClass(WebGraphMapper.class);
    job.setOutputKeyClass(VertexWritable.class);
    job.setOutputValueClass(VertexArrayWritable.class);
    job.setNumReduceTasks(1);
    job.setInputFormatClass(SequenceFileInputFormat.class);
    job.setOutputFormatClass(SequenceFileOutputFormat.class);
    SequenceFileInputFormat.addInputPath(job, new Path(
        "/Users/thomas.jungblut/Downloads/crawled/"));
    FileSystem fs = FileSystem.get(conf);
    Path out = new Path("/Users/thomas.jungblut/Downloads/crawledProcessed");
    fs.delete(out, true);
    TextOutputFormat.setOutputPath(job, out);

    job.waitForCompletion(true);
  }

  private static void writeSequenceFile() throws FileNotFoundException,
      IOException {
    CSVReader reader = new CSVReader(new FileReader(
        "/Users/thomas.jungblut/Downloads/crawled.csv"), ',', '"');
    Configuration conf = new Configuration();
    SequenceFile.Writer writer = new SequenceFile.Writer(FileSystem.get(conf),
        conf, new Path("/Users/thomas.jungblut/Downloads/crawled.seq"),
        VertexWritable.class, VertexArrayWritable.class);
    String base = "http://www.testberichte.de";
    String[] line = null;
    String lastIdentifier = null;
    List<VertexWritable> outlinkList = new ArrayList<VertexWritable>();
    while ((line = reader.readNext()) != null) {
      String identifier = base + line[1];
      if (lastIdentifier == null) {
        lastIdentifier = identifier;
      }
      if (!identifier.equals(lastIdentifier)) {
        VertexArrayWritable val = new VertexArrayWritable();
        val.set(outlinkList.toArray(new VertexWritable[outlinkList.size()]));
        writer.append(new VertexWritable(identifier), val);
        outlinkList.clear();
      }
      String outlink = line[2];
      outlinkList.add(new VertexWritable(outlink));
    }
    writer.close();
    reader.close();
  }

}
