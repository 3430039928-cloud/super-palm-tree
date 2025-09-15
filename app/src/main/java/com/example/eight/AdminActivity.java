package com.example.eight;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    private DB dbh;
    private final ArrayList<Long> userIds = new ArrayList<>();
    private final ArrayList<String> userNames = new ArrayList<>();
    private final ArrayList<String> userRoles = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        ((TitleBarView) findViewById(R.id.titleAdmin)).setTitle("管理员工具");

        dbh = new DB(this);

        ListView lv = findViewById(R.id.lvUsers);
        Button btnClearMsg = findViewById(R.id.btnClearMessages);
        Button btnClearFriends = findViewById(R.id.btnClearFriends);
        Button btnSeed = findViewById(R.id.btnSeedData);
        Button btnGlobalStats = findViewById(R.id.btnGlobalStats);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userNames);
        lv.setAdapter(adapter);

        lv.setOnItemLongClickListener((parent, view, position, id) -> {
            long uid = userIds.get(position);
            String name = userNames.get(position);
            String role = userRoles.get(position);
            showUserOpsDialog(uid, name, role);
            return true;
        });

        btnClearMsg.setOnClickListener(v -> {
            SQLiteDatabase db = dbh.getWritableDatabase();
            db.execSQL("DELETE FROM messages");
            Toast.makeText(this, "已清空消息表", Toast.LENGTH_SHORT).show();
        });

        btnClearFriends.setOnClickListener(v -> {
            SQLiteDatabase db = dbh.getWritableDatabase();
            db.execSQL("DELETE FROM friends");
            Toast.makeText(this, "已清空好友关系表", Toast.LENGTH_SHORT).show();
        });

        btnSeed.setOnClickListener(v -> {
            seedDemoData();
            Toast.makeText(this, "已生成示例好友与消息", Toast.LENGTH_SHORT).show();
        });

        btnGlobalStats.setOnClickListener(v -> {
            // ownerId = -1 表示全局统计（下面修改的 StatsActivity 支持）
            startActivity(new android.content.Intent(this, StatsActivity.class)
                    .putExtra("ownerId", -1L));
        });

        loadUsers();
    }

    private void loadUsers() {
        userIds.clear();
        userNames.clear();
        userRoles.clear();
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, username, role FROM users ORDER BY role DESC, username", null);
        while (c.moveToNext()) {
            userIds.add(c.getLong(0));
            String name = c.getString(1);
            String role = c.getString(2);
            userNames.add(name + " (" + role + ")");
            userRoles.add(role);
        }
        c.close();
        adapter.notifyDataSetChanged();
    }

    private void showUserOpsDialog(long uid, String displayName, String role) {
        String[] items = {"重置密码为 123", "删除该用户（含好友和消息）"};
        new AlertDialog.Builder(this)
                .setTitle(displayName)
                .setItems(items, (d, which) -> {
                    if (which == 0) {
                        SQLiteDatabase db = dbh.getWritableDatabase();
                        db.execSQL("UPDATE users SET password='123' WHERE id=?", new Object[]{uid});
                        Toast.makeText(this, "已重置密码为 123", Toast.LENGTH_SHORT).show();
                    } else if (which == 1) {
                        if ("admin".equalsIgnoreCase(role)) {
                            Toast.makeText(this, "不能删除管理员账户", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        SQLiteDatabase db = dbh.getWritableDatabase();
                        db.beginTransaction();
                        try {
                            db.execSQL("DELETE FROM messages WHERE ownerId=? OR friendId=?", new Object[]{uid, uid});
                            db.execSQL("DELETE FROM friends WHERE ownerId=? OR friendId=?", new Object[]{uid, uid});
                            db.execSQL("DELETE FROM users WHERE id=?", new Object[]{uid});
                            db.setTransactionSuccessful();
                            Toast.makeText(this, "已删除用户及其关联数据", Toast.LENGTH_SHORT).show();
                        } finally {
                            db.endTransaction();
                        }
                        loadUsers();
                    }
                })
                .show();
    }

    /** 为所有普通用户彼此建立部分好友关系，并生成少量示例消息 */
    private void seedDemoData() {
        SQLiteDatabase db = dbh.getWritableDatabase();
        db.beginTransaction();
        try {
            // 取所有普通用户
            ArrayList<Long> ids = new ArrayList<>();
            Cursor c = db.rawQuery("SELECT id FROM users WHERE role='user' ORDER BY id", null);
            while (c.moveToNext()) ids.add(c.getLong(0));
            c.close();

            // 每个用户给前面 3 个用户加好友并发两条消息（避免数据爆炸）
            long now = System.currentTimeMillis();
            for (int i = 0; i < ids.size(); i++) {
                long owner = ids.get(i);
                for (int j = 0; j < ids.size() && j < i && j < i + 3; j++) {
                    long friend = ids.get(j);
                    if (owner == friend) continue;

                    // 双向加好友
                    db.execSQL("INSERT OR IGNORE INTO friends(ownerId, friendId) VALUES(?,?)", new Object[]{owner, friend});
                    db.execSQL("INSERT OR IGNORE INTO friends(ownerId, friendId) VALUES(?,?)", new Object[]{friend, owner});

                    // 各发一条消息
                    db.execSQL("INSERT INTO messages(ownerId,friendId,fromMe,content,ts) VALUES(?,?,?,?,?)",
                            new Object[]{owner, friend, 1, "Hi " + friend, now});
                    db.execSQL("INSERT INTO messages(ownerId,friendId,fromMe,content,ts) VALUES(?,?,?,?,?)",
                            new Object[]{friend, owner, 1, "Hello " + owner, now + 1});
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
