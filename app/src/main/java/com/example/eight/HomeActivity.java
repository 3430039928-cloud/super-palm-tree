package com.example.eight;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private int currentAvatarRes;
    private long ownerId;
    private String username;

    private final int[] avatarRes = new int[]{
            R.drawable.baseline_architecture_24,
            R.drawable.baseline_brightness_low_24,
            R.drawable.ic_android_black_24_1dp,
            R.drawable.ic_android_black_24dp
    };

    private DB dbh;
    private final ArrayList<Long> friendIds = new ArrayList<>();
    private final ArrayList<String> friendNames = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TitleBarView tb = findViewById(R.id.titleHome);
        tb.setTitle("主页");
        // 主页不需要返回按钮，避免误触直接退出
        tb.findViewById(R.id.btnBack).setVisibility(ImageView.GONE);

        dbh = new DB(this);

        TextView tvName = findViewById(R.id.tvName);
        ivAvatar = findViewById(R.id.ivAvatar);
        ListView listView = findViewById(R.id.listView);
        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnStats = findViewById(R.id.btnStats);

        ownerId = getIntent().getLongExtra("userId", -1);
        username = getIntent().getStringExtra("username");
        currentAvatarRes = getIntent().getIntExtra("avatar_res", R.drawable.ic_android_black_24dp);

        tvName.setText("欢迎，" + username);
        ivAvatar.setImageResource(currentAvatarRes);

        ivAvatar.setOnClickListener(v -> showAvatarPickerDialog());

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, friendNames);
        listView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> addFriendDialog());
        btnStats.setOnClickListener(v -> {
            Intent it = new Intent(HomeActivity.this, StatsActivity.class);
            it.putExtra("ownerId", ownerId);
            startActivity(it);
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            long friendId = friendIds.get(position);
            String friendName = friendNames.get(position);
            int friendAvatar = getAvatarResByUserId(friendId);
            Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
            intent.putExtra("friend_name", friendName);
            intent.putExtra("friend_avatar_res", friendAvatar);
            intent.putExtra("my_avatar_res", currentAvatarRes);
            intent.putExtra("ownerId", ownerId);
            intent.putExtra("friendId", friendId);
            startActivity(intent);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            long fid = friendIds.get(position);
            showEditDeleteDialog(fid);
            return true;
        });

        ensureSeedFriendsIfEmpty(); // 首次自动填充好友
        loadFriends();
    }

    /** 如果该用户还没有任何好友，自动添加几位现有用户为好友（只做一次） */
    private void ensureSeedFriendsIfEmpty() {
        SQLiteDatabase rdb = dbh.getReadableDatabase();
        Cursor c = rdb.rawQuery("SELECT COUNT(*) FROM friends WHERE ownerId=?",
                new String[]{String.valueOf(ownerId)});
        int n = 0;
        if (c.moveToFirst()) n = c.getInt(0);
        c.close();
        if (n > 0) return;

        Cursor u = rdb.rawQuery(
                "SELECT id FROM users WHERE id<>? AND role='user' LIMIT 4",
                new String[]{String.valueOf(ownerId)});
        SQLiteDatabase wdb = dbh.getWritableDatabase();
        while (u.moveToNext()) {
            long fid = u.getLong(0);
            wdb.execSQL("INSERT OR IGNORE INTO friends(ownerId, friendId) VALUES(?,?)",
                    new Object[]{ownerId, fid});
        }
        u.close();
    }

    private void loadFriends() {
        friendIds.clear();
        friendNames.clear();
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT u.id, COALESCE(f.note, u.username) " +
                        "FROM friends f JOIN users u ON f.friendId=u.id " +
                        "WHERE f.ownerId=? ORDER BY u.username",
                new String[]{String.valueOf(ownerId)});
        while (c.moveToNext()) {
            friendIds.add(c.getLong(0));
            friendNames.add(c.getString(1));
        }
        c.close();
        adapter.notifyDataSetChanged();
    }

    private int getAvatarResByUserId(long uid) {
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT avatarRes FROM users WHERE id=?",
                new String[]{String.valueOf(uid)});
        int res = R.drawable.ic_android_black_24dp;
        if (c.moveToFirst()) res = c.getInt(0);
        c.close();
        return res;
    }

    private void addFriendDialog() {
        EditText et = new EditText(this);
        et.setHint("输入对方用户名（已存在的用户）");
        new AlertDialog.Builder(this)
                .setTitle("添加好友")
                .setView(et)
                .setPositiveButton("添加", (d, w) -> {
                    String uname = et.getText().toString().trim();
                    if (uname.isEmpty()) return;
                    SQLiteDatabase db = dbh.getReadableDatabase();
                    Cursor c = db.rawQuery("SELECT id FROM users WHERE username=? AND role='user'",
                            new String[]{uname});
                    if (c.moveToFirst()) {
                        long fid = c.getLong(0);
                        c.close();
                        db = dbh.getWritableDatabase();
                        try {
                            db.execSQL("INSERT OR IGNORE INTO friends(ownerId,friendId) VALUES(?,?)",
                                    new Object[]{ownerId, fid});
                            Toast.makeText(this, "已添加", Toast.LENGTH_SHORT).show();
                            loadFriends();
                        } catch (Exception ex) {
                            Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        c.close();
                        Toast.makeText(this, "用户不存在", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showEditDeleteDialog(long fid) {
        String[] items = {"改备注", "删除好友"};
        new AlertDialog.Builder(this)
                .setItems(items, (d, which) -> {
                    if (which == 0) {
                        EditText et = new EditText(this);
                        et.setHint("备注名（留空还原用户名）");
                        new AlertDialog.Builder(this)
                                .setTitle("修改备注")
                                .setView(et)
                                .setPositiveButton("确定", (d2, w) -> {
                                    String note = et.getText().toString().trim();
                                    SQLiteDatabase db = dbh.getWritableDatabase();
                                    ContentValues cv = new ContentValues();
                                    cv.put("note", note.isEmpty() ? null : note);
                                    db.update("friends", cv, "ownerId=? AND friendId=?",
                                            new String[]{String.valueOf(ownerId), String.valueOf(fid)});
                                    loadFriends();
                                })
                                .setNegativeButton("取消", null).show();
                    } else {
                        SQLiteDatabase db = dbh.getWritableDatabase();
                        db.delete("friends", "ownerId=? AND friendId=?",
                                new String[]{String.valueOf(ownerId), String.valueOf(fid)});
                        loadFriends();
                    }
                })
                .show();
    }

    private void showAvatarPickerDialog() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 0);

        RadioGroup group = new RadioGroup(this);
        group.setOrientation(LinearLayout.HORIZONTAL);
        group.setGravity(Gravity.CENTER);
        root.addView(group, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ImageView preview = new ImageView(this);
        preview.setImageResource(currentAvatarRes);
        preview.setAdjustViewBounds(true);
        LinearLayout.LayoutParams lpPrev = new LinearLayout.LayoutParams(160, 160);
        lpPrev.topMargin = 24;
        lpPrev.gravity = Gravity.CENTER_HORIZONTAL;
        preview.setLayoutParams(lpPrev);
        root.addView(preview);

        int precheckedIndex = 0;
        for (int i = 0; i < avatarRes.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setButtonDrawable(null);
            rb.setText("");
            rb.setBackground(getDrawable(R.drawable.selector_avatar_bg));
            rb.setCompoundDrawablesWithIntrinsicBounds(0, avatarRes[i], 0, 0);
            rb.setCompoundDrawablePadding(6);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(140, 140);
            lp.rightMargin = 12;
            rb.setLayoutParams(lp);
            rb.setId(1000 + i);
            group.addView(rb);

            if (currentAvatarRes == avatarRes[i]) precheckedIndex = i;
        }
        group.check(1000 + precheckedIndex);

        group.setOnCheckedChangeListener((g, checkedId) -> {
            int idx = checkedId - 1000;
            if (idx < 0 || idx >= avatarRes.length) return;
            preview.setImageResource(avatarRes[idx]);
        });

        new AlertDialog.Builder(this)
                .setTitle("选择头像")
                .setView(root)
                .setPositiveButton("确定", (dialog, which) -> {
                    int idx = group.getCheckedRadioButtonId() - 1000;
                    if (idx < 0 || idx >= avatarRes.length) return;
                    currentAvatarRes = avatarRes[idx];
                    ivAvatar.setImageResource(currentAvatarRes);

                    SQLiteDatabase db = dbh.getWritableDatabase();
                    ContentValues cv = new ContentValues();
                    cv.put("avatarRes", currentAvatarRes);
                    db.update("users", cv, "id=?", new String[]{String.valueOf(ownerId)});
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
