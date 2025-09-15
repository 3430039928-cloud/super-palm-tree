package com.example.eight;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // 使用你列出的四个 drawable 资源
    private final int[] avatarRes = new int[]{
            R.drawable.baseline_architecture_24,
            R.drawable.baseline_brightness_low_24,
            R.drawable.ic_android_black_24_1dp,
            R.drawable.ic_android_black_24dp
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText etUsername = findViewById(R.id.etUsername);
        EditText etPassword = findViewById(R.id.etPassword);
        RadioGroup avatarGroup = findViewById(R.id.avatarGroup);
        Button btnLogin = findViewById(R.id.btnLogin);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        ImageView ivAvatarPreview = findViewById(R.id.ivAvatarPreview);

        // 默认选第一个并显示预览
        avatarGroup.check(R.id.rbA1);
        ivAvatarPreview.setImageResource(avatarRes[0]);

        // 选择变化时更新预览
        avatarGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int idx = indexFromCheckedId(checkedId);
            ivAvatarPreview.setImageResource(avatarRes[idx]);
        });

        // 登录
        btnLogin.setOnClickListener(v -> {
            progressBar.setVisibility(android.view.View.VISIBLE);
            btnLogin.setEnabled(false);

            String username = etUsername.getText().toString().trim();
            if (username.isEmpty()) {
                progressBar.setVisibility(android.view.View.GONE);
                btnLogin.setEnabled(true);
                etUsername.setError("请输入用户名");
                return;
            }

            int idx = indexFromCheckedId(avatarGroup.getCheckedRadioButtonId());
            int selectedAvatar = avatarRes[idx];

            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("avatar_res", selectedAvatar);
                startActivity(intent);
                finish();
            }, 1500);
        });
    }

    // 将被选中的 RadioButton id 映射为数组下标
    private int indexFromCheckedId(int checkedId) {
        if (checkedId == R.id.rbA2) return 1;
        if (checkedId == R.id.rbA3) return 2;
        if (checkedId == R.id.rbA4) return 3;
        return 0; // rbA1
    }
}
