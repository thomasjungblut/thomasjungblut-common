package de.jungblut.classification.tree;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import de.jungblut.math.DoubleVector;

public final class LeafNode implements TreeNode {

  private final int label;

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
}
