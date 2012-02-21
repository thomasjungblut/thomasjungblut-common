package de.jungblut.bsp.realtime;

import java.io.IOException;

import org.apache.hama.bsp.BSPJob;
import org.apache.hama.bsp.InputFormat;
import org.apache.hama.bsp.InputSplit;
import org.apache.hama.bsp.RecordReader;

import twitter4j.Status;

/**
 * Twitter real time input format.
 * 
 * @author thomas.jungblut
 * 
 */
public class RealTimeInputFormat implements InputFormat<String, Status> {

  @Override
  public RecordReader<String, Status> getRecordReader(InputSplit split,
      BSPJob job) throws IOException {
    return null;
  }

  @Override
  public InputSplit[] getSplits(BSPJob job, int numSplits) throws IOException {
    return null;
  }

}
