package de.jungblut.crawl.processing;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.PriorityQueue;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class WebGraphProcessingJob {

  static class WebGraphMapper extends Mapper<Text, Text, Text, Text> {

    static final Text dangling = new Text("DANGLING");

    @Override
    protected void map(Text key, Text value, Context context)
        throws IOException, InterruptedException {

      URL url;
      try {
        url = new URL(key.toString());
      } catch (MalformedURLException e) {
        System.out.println(e.getMessage());
        return;
      }
      String keyDomain = extractDomain(url);

      String[] splittedValues = value.toString().split(";");

      Text k = new Text(keyDomain);
      for (String split : splittedValues) {
        URL v;
        try {
          v = new URL(split);
        } catch (MalformedURLException e) {
          System.out.println(e.getMessage());
          return;
        }
        Text val = new Text(extractDomain(v));
        context.write(k, val);
        // write the val as key to ensure the integrity
        context.write(val, dangling);
      }
    }

    public static String extractDomain(URL url) {
      String host = url.getHost();
      // check for subdomains and strip them out
      // String[] split = host.split("\\.");
      String domain;
      // if (split.length >= 2) {
      // domain = split[split.length - 2] + "."
      // + split[split.length - 1];
      // } else {
      domain = host;
      // }
      return domain;
    }

  }

  private static class WebGraphReducer extends Reducer<Text, Text, Text, Text> {

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context)
        throws IOException, InterruptedException {
      HashSet<String> urlSet = new HashSet<>();

      for (Text t : values) {
        if (!t.equals(WebGraphMapper.dangling))
          urlSet.add(t.toString());
      }

      String kString = key.toString();

      StringBuilder sb = new StringBuilder();
      for (String s : urlSet) {
        if (!s.equals(kString)) {
          sb.append(s);
          sb.append("\t");
        }
      }

      if (urlSet.isEmpty()) {
        context.write(null, key);
      } else {
        context.write(null, new Text(key.toString() + "\t" + sb.toString()));
      }
    }

  }


  

  private static void runPagerank() throws IOException, InterruptedException,
      ClassNotFoundException {
    Configuration conf = new Configuration();
    Job job = new Job(conf);
    job.setMapperClass(WebGraphMapper.class);
    job.setReducerClass(WebGraphReducer.class);
    job.setJarByClass(WebGraphMapper.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    job.setInputFormatClass(SequenceFileInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);
    SequenceFileInputFormat.addInputPath(job, new Path("files/crawl/"));
    FileSystem fs = FileSystem.get(conf);
    Path out = new Path("files/crawl/processed/");
    fs.delete(out, true);
    TextOutputFormat.setOutputPath(job, out);

    job.waitForCompletion(true);
  }

}
