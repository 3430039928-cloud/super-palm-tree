package com.example.eight;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        friendName = getIntent().getStringExtra("friend_name");
        myAvatarRes = getIntent().getIntExtra("my_avatar_res", R.drawable.ic_android_black_24dp);
        friendAvatarRes = getIntent().getIntExtra("friend_avatar_res", R.drawable.baseline_architecture_24);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(friendName == null ? "聊天" : friendName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView rv = findViewById(R.id.rvChat);
        EditText et = findViewById(R.id.etMessage);
        Button btn = findViewById(R.id.btnSend);

        adapter = new ChatAdapter(data, myAvatarRes, friendAvatarRes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        if (friendName != null) {
            addMsg(TYPE_OTHER, "你好，我是 " + friendName + "，很高兴认识你～");
        }

        btn.setOnClickListener(v -> {
            String text = et.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;

            addMsg(TYPE_ME, text);
            et.setText("");

            new Handler().postDelayed(() ->
                    addMsg(TYPE_OTHER, "收到：「" + text + "」"), 700);
        });
    }

    private void addMsg(int type, String text) {
        data.add(new Message(type, text, System.currentTimeMillis()));
        adapter.notifyItemInserted(data.size() - 1);
        RecyclerView rv = findViewById(R.id.rvChat);
        rv.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
