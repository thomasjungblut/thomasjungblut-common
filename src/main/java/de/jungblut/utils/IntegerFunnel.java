package de.jungblut.utils;

import com.google.common.base.Preconditions;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

public class IntegerFunnel implements Funnel<Integer> {

    private static final long serialVersionUID = -6424400126495324582L;

    @Override
    public void funnel(Integer from, PrimitiveSink into) {
        Preconditions.checkNotNull(from);
        into.putInt(from.intValue());
    }

}
