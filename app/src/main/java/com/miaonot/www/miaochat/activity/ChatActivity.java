package com.miaonot.www.miaochat.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.miaonot.www.miaochat.R;
import com.miaonot.www.miaochat.database.DatabaseHelper;
import com.miaonot.www.miaochat.module.ChatMessage;
import com.miaonot.www.miaochat.service.SocketService;

public class ChatActivity extends AppCompatActivity {

    SocketService socketService;
    boolean isBound = false;

    //Friend id
    String id;

    //UI
    Button sendButton;
    TextView textView;

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

        id = getIntent().getAction();
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = textView.getText().toString();
                textView.setText("");
                DatabaseHelper databaseHelper = new DatabaseHelper(v.getContext(), "miaochat.db", null, 1);
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor cursor = db.query("user", null, null, null, null, null, null);
                String userId;
                if (cursor.moveToFirst()) {
                    userId = cursor.getString(cursor.getColumnIndex("user_id"));
                } else {
                    userId = null;
                }
                ChatMessage chatMessage = new ChatMessage(userId, id, text);
                Log.d("chatMessage", userId + id + text);
                socketService.sendMessage(chatMessage);
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
