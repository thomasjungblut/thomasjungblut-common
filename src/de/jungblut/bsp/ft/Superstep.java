package de.jungblut.bsp.ft;

import java.io.IOException;

import org.apache.hama.bsp.BSPPeer;

public abstract class Superstep<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {

  protected void setup(BSPPeer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> peer) {
  }

  protected void cleanup(BSPPeer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> peer) {
  }

  protected abstract void compute(BSPPeer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> peer)
      throws IOException;

  protected boolean haltComputation(
      BSPPeer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> peer) {
    return false;
  }

}
