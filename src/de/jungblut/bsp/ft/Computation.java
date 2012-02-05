package de.jungblut.bsp.ft;

import java.util.List;

import org.apache.hama.bsp.BSPMessage;

public class Computation {
    
    @Superstep(1)
    public List<BSPMessage> first(List<BSPMessage> inMessages, List<BSPMessage> outMessages){
	
	
	return outMessages;
    }
    
    @Superstep(2)
    public List<BSPMessage> second(List<BSPMessage> inMessages, List<BSPMessage> outMessages){
	
	
	return outMessages;
    }

}
