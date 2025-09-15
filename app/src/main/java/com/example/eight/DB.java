package com.example.eight;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper {
    public static final String DB_NAME = "eight.db";
    public static final int DB_VER = 1;

    public DB(Context c) { super(c, DB_NAME, null, DB_VER); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS users(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE," +
                "password TEXT," +
                "role TEXT," +                 // 'admin' or 'user'
                "avatarRes INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS friends(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "ownerId INTEGER," +
                "friendId INTEGER," +
                "note TEXT," +
                "UNIQUE(ownerId, friendId))");

        db.execSQL("CREATE TABLE IF NOT EXISTS messages(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "ownerId INTEGER," +
                "friendId INTEGER," +
                "fromMe INTEGER," +            // 1:我方 0:对方
                "content TEXT," +
                "ts INTEGER)");

        // 默认一些账号，便于登录/加好友演示
        db.execSQL("INSERT OR IGNORE INTO users(username,password,role,avatarRes) VALUES" +
                "('admin','admin','admin'," + R.drawable.ic_android_black_24dp + ")," +
                "('zhangsan','123','user'," + R.drawable.baseline_architecture_24 + ")," +
                "('lisi','123','user'," + R.drawable.baseline_brightness_low_24 + ")," +
                "('wangwu','123','user'," + R.drawable.ic_android_black_24_1dp + ")");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
}
