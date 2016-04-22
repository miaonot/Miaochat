package com.miaonot.www.miaochat.utils;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by miaonot on 16-4-19.
 * @author miaonot
 */
public class SocketUtil {

    private Socket socket = null;

    public boolean bind() {
        try {
            socket = new Socket("192.168.56.1", 3001);
            Log.d("socketUtil", "no exception");
            return true;
        } catch (IOException e) {
            Log.d("socketUtil", "exception caught");
            e.printStackTrace();
            return false;
        }
    }
}
