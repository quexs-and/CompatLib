package com.quexs.compatlib.base;

public abstract class CompatLibRunnable<T> implements Runnable{
    private T t;

    public CompatLibRunnable(T t){
        this.t = t;
    }

    public abstract void compatLibRun(T t);

    @Override
    public void run() {
        compatLibRun(t);
    }
}
