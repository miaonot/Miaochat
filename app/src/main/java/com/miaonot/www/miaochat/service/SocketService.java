package com.miaonot.www.miaochat.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.miaonot.www.miaochat.R;
import com.miaonot.www.miaochat.activity.ChatActivity;
import com.miaonot.www.miaochat.activity.MainActivity;
import com.miaonot.www.miaochat.database.DatabaseHelper;
import com.miaonot.www.miaochat.module.ChatMessage;
import com.miaonot.www.miaochat.utils.SocketUtil;

import java.util.List;

public class SocketService extends Service {

    private static final int CONNECTION = 0;
    private Message message = new Message();

    private final IBinder mBinder = new SocketBinder();

    private SocketUtil socketUtil;

    public static Handler handler;

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
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SocketService", "create");

//        SharedPreferences sharedPreferences = getSharedPreferences("user",0);
//        String msg = sharedPreferences.getString("user_name",null);
        DatabaseHelper databaseHelper = new DatabaseHelper(this, "miaochat.db", null, 1);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Cursor cursor = db.query("user", null, null, null, null, null, null);
        String msg;
        if (cursor.moveToFirst()) {
            msg = cursor.getString(cursor.getColumnIndex("user_id"));
        }
        else msg = "0";
        socketUtil = new SocketUtil(msg);
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

        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                super.handleMessage(message);
                ChatMessage chatMessage = (ChatMessage) message.obj;
                ContentValues values = new ContentValues();
                values.put("id", chatMessage.getId());
                values.put("inf_from", chatMessage.getFrom());
                values.put("inf_to", chatMessage.getTo());
                values.put("time", chatMessage.getTime());
                values.put("content", chatMessage.getContent());
                db.insert("information", null, values);
                values.clear();

                boolean flag = false;
                ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> activityList = activityManager.getRunningAppProcesses();
                if (activityList.size() != 0) {
                    for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo:activityList) {
                        if (runningAppProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && runningAppProcessInfo.processName.equals("com.miaonot.www.miaochat")) {
                            flag = true;
                        }
                    }
                }

                if (flag) {
                    ChatActivity.handler.sendMessage(message);
                } else {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
                    builder.setSmallIcon(R.mipmap.ic_launcher);
                    builder.setContentTitle(chatMessage.getFrom());
                    builder.setContentText(chatMessage.getContent());
                    builder.setAutoCancel(true);
                    Intent intent = new Intent(getBaseContext(), ChatActivity.class);
                    intent.setAction(chatMessage.getFrom());
                    PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(pendingIntent);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(0, builder.build());
                }
            }
        };

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("SocketService", "destroy");
    }

    public void sendMessage(ChatMessage message) {
        socketUtil.sendChatMessage(message);
    }

}
