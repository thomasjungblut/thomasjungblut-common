package de.jungblut.classification.tree;

import java.util.Arrays;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import de.jungblut.math.DoubleVector;

public final class NominalNode implements TreeNode {

  private final int splitAttributeIndex;
  // this is a parallel array to the children
  final int[] nominalSplitValues;
  final TreeNode[] children;

  private class SortItem implements Comparable<SortItem> {
    final int val;
    final TreeNode child;

    public SortItem(int val, TreeNode child) {
      super();
      this.val = val;
      this.child = child;
    }

    @Override
    public int compareTo(SortItem o) {
      return Integer.compare(val, o.val);
    }
  }

  public NominalNode(int splitAttributeIndex, int numCategories) {
    this.splitAttributeIndex = splitAttributeIndex;
    this.nominalSplitValues = new int[numCategories];
    this.children = new TreeNode[numCategories];
  }

  public void sortInternal() {
    SortItem[] arr = new SortItem[nominalSplitValues.length];
    for (int i = 0; i < nominalSplitValues.length; i++) {
      arr[i] = new SortItem(nominalSplitValues[i], children[i]);
    }
    Arrays.sort(arr);
    for (int i = 0; i < nominalSplitValues.length; i++) {
      nominalSplitValues[i] = arr[i].val;
      children[i] = arr[i].child;
    }
  }

  @Override
  public int predict(DoubleVector features) {
    int categoricalValue = (int) features.get(splitAttributeIndex);
    int foundIndex = Arrays.binarySearch(nominalSplitValues, categoricalValue);
    if (foundIndex != -1) {
      return children[foundIndex].predict(features);
    }
    return foundIndex;
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
    // we will cast this double to an int
    visitor.visitInsn(Opcodes.D2I);

    Label end = new Label();
    Label defaultLabel = new Label();
    Label[] labels = new Label[nominalSplitValues.length];
    for (int i = 0; i < nominalSplitValues.length; i++) {
      labels[i] = new Label();
    }
    visitor.visitLookupSwitchInsn(defaultLabel, nominalSplitValues, labels);

    for (int i = 0; i < nominalSplitValues.length; i++) {
      visitor.visitLabel(labels[i]);
      children[i].transformToByteCode(visitor, returnLabel);
      visitor.visitJumpInsn(Opcodes.GOTO, end);
    }

    visitor.visitLabel(defaultLabel);
    // for the default item we return -1
    visitor.visitLdcInsn(-1);
    visitor.visitLabel(end);

  }

}
