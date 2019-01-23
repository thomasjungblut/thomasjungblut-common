package de.jungblut.utils;

import com.google.common.base.Preconditions;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

public class LongFunnel implements Funnel<Long> {

    private static final long serialVersionUID = -6823398842539676215L;

    @Override
    public void funnel(Long from, PrimitiveSink into) {
        Preconditions.checkNotNull(from);
        into.putLong(from.longValue());
    }

}
