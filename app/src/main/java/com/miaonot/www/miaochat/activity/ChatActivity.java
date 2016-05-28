package com.miaonot.www.miaochat.activity;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.miaonot.www.miaochat.R;
import com.miaonot.www.miaochat.activity.adapter.MyAdapter;
import com.miaonot.www.miaochat.database.DatabaseHelper;
import com.miaonot.www.miaochat.module.ChatMessage;
import com.miaonot.www.miaochat.service.SocketService;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    SocketService socketService;
    private boolean isBound = false;

    //Friend id
    private String id;

    //UI
    private Button sendButton;
    private TextView textView;
    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketService.SocketBinder binder = (SocketService.SocketBinder) service;
            socketService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sendButton = (Button) findViewById(R.id.send_chat);
        textView = (TextView) findViewById(R.id.send_text);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        id = getIntent().getAction();

        final List<ChatMessage> chatMessages = new ArrayList<>();
        DatabaseHelper databaseHelper = new DatabaseHelper(this, "miaochat.db", null, 1);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query("information", null, "inf_from = ? OR inf_to = ?", new String[]{id, id}, null, null, null); //怎么只查找前10条消息？
        Log.d("message", cursor.getCount() + "");
        if (cursor.moveToFirst()) {
            Log.d("message", "cursor is not null");
            do {
                String id = cursor.getString(cursor.getColumnIndex("id"));
                String from = cursor.getString(cursor.getColumnIndex("inf_from"));
                String to = cursor.getString(cursor.getColumnIndex("inf_to"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                ChatMessage chatMessage = new ChatMessage(id, from, to, time, content);
                chatMessages.add(chatMessage);
                Log.d("message", id + from + to + time + content);
            } while (cursor.moveToNext());
        }
        cursor = db.query("user", null, null, null, null, null, null);
        final String userId;
        if (cursor.moveToFirst()) {
            userId = cursor.getString(cursor.getColumnIndex("user_id"));
        } else {
            userId = null;
        }
        cursor.close();
        db.close();
        databaseHelper.close();
        assert recyclerView != null;
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MyAdapter(chatMessages, userId);
        recyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = textView.getText().toString();
                if (!text.isEmpty()) {
                    textView.setText("");
                    DatabaseHelper databaseHelper = new DatabaseHelper(v.getContext(), "miaochat.db", null, 1);
                    SQLiteDatabase db = databaseHelper.getWritableDatabase();
                    ChatMessage chatMessage = new ChatMessage(userId, id, text);
                    Log.d("chatMessage", userId + id + text);
                    ContentValues values = new ContentValues();
                    values.put("id", chatMessage.getId());
                    values.put("inf_from", chatMessage.getFrom());
                    values.put("inf_to", chatMessage.getTo());
                    values.put("time", chatMessage.getTime());
                    values.put("content", chatMessage.getContent());
                    db.insert("information", null, values);
                    values.clear();
                    //chatMessages.add(chatMessage);
                    //adapter.notifyItemInserted(adapter.getItemCount());
                    adapter.addItem(adapter.getItemCount(), chatMessage);
                    socketService.sendMessage(chatMessage);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //bind service
        Intent intent = new Intent(this, SocketService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //unbind service
        if (isBound) {
            unbindService(serviceConnection);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
