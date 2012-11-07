package de.jungblut.nlp.mr;

import java.io.IOException;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import com.google.common.collect.HashMultiset;

import de.jungblut.writable.VectorWritable;

/**
 * Job that will calculate tf-idf based on the output of the
 * {@link WordCorpusFrequencyJob}.
 * 
 * @author thomas.jungblut
 * 
 */
public final class TfIdfCalculatorJob {

  public static final String NUMBER_OF_DOCUMENTS_KEY = "documents.num";

  /**
   * Calculate the sparse vector with TF-IDF.
   */
  public static class DocumentReducer extends
      Reducer<Text, TextIntPairWritable, Text, VectorWritable> {
    
    @Override
    protected void setup(Context context)
        throws IOException, InterruptedException {
      
    }

    @Override
    protected void reduce(Text key, Iterable<TextIntPairWritable> values,
        Context context) throws IOException, InterruptedException {
      
      HashMultiset<String> tokenCountSet = HashMultiset.create();
      HashMap<String,Integer> tokenDocumentCount = new HashMap<>();
      
      for(TextIntPairWritable pair : values){
        String token = pair.getFirst().toString();
        tokenCountSet.add(token);
//        tokenDocumentCount.put(token, );
      }
      

    }

  }

  /**
   * Creates a tf-idf job.
   * 
   * @param in the input path, the output of the {@link WordCorpusFrequencyJob}.
   * @param out the output directory.
   * @param conf the configuration.
   * @param numberOfDocuments the number of documents in the corpus per token.
   *          (map input counter value of {@link WordCorpusFrequencyJob}.)
   * @return a job with the configured propertys like name, key/value classes
   *         and input format as text.
   */
  public static Job createJob(String in, String out, Configuration conf,
      int numberOfDocuments) throws IOException {

    conf.setInt(NUMBER_OF_DOCUMENTS_KEY, numberOfDocuments);

    Job job = new Job(conf, "TF-IDF Calculator");

    job.setInputFormatClass(SequenceFileInputFormat.class);
    job.setOutputFormatClass(SequenceFileOutputFormat.class);

    FileInputFormat.setInputPaths(job, in);
    FileOutputFormat.setOutputPath(job, new Path(out));

    job.setMapperClass(Mapper.class);
    job.setReducerClass(DocumentReducer.class);

    job.setOutputKeyClass(TextTextPairWritable.class);
    job.setOutputValueClass(IntWritable.class);

    job.setNumReduceTasks(1);
    return job;
  }

}
