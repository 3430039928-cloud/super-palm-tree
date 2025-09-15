package com.example.eight;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private final int[] avatarRes = new int[]{
            R.drawable.baseline_architecture_24,
            R.drawable.baseline_brightness_low_24,
            R.drawable.ic_android_black_24_1dp,
            R.drawable.ic_android_black_24dp
    };

    private EditText etUsername, etPassword;
    private RadioGroup avatarGroup, rgRole;
    private ImageView ivAvatarPreview;
    private ProgressBar progressBar;
    private Button btnLogin, btnRegister;
    private DB dbh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbh = new DB(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        avatarGroup = findViewById(R.id.avatarGroup);
        rgRole = findViewById(R.id.rgRole);
        ivAvatarPreview = findViewById(R.id.ivAvatarPreview);
        progressBar = findViewById(R.id.progressBar);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        avatarGroup.check(R.id.rbA1);
        ivAvatarPreview.setImageResource(avatarRes[0]);
        avatarGroup.setOnCheckedChangeListener((g, id) ->
                ivAvatarPreview.setImageResource(avatarRes[indexFromCheckedId(id)]));

        btnLogin.setOnClickListener(v -> doLogin());
        btnRegister.setOnClickListener(v -> doRegister());
        loading(false);
    }

    @Override protected void onResume() {
        super.onResume();
        loading(false);
        etPassword.setText("");
    }

    private void doLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = (rgRole.getCheckedRadioButtonId() == R.id.rbAdmin) ? "admin" : "user";

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        loading(true);

        new Handler().postDelayed(() -> {
            SQLiteDatabase db = dbh.getReadableDatabase();
            Cursor c = db.rawQuery(
                    "SELECT id, avatarRes FROM users WHERE username=? AND password=? AND role=?",
                    new String[]{username, password, role});
            if (c.moveToFirst()) {
                long userId = c.getLong(0);
                int avatarFromDb = c.getInt(1);
                c.close();
                loading(false);

                Intent it;
                if ("admin".equals(role)) {
                    it = new Intent(this, AdminActivity.class);
                } else {
                    it = new Intent(this, HomeActivity.class);
                    it.putExtra("username", username);
                    it.putExtra("avatar_res", avatarFromDb);
                }
                it.putExtra("userId", userId);
                startActivity(it); // 不 finish，便于返回切换账号
            } else {
                c.close();
                loading(false);
                Toast.makeText(this, "账号/密码/角色不匹配", Toast.LENGTH_SHORT).show();
            }
        }, 500);
    }

    private void doRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        int avatar = avatarRes[indexFromCheckedId(avatarGroup.getCheckedRadioButtonId())];

        SQLiteDatabase db = dbh.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password", password);
        cv.put("role", "user");
        cv.put("avatarRes", avatar);

        long rowId = -1;
        try { rowId = db.insertOrThrow("users", null, cv); } catch (Exception ignored) {}
        if (rowId > 0) {
            Toast.makeText(this, "注册成功！请登录", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "注册失败：用户名已存在？", Toast.LENGTH_SHORT).show();
        }
    }

    private int indexFromCheckedId(int checkedId) {
        if (checkedId == R.id.rbA2) return 1;
        if (checkedId == R.id.rbA3) return 2;
        if (checkedId == R.id.rbA4) return 3;
        return 0;
    }

    private void loading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnRegister.setEnabled(!show);
    }
}
