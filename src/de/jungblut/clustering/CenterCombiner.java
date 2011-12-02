package de.jungblut.clustering;

import org.apache.hama.bsp.BSPMessage;
import org.apache.hama.bsp.BSPMessageBundle;
import org.apache.hama.bsp.Combiner;

public class CenterCombiner extends Combiner{

    @Override
    public BSPMessageBundle combine(Iterable<BSPMessage> messages) {
	
	// TODO needed?
	return null;
    }

}
