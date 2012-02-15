package de.jungblut.bsp;

import org.apache.hama.bsp.BSPMessage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

class LongMessage extends BSPMessage {

    private long tag;
    private String data;

    public LongMessage() {
    }


    public LongMessage(long tag, String data) {
        super();
        this.tag = tag;
        this.data = data;
    }


    @Override
    public void readFields(DataInput in) throws IOException {
        tag = in.readLong();
        data = in.readUTF();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeLong(tag);
        out.writeUTF(data);
    }

    @Override
    public Long getTag() {
        return tag;
    }

    @Override
    public String getData() {
        return data;
    }

}
