package com.example.eight;

import android.content.Intent; // ✅ 新增
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private int currentAvatarRes;

    // 与 MainActivity 使用同一组资源
    private final int[] avatarRes = new int[]{
            R.drawable.baseline_architecture_24,
            R.drawable.baseline_brightness_low_24,
            R.drawable.ic_android_black_24_1dp,
            R.drawable.ic_android_black_24dp
    };
    private final String[] avatarNames = {"Architecture", "Brightness", "Android 1dp", "Android 24dp"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView tvName = findViewById(R.id.tvName);
        ivAvatar = findViewById(R.id.ivAvatar);
        ListView listView = findViewById(R.id.listView);

        String username = getIntent().getStringExtra("username");
        currentAvatarRes = getIntent().getIntExtra("avatar_res", avatarRes[0]);

        tvName.setText("欢迎，" + username);
        ivAvatar.setImageResource(currentAvatarRes);

        // 点击头像更换
        ivAvatar.setOnClickListener(v -> showAvatarPickerDialog());

        ArrayList<String> friends = new ArrayList<>();
        friends.add("张三");
        friends.add("李四");
        friends.add("王五");
        friends.add("小红");
        friends.add("小明");

        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, friends));

        // ✅ 新增：点击好友进入聊天页
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String friendName = (String) parent.getItemAtPosition(position);
            // 给好友分配一个头像（按位置轮流用你项目里的四张）
            int friendAvatarRes = avatarRes[position % avatarRes.length];

            Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
            intent.putExtra("friend_name", friendName);
            intent.putExtra("friend_avatar_res", friendAvatarRes);
            intent.putExtra("my_avatar_res", currentAvatarRes); // 你当前的头像
            startActivity(intent);
        });
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

        // 预览
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
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
