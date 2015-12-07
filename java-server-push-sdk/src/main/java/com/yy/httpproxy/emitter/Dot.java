package com.yy.httpproxy.emitter;

public class Dot {

    public float xPercent;
    public float yPercent;
    public int myColor;
    public long timestamp = System.currentTimeMillis();

    @Override
    public String toString() {
        return "Dot{" +
                "xPercent=" + xPercent +
                ", yPercent=" + yPercent + ", myColor = " + myColor +
                '}';
    }
}