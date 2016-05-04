package com.miaonot.www.miaochat.utils;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by miaonot on 16-4-19.
 */
public class SocketUtil {

    private static final String ip = "192.168.2.1";
    private static final int shortPort = 1101;
    private static final int longPort = 1100;

    private static final int CLIENT_REQUEST_LOGIN = 11;

    public boolean bind() {
        try {
            Socket socket = new Socket(ip, longPort);
            Log.d("socketUtil", "no exception");
            return true;
        } catch (IOException e) {
            Log.d("socketUtil", "exception caught");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean signIn(String userId, String mPassword) throws IOException {
        Socket socket = new Socket(ip,shortPort);
        String login = userId + "\n" + mPassword;
        byte[] b = login.getBytes("UTF-8");
        int totalLen = 1 + 4 + b.length;
        OutputStream out = socket.getOutputStream();
        DataOutputStream outs = new DataOutputStream(out);

        //发送登录请求
        outs.writeByte(CLIENT_REQUEST_LOGIN);
        outs.writeInt(totalLen);
        outs.write(b);
        Log.d("Sign up", "send successful");


        //接收登录情况
        InputStream in = socket.getInputStream();
        DataInputStream ins = new DataInputStream(in);
        Log.d("Sign up", "get successful");
        byte temp = (byte) ins.read();
        Log.d("Sign up", temp + "");
        totalLen = ins.readInt();
        Log.d("Sign up", "type read successful");
        b = new byte[totalLen-4-1];
        ins.read(b);
        Log.d("Sign up", "Sign up state read successful");
        Log.d("Sign up", b[0] + "");

        if(b[0] == 1) {
            socket.close();
            Log.d("Sign up", "socket closed");
        } else {
            Log.d("Sign up", "account error");
            return false;
        }
        return true;
    }

}
