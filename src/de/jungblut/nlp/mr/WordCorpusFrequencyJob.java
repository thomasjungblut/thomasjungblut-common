package de.jungblut.nlp.mr;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
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

  public static final String DICT_OUT_PATH_KEY = "dict.out.path";
  public static final String MIN_WORD_COUNT_KEY = "min.word.count";
  public static final String TOKENIZER_CLASS_KEY = "tokenizer.class";

  private static final Log LOG = LogFactory
      .getLog(WordCorpusFrequencyJob.class);

  public static enum WordCorpusCounter {
    TOKEN_ADDED, TOKEN_DISCARDED
  }

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
      if (split.length == 2) {
        Text documentId = new Text(split[0]);
        String[] tokens = tokenizer.tokenize(split[1]);
        // this set stores the term frequency
        HashMultiset<String> set = HashMultiset.create(Arrays.asList(tokens));

        for (String entry : set.elementSet()) {
          context.write(new Text(entry), new TextIntPairWritable(documentId,
              new IntWritable(set.count(entry))));
        }
      } else {
        LOG.warn("Ignore line (couldn't be split correctly): " + value);
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

    // write the dictionary out as well
    private BufferedWriter dictWriter;
    // ID assigned to the token
    private int currentIndex = 0;
    private int minWordCount = 0;

    @Override
    protected void setup(Context context) throws IOException,
        InterruptedException {
      FileSystem fs = org.apache.hadoop.fs.FileSystem.get(context
          .getConfiguration());
      this.dictWriter = new BufferedWriter(
          new OutputStreamWriter(fs.create(new Path(context.getConfiguration()
              .get(DICT_OUT_PATH_KEY)))));
      this.minWordCount = context.getConfiguration().getInt(MIN_WORD_COUNT_KEY,
          minWordCount);
    }

    @Override
    protected void reduce(Text key, Iterable<TextIntPairWritable> values,
        Context context) throws IOException, InterruptedException {

      Map<Text, IntWritable> documents = new HashMap<>();
      int wordCount = 0;
      for (TextIntPairWritable docId : values) {
        documents.put(new Text(docId.getFirst()), new IntWritable(docId
            .getSecond().get()));
        wordCount += docId.getSecond().get();
      }
      if (wordCount > minWordCount) {
        dictWriter.write(currentIndex + "\t" + key.toString() + "\n");
        for (Entry<Text, IntWritable> entry : documents.entrySet()) {
          context.write(entry.getKey(), new TextIntIntIntWritable(key,
              new IntWritable(documents.size()), entry.getValue(),
              new IntWritable(currentIndex)));
        }
        currentIndex++;
        context.getCounter(WordCorpusCounter.TOKEN_ADDED).increment(1);
      } else {
        context.getCounter(WordCorpusCounter.TOKEN_DISCARDED).increment(1);
      }
    }

    @Override
    protected void cleanup(Context context) throws IOException,
        InterruptedException {
      dictWriter.close();
    }

  }

  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      System.out
          .println("Usage: <Comma separated input paths> <Dictionary output path> <Output path>");
      System.exit(1);
    }
    Configuration conf = new Configuration();
    Job job = createJob(args[0], args[1], args[2], conf);

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
   * Gets the counter of the reduce output values. This determines the
   * dictionary sizes.
   * 
   * @param finishedJob the job that has successfully finished.
   * @return the number of map input records / number of documents.
   */
  public static long getNumberOfTokens(Job finishedJob) throws IOException {
    return finishedJob
        .getCounters()
        .findCounter(
            "de.jungblut.nlp.mr.WordCorpusFrequencyJob$WordCorpusCounter",
            "TOKEN_ADDED").getValue();
  }

  /**
   * Creates a token frequency job.
   * 
   * @param in the input path, may comma separate multiple paths.
   * @param dictOut the output path of the dictionary.
   * @param out the output directory.
   * @param conf the configuration.
   * @return a job with the configured propertys like name, key/value classes
   *         and input format as text.
   */
  public static Job createJob(String in, String dictOut, String out,
      Configuration conf) throws IOException {
    conf.set(DICT_OUT_PATH_KEY, dictOut);
    Job job = new Job(conf, "Token Document Frequency Calculator");

    job.setInputFormatClass(TextInputFormat.class);
    job.setOutputFormatClass(SequenceFileOutputFormat.class);

    FileInputFormat.setInputPaths(job, in);
    FileOutputFormat.setOutputPath(job, new Path(out));

    job.setMapperClass(TokenMapper.class);
    job.setReducerClass(DocumentSumReducer.class);

    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(TextIntPairWritable.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(TextIntIntIntWritable.class);

    job.setNumReduceTasks(1);
    return job;
  }

}
