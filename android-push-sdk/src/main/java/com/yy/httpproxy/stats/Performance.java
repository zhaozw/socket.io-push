package com.yy.httpproxy.stats;

/**
 * Created by xuduo on 1/8/16.
 */
public class Performance {

    public long successCount;
    public long errorCount;
    public long totalLatency;
    public String path;

    public void addError() {
        errorCount++;
    }

    public void addSuccess(long latency) {
        successCount++;
        this.totalLatency += latency;
    }

    @Override
    public String toString() {
        return "Performance{" +
                "successCount=" + successCount +
                ", errorCount=" + errorCount +
                ", totalLatency=" + totalLatency +
                '}';
    }
}
