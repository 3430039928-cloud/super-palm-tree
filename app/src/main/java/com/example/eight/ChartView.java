package com.example.eight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class ChartView extends View {
    public static class Entry {
        public String label; public int value;
        public Entry(String l, int v){ label=l; value=v; }
    }
    private List<Entry> data;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ChartView(Context c){ super(c); }
    public ChartView(Context c, AttributeSet a){ super(c, a); }

    public void setData(List<Entry> d){ this.data = d; invalidate(); }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (data==null || data.isEmpty()) return;

        int w = getWidth(), h = getHeight();
        int left = dp(40), bottom = dp(30), top = dp(10), right = dp(10);
        int cw = w - left - right, ch = h - top - bottom;

        // 轴
        p.setColor(0xFF999999); p.setStrokeWidth(dp(1));
        c.drawLine(left, h-bottom, w-right, h-bottom, p); // x
        c.drawLine(left, top, left, h-bottom, p);         // y

        // 最大值
        int max = 1;
        for (Entry e : data) if (e.value > max) max = e.value;

        float barW = cw * 1f / data.size() * 0.6f;
        float step = cw * 1f / data.size();
        float x = left + step/2f;

        // 柱
        p.setColor(0xFF80CBC4);
        for (Entry e : data) {
            float bh = (e.value * 1f / max) * (ch - dp(8));
            c.drawRect(x - barW/2f, h-bottom - bh, x + barW/2f, h-bottom, p);
            x += step;
        }
        // 标签
        p.setColor(0xFF333333); p.setTextSize(dp(12));
        x = left + step/2f;
        for (Entry e : data) {
            String lab = e.label;
            float tw = p.measureText(lab);
            c.drawText(lab, x - tw/2f, h - dp(10), p);
            x += step;
        }
    }

    private int dp(int v){ return (int)(getResources().getDisplayMetrics().density * v + 0.5f); }
}
