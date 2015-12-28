package com.yy.misaka.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by xuduo on 11/23/15.
 */
public class DrawView extends View {

    public static class Dot {
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

    private List<List<Dot>> lines = new ArrayList<List<Dot>>();

    public DrawView(Context context) {
        super(context);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);

        for (List<Dot> line : lines) {

            Path path = new Path();
            boolean first = true;
            for (Dot dot : line) {
                float x = dot.xPercent * canvas.getWidth();
                float y = dot.yPercent * canvas.getHeight();
                paint.setColor(dot.myColor);
                if (first) {
                    first = false;
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
            canvas.drawPath(path, paint);
        }

    }

    public void addDot(Dot dot) {
        if (lines.size() == 0) {
            endLine();
        }
        List<Dot> currentLine = lines.get(lines.size() - 1);
        currentLine.add(dot);
        invalidate();
    }

    public void endLine() {
        lines.add(new ArrayList<Dot>());
        invalidate();
    }

    public void clear() {
        lines.clear();
        invalidate();
    }

    public int  clearColor(Dot dot){
        int color = dot.myColor;
        int count = 0;
        for(Iterator it = lines.iterator(); it.hasNext();){
            ArrayList<Dot> dots = (ArrayList<Dot>)it.next();

            for (Iterator itDot = dots.iterator(); itDot.hasNext();){
                Dot temp = (Dot)itDot.next();
                if (temp.myColor == color) {
                    itDot.remove();
                } else {
                    count++;
                }
            }
        }
        invalidate();
        return count;
    }
}
