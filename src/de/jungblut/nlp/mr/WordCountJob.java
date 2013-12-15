package de.jungblut.nlp.mr;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset.Entry;

import de.jungblut.nlp.Tokenizer;

/**
 * MapReduce job that calculates the token frequency by an improved word count.
 * 
 * @author thomas.jungblut
 * 
 */
public class WordCountJob {

  private static final Log LOG = LogFactory.getLog(WordCountJob.class);

  public static final String MIN_WORD_COUNT_KEY = "min.word.count";

  /**
   * Group the tokens in memory for each chunk, write it in the cleanup step.
   */
  public static class WordFrequencyMapper extends
      Mapper<LongWritable, Text, Text, LongWritable> {

    enum TokenCounter {
      NUM_TOKENS, COUNT_SUM
    }

    private final HashMultiset<String> wordCountSet = HashMultiset.create();
    private Tokenizer tokenizer;
    private int minWordCount = 0;

    @Override
    protected void setup(Context context) throws IOException,
        InterruptedException {

      final Configuration conf = context.getConfiguration();
      tokenizer = WordCorpusFrequencyJob.getTokenizer(conf);
      minWordCount = conf.getInt(MIN_WORD_COUNT_KEY, minWordCount);
    }

    @Override
    protected void map(LongWritable key, Text value, Context context)
        throws IOException, InterruptedException {

      String[] tokens = tokenizer.tokenize(value.toString());
      for (String token : tokens) {
        wordCountSet.add(token);
      }

    }

    @Override
    protected void cleanup(Context context) throws IOException,
        InterruptedException {
      // Guavas multiset counts the inserts, not the distinct keys.
      context.getCounter(TokenCounter.COUNT_SUM).increment(wordCountSet.size());
      Text key = new Text();
      LongWritable value = new LongWritable();
      for (Entry<String> entry : wordCountSet.entrySet()) {
        if (entry.getCount() > minWordCount) {
          key.set(entry.getElement());
          value.set(entry.getCount());
          context.getCounter(TokenCounter.NUM_TOKENS).increment(1);
          context.progress();
          context.write(key, value);
        }
      }
    }

  }

  /**
   * Group the tokens by reducing the mappers output and summing the sums for
   * each token.
   */
  public static class WordFrequencyReducer extends
      Reducer<Text, LongWritable, Text, LongWritable> {

    private final LongWritable sumValue = new LongWritable();

    @Override
    protected void reduce(Text key, Iterable<LongWritable> values,
        Context context) throws IOException, InterruptedException {

      long sum = 0l;
      for (LongWritable value : values) {
        sum += value.get();
      }
      sumValue.set(sum);
      context.write(key, sumValue);

    }

  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      LOG.fatal("Usage: <Comma separated input paths> <Output path>");
      System.exit(1);
    }
    Configuration conf = new Configuration();
    Job job = createJob(args[0], args[1], conf);

    job.waitForCompletion(true);

  }

  /**
   * Creates a token frequency job.
   * 
   * @param in the input path, may comma separate multiple paths.
   * @param out the output directory.
   * @param conf the configuration.
   * @return a job with the configured propertys like name, key/value classes
   *         and in/output format as text.
   */
  public static Job createJob(String in, String out, Configuration conf)
      throws IOException {
    Job job = Job.getInstance(conf, "Token Frequency Calculator");

    job.setInputFormatClass(TextInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);

    FileInputFormat.setInputPaths(job, in);
    FileOutputFormat.setOutputPath(job, new Path(out));

    job.setMapperClass(WordFrequencyMapper.class);
    job.setReducerClass(WordFrequencyReducer.class);
    job.setCombinerClass(WordFrequencyReducer.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(LongWritable.class);

    job.setNumReduceTasks(1);
    return job;
  }
}
