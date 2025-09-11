package com.example.eight;


import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;


import java.util.ArrayList;


public class HomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        TextView tvName = findViewById(R.id.tvName);
        ImageView ivAvatar = findViewById(R.id.ivAvatar);
        ListView listView = findViewById(R.id.listView);


        String username = getIntent().getStringExtra("username");
        int avatarResId = getIntent().getIntExtra("avatar", android.R.drawable.sym_def_app_icon);


        tvName.setText("欢迎，" + username);
        ivAvatar.setImageResource(avatarResId);


        ArrayList<String> friends = new ArrayList<>();
        friends.add("张三");
        friends.add("李四");
        friends.add("王五");
        friends.add("小红");
        friends.add("小明");


        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                friends
        );
        listView.setAdapter(adapter);
    }
}
