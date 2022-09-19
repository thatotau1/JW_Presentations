package com.example.pdfreader;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class Socket_Handler {
    private Socket mSocket;
    private static Socket_Handler instance;
    private Socket_Handler(){

    }
    public static Socket_Handler getInstance(){
        if(instance==null){
            instance = new Socket_Handler();
        }
        return instance;
    }

    public void setmSocket() {
        {
            try {
                mSocket = IO.socket("http://192.168.0.22:5000");
            } catch (URISyntaxException e) {
            }
        }
    }
    public Socket getmSocket(){
        return mSocket;
    }
}
