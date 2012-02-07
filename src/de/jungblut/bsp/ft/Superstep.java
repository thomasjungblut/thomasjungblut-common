package de.jungblut.bsp.ft;

import org.apache.hama.bsp.BSPPeer;

public abstract class Superstep<KEYIN, VALUEIN, MESSAGEIN, MESSAGEOUT, KEYOUT, VALUEOUT> {

	protected void setup(BSPPeer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> peer) {
	}

	protected void cleanup(BSPPeer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> peer) {
	}

	protected abstract void compute(BSPPeer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> peer);

	protected boolean haltComputation(BSPPeer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> peer) {
		return false;
	}

}
