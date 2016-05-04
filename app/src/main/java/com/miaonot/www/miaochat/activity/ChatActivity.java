package com.miaonot.www.miaochat.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.miaonot.www.miaochat.R;
import com.miaonot.www.miaochat.service.SocketService;

public class ChatActivity extends AppCompatActivity {

    SocketService socketService;
    boolean isBound = false;

    final private Context context = this;

    final static private int IS_BIND = 0;

    public static Handler handler;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketService.SocketBinder binder = (SocketService.SocketBinder) service;
            socketService = binder.getService();
            isBound = true;
            socketService.socketBind();
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
    }

    @Override
    protected void onStart() {
        super.onStart();

        //bind service
        Intent intent = new Intent(this, SocketService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        handler = new Handler() {

            @Override
            public void handleMessage(Message message) {
                super.handleMessage(message);
                if (message.what == IS_BIND) {
                    if ((boolean)message.obj) {
                        Toast.makeText(context, "Connect Successful", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Connect Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

    @Override
    protected void onStop() {
        super.onStop();

        //unbind service
        unbindService(serviceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
