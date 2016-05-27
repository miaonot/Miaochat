package com.miaonot.www.miaochat.utils;

import android.util.Log;

import com.miaonot.www.miaochat.module.ChatMessage;
import com.miaonot.www.miaochat.module.Friend;
import com.miaonot.www.miaochat.module.HeartBeat;
import com.miaonot.www.miaochat.service.SocketService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by miaonot on 16-4-19.
 */
public class SocketUtil {

    private static final String ip = "192.168.43.232";
    private static final int shortPort = 1101;
    private static final int longPort = 1100;

    static final byte CLIENT_SEND_HEART_BEAT = 1;
    static final byte SERVER_RESPONSE_HEART_BEAT = 2;
    static final byte CLIENT_SEND_MESSAGE = 3;
    static final byte CLIENT_REQUEST_FRIENDS = 7;
    static final byte SERVER_RESPONSE_FRIENDS = 8;
    private static final int CLIENT_REQUEST_LOGIN = 11;
    static final byte CLIENT_SET_LONG_SOCKET = 13;

    private byte[] lock = new byte[0];
    private Socket socket = null;

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
        Log.d("Sign in", "send successful");


        //接收登录情况
        InputStream in = socket.getInputStream();
        DataInputStream ins = new DataInputStream(in);
        Log.d("Sign in", "get successful");
        byte temp = (byte) ins.read();
        Log.d("Sign in", temp + "");
        totalLen = ins.readInt();
        Log.d("Sign in", "type read successful");
        b = new byte[totalLen-4-1];
        ins.read(b);
        Log.d("Sign in", "Sign up state read successful");
        Log.d("Sign in", b[0] + "");

        if(b[0] == 1) {
            socket.close();
            Log.d("Sign in", "socket closed");
        } else {
            Log.d("Sign in", "account error");
            return false;
        }
        return true;
    }

    public static Friend[] requestFriends(String userid) {
        Friend[] friends = null;
        try {
            Socket socket = new Socket(ip,shortPort);
            byte[] b = userid.getBytes("UTF-8");
            int totalLen = 1 + 4 + b.length;
            OutputStream out = socket.getOutputStream();
            DataOutputStream outs = new DataOutputStream(out);
            //发送请求
            outs.writeByte(CLIENT_REQUEST_FRIENDS);
            outs.writeInt(totalLen);
            outs.write(b);

            //接收好友列表
            InputStream in = socket.getInputStream();
            DataInputStream ins = new DataInputStream(in);
            byte type = (byte) ins.read();
            totalLen = ins.readInt();
            b = new byte[totalLen-4-1];
            ins.read(b);
            String msg = new String(b,"UTF-8");
            String[] friendsinfo = msg.split("\n");


            //生成好友的类数组
            int friendsNr = (friendsinfo.length)/2;
            friends = new Friend[friendsNr];
            for(int i=0; i<friendsNr; i++)
            {
                friends[i] = new Friend(friendsinfo[i*2],friendsinfo[i*2+1]);
            }


            //关闭socket
            socket.close();


        } catch (EOFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return friends;
    }

    //心跳任务类
    class HeartTask extends TimerTask
    {
        private Socket socket = null;
        private byte[] lock = null;

        public HeartTask(Socket socket, byte[] lock)
        {
            this.socket = socket;
            this.lock = lock;
        }

        //客户端发送心跳报文
        public void run()
        {
            synchronized(lock)
            {
                try{
                    Log.d("Heartbeat", "send");
                    HeartBeat heart = new HeartBeat();
                    String msg = "1";
                    byte[] b = msg.getBytes("UTF-8");
                    int totalLen = 1 + 4 + b.length;
                    OutputStream out = socket.getOutputStream();
                    DataOutputStream outs = new DataOutputStream(out);
                    //发送心跳
                    outs.writeByte(CLIENT_SEND_HEART_BEAT);
                    outs.writeInt(totalLen);
                    outs.write(b);

                }
                catch(IOException e){
                    Log.d("Heartbeat", "connection error");
                    SocketService.isConnect = false;
                    e.printStackTrace();
                }
            }
        }
    }

    public SocketUtil(final String msg)
    {
        //开启用于接收报文的线程
        new Thread (new Runnable(){
            public void run()
            {
                try {
                    socket = new Socket(ip, longPort);

                    //发送客户的信息

                    byte[] b = msg.getBytes("UTF-8");
                    int totalLen = 1 + 4 + b.length;
                    OutputStream out = socket.getOutputStream();
                    DataOutputStream outs = new DataOutputStream(out);
                    //发送心跳
                    outs.writeByte(CLIENT_SET_LONG_SOCKET);
                    outs.writeInt(totalLen);
                    outs.write(b);

                    //开启心跳
                    Timer heartTask = new Timer();
                    heartTask.scheduleAtFixedRate(new HeartTask(socket, lock), 0, 20000);
                    Log.d("Heartbeat", "open");

                    InputStream in = socket.getInputStream();
                    while (true) {
                        DataInputStream ins = new DataInputStream(in);
                        byte type = (byte) ins.read();
                        totalLen = ins.readInt();
                        b = new byte[totalLen - 4 - 1];
                        ins.read(b);
                        String msg = new String(b, "UTF-8");
                        findReceiveMsgType(type, msg);
                    }
                } catch (EOFException e) {
                    Log.d("Heartbeat", "EOFException");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.d("Heartbeat", "connection error");
                    SocketService.isConnect = false;
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void sendChatMessage(ChatMessage message)
    {
        synchronized(lock)
        {
            try{
                //MessageType.clientSendMsg.add(message);
                String msg = message.getMessage();
                byte[] b = msg.getBytes("UTF-8");
                int totalLen = 1 + 4 + b.length;
                OutputStream out = socket.getOutputStream();
                DataOutputStream outs = new DataOutputStream(out);
                //发送心跳
                outs.writeByte(CLIENT_SEND_MESSAGE);
                outs.writeInt(totalLen);
                outs.write(b);

            }
            catch(IOException e){

            }
        }
    }

    //客户端检验接收报文的类型并调用相应的处理方法
    private void findReceiveMsgType(byte type, String content)
    {

        if(type == SERVER_RESPONSE_HEART_BEAT) //服务器回应心跳包
        {
            handleServerHeartBeat(content);
        }
//        else if (type == SERVER_RECV_MESSAGE) //服务器回应客户发送的聊天记录
//        {
//            handleServerResponseChatMessage(content);
//        }
//        else if(type == SERVER_SEND_MESSAGE) //服务器发送聊天记录
//        {
//            handleServerChatMessage(content);
//        }
    }

    //处理服务器的心跳包
    private void handleServerHeartBeat(String content)
    {
        Log.d("Heartbeat", "receive");
    }

}
