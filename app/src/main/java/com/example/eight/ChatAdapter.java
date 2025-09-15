package com.example.eight;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> data;
    private final int myAvatarRes;
    private final int friendAvatarRes;

    public ChatAdapter(List<Message> data, int myAvatarRes, int friendAvatarRes) {
        this.data = data;
        this.myAvatarRes = myAvatarRes;
        this.friendAvatarRes = friendAvatarRes;
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ChatActivity.TYPE_ME) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_msg_me, parent, false);
            return new MeVH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_msg_other, parent, false);
            return new OtherVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message m = data.get(position);
        if (holder instanceof MeVH) {
            MeVH h = (MeVH) holder;
            h.tv.setText(m.text);
            h.avatar.setImageResource(myAvatarRes);
        } else {
            OtherVH h = (OtherVH) holder;
            h.tv.setText(m.text);
            h.avatar.setImageResource(friendAvatarRes);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MeVH extends RecyclerView.ViewHolder {
        TextView tv; ImageView avatar;
        MeVH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tvMsg);
            avatar = itemView.findViewById(R.id.ivAvatar);
        }
    }

    static class OtherVH extends RecyclerView.ViewHolder {
        TextView tv; ImageView avatar;
        OtherVH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tvMsg);
            avatar = itemView.findViewById(R.id.ivAvatar);
        }
    }
}
