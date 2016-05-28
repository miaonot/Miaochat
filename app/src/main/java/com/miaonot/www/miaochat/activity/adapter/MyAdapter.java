package com.miaonot.www.miaochat.activity.adapter;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miaonot.www.miaochat.R;
import com.miaonot.www.miaochat.activity.ChatActivity;
import com.miaonot.www.miaochat.module.ChatMessage;
import com.miaonot.www.miaochat.module.Friend;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<Friend> friends;
    private List<ChatMessage> chatMessages;
    private String type = "friend";

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public TextView sendTextView;
        public TextView getTextView;
        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
        public ViewHolder(CardView v) {
            super(v);
            sendTextView = (TextView) v.getChildAt(0);
            getTextView = (TextView) v.getChildAt(1);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(List<Friend> myFriends) {
        friends = myFriends;
    }

    public MyAdapter(List<ChatMessage> myChatMessages, String myType) {
        chatMessages = myChatMessages;
        type = myType;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (type.equals("friend")) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.text_view, parent, false);
            // set the view's size, margins, paddings and layout parameters
            //TODO
            return new ViewHolder((TextView) v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_view, parent, false);
            return new ViewHolder((CardView) v);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from dataset at this position
        if (type.equals("friend")) {
            holder.mTextView.setText(friends.get(position).getNickname());
            holder.mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int n = holder.getLayoutPosition();
                    Log.d("click", n + "");
                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                    intent.setType("Friend Name");
                    intent.setAction(friends.get(n).getId());
                    v.getContext().startActivity(intent);
                }
            });
        } else {
            if (chatMessages.get(position).getFrom().equals(type)) {
                holder.sendTextView.setText(chatMessages.get(position).getContent());
                holder.getTextView.setVisibility(View.INVISIBLE);
            } else {
                holder.getTextView.setText(chatMessages.get(position).getContent());
                holder.sendTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (type.equals("friend")) {
            while (friends == null) {
                return 0;
            }
            return friends.size();
        }
        else {
            Log.d("getItemCount", chatMessages.size() + "");
            return chatMessages.size();
        }
    }

    public void addItem(int position, ChatMessage chatMessage) {
        chatMessages.add(position, chatMessage);
        notifyItemInserted(position);
    }
}
