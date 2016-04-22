package com.miaonot.www.miaochat.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;

import com.miaonot.www.miaochat.Activity.ChatActivity;
import com.miaonot.www.miaochat.utils.SocketUtil;

public class SocketService extends Service {

    final static private int IS_BIND = 0;
    private Message message;

    public class SocketBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    private final SocketBinder socketBinder = new SocketBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return socketBinder;
    }

    public void socketBind() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SocketUtil socketUtil = new SocketUtil();
                boolean flag = socketUtil.bind();
                message = new Message();
                if (flag) {
                    message.what = IS_BIND;
                    message.obj = true;
                    ChatActivity.handler.sendMessage(message);
                } else {
                    message.what = IS_BIND;
                    message.obj = false;
                    ChatActivity.handler.sendMessage(message);
                }

            }
        }).start();
    }
}
