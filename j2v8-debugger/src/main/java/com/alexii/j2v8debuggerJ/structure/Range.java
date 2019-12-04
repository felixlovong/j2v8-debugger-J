package com.alexii.j2v8debuggerJ.structure;

import android.support.annotation.NonNull;

import java.util.Iterator;

public class Range implements Iterable<Integer> {

    private final int start;
    private final int end;
    private final int step;

    public Range(
            int end
    ) {
        this(0, end, 1);
    }

    public Range(
            int start,
            int end
    ) {
        this(start, end, 1);
    }

    public Range(
            int start,
            int end,
            int step
    ) {
        this.start = start;
        this.end = end;
        this.step = step;
    }

    @NonNull
    @Override
    public Iterator<Integer> iterator() {
        return new IntegerIterator();
    }

    private class IntegerIterator implements Iterator<Integer> {
        int current = start;


        @Override
        public boolean hasNext() {
            return step > 0 ? current < end : current > end;
        }

        @Override
        public Integer next() {
            int t = current;
            current += step;
            return t;
        }
    }
}
