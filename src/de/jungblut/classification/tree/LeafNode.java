package de.jungblut.classification.tree;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import de.jungblut.math.DoubleVector;

public final class LeafNode extends AbstractTreeNode {

  private int label;

  public LeafNode() {
  }

  public LeafNode(int label) {
    this.label = label;
  }

  @Override
  public int predict(DoubleVector features) {
    return label;
  }

  @Override
  public void transformToByteCode(MethodVisitor visitor, Label returnLabel) {
    // allocate a constant for the label, push it on the stack
    visitor.visitLdcInsn(label);
    // go to the return label
    visitor.visitJumpInsn(Opcodes.GOTO, returnLabel);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    label = in.readInt();
  }

  @Override
  protected void writeInternal(DataOutput out) throws IOException {
    out.writeInt(label);
  }

  @Override
  public byte getType() {
    return 1;
  }

}
