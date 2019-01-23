package de.jungblut.classification.tree;

import de.jungblut.math.DoubleVector;
import org.apache.hadoop.io.Writable;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class AbstractTreeNode implements Writable {

    /**
     * @return predicts the index of the outcome, or -1 if not known. In the
     * binary case, 0 and 1 are used to distinguish.
     */
    public abstract int predict(DoubleVector features);

    /**
     * Transforms this node to byte code, given a visitor that already starts
     * containing the methods and a label that must be jumped to in case of a
     * return.
     */
    public abstract void transformToByteCode(MethodVisitor visitor,
                                             Label returnLabel);

    /**
     * @return the byte id of the type: leaf = 1, numerical = 2; nominal = 3
     */
    public abstract byte getType();

    /**
     * serialize internal state.
     */
    protected abstract void writeInternal(DataOutput out) throws IOException;

    @Override
    public final void write(DataOutput out) throws IOException {
        out.writeByte(getType());
        writeInternal(out);
    }

    public static AbstractTreeNode read(DataInput in) throws IOException {

        byte type = in.readByte();
        AbstractTreeNode node;
        switch (type) {
            case 1:
                node = new LeafNode();
                break;
            case 2:
                node = new NumericalNode();
                break;
            case 3:
                node = new NominalNode();
                break;
            default:
                throw new IllegalArgumentException(type + " is unknown.");
        }
        node.readFields(in);

        return node;
    }

}
