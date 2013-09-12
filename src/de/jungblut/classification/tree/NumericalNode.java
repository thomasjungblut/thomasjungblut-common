package de.jungblut.classification.tree;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import de.jungblut.math.DoubleVector;

public final class NumericalNode implements TreeNode {

  private final int splitAttributeIndex;
  private final double splitAttributeValue;

  private final TreeNode lower;
  private final TreeNode higher;

  public NumericalNode(int splitAttributeIndex, double splitAttributeValue,
      TreeNode lower, TreeNode higher) {
    super();
    this.splitAttributeIndex = splitAttributeIndex;
    this.splitAttributeValue = splitAttributeValue;
    this.lower = lower;
    this.higher = higher;
  }

  @Override
  public int predict(DoubleVector features) {
    if (features.get(splitAttributeIndex) > splitAttributeValue) {
      return higher.predict(features);
    } else {
      return lower.predict(features);
    }
  }

  @Override
  public void transformToByteCode(MethodVisitor visitor, Label returnLabel) {
    // load the parameter vector to the stack
    visitor.visitVarInsn(
        Type.getType(DoubleVector.class).getOpcode(Opcodes.ILOAD), 1);
    // we put the index of the split attribute on the stack
    visitor.visitLdcInsn(splitAttributeIndex);
    visitor.visitMethodInsn(
        Opcodes.INVOKEINTERFACE,
        Type.getInternalName(DoubleVector.class),
        "get",
        "(" + Type.INT_TYPE.getDescriptor() + ")"
            + Type.DOUBLE_TYPE.getDescriptor());
    // now we have the double value of the vector at the index on the stack.
    // let's load the comparison value.
    visitor.visitLdcInsn(splitAttributeValue);

    Label smallerLabel = new Label();
    Label end = new Label();
    visitor.visitInsn(Opcodes.DCMPG);
    // in case DCMP returns greater (stack value = 1) we want to get to the
    // greater value labels
    visitor.visitJumpInsn(Opcodes.IFLE, smallerLabel);
    // execute the higher portion
    higher.transformToByteCode(visitor, returnLabel);
    // go to the end
    visitor.visitJumpInsn(Opcodes.GOTO, end);
    visitor.visitLabel(smallerLabel);
    // execute smaller portion
    lower.transformToByteCode(visitor, returnLabel);
    visitor.visitLabel(end);
  }
}
