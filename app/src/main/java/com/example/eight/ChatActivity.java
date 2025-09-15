package com.example.eight;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static final int TYPE_OTHER = 0;
    public static final int TYPE_ME = 1;

    private ChatAdapter adapter;
    private final List<Message> data = new ArrayList<>();

    private int myAvatarRes;
    private int friendAvatarRes;
    private String friendName;
    private long ownerId, friendId;

    private DB dbh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        TitleBarView tb = findViewById(R.id.titleChat);
        tb.setTitle("聊天");

        dbh = new DB(this);

        friendName = getIntent().getStringExtra("friend_name");
        myAvatarRes = getIntent().getIntExtra("my_avatar_res", R.drawable.ic_android_black_24dp);
        friendAvatarRes = getIntent().getIntExtra("friend_avatar_res", R.drawable.baseline_architecture_24);
        ownerId = getIntent().getLongExtra("ownerId", -1);
        friendId = getIntent().getLongExtra("friendId", -1);

        RecyclerView rv = findViewById(R.id.rvChat);
        EditText et = findViewById(R.id.etMessage);
        Button btn = findViewById(R.id.btnSend);

        adapter = new ChatAdapter(data, myAvatarRes, friendAvatarRes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        loadHistory();

        btn.setOnClickListener(v -> {
            String text = et.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;
            appendAndSave(TYPE_ME, text);
            et.setText("");

            // 简单自动回复（可不要）
            new Handler().postDelayed(() -> appendAndSave(TYPE_OTHER, "收到：「" + text + "」"), 600);
        });
    }

    private void loadHistory() {
        data.clear();
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT fromMe, content, ts FROM messages WHERE ownerId=? AND friendId=? ORDER BY ts",
                new String[]{String.valueOf(ownerId), String.valueOf(friendId)});
        while (c.moveToNext()) {
            int t = c.getInt(0) == 1 ? TYPE_ME : TYPE_OTHER;
            String txt = c.getString(1);
            long ts = c.getLong(2);
            data.add(new Message(t, txt, ts));
        }
        c.close();
        adapter.notifyDataSetChanged();
        RecyclerView rv = findViewById(R.id.rvChat);
        rv.scrollToPosition(adapter.getItemCount() - 1);
    }

    private void appendAndSave(int type, String text) {
        long now = System.currentTimeMillis();
        data.add(new Message(type, text, now));
        adapter.notifyItemInserted(data.size() - 1);
        RecyclerView rv = findViewById(R.id.rvChat);
        rv.scrollToPosition(adapter.getItemCount() - 1);

        SQLiteDatabase db = dbh.getWritableDatabase();
        db.execSQL("INSERT INTO messages(ownerId,friendId,fromMe,content,ts) VALUES(?,?,?,?,?)",
                new Object[]{ownerId, friendId, type==TYPE_ME?1:0, text, now});
    }
}
