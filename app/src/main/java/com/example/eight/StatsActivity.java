package com.example.eight;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class StatsActivity extends AppCompatActivity {
    private DB dbh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        ((TitleBarView)findViewById(R.id.titleStats)).setTitle("消息统计");

        dbh = new DB(this);
        long ownerId = getIntent().getLongExtra("ownerId", -1);

        List<ChartView.Entry> entries = new ArrayList<>();
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor c;

        if (ownerId >= 0) {
            c = db.rawQuery(
                    "SELECT u.username, COUNT(m.id) " +
                            "FROM messages m JOIN users u ON m.friendId=u.id " +
                            "WHERE m.ownerId=? GROUP BY m.friendId ORDER BY COUNT(m.id) DESC",
                    new String[]{String.valueOf(ownerId)});
        } else {
            c = db.rawQuery(
                    "SELECT u.username, COUNT(m.id) " +
                            "FROM messages m JOIN users u ON m.friendId=u.id " +
                            "GROUP BY m.friendId ORDER BY COUNT(m.id) DESC", null);
        }

        while (c.moveToNext()) {
            entries.add(new ChartView.Entry(c.getString(0), c.getInt(1)));
        }
        c.close();

        ChartView chart = findViewById(R.id.chart);
        chart.setData(entries);
    }
}
