package de.jungblut.bsp.ft;

import org.apache.hama.bsp.BSPPeer;

import java.io.IOException;

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
