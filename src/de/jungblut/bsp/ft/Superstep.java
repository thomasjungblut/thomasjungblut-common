package de.jungblut.bsp.ft;

import java.io.IOException;

import org.apache.hama.bsp.BSPPeer;

abstract class Superstep<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {

  void setup(BSPPeer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> peer) {
  }

  void cleanup(BSPPeer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> peer) {
  }

  protected abstract void compute(BSPPeer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> peer)
      throws IOException;

  boolean haltComputation(BSPPeer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> peer) {
    return false;
  }

}
