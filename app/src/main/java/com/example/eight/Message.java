package com.example.eight;

public class Message {
    public int type;      // 0: 对方  1: 我
    public String text;
    public long time;

    public Message(int type, String text, long time) {
        this.type = type;
        this.text = text;
        this.time = time;
    }
}
