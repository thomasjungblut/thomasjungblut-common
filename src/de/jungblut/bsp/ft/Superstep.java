package de.jungblut.bsp.ft;

import org.apache.hama.bsp.BSPPeer;

public abstract class Superstep<KEYIN, VALUEIN, MESSAGEIN, MESSAGEOUT,KEYOUT, VALUEOUT> {
  
  protected abstract void compute(BSPPeer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> peer);

}
