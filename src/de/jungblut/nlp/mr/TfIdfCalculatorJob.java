package de.jungblut.nlp.mr;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import de.jungblut.math.sparse.SparseDoubleVector;
import de.jungblut.writable.VectorWritable;

/**
 * Job that will calculate tf-idf based on the output of the
 * {@link WordCorpusFrequencyJob}.
 * 
 * @author thomas.jungblut
 * 
 */
public class TfIdfCalculatorJob {

  public static final String NUMBER_OF_DOCUMENTS_KEY = "documents.num";
  public static final String NUMBER_OF_TOKENS_KEY = "tokens.num";
  public static final String SPAM_DOCUMENT_PERCENTAGE_KEY = "spam.percentage";

  /**
   * Calculate the sparse vector with TF-IDF.
   */
  public static class DocumentVectorizerReducer extends
      Reducer<Text, TextIntIntIntWritable, Text, VectorWritable> {

    private long numDocs;
    private long documentThreshold;
    private int numTokens;

    @Override
    protected void setup(Context context) throws IOException,
        InterruptedException {
      numDocs = context.getConfiguration().getLong(NUMBER_OF_DOCUMENTS_KEY, 1);
      numTokens = context.getConfiguration().getInt(NUMBER_OF_TOKENS_KEY, 1);
      documentThreshold = (long) (numDocs * context.getConfiguration()
          .getFloat(SPAM_DOCUMENT_PERCENTAGE_KEY, 0.8f));
    }

    /**
     * Input is the document ID with several (token, document frequency, term
     * frequency, token index) pairs.
     */
    @Override
    protected void reduce(Text key, Iterable<TextIntIntIntWritable> values,
        Context context) throws IOException, InterruptedException {

      SparseDoubleVector vector = new SparseDoubleVector(numTokens);
      for (TextIntIntIntWritable pair : values) {
        if (documentThreshold > pair.getSecond().get()) {
          double tfIdf = pair.getThird().get()
              * Math.log(numDocs / (double) pair.getSecond().get());
          vector.set(pair.getFourth().get(), tfIdf);
        }
      }

      context.write(key, new VectorWritable(vector));

    }

  }

  /**
   * Calculates TF-IDF vectors from text input in the following format:<br/>
   * 
   * <pre>
   * documentid \t corpus
   * </pre>
   * 
   * <br/>
   * <br/>
   * 
   * It will run two jobs, a first job determines the document frequency of a
   * token, as well as its index in the resulting vector. The output is a
   * {@link SequenceFile} with {@link Text} as key and {@link VectorWritable} as
   * value.
   * 
   */
  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      System.out
          .println("Usage: <Comma separated input paths> <immediate output path> <Output path>");
      System.exit(1);
    }
    Configuration conf = new Configuration();
    Job job = WordCorpusFrequencyJob.createJob(args[0], args[1], conf);
    job.waitForCompletion(true);
    long numDocs = WordCorpusFrequencyJob.getNumberOfDocuments(job);
    long numTokens = WordCorpusFrequencyJob.getNumberOfTokens(job);
    conf = new Configuration();
    Job createJob = createJob(args[1], args[2], conf, numDocs, numTokens);
    createJob.waitForCompletion(true);
  }

  /**
   * Creates a tf-idf job.
   * 
   * @param in the input path, the output of the {@link WordCorpusFrequencyJob}.
   * @param out the output directory.
   * @param conf the configuration.
   * @param numberOfDocuments the number of documents in the corpus per token.
   *          (map input counter value of {@link WordCorpusFrequencyJob}.)
   * @param numberOfTokens the number of tokens in the corpus. (reduce input
   *          group counter value of {@link WordCorpusFrequencyJob}.)
   * @return a job with the configured propertys like name, key/value classes
   *         and input format as text.
   */
  public static Job createJob(String in, String out, Configuration conf,
      long numberOfDocuments, long numberOfTokens) throws IOException {

    conf.setLong(NUMBER_OF_DOCUMENTS_KEY, numberOfDocuments);
    conf.setLong(NUMBER_OF_TOKENS_KEY, numberOfTokens);

    Job job = new Job(conf, "TF-IDF Calculator");

    job.setInputFormatClass(SequenceFileInputFormat.class);
    job.setOutputFormatClass(SequenceFileOutputFormat.class);

    FileInputFormat.setInputPaths(job, in);
    FileOutputFormat.setOutputPath(job, new Path(out));

    job.setMapperClass(Mapper.class);
    job.setReducerClass(DocumentVectorizerReducer.class);

    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(TextIntIntIntWritable.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(VectorWritable.class);

    job.setNumReduceTasks(1);
    return job;
  }

}
