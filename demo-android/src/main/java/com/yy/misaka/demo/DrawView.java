package com.yy.misaka.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuduo on 11/23/15.
 */
public class DrawView extends View {

    public static class Dot {

        public float xPercent;
        public float yPercent;

        @Override
        public String toString() {
            return "Dot{" +
                    "xPercent=" + xPercent +
                    ", yPercent=" + yPercent +
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
//        Paint paint = new Paint();

//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(Color.RED);
//        canvas.drawPaint(paint);

//        Path path = new Path();
//        for (int i = 100; i < 300; i++) {
//            path.moveTo(i, i);
//            path.lineTo(i, i);
//        }
//        path.close();
//        paint.setStrokeWidth(3);
//        paint.setPathEffect(null);
//        paint.setColor(Color.BLACK);
//        paint.setStyle(Paint.Style.STROKE);
//        canvas.drawPath(path, paint);

        Paint paint = new Paint();

        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);

        for (List<Dot> line : lines) {

            Path path = new Path();
            boolean first = true;
            for (Dot dot : line) {
                float x = dot.xPercent * canvas.getWidth();
                float y = dot.yPercent * canvas.getHeight();
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
}
