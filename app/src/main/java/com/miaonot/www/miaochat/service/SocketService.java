package com.miaonot.www.miaochat.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.miaonot.www.miaochat.activity.ChatActivity;
import com.miaonot.www.miaochat.activity.MainActivity;
import com.miaonot.www.miaochat.utils.SocketUtil;

public class SocketService extends Service {

    private static final int CONNECTION = 0;
    private Message message = new Message();

    //the flag of the connection, if the connection error, end stop the service
    //this way is not good and safe, if having time, change it to broadcast
    public static boolean isConnect = true;

    public class SocketBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO:send message here
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SocketService", "create");

        SharedPreferences sharedPreferences = getSharedPreferences("user",0);
        String msg = sharedPreferences.getString("user_name",null);
        SocketUtil socketUtil = new SocketUtil(msg);
        Log.d("SocketService", isConnect + "");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isConnect) {}
                Log.d("SocketService", "connection error, exit");
                message.what = CONNECTION;
                message.obj = false;
                MainActivity.handler.sendMessage(message);
                stopSelf();
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("SocketService", "destroy");
    }

}
