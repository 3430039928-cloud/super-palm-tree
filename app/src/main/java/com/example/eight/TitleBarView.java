package com.example.eight;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TitleBarView extends LinearLayout {
    private TextView tv;

    public TitleBarView(Context c, AttributeSet a) { super(c, a); init(c); }
    public TitleBarView(Context c) { super(c); init(c); }

    private void init(Context c){
        LayoutInflater.from(c).inflate(R.layout.view_titlebar, this, true);
        tv = findViewById(R.id.tvTitle);
        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> {
            if (getContext() instanceof Activity) ((Activity)getContext()).onBackPressed();
        });
    }

    public void setTitle(CharSequence s){ tv.setText(s); }
}
