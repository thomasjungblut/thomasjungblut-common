package de.jungblut.classification.tree;

import de.jungblut.math.DoubleVector;
import org.objectweb.asm.*;

import java.util.Random;

/**
 * Compilation unit for the object tree structure of the {@link DecisionTree}.
 * It uses the ASM framework to calculate bytecode from the object structure
 * under the proxy of a {@link AbstractTreeNode}. <br/>
 * Nodes are compiled in the following manner:
 * <ul>
 * <li>Nominal nodes are directly compiled into switch statements, default case
 * is a return of -1.</li>
 * <li>Leaf nodes directly return their label.</li>
 * <li>Numerical nodes are compiled into a single branch that compares the value
 * of the split with a reference.</li>
 * </ul>
 * <p>
 * The data that needs to be stored in order to compare is put into the constant
 * space of a class. Thus this is only limited to 2^16 entries on some JVMs. The
 * created class is called "CompiledNode_{timestamp}_{random}" and it's
 * {@link AbstractTreeNode} {@link #compileAndLoad(AbstractTreeNode)} method
 * throws an unsupported operation exception.
 *
 * @author thomas.jungblut
 */
public final class TreeCompiler implements Opcodes {

    private static final String CLAZZ_NAME = "CompiledNode";
    private static final Random RNG = new Random();

    /**
     * Compiles the given node and directly loads it.
     *
     * @param name the name of the class. Possibly generated by
     *             {@link #generateClassName()}.
     * @return a new compiled {@link AbstractTreeNode}.
     */
    public static AbstractTreeNode compileAndLoad(String name,
                                                  AbstractTreeNode node) throws Exception {
        return load(name, compileNode(name, node));
    }

    /**
     * Loads the given tree node via its name and bytecode.
     *
     * @param name     the name of the class.
     * @param byteCode the byte code of the class.
     * @return a new {@link AbstractTreeNode}.
     */
    public static AbstractTreeNode load(String name, byte[] byteCode)
            throws Exception {
        Class<AbstractTreeNode> loadClass = loadClass(byteCode, name);
        return loadClass.newInstance();
    }

    /**
     * Compiles the given tree node and name into a class.
     *
     * @param name the name of the class.
     * @param root the treenode to compile.
     * @return a byte[] representing the class contents.
     */
    public static byte[] compileNode(String name, AbstractTreeNode root)
            throws Exception {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_6, ACC_PUBLIC + ACC_FINAL, name, null,
                Type.getInternalName(AbstractTreeNode.class), null);
        cw.visitSource(name + ".java", null);

        // add the constructor
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()"
                    + Type.VOID_TYPE.getDescriptor(), null, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL,
                    Type.getInternalName(AbstractTreeNode.class), "<init>", "()"
                            + Type.VOID_TYPE.getDescriptor());
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        // override the "predict" method
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "predict", Type
                    .getMethodDescriptor(AbstractTreeNode.class.getDeclaredMethod(
                            "predict", DoubleVector.class)), null, null);
            Label end = new Label();

            root.transformToByteCode(mv, end);

            mv.visitLabel(end);
            // return the last int on the stack.
            mv.visitInsn(Type.INT_TYPE.getOpcode(IRETURN));
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        // make the "transformToByteCode" throw an exception
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "transformToByteCode", Type
                            .getMethodDescriptor(AbstractTreeNode.class.getDeclaredMethod(
                                    "transformToByteCode", MethodVisitor.class, Label.class)), null,
                    null);
            mv.visitTypeInsn(NEW, "java/lang/UnsupportedOperationException");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL,
                    "java/lang/UnsupportedOperationException", "<init>", "()V");
            mv.visitInsn(ATHROW);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }

    /**
     * @return generate a pseudo-unique classname using the classname prefix and
     * the timestamp in ms, because some collisions happened, a random
     * string is appended as well.
     */
    public static synchronized String generateClassName() {
        return CLAZZ_NAME + "_" + System.currentTimeMillis() + "_"
                + Integer.toString(Math.abs(RNG.nextInt()), 36);
    }

    @SuppressWarnings("unchecked")
    private static <CLAZZ> Class<CLAZZ> loadClass(byte[] b, String className)
            throws Exception {
        // override classDefine (as it is protected) and define the class.
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        Class<CLAZZ> cls = (Class<CLAZZ>) Class.forName("java.lang.ClassLoader");
        java.lang.reflect.Method method = cls.getDeclaredMethod("defineClass",
                new Class<?>[]{String.class, byte[].class, int.class, int.class});

        try {
            Class<CLAZZ> clz = (Class<CLAZZ>) Class.forName(className);
            return clz;
        } catch (Exception e) {
            // swallow.
        }

        // protected method invocaton
        method.setAccessible(true);
        try {
            Object[] args = new Object[]{className, b, new Integer(0),
                    new Integer(b.length)};
            return (Class<CLAZZ>) method.invoke(loader, args);
        } finally {
            method.setAccessible(false);
        }
    }
}
