package de.jungblut.nlp.mr;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import com.google.common.collect.HashMultiset;

import de.jungblut.nlp.StandardTokenizer;
import de.jungblut.nlp.Tokenizer;

/**
 * MapReduce job that calculates the word frequency over all documents by
 * inverting document->words and writing the sum of the assigned documents per
 * word and its document. Assuming that the document is small enough to fit in
 * memory and a single line (this is not a good solution if you want to
 * vectorize huge text books).
 * 
 * @author thomas.jungblut
 * 
 */
public class WordCorpusFrequencyJob {

  public static final String TOKENIZER_CLASS_KEY = "tokenizer.class";

  /**
   * Write a token with its document id.
   */
  public static class TokenMapper extends
      Mapper<LongWritable, Text, Text, TextIntPairWritable> {

    private Tokenizer tokenizer;

    @Override
    protected void setup(Context context) throws IOException,
        InterruptedException {
      tokenizer = getTokenizer(context.getConfiguration());
    }

    @Override
    protected void map(LongWritable key, Text value, Context context)
        throws IOException, InterruptedException {
      // assuming that the document ID is tab separated with the document
      String[] split = value.toString().split("\t");
      Text documentId = new Text(split[0]);
      String[] tokens = tokenizer.tokenize(split[1]);
      // this set stores the term frequency
      HashMultiset<String> set = HashMultiset.create(Arrays.asList(tokens));

      for (String entry : set.elementSet()) {
        context.write(new Text(entry), new TextIntPairWritable(documentId,
            new IntWritable(set.count(entry))));
      }

    }
  }

  /**
   * Sums up all the documents per token index by docID. <br/>
   * Output is docID as key, value is the token and its document frequency and
   * its term frequency in the doc as well as its index in the dictionary (this
   * must run as single reducer).
   */
  public static class DocumentSumReducer extends
      Reducer<Text, TextIntPairWritable, Text, TextIntIntIntWritable> {

    // ID assigned to the token
    int currentIndex = 0;

    @Override
    protected void reduce(Text key, Iterable<TextIntPairWritable> values,
        Context context) throws IOException, InterruptedException {

      Map<Text, IntWritable> documents = new HashMap<>();
      for (TextIntPairWritable docId : values) {
        documents.put(new Text(docId.getFirst()), new IntWritable(docId
            .getSecond().get()));
      }
      for (Entry<Text, IntWritable> entry : documents.entrySet()) {
        context.write(entry.getKey(), new TextIntIntIntWritable(key,
            new IntWritable(documents.size()), entry.getValue(),
            new IntWritable(currentIndex)));
      }
      currentIndex++;
    }

  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.out.println("Usage: <Comma separated input paths> <Output path>");
      System.exit(1);
    }
    Configuration conf = new Configuration();
    Job job = createJob(args[0], args[1], conf);

    job.waitForCompletion(true);

  }

  /**
   * Gets a tokenizer, based on the configured class in "tokenizer.class".
   */
  public static Tokenizer getTokenizer(Configuration conf) {
    try {
      return conf.getClass(TOKENIZER_CLASS_KEY, StandardTokenizer.class,
          Tokenizer.class).newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets the counter of the input lines read, in this case it should be the
   * number of documents.
   * 
   * @param finishedJob the job that has successfully finished.
   * @return the number of map input records / number of documents.
   */
  public static long getNumberOfDocuments(Job finishedJob) throws IOException {
    return finishedJob
        .getCounters()
        .findCounter("org.apache.hadoop.mapred.Task$Counter",
            "MAP_INPUT_RECORDS").getValue();
  }

  /**
   * Gets the counter of the reduce input groups, in this case it should be the
   * number of tokens.
   * 
   * @param finishedJob the job that has successfully finished.
   * @return the number of map input records / number of documents.
   */
  public static long getNumberOfTokens(Job finishedJob) throws IOException {
    return finishedJob
        .getCounters()
        .findCounter("org.apache.hadoop.mapred.Task$Counter",
            "REDUCE_INPUT_GROUPS").getValue();
  }

  /**
   * Creates a token frequency job.
   * 
   * @param in the input path, may comma separate multiple paths.
   * @param out the output directory.
   * @param conf the configuration.
   * @return a job with the configured propertys like name, key/value classes
   *         and input format as text.
   */
  public static Job createJob(String in, String out, Configuration conf)
      throws IOException {
    Job job = new Job(conf, "Token Document Frequency Calculator");

    job.setInputFormatClass(TextInputFormat.class);
    job.setOutputFormatClass(SequenceFileOutputFormat.class);

    FileInputFormat.setInputPaths(job, in);
    FileOutputFormat.setOutputPath(job, new Path(out));

    job.setMapperClass(TokenMapper.class);
    job.setReducerClass(DocumentSumReducer.class);
    job.setCombinerClass(DocumentSumReducer.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(TextIntIntIntWritable.class);

    job.setNumReduceTasks(1);
    return job;
  }

}
