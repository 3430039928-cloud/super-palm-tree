package com.example.eight;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    int[] avatarList = {
            android.R.drawable.sym_def_app_icon,
            android.R.drawable.ic_menu_camera,
            android.R.drawable.ic_menu_gallery
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText etUsername = findViewById(R.id.etUsername);
        EditText etPassword = findViewById(R.id.etPassword);
        Spinner spinner = findViewById(R.id.spinnerAvatar);
        Button btnLogin = findViewById(R.id.btnLogin);
        ProgressBar progressBar = findViewById(R.id.progressBar); // 记得在 XML 中添加

        // 设置 Spinner 的数据
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"默认图标", "相机", "相册"}
        );
        spinner.setAdapter(adapter);

        // 登录按钮点击事件
        btnLogin.setOnClickListener(v -> {
            // 显示进度条，禁用按钮
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);

            String username = etUsername.getText().toString();
            int selectedAvatar = avatarList[spinner.getSelectedItemPosition()];

            // 延迟2秒后跳转
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("avatar", selectedAvatar);
                startActivity(intent);
                finish(); // 可选，关闭当前Activity
            }, 2000); // 延迟2秒 = 2000毫秒
        });
    }
}
